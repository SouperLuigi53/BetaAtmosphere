package dev.reed.betaatmosphere.qa;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biomes;

/** Focused runtime checks for the 0.4 changes, in the same disposable smoke world. */
public final class DropSmokeHarness {
    private static int ticks, captures;
    private static String pending;
    private static final List<String> results = new ArrayList<>();
    private static final String prefix = FabricLoader.getInstance().isModLoaded("sodium") ? "sodium-" : "vanilla-";
    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        switch (++ticks) {
            case 1 -> {
                mc.options.pauseOnLostFocus = false;
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
                mc.gui.toastManager().clear();
                command(mc, "gamemode spectator", "weather clear", "time set noon", "effect clear @s",
                        "execute in minecraft:overworld run tp @s 0 102 -5 0 18",
                        "fill -12 100 -8 12 108 12 air", "fill -12 99 -8 12 100 12 grass_block",
                        "fillbiome -20 96 -20 20 112 20 minecraft:dappled_forest",
                        "fill -8 101 5 -5 104 8 oak_leaves[persistent=true]", "fill 5 101 5 8 104 8 oak_leaves[persistent=true]",
                        "kill @e[type=minecraft:item,tag=beta_atmosphere_smoke]");
                String[] items = {"diamond", "iron_sword", "apple", "stone", "potion"};
                for (int i = 0; i < items.length; i++) command(mc, "summon minecraft:item " + (i * 1.5 - 3) + " 101.5 0 {NoGravity:1b,PickupDelay:32767s,Tags:[\"beta_atmosphere_smoke\"],Item:{id:\"minecraft:" + items[i] + "\",count:1}}");
            }
            case 180 -> { DropFixtures.run(mc, results); pending = "dappled-drops-front"; }
            case 200 -> command(mc, "tp @s -5 102 0 -90 18");
            case 240 -> pending = "drops-side";
            case 260 -> command(mc, "tp @s 0 107 -1 0 75");
            case 300 -> pending = "drops-above";
            case 320 -> { command(mc, "tp @s 0 102 -5 0 18"); mc.reloadResourcePacks(); }
            case 480 -> { DropFixtures.run(mc, results); pending = "drops-reloaded"; }
            case 540 -> {
                DropFixtures.check(captures == 4, "four captured scenes");
                results.add("PASS all focused 0.4 runtime checks; renderer=" + prefix);
                try { Files.write(mc.gameDirectory.toPath().resolve(prefix + "0.4-smoke-results.txt"), results); }
                catch (Exception e) { throw new RuntimeException(e); }
                System.out.println("BETA_DROP_SMOKE_PASS " + String.join("; ", results));
                mc.stop();
            }
            default -> {}
        }
    }
    public static void afterRender(Minecraft mc) {
        if (pending == null || mc.level == null || !mc.gameRenderer.gameRenderState().shouldRenderLevel) return;
        String scene = pending; pending = null;
        DropFixtures.check(mc.level.getBiome(new BlockPos(0, 101, 0)).is(Biomes.DAPPLED_FOREST), "fixture biome update reached client");
        var grass = mc.level.getBiome(new BlockPos(0, 101, 0)).value();
        DropFixtures.check((grass.getGrassColor(0, 0) & 0xFFFFFF) == 14641191, "in-world orange grass");
        results.add("PASS scene " + scene + "; Dappled grass=" + Integer.toHexString(grass.getGrassColor(0, 0)) + "; foliage=" + Integer.toHexString(grass.getFoliageColor()));
        Screenshot.grab(mc.gameDirectory, prefix + "0.4-" + scene + ".png", mc.gameRenderer.mainRenderTarget(), 1,
                message -> System.out.println("BETA_DROP_SCREENSHOT " + message.getString()));
        captures++;
    }
    private static void command(Minecraft mc, String... commands) { for (String command : commands) mc.player.connection.sendCommand(command); }
}
