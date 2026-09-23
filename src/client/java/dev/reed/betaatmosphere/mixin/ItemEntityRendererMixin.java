package dev.reed.betaatmosphere.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.reed.betaatmosphere.BetaDroppedItemState;
import dev.reed.betaatmosphere.BetaDroppedItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
abstract class ItemEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V", at = @At("TAIL"))
    private void betaAtmosphere$sprite(ItemEntity entity, ItemEntityRenderState state, float partial, CallbackInfo ci) {
        boolean flat = BetaDroppedItems.flatten(state.item);
        ((BetaDroppedItemState) state).betaAtmosphere$setFlat(flat);
        if (flat) state.count = BetaDroppedItems.renderedCopies(entity.getItem().getCount());
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotate(Lcom/mojang/math/Axis;F)V"))
    private void betaAtmosphere$billboard(PoseStack pose, Axis axis, float spin, ItemEntityRenderState state,
                                         PoseStack outerPose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (((BetaDroppedItemState) state).betaAtmosphere$isFlat()) pose.rotate(camera.orientation);
        else pose.rotate(axis, spin);
    }

    @ModifyArg(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V"), index = 5)
    private AABB betaAtmosphere$stackSpacing(AABB bounds) {
        // Keep separate stack copies from sharing a depth plane; each sprite itself is still 2D.
        return bounds.getZsize() == 0 ? bounds.inflate(0, 0, 0.015625) : bounds;
    }
}
