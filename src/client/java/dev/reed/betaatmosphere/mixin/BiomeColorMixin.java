package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaBiomeColors;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
abstract class BiomeColorMixin {
    @Shadow protected abstract int getGrassColorFromTexture();
    @Shadow protected abstract int getFoliageColorFromTexture();
    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$grass(double x, double z, CallbackInfoReturnable<Integer> cir) {
        if (BetaBiomeColors.preserves((Biome) (Object) this)) return;
        cir.setReturnValue(getGrassColorFromTexture());
    }
    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$leaves(CallbackInfoReturnable<Integer> cir) {
        if (BetaBiomeColors.preserves((Biome) (Object) this)) return;
        cir.setReturnValue(getFoliageColorFromTexture());
    }
    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$water(CallbackInfoReturnable<Integer> cir) { cir.setReturnValue(0xFFFFFFFF); }
}
