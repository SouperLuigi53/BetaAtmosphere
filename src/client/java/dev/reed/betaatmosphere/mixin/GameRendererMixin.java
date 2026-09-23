package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaFogBrightness;
import dev.reed.betaatmosphere.BetaVisualLighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void betaAtmosphere$tickFog(CallbackInfo ci) {
        BetaFogBrightness.tick(Minecraft.getInstance());
        BetaVisualLighting.tick(Minecraft.getInstance());
    }
}
