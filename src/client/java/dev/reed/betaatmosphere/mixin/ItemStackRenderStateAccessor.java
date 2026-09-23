package dev.reed.betaatmosphere.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {
    @Accessor("activeLayerCount") int betaAtmosphere$layerCount();
    @Accessor("layers") ItemStackRenderState.LayerRenderState[] betaAtmosphere$layers();
}
