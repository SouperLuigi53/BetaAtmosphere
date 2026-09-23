package dev.reed.betaatmosphere.mixin.sodium;

import dev.reed.betaatmosphere.BetaBlockLighting;
import dev.reed.betaatmosphere.BetaSodiumLighting;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = {SmoothLightPipeline.class, FlatLightPipeline.class}, remap = false)
abstract class SodiumLightPipelineMixin {
    @Shadow(remap = false) @Final private LightDataAccess lightCache;
    @Unique private final BetaBlockLighting betaAtmosphere$lighting = new BetaBlockLighting();
    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true, remap = false)
    private void betaAtmosphere$calculate(ModelQuadView quad, BlockPos pos, QuadLightData out,
                                          Direction cull, Direction face, Direction shade, boolean enhanced, CallbackInfo ci) {
        if (BetaSodiumLighting.calculate(betaAtmosphere$lighting, lightCache, quad, pos, out, face, shade,
                (Object) this instanceof SmoothLightPipeline)) ci.cancel();
    }
}
