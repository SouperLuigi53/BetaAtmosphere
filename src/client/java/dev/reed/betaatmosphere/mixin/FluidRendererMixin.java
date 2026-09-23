package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaBlockLighting;
import dev.reed.betaatmosphere.BetaVisualLighting;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FluidRenderer.class)
abstract class FluidRendererMixin {
    @Unique private static final ThreadLocal<BetaBlockLighting> betaAtmosphere$fluid = ThreadLocal.withInitial(BetaBlockLighting::new);

    @ModifyArgs(method = "tesselate", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/renderer/block/FluidRenderer;addFace(Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFFFFFFFFFFFFFFFFIIZ)V"))
    private void betaAtmosphere$fluidFace(Args args, BlockAndTintGetter level, BlockPos pos,
                                          FluidRenderer.Output output, BlockState state, FluidState fluid) {
        var parameters = BetaVisualLighting.current();
        if (parameters == null) return;
        float ax = (float) args.get(6) - (float) args.get(1);
        float ay = (float) args.get(7) - (float) args.get(2);
        float az = (float) args.get(8) - (float) args.get(3);
        float bx = (float) args.get(11) - (float) args.get(1);
        float by = (float) args.get(12) - (float) args.get(2);
        float bz = (float) args.get(13) - (float) args.get(3);
        float nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
        Direction face = Math.abs(ny) > 0.0001F ? (ny > 0 ? Direction.UP : Direction.DOWN)
                : Math.abs(nx) > Math.abs(nz) ? (nx > 0 ? Direction.EAST : Direction.WEST)
                : (nz > 0 ? Direction.SOUTH : Direction.NORTH);
        args.set(22, betaAtmosphere$fluid.get().flat(level, state, pos, face, face != Direction.UP, parameters));
        float oldShade = level.cardinalLighting().byFace(face);
        if (face.getAxis() != Direction.Axis.Y) oldShade *= level.cardinalLighting().up();
        if (oldShade > 0) args.set(21, ARGB.scaleRGB((int) args.get(21), BetaBlockLighting.shade(face) / oldShade));
    }
}
