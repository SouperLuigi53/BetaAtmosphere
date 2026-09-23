package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaFogBrightness;
import dev.reed.betaatmosphere.BetaFogMath;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AtmosphericFogEnvironment.class)
abstract class AtmosphericFogMixin {
    @Inject(method = "getBaseColor", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$color(ClientLevel level, Camera camera, int renderDistance,
                                     float partialTicks, CallbackInfoReturnable<Vector3fc> cir) {
        if (!BetaFogBrightness.supports(level)) return;
        Vector3f color;
        if (level.dimension() == Level.NETHER) {
            color = new Vector3f(0.2F, 0.03F, 0.03F);
        } else {
            float angle = camera.attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, partialTicks);
            float daylight = Mth.clamp(Mth.cos(angle * Mth.DEG_TO_RAD) * 2.0F + 0.5F, 0.0F, 1.0F);
            color = new Vector3f((192.0F / 255.0F) * (daylight * 0.94F + 0.06F),
                    (216.0F / 255.0F) * (daylight * 0.94F + 0.06F), daylight * 0.91F + 0.09F);
            // The probe supplies Beta's temperature-based sky color in the Overworld.
            Vector3fc sky = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_COLOR, partialTicks);
            color.lerp(sky, BetaFogMath.skyMix(renderDistance * 16.0F));
            float rain = level.getRainLevel(partialTicks);
            float thunder = 1.0F - level.getThunderLevel(partialTicks) * 0.5F;
            color.mul((1.0F - rain * 0.5F) * thunder, (1.0F - rain * 0.5F) * thunder,
                    (1.0F - rain * 0.4F) * thunder);
        }
        color.mul(BetaFogBrightness.interpolated(partialTicks));
        cir.setReturnValue(color);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private void betaAtmosphere$distance(FogData fog, Camera camera, ClientLevel level,
                                        float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!BetaFogBrightness.supports(level)) return;
        // Keep the boss-imposed visibility restriction. Fluids and status-effect fog use other classes.
        if (Minecraft.getInstance().gui.hud.getBossOverlay().shouldCreateWorldFog()) return;
        float end = BetaFogMath.distance(renderDistance);
        fog.environmentalStart = level.dimension() == Level.NETHER ? 0.0F : end * 0.25F;
        fog.environmentalEnd = end;
        fog.skyEnd = end * 0.8F;
        fog.cloudEnd = end;
        // This executes inside FogRenderer.setupFog, before Sodium captures the result at RETURN.
    }
}
