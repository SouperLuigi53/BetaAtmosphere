package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaAtmosphereMath;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnvironmentAttributeProbe.class)
abstract class EnvironmentProbeMixin {
    @Shadow private Level level;
    @Shadow private Vec3 position;
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$sky(EnvironmentAttribute<?> attribute, float partial, CallbackInfoReturnable<Object> cir) {
        if (!(level instanceof ClientLevel client) || level.dimension() != Level.OVERWORLD || position == null) return;
        if (attribute == EnvironmentAttributes.CLOUD_HEIGHT) { cir.setReturnValue(108.33F); return; }
        if (attribute == EnvironmentAttributes.MOON_PHASE) { cir.setReturnValue(MoonPhase.FULL_MOON); return; }
        if (attribute != EnvironmentAttributes.SUN_ANGLE && attribute != EnvironmentAttributes.MOON_ANGLE
                && attribute != EnvironmentAttributes.STAR_ANGLE && attribute != EnvironmentAttributes.SKY_COLOR
                && attribute != EnvironmentAttributes.CLOUD_COLOR && attribute != EnvironmentAttributes.SUNRISE_SUNSET_COLOR
                && attribute != EnvironmentAttributes.STAR_BRIGHTNESS) return;
        float angle = BetaAtmosphereMath.celestialAngle(level.getOverworldClockTime(), partial);
        float rain = client.getRainLevel(partial), thunder = client.getThunderLevel(partial);
        if (attribute == EnvironmentAttributes.SUN_ANGLE || attribute == EnvironmentAttributes.STAR_ANGLE) cir.setReturnValue(angle * 360);
        else if (attribute == EnvironmentAttributes.MOON_ANGLE) cir.setReturnValue(angle * 360 + 180);
        else if (attribute == EnvironmentAttributes.STAR_BRIGHTNESS) cir.setReturnValue(BetaAtmosphereMath.stars(angle, rain));
        else if (attribute == EnvironmentAttributes.SUNRISE_SUNSET_COLOR) {
            float[] c = BetaAtmosphereMath.sunset(angle);
            cir.setReturnValue(new Vector4f(c[0], c[1], c[2], c[3]));
        } else if (attribute == EnvironmentAttributes.CLOUD_COLOR) {
            float[] c = BetaAtmosphereMath.cloud(angle, rain, thunder);
            cir.setReturnValue(new Vector4f(c[0], c[1], c[2], 0.8F));
        } else {
            float temperature = level.getBiome(BlockPos.containing(position)).value().getBaseTemperature();
            float flash = ((ClientLevelAccessor) client).betaAtmosphere$skyFlashTime() - partial;
            float[] c = BetaAtmosphereMath.sky(temperature, angle, rain, thunder, flash);
            cir.setReturnValue(new Vector3f(c[0], c[1], c[2]));
        }
    }
}
