package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.*;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightCoordsUtil.class)
abstract class LightEmissionMixin {
    @Inject(method = "lightCoordsWithEmission", at = @At("HEAD"), cancellable = true)
    private static void betaAtmosphere$emission(int light, int emission, CallbackInfoReturnable<Integer> cir) {
        var p = BetaVisualLighting.current();
        if (p != null && BetaVertexMath.encoded(light)) cir.setReturnValue(BetaVertexMath.encode(
                Math.max(BetaVertexMath.decode(light, p.ambient()), BetaLightMath.brightness(emission, p.ambient())), p.ambient()));
    }
}
