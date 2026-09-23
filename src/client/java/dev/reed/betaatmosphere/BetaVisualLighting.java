package dev.reed.betaatmosphere;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Publishes immutable light parameters to workers and budgets Beta's sunlight mesh updates. */
public final class BetaVisualLighting {
    private static volatile BetaLightmapState.Parameters current;
    private static ClientLevel lastLevel;
    private static final ArrayDeque<SectionPos> pending = new ArrayDeque<>();
    private static boolean rescan;
    private static long rebuiltSections;

    private BetaVisualLighting() {}
    public static BetaLightmapState.Parameters current() { return current; }
    public static int pendingSections() { return pending.size(); }
    public static long rebuiltSections() { return rebuiltSections; }

    public static void tick(Minecraft mc) {
        var level = mc.level;
        if (level == null || !BetaFogBrightness.supports(level) || mc.getCameraEntity() == null) {
            current = null;
            lastLevel = null;
            pending.clear();
            rescan = false;
            return;
        }
        boolean nether = level.dimension() == Level.NETHER;
        float angle = mc.gameRenderer.mainCamera().attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, 1);
        var next = new BetaLightmapState.Parameters(nether ? 15 : BetaLightMath.skyDarkening(angle,
                level.getRainLevel(1), level.getThunderLevel(1)), nether ? 0.1F : 0.05F);
        if (lastLevel != level) {
            pending.clear();
            lastLevel = level;
            current = null;
        }
        if (!next.equals(current)) {
            current = next;
            rescan = true;
        }
        // Finish an existing pass before rescanning: rapid weather/time changes cannot starve far sections.
        if (pending.isEmpty() && rescan) {
            rescan = false;
            var center = SectionPos.of(mc.getCameraEntity().blockPosition());
            int radius = mc.options.getEffectiveRenderDistance() + 1;
            var sections = new ArrayList<SectionPos>();
            for (int z = center.z() - radius; z <= center.z() + radius; z++)
                for (int x = center.x() - radius; x <= center.x() + radius; x++) {
                    var chunk = level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
                    if (chunk == null) continue;
                    var data = chunk.getSections();
                    for (int i = 0; i < data.length; i++) if (!data[i].hasOnlyAir())
                        sections.add(SectionPos.of(x, level.getSectionYFromSectionIndex(i), z));
                }
            sections.sort(Comparator.comparingLong(p -> distance(p, center)));
            pending.addAll(sections);
        }
        for (int budget = 0; budget < 64 && !pending.isEmpty(); budget++) {
            var section = pending.removeFirst();
            if (level.getChunkSource().getChunk(section.x(), section.z(), ChunkStatus.FULL, false) != null) {
                mc.levelExtractor.setSectionDirty(section.x(), section.y(), section.z());
                rebuiltSections++;
            }
        }
    }

    private static long distance(SectionPos p, SectionPos c) {
        long x = p.x() - c.x(), y = p.y() - c.y(), z = p.z() - c.z();
        return x * x + y * y + z * z;
    }
}
