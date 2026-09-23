package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaLightmapState;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LightmapRenderState.class)
abstract class LightmapStateMixin implements BetaLightmapState {
    @Unique private Parameters betaAtmosphere$parameters;

    @Override public Parameters betaAtmosphere$getParameters() { return betaAtmosphere$parameters; }
    @Override public void betaAtmosphere$setParameters(Parameters parameters) { betaAtmosphere$parameters = parameters; }
}
