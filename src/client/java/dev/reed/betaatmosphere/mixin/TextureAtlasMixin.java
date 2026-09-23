package dev.reed.betaatmosphere.mixin;

import dev.reed.betaatmosphere.BetaFluidTextures;
import net.minecraft.client.renderer.texture.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
abstract class TextureAtlasMixin {
    @Shadow private int maxMipLevel;
    @Unique private BetaFluidTextures betaAtmosphere$fluids;
    @Inject(method = "upload", at = @At("TAIL"))
    private void betaAtmosphere$initialize(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        var atlas = (TextureAtlas) (Object) this;
        if (atlas.location().equals(TextureAtlas.LOCATION_BLOCKS)) betaAtmosphere$fluids = new BetaFluidTextures(atlas, maxMipLevel);
    }
    @Inject(method = "cycleAnimationFrames", at = @At("TAIL"))
    private void betaAtmosphere$animate(CallbackInfo ci) {
        if (betaAtmosphere$fluids != null) betaAtmosphere$fluids.tick();
    }
    @Inject(method = "clearTextureData", at = @At("HEAD"))
    private void betaAtmosphere$release(CallbackInfo ci) {
        if (betaAtmosphere$fluids != null) { betaAtmosphere$fluids.close(); betaAtmosphere$fluids = null; }
    }
}
