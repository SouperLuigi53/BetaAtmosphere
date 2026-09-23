package dev.reed.betaatmosphere;

import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuad;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Loaded only when Sodium is installed. Uses the same sampler as the vanilla adapter. */
public final class BetaSodiumLighting {
    private BetaSodiumLighting() {}
    public static boolean calculate(BetaBlockLighting sampler, LightDataAccess cache, ModelQuadView quad,
                                    BlockPos pos, QuadLightData out, Direction face, Direction shade,
                                    boolean smooth) {
        var parameters = BetaVisualLighting.current();
        if (parameters == null) return false;
        var level = cache.getLevel();
        var state = level.getBlockState(pos);
        boolean fluid = quad instanceof ModelQuad;
        smooth &= !fluid;
        if (smooth) sampler.prepare(level, state, pos, face, parameters);
        boolean boundary = true;
        for (int i = 0; i < 4; i++) boundary &= BetaBlockLighting.boundary(face,
                face.getAxis().choose(quad.getX(i), quad.getY(i), quad.getZ(i)));
        if (fluid) boundary = face != Direction.UP;
        int flat = smooth ? 0 : sampler.flat(level, state, pos, face, boundary, parameters);
        float diffuse = BetaBlockLighting.shade(shade == null ? face : shade);
        if (fluid) {
            // Sodium's fluid emitter already multiplies horizontal faces by 0.8 / 0.6.
            diffuse = face == Direction.DOWN ? 0.5F : 1;
        }
        for (int i = 0; i < 4; i++) {
            out.br[i] = diffuse;
            out.lm[i] = smooth ? sampler.smooth(quad.getX(i), quad.getY(i), quad.getZ(i)) : flat;
        }
        return true;
    }
}
