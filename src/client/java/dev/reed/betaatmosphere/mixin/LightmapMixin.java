package dev.reed.betaatmosphere.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.GpuFormat;
import dev.reed.betaatmosphere.BetaLightAtlas;
import dev.reed.betaatmosphere.BetaLightMath;
import dev.reed.betaatmosphere.BetaLightmapState;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lightmap.class)
abstract class LightmapMixin {
    @Unique private NativeImage betaAtmosphere$pixels;
    @Unique private GpuTexture betaAtmosphere$texture;
    @Unique private GpuTextureView betaAtmosphere$view;
    @Unique private boolean betaAtmosphere$active;
    @Unique private final BetaLightAtlas betaAtmosphere$atlas = new BetaLightAtlas();

    @Inject(method = "getTextureView", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$texture(CallbackInfoReturnable<GpuTextureView> cir) {
        if (betaAtmosphere$active && betaAtmosphere$view != null) cir.setReturnValue(betaAtmosphere$view);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void betaAtmosphere$render(LightmapRenderState state, CallbackInfo ci) {
        var parameters = ((BetaLightmapState) state).betaAtmosphere$getParameters();
        boolean wasActive = betaAtmosphere$active;
        betaAtmosphere$active = parameters != null;
        if (parameters == null) {
            if (wasActive) state.needsUpdate = true;
            return;
        }
        if (state.needsUpdate || !wasActive || betaAtmosphere$texture == null) {
            if (betaAtmosphere$pixels == null) {
                betaAtmosphere$pixels = new NativeImage(256, 256, false);
                var device = RenderSystem.getDevice();
                betaAtmosphere$texture = device.createTexture("Beta light atlas", 13, GpuFormat.RGBA8_UNORM, 256, 256, 1, 1);
                betaAtmosphere$view = device.createTextureView(betaAtmosphere$texture);
            }
            betaAtmosphere$atlas.update(parameters.skyDarkening(), parameters.ambient());
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 256; x++) {
                    betaAtmosphere$pixels.setPixel(x, y, BetaLightMath.applyEffects(betaAtmosphere$atlas.brightness(x, y),
                            state.nightVisionEffectIntensity, state.darknessEffectScale, state.bossOverlayWorldDarkening));
                }
            }
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(betaAtmosphere$texture, betaAtmosphere$pixels);
        }
        ci.cancel();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void betaAtmosphere$close(CallbackInfo ci) {
        if (betaAtmosphere$pixels != null) {
            betaAtmosphere$view.close();
            betaAtmosphere$texture.close();
            betaAtmosphere$pixels.close();
            betaAtmosphere$pixels = null;
        }
    }
}
