package dev.reed.betaatmosphere.mixin;

import com.mojang.renderpearl.api.pipeline.ShaderSource;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderSource.CachedIncludeSource.class)
abstract class FogShaderMixin {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void betaAtmosphere$exponential(Identifier id, String source, CallbackInfoReturnable<ShaderSource.CachedIncludeSource> cir) {
        String function = "float linear_fog_value(float vertexDistance, float fogStart, float fogEnd) {";
        if ((id.getNamespace().equals("minecraft") || id.getNamespace().equals("sodium"))
                && id.getPath().endsWith("fog.glsl") && source.contains(function) && !source.contains("beta_exponential_fog")) {
            String branch = "\n    // beta_exponential_fog: Beta's GL_EXP density in the modern fog uniform.\n"
                    + "    if (fogStart == -4096.0) return 1.0 - exp(-6.90775527898 * max(vertexDistance, 0.0) / fogEnd);\n";
            cir.setReturnValue(ShaderSource.CachedIncludeSource.create(id, source.replace(function, function + branch)));
        }
    }
}
