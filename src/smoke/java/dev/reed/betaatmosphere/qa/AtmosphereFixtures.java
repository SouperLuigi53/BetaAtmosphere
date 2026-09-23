package dev.reed.betaatmosphere.qa;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.reed.betaatmosphere.*;
import dev.reed.betaatmosphere.mixin.TextureAtlasSpriteAccessor;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.FoliageColor;

public final class AtmosphereFixtures {
    public static final Set<String> shaders = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public static int atlasChecks;
    private static int previousHash;

    public static void check(Minecraft mc, boolean sodium, List<String> results) {
        try {
            var mod = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("beta_atmosphere").orElseThrow();
            for (String path : new String[]{"textures/environment/clouds.png", "textures/environment/celestial/sun.png", "textures/environment/celestial/moon/full_moon.png"}) {
                byte[] original = java.nio.file.Files.readAllBytes(mod.findPath("assets/minecraft/" + path).orElseThrow());
                try (var actual = mc.getResourceManager().open(Identifier.withDefaultNamespace(path))) {
                    require(Arrays.equals(original, actual.readAllBytes()), "original Beta resource: " + path);
                }
            }
            results.add("PASS active cloud, sun and moon resources are byte-identical to bundled Beta originals");
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
        var probe = mc.gameRenderer.mainCamera().attributeProbe();
        require(probe.getValue(EnvironmentAttributes.CLOUD_HEIGHT, 1) == 108.33F, "Beta cloud height");
        require(probe.getValue(EnvironmentAttributes.MOON_PHASE, 1) == MoonPhase.FULL_MOON, "Beta moon phase");
        for (double temp : new double[]{0, 0.25, 0.75, 1}) for (double rain : new double[]{0, 0.5, 1}) {
            require(GrassColor.get(temp, rain) == (0xFF000000 | BetaColorMaps.grass(temp, rain)), "grass map");
            require(FoliageColor.get(temp, rain) == (0xFF000000 | BetaColorMaps.foliage(temp, rain)), "foliage map");
        }
        require(mc.level.getBiome(mc.player.blockPosition()).value().getWaterColor() == -1, "untinted water");
        require(shaders.stream().anyMatch(s -> s.startsWith("minecraft:")), "vanilla exponential fog shader hook");
        if (sodium) require(shaders.stream().anyMatch(s -> s.startsWith("sodium:")), "Sodium exponential fog shader hook");
        results.add("PASS sky probe, full moon, cloud height, vegetation maps, water tint, exponential shader includes: " + shaders);
        var atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
        for (int mip : new int[]{0, Math.min(4, atlas.getTexture().getMipLevels() - 1)}) readAtlas(atlas, mip, results);
    }

    private static void readAtlas(TextureAtlas atlas, int mip, List<String> results) {
        var texture = atlas.getTexture();
        int width = texture.getWidth(mip), height = texture.getHeight(mip);
        var buffer = RenderSystem.getDevice().createBuffer(() -> "Beta fluid atlas readback", 9, (long) width * height * 4);
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, buffer, 0, () -> {
            try (var view = buffer.map(true, false)) {
                int hash = 1;
                for (String name : new String[]{"water_still", "water_flow", "lava_still", "lava_flow"}) {
                    var sprite = atlas.getSprite(Identifier.withDefaultNamespace("block/" + name));
                    int padding = ((TextureAtlasSpriteAccessor) sprite).betaAtmosphere$padding();
                    int sx = (sprite.getX() + padding) >> mip, sy = (sprite.getY() + padding) >> mip;
                    for (int y = 0; y < Math.max(1, sprite.contents().height() >> mip); y++)
                        for (int x = 0; x < Math.max(1, sprite.contents().width() >> mip); x++) {
                            int index = ((sy + y) * width + sx + x) * 4;
                            int r = view.data().get(index) & 255, b = view.data().get(index + 2) & 255, a = view.data().get(index + 3) & 255;
                            if (name.startsWith("water")) require(r >= 32 && r <= 64 && b == 255 && a >= 146 && a <= 196, name + " mip " + mip + " palette " + r + "/" + b + "/" + a);
                            else require(r >= 155 && b <= 128 && a == 255, name + " mip " + mip + " palette");
                            hash = hash * 31 + r * 257 + b;
                        }
                }
                if (mip == 0) { require(previousHash != hash, "fluids failed to animate between captures"); previousHash = hash; }
                atlasChecks++;
                results.add("PASS all four procedural fluid sprites on GPU; mip=" + mip + "; hash=" + hash);
            } finally { buffer.close(); }
        }, mip);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError("BETA_ATMOSPHERE_SMOKE_FAIL: " + message);
    }
}
