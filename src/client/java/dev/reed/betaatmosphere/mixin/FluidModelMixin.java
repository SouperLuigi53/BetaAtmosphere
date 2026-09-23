package dev.reed.betaatmosphere.mixin;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidModel.Unbaked.class)
abstract class FluidModelMixin {
    @Inject(method = "bake", at = @At("RETURN"), cancellable = true)
    private void betaAtmosphere$water(MaterialBaker materials, ModelDebugName name, CallbackInfoReturnable<FluidModel> cir) {
        var model = cir.getReturnValue();
        if (model.stillMaterial().sprite().contents().name().equals(Identifier.withDefaultNamespace("block/water_still")))
            cir.setReturnValue(new FluidModel(model.layer(), model.stillMaterial(), model.flowingMaterial(), null, null));
    }
}
