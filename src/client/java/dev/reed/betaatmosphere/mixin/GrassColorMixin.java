package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaColorMaps;
import net.minecraft.world.level.GrassColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrassColor.class)
abstract class GrassColorMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void betaAtmosphere$map(double temperature, double rain, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0xFF000000 | BetaColorMaps.grass(temperature, rain));
    }
}
