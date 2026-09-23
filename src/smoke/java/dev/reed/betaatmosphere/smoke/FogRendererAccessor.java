package dev.reed.betaatmosphere.smoke;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface FogRendererAccessor {
    @Accessor("fogRenderer") FogRenderer smoke$getFogRenderer();
}
