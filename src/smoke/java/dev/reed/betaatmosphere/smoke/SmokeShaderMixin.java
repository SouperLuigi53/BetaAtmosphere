package dev.reed.betaatmosphere.smoke;

import com.mojang.renderpearl.api.pipeline.ShaderSource;
import dev.reed.betaatmosphere.qa.AtmosphereFixtures;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderSource.CachedIncludeSource.class)
abstract class SmokeShaderMixin {
    @Inject(method = "create", at = @At("HEAD"))
    private static void smoke$include(Identifier id, String source, CallbackInfoReturnable<ShaderSource.CachedIncludeSource> cir) {
        if (source.contains("beta_exponential_fog")) AtmosphereFixtures.shaders.add(id.toString());
    }
}
