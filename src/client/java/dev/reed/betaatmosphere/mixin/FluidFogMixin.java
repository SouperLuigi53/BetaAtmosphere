package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.*;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.*;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin({WaterFogEnvironment.class, LavaFogEnvironment.class})
abstract class FluidFogMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void betaAtmosphere$density(FogData fog, Camera camera, ClientLevel level, float distance, DeltaTracker delta, CallbackInfo ci) {
        if (!BetaFogBrightness.supports(level)) return;
        float density = (Object) this instanceof WaterFogEnvironment ? 0.1F : 2F;
        fog.environmentalStart = BetaAtmosphereMath.EXPONENTIAL_FOG;
        fog.environmentalEnd = BetaAtmosphereMath.FOG_OPAQUE_EXPONENT / density;
        fog.skyEnd = fog.cloudEnd = fog.environmentalEnd;
    }
    @Inject(method = "getBaseColor", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$color(ClientLevel level, Camera camera, int distance, float partial, CallbackInfoReturnable<Vector3fc> cir) {
        if (!BetaFogBrightness.supports(level)) return;
        var color = (Object) this instanceof WaterFogEnvironment ? new Vector3f(0.02F, 0.02F, 0.2F) : new Vector3f(0.6F, 0.1F, 0);
        cir.setReturnValue(color.mul(BetaFogBrightness.interpolated(partial)));
    }
}
