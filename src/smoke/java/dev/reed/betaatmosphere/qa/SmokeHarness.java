package dev.reed.betaatmosphere.qa;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.reed.betaatmosphere.BetaLightMath;
import dev.reed.betaatmosphere.BetaLightmapState;
import dev.reed.betaatmosphere.BetaLightAtlas;
import dev.reed.betaatmosphere.BetaVisualLighting;
import dev.reed.betaatmosphere.smoke.FogRendererAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Development-only checks against the real renderer and GPU, never included in the release. */
public final class SmokeHarness {
    private static int ticks;
    private static String pending;
    private static int gpuChecks;
    private static long dayRebuilds;
    private static final List<String> results = new ArrayList<>();
    private static final boolean sodium = FabricLoader.getInstance().isModLoaded("sodium");
    private static final String prefix = sodium ? "sodium-" : "vanilla-";

    private SmokeHarness() {}

    public static void tick(Minecraft mc) {
        if (Boolean.getBoolean("betaAtmosphere.dropSmoke")) { DropSmokeHarness.tick(mc); return; }
        if (mc.level == null || mc.player == null) return;
        ticks++;
        switch (ticks) {
            case 1 -> {
                mc.options.pauseOnLostFocus = false;
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
                mc.gui.toastManager().clear();
                command(mc, "gamemode spectator", "weather clear", "time set noon", "effect clear @s",
                        "execute in minecraft:overworld run tp @s 0 110 0 0 12");
            }
            case 200 -> {
                VertexFixtures.run();
                results.add("PASS vertex fixtures: six faces, blocked diagonals, smooth/flat lighting, emission; sodium=" + sodium);
                dayRebuilds = BetaVisualLighting.rebuiltSections();
                pending = "day";
            }
            case 220 -> command(mc, "time set midnight");
            case 340 -> pending = "night";
            case 360 -> command(mc, "fill -5 90 -5 5 98 9 minecraft:stone hollow",
                    "fill -2 91 6 -2 94 6 minecraft:stone", "fill 2 91 6 2 93 6 minecraft:stone",
                    "setblock 0 91 6 minecraft:torch", "tp @s 0 92 0 0 10");
            case 480 -> pending = "torch";
            case 500 -> command(mc, "effect give @s minecraft:night_vision 60 0 true");
            case 620 -> pending = "night-vision";
            case 640 -> command(mc, "effect clear @s", "effect give @s minecraft:darkness 60 0 true");
            case 760 -> pending = "darkness";
            case 780 -> command(mc, "effect clear @s", "fill -4 91 -4 4 97 8 minecraft:water");
            case 900 -> pending = "water";
            case 920 -> command(mc, "execute in minecraft:the_nether run tp @s 0 110 0 0 12");
            case 1120 -> pending = "nether";
            case 1140 -> command(mc, "execute in minecraft:the_end run tp @s 0 80 0 0 12");
            case 1340 -> pending = "end";
            case 1360 -> {
                command(mc, "execute in minecraft:overworld run tp @s 0 110 0 0 12", "time set noon");
                mc.reloadResourcePacks();
            }
            case 1580 -> pending = "reloaded";
            case 1620 -> command(mc, "time set 12500", "tp @s 0 104 0 0 -8");
            case 1740 -> pending = "sunset";
            case 1760 -> { command(mc, "time set noon", "tp @s 0 104 0 0 -15"); mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.FAST); }
            case 1880 -> pending = "clouds-fast";
            case 1900 -> mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.FANCY);
            case 2020 -> pending = "clouds-fancy";
            case 2040 -> command(mc, "fill -10 99 -4 10 100 12 minecraft:grass_block", "fill -7 100 2 -2 100 8 minecraft:water",
                    "fill 2 100 2 7 100 8 minecraft:lava", "fill -10 101 8 -8 104 10 minecraft:oak_leaves[persistent=true]",
                    "fill 8 101 8 10 104 10 minecraft:birch_leaves[persistent=true]", "tp @s 0 106 -6 0 22", "time set noon");
            case 2180 -> pending = "fluids";
            case 2200 -> command(mc, "fill -4 91 -4 4 97 8 minecraft:air", "fill -4 91 -4 4 97 8 minecraft:lava", "tp @s 0 92 0 0 10");
            case 2320 -> pending = "lava";
            case 2400 -> {
                check(gpuChecks == 13, "expected thirteen GPU checks, got " + gpuChecks);
                check(AtmosphereFixtures.atlasChecks == 6, "expected six fluid atlas readbacks");
                results.add("PASS: all fourteen scenes, thirteen GPU lightmap readbacks and six fluid atlas readbacks; sodium=" + sodium);
                try {
                    Files.write(mc.gameDirectory.toPath().resolve(prefix + "smoke-results.txt"), results);
                } catch (Exception e) { throw new RuntimeException(e); }
                System.out.println("BETA_ATMOSPHERE_SMOKE_PASS " + String.join("; ", results));
                mc.stop();
            }
            default -> {}
        }
    }

    private static void command(Minecraft mc, String... commands) {
        for (String command : commands) mc.player.connection.sendCommand(command);
    }

    public static void afterRender(Minecraft mc) {
        if (Boolean.getBoolean("betaAtmosphere.dropSmoke")) { DropSmokeHarness.afterRender(mc); return; }
        if (pending == null || mc.level == null || !mc.gameRenderer.gameRenderState().shouldRenderLevel) return;
        String scene = pending;
        pending = null;
        var state = mc.gameRenderer.gameRenderState();
        var light = state.lightmapRenderState;
        var parameters = ((BetaLightmapState) light).betaAtmosphere$getParameters();
        var camera = state.levelRenderState.cameraRenderState;
        var fog = camera.fogData;
        if (scene.equals("day") || scene.equals("reloaded") || scene.equals("fluids")) AtmosphereFixtures.check(mc, sodium, results);
        if (scene.equals("end")) {
            check(mc.level.dimension() == Level.END && parameters == null, "End should retain its own renderer values");
        } else {
            check(parameters != null, "Beta lighting mixin is inactive in " + scene);
            if (scene.equals("day") || scene.equals("reloaded")) check(parameters.skyDarkening() == 0, "daylight darkening");
            if (scene.equals("night")) {
                check(parameters.skyDarkening() == 11, "night darkening");
                check(BetaVisualLighting.current().skyDarkening() == 11, "vertex sky darkening");
                check(BetaVisualLighting.rebuiltSections() > dayRebuilds, "sunlight failed to invalidate meshes");
                results.add("PASS sunlight mesh invalidation: " + (BetaVisualLighting.rebuiltSections() - dayRebuilds));
            }
            if (scene.equals("night-vision")) check(light.nightVisionEffectIntensity > 0.99F, "Night Vision missing");
            if (scene.equals("water")) check(camera.fogType == FogType.WATER, "underwater fog missing");
            if (scene.equals("water") || scene.equals("lava")) {
                check(fog.environmentalStart == -4096, "exponential fluid fog sentinel: " + scene + ", type=" + camera.fogType + ", start=" + fog.environmentalStart);
                float end = scene.equals("water") ? 69.07755F : 3.4538777F;
                check(Math.abs(fog.environmentalEnd - end) < 0.001F, "fluid fog density");
                if (scene.equals("lava")) check(camera.fogType == FogType.LAVA, "lava fog missing");
            }
            if (scene.equals("nether")) check(parameters.ambient() == 0.1F, "Nether floor");
            if (!scene.equals("water") && !scene.equals("lava") && !scene.equals("darkness")) {
                check(Math.abs(fog.environmentalEnd - 256.0F) < 0.001F, "Beta fog end: " + fog.environmentalEnd);
                float start = scene.equals("nether") ? 0.0F : 64.0F;
                check(Math.abs(fog.environmentalStart - start) < 0.001F, "Beta fog start");
            }
            if (sodium) {
                try {
                    Object fogRenderer = ((FogRendererAccessor) mc.gameRenderer).smoke$getFogRenderer();
                    Object stored = fogRenderer.getClass().getMethod("sodium$getFogParameters").invoke(fogRenderer);
                    float sodiumStart = (float) stored.getClass().getMethod("environmentalStart").invoke(stored);
                    float sodiumEnd = (float) stored.getClass().getMethod("environmentalEnd").invoke(stored);
                    check(sodiumStart == fog.environmentalStart && sodiumEnd == fog.environmentalEnd,
                            "Sodium captured different fog values");
                } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
            }
            var atlas = new BetaLightAtlas();
            atlas.update(parameters.skyDarkening(), parameters.ambient());
            int[] expected = new int[256 * 256];
            for (int sky = 0; sky < 256; sky++) {
                for (int block = 0; block < 256; block++) {
                    expected[sky * 256 + block] = BetaLightMath.applyEffects(atlas.brightness(block, sky),
                            light.nightVisionEffectIntensity, light.darknessEffectScale,
                            light.bossOverlayWorldDarkening);
                }
            }
            var texture = mc.gameRenderer.levelLightmap().texture();
            var buffer = RenderSystem.getDevice().createBuffer(() -> "Beta smoke readback", 9, 256L * 256 * 4);
            RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, buffer, 0L, () -> {
                try (var read = buffer.map(true, false)) {
                    for (int i = 0; i < expected.length; i++) {
                        int pixel = expected[i];
                        int offset = i * 4;
                        check((read.data().get(offset) & 255) == (pixel >>> 16 & 255), scene + " GPU red " + i);
                        check((read.data().get(offset + 1) & 255) == (pixel >>> 8 & 255), scene + " GPU green " + i);
                        check((read.data().get(offset + 2) & 255) == (pixel & 255), scene + " GPU blue " + i);
                    }
                    gpuChecks++;
                    results.add("PASS GPU: " + scene);
                } finally { buffer.close(); }
            }, 0);
        }
        results.add("PASS scene: " + scene + "; fog=" + fog.environmentalStart + ".." + fog.environmentalEnd
                + "; color=" + fog.color + "; beta=" + parameters);
        Screenshot.grab(mc.gameDirectory, prefix + scene + ".png", mc.gameRenderer.mainRenderTarget(), 1,
                message -> System.out.println("BETA_SCREENSHOT " + scene + ": " + message.getString()));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("BETA_ATMOSPHERE_SMOKE_FAIL: " + message);
    }
}
