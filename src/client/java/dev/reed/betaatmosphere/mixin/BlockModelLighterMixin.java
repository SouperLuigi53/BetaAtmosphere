package dev.reed.betaatmosphere.mixin;

import com.mojang.blaze3d.vertex.QuadInstance;
import dev.reed.betaatmosphere.BetaBlockLighting;
import dev.reed.betaatmosphere.BetaVisualLighting;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelLighter.class)
abstract class BlockModelLighterMixin {
    @Unique private final BetaBlockLighting betaAtmosphere$lighting = new BetaBlockLighting();

    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$smooth(BlockAndTintGetter level, BlockState state, BlockPos pos,
                                       BakedQuad quad, QuadInstance output, CallbackInfo ci) {
        var parameters = BetaVisualLighting.current();
        if (parameters == null || level == BlockAndTintGetter.EMPTY) return;
        betaAtmosphere$lighting.prepare(level, state, pos, quad.direction(), parameters);
        var shade = quad.materialInfo().shadeDirectionOverride();
        output.setColor(ARGB.gray(BetaBlockLighting.shade(shade == null ? quad.direction() : shade)));
        for (int i = 0; i < 4; i++) {
            var vertex = quad.position(i);
            output.setLightCoords(i, betaAtmosphere$lighting.smooth(vertex.x(), vertex.y(), vertex.z()));
        }
        ci.cancel();
    }

    @Inject(method = "prepareQuadFlat", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$flat(BlockAndTintGetter level, BlockState state, BlockPos pos, int lightCoords,
                                     BakedQuad quad, QuadInstance output, CallbackInfo ci) {
        var parameters = BetaVisualLighting.current();
        if (parameters == null || level == BlockAndTintGetter.EMPTY) return;
        var face = quad.direction();
        boolean boundary = true;
        for (int i = 0; i < 4; i++) {
            var v = quad.position(i);
            double normal = face.getAxis().choose(v.x(), v.y(), v.z());
            boundary &= BetaBlockLighting.boundary(face, normal);
        }
        var shade = quad.materialInfo().shadeDirectionOverride();
        output.setColor(ARGB.gray(BetaBlockLighting.shade(shade == null ? face : shade)));
        output.setLightCoords(betaAtmosphere$lighting.flat(level, state, pos, face, boundary, parameters));
        ci.cancel();
    }
}
