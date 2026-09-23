package dev.reed.betaatmosphere.mixin;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
abstract class CloudRendererMixin {
    @Shadow private CloudRenderer.TextureData texture;
    @Unique private boolean betaAtmosphere$fast;
    @Unique private long betaAtmosphere$ticks;
    @Unique private float betaAtmosphere$partial;
    @Unique private static final String PREPARE = "prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V";

    @Inject(method = PREPARE, at = @At("HEAD"))
    private void betaAtmosphere$mode(int color, CloudStatus status, float bottom, int range, Vec3 camera,
                                      long ticks, float partial, CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        betaAtmosphere$fast = level != null && level.dimension() == Level.OVERWORLD && status == CloudStatus.FAST;
        betaAtmosphere$ticks = ticks;
        betaAtmosphere$partial = partial;
    }
    @ModifyConstant(method = PREPARE, constant = @Constant(floatValue = 12F))
    private float betaAtmosphere$flatScale(float value) { return betaAtmosphere$fast ? 8F : value; }
    @ModifyConstant(method = PREPARE, constant = @Constant(doubleValue = 12D))
    private double betaAtmosphere$flatScaleDouble(double value) { return betaAtmosphere$fast ? 8D : value; }
    @ModifyConstant(method = PREPARE, constant = @Constant(doubleValue = 3.9600000381469727D))
    private double betaAtmosphere$flatOffset(double value) { return betaAtmosphere$fast ? 0D : value; }
    // Float slots: bottomY, partialTicks, relativeBottomY, relativeTopY, cloudOffset.
    @ModifyVariable(method = PREPARE, at = @At("STORE"), ordinal = 4)
    private float betaAtmosphere$flatDrift(float value) {
        if (!betaAtmosphere$fast || texture == null) return value;
        double period = texture.width() * 8D / 0.03D;
        return (float) ((betaAtmosphere$ticks % period) + betaAtmosphere$partial);
    }
}
