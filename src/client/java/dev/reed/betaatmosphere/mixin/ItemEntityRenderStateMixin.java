package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaDroppedItemState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
abstract class ItemEntityRenderStateMixin implements BetaDroppedItemState {
    @Unique private boolean betaAtmosphere$flat;
    @Override public boolean betaAtmosphere$isFlat() { return betaAtmosphere$flat; }
    @Override public void betaAtmosphere$setFlat(boolean flat) { betaAtmosphere$flat = flat; }
}
