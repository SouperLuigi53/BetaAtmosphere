package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaFogBrightness;
import dev.reed.betaatmosphere.BetaLightmapState;
import dev.reed.betaatmosphere.BetaLightMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
abstract class LightmapExtractorMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extract", at = @At("TAIL"))
    private void betaAtmosphere$capture(LightmapRenderState state, float partialTicks, CallbackInfo ci) {
        var target = (BetaLightmapState) state;
        var level = minecraft.level;
        if (level == null || !BetaFogBrightness.supports(level)) {
            target.betaAtmosphere$setParameters(null);
        } else {
            boolean nether = level.dimension() == Level.NETHER;
            float angle = minecraft.gameRenderer.mainCamera().attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, partialTicks);
            int darkening = nether ? 15 : BetaLightMath.skyDarkening(angle,
                    level.getRainLevel(partialTicks), level.getThunderLevel(partialTicks));
            target.betaAtmosphere$setParameters(new BetaLightmapState.Parameters(darkening, nether ? 0.1F : 0.05F));
        }
    }
}
