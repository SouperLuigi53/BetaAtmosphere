package dev.reed.betaatmosphere.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.buffers.*;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(SkyRenderer.class)
abstract class SkyRendererMixin {
    @Shadow @Final private GpuBuffer bottomSkyBuffer;
    @Shadow private int starIndexCount;
    @Unique private boolean betaAtmosphere$active;
    @Unique private final Vector3f betaAtmosphere$sky = new Vector3f();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void betaAtmosphere$lowerSky(ClientLevel level, float partial, Camera camera, SkyRenderState state, CallbackInfo ci) {
        if (level.dimension() == Level.OVERWORLD) state.shouldRenderDarkDisc = true;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void betaAtmosphere$color(GpuBufferSlice fog, SkyRenderState state, CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        betaAtmosphere$active = level != null && level.dimension() == Level.OVERWORLD;
        if (betaAtmosphere$active) betaAtmosphere$sky.set(state.skyColor);
    }

    @ModifyArg(method = "renderSunMoonAndStars", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V"), index = 1)
    private float betaAtmosphere$orbit(float angle) { return betaAtmosphere$active ? 0 : angle; }

    @ModifyArg(method = "renderSunriseAndSunset", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;rotateDegrees(Lcom/mojang/math/Axis;F)V", ordinal = 1), index = 1)
    private float betaAtmosphere$sunsetDirection(float angle) { return betaAtmosphere$active ? angle - 90 : angle; }

    @Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$bottomColor(RenderPass pass, CallbackInfo ci) {
        if (!betaAtmosphere$active) return;
        var c = betaAtmosphere$sky;
        var transform = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy(),
                new Vector4f(c.x * 0.2F + 0.04F, c.y * 0.2F + 0.04F, c.z * 0.6F + 0.1F, 1));
        pass.pushDebugGroup(() -> "Beta lower sky");
        pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.SKY));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", transform);
        pass.setVertexBuffer(0, bottomSkyBuffer.slice());
        pass.draw(10, 1, 0, 0);
        pass.popDebugGroup();
        ci.cancel();
    }

    @Inject(method = "buildStars", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$stars(CallbackInfoReturnable<GpuBuffer> cir) {
        Random random = new Random(10842L);
        try (var bytes = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 1500 * 4)) {
            var builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
            for (int i = 0; i < 1500; i++) {
                double x = random.nextFloat() * 2F - 1F, y = random.nextFloat() * 2F - 1F, z = random.nextFloat() * 2F - 1F;
                double size = 0.25F + random.nextFloat() * 0.25F;
                double length = x * x + y * y + z * z;
                if (length >= 1 || length <= 0.01) continue;
                double inverse = 1 / Math.sqrt(length);
                x *= inverse; y *= inverse; z *= inverse;
                double azimuth = Math.atan2(x, z), polar = Math.atan2(Math.sqrt(x * x + z * z), y);
                double rotation = random.nextDouble() * Math.PI * 2;
                for (int corner = 0; corner < 4; corner++) {
                    double u = ((corner & 2) - 1) * size, v = (((corner + 1) & 2) - 1) * size;
                    double a = u * Math.cos(rotation) - v * Math.sin(rotation);
                    double b = v * Math.cos(rotation) + u * Math.sin(rotation);
                    double vertical = a * Math.sin(polar), radial = -a * Math.cos(polar);
                    builder.addVertex((float) (x * 100 + radial * Math.sin(azimuth) - b * Math.cos(azimuth)),
                            (float) (y * 100 + vertical), (float) (z * 100 + b * Math.sin(azimuth) + radial * Math.cos(azimuth)));
                }
            }
            try (var mesh = builder.buildOrThrow()) {
                starIndexCount = mesh.drawState().indexCount();
                cir.setReturnValue(RenderSystem.getDevice().createBuffer(() -> "Beta stars", 40, mesh.vertexBuffer()));
            }
        }
    }
}
