package dev.reed.betaatmosphere.smoke;

import net.minecraft.client.Minecraft;
import dev.reed.betaatmosphere.qa.SmokeHarness;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class SmokeTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void smoke$tick(CallbackInfo ci) { SmokeHarness.tick(Minecraft.getInstance()); }
}
