package dev.reed.betaatmosphere.qa;

import java.lang.reflect.Proxy;
import dev.reed.betaatmosphere.*;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuad;
import net.caffeinemc.mods.sodium.client.render.helper.ColorHelper;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

final class SodiumVertexFixtures {
    static void run(BlockAndTintGetter world, Direction face, Vector3f[] vertices, float[] expected) {
        var cache = new LightDataAccess() {
            { level = world; }
            public int get(int x, int y, int z) { throw new AssertionError("Modern Sodium light path was used"); }
        };
        var quad = (ModelQuadView) Proxy.newProxyInstance(SodiumVertexFixtures.class.getClassLoader(), new Class[]{ModelQuadView.class}, (proxy, method, args) -> {
            var v = vertices[(int) args[0]];
            return switch (method.getName()) {
                case "getX" -> v.x(); case "getY" -> v.y(); case "getZ" -> v.z();
                default -> throw new AssertionError("Unexpected quad access " + method);
            };
        });
        var data = new QuadLightData();
        new SmoothLightPipeline(cache).calculate(quad, BlockPos.ZERO, data, face, face, null, true);
        for (int i = 0; i < 4; i++) {
            VertexFixtures.assertLight(data.lm[i], expected[i], "Sodium " + face + " corner " + i);
            VertexFixtures.check(data.br[i] == BetaBlockLighting.shade(face), "Sodium face shade");
        }
        new FlatLightPipeline(cache).calculate(quad, BlockPos.ZERO, data, face, face, null, false);
        for (int light : data.lm) VertexFixtures.assertLight(light, 1, "Sodium flat " + face);
        new FlatFluidLightPipeline(cache).calculate(new ModelQuad(), BlockPos.ZERO, data, null, face, Direction.UP, false);
        for (int light : data.lm) VertexFixtures.assertLight(light, 1, "Sodium fluid " + face);
        float ambient = BetaVisualLighting.current().ambient();
        VertexFixtures.assertLight(ColorHelper.maxBrightness(14 << 4, BetaVertexMath.encode(ambient, ambient)),
                BetaLightMath.brightness(14, ambient), "Sodium material emission");
    }
}
