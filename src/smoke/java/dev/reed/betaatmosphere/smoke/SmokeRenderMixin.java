package dev.reed.betaatmosphere.smoke;

import net.minecraft.client.Minecraft;
import dev.reed.betaatmosphere.qa.SmokeHarness;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class SmokeRenderMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void smoke$render(CallbackInfo ci) { SmokeHarness.afterRender(Minecraft.getInstance()); }
}
