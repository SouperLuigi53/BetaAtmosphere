package dev.reed.betaatmosphere.mixin.sodium;

import dev.reed.betaatmosphere.BetaBlockLighting;
import dev.reed.betaatmosphere.BetaSodiumLighting;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatFluidLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = FlatFluidLightPipeline.class, remap = false)
abstract class SodiumFluidLightMixin extends FlatLightPipeline {
    @Unique private final BetaBlockLighting betaAtmosphere$fluid = new BetaBlockLighting();
    protected SodiumFluidLightMixin(LightDataAccess cache) { super(cache); }
    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$calculate(ModelQuadView quad, BlockPos pos, QuadLightData out,
                                          Direction cull, Direction face, Direction shade, boolean enhanced, CallbackInfo ci) {
        if (BetaSodiumLighting.calculate(betaAtmosphere$fluid, lightCache, quad, pos, out, face, shade, false)) ci.cancel();
    }
}
