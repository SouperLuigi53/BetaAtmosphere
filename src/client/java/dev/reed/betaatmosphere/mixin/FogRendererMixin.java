package dev.reed.betaatmosphere.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.reed.betaatmosphere.BetaFogBrightness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
    @Unique private static boolean betaAtmosphere$active() {
        var level = Minecraft.getInstance().level;
        return level != null && BetaFogBrightness.supports(level);
    }
    @ModifyExpressionValue(method = "computeFogColor", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getWaterVision()F"))
    private float betaAtmosphere$waterAdaptation(float value) { return betaAtmosphere$active() ? 0 : value; }

    // Only the initial height-based darkness, before status effects modify it.
    @ModifyVariable(method = "computeFogColor", at = @At(value = "STORE", ordinal = 0), ordinal = 3)
    private float betaAtmosphere$noVoidDarkness(float value) { return betaAtmosphere$active() ? 0 : value; }
}
