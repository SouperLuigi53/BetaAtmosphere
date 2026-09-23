package dev.reed.betaatmosphere.qa;

import java.lang.reflect.Proxy;
import com.mojang.blaze3d.vertex.QuadInstance;
import dev.reed.betaatmosphere.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

/** Calls transformed renderer classes against an asymmetric, known light field. */
public final class VertexFixtures {
    public static void run() {
        float ambient = BetaVisualLighting.current().ambient();
        float b4 = BetaLightMath.brightness(4, ambient);
        float[] expected = {(1 + ambient + b4 + ambient) / 4, (3 + b4) / 4,
                (3 + ambient) / 4, 1};
        for (Direction face : Direction.values()) {
            var level = level(face);
            var v = vertices(face);
            var material = new BakedQuad.MaterialInfo(null, null, null, null, null, -1, null, 0);
            var quad = new BakedQuad(v[0], v[1], v[2], v[3], 0, 0, 0, 0, face, material);
            var output = new QuadInstance();
            var lighter = new BlockModelLighter();
            lighter.prepareQuadAmbientOcclusion(level, Blocks.STONE.defaultBlockState(), BlockPos.ZERO, quad, output);
            for (int i = 0; i < 4; i++) {
                assertLight(output.getLightCoords(i), expected[i], "vanilla " + face + " corner " + i);
                check(output.getColor(i) == ARGB.gray(BetaBlockLighting.shade(face)), "vanilla face shade " + face);
            }
            lighter.prepareQuadFlat(level, Blocks.STONE.defaultBlockState(), BlockPos.ZERO, -1, quad, output);
            for (int i = 0; i < 4; i++) assertLight(output.getLightCoords(i), 1, "vanilla flat " + face);
            if (FabricLoader.getInstance().isModLoaded("sodium")) SodiumVertexFixtures.run(level, face, v, expected);
        }
        int encoded = BetaVertexMath.encode(ambient, ambient);
        assertLight(LightCoordsUtil.lightCoordsWithEmission(encoded, 14), BetaLightMath.brightness(14, ambient), "material emission");
    }

    static BlockAndTintGetter level(Direction face) {
        return (BlockAndTintGetter) Proxy.newProxyInstance(VertexFixtures.class.getClassLoader(), new Class[]{BlockAndTintGetter.class}, (proxy, method, args) -> {
            if (method.getName().equals("cardinalLighting")) return CardinalLighting.DEFAULT;
            var pos = (BlockPos) args[method.getName().equals("getBrightness") ? 1 : 0];
            int a = face.getAxis() == Direction.Axis.X ? pos.getZ() : pos.getX();
            int b = face.getAxis() == Direction.Axis.Y ? pos.getZ() : pos.getY();
            if (method.getName().equals("getBlockState"))
                return (a == -1 && b == 0 || a == 0 && b == -1 ? Blocks.STONE : Blocks.AIR).defaultBlockState();
            if (method.getName().equals("getBrightness")) {
                if (args[0] == LightLayer.SKY) return 0;
                return a == -1 && b == 0 ? 0 : a == 0 && b == -1 ? 4 : 15;
            }
            throw new AssertionError("Unexpected fixture access: " + method);
        });
    }

    static Vector3f[] vertices(Direction face) {
        var vertices = new Vector3f[4];
        float normal = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
        for (int i = 0; i < 4; i++) {
            float a = i % 2, b = i / 2;
            vertices[i] = switch (face.getAxis()) {
                case X -> new Vector3f(normal, b, a);
                case Y -> new Vector3f(a, normal, b);
                case Z -> new Vector3f(a, b, normal);
            };
        }
        return vertices;
    }

    static void assertLight(int actual, float expected, String label) {
        check(BetaVertexMath.encoded(actual), label + " did not enter Beta pipeline: " + actual);
        check(Math.abs(BetaVertexMath.decode(actual, BetaVisualLighting.current().ambient()) - expected) < 0.0021F, label + " incorrect brightness");
    }
    static void check(boolean condition, String label) { if (!condition) throw new AssertionError(label); }
}
