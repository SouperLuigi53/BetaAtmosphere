package dev.reed.betaatmosphere.mixin;

import java.nio.file.Files;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Register the five small legacy textures without requiring Fabric API or an external pack. */
@Mixin(ClientPackSource.class)
abstract class ClientPackSourceMixin {
    @Redirect(method = "createVanillaPackSource", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/packs/VanillaPackResourcesBuilder;build(Lnet/minecraft/server/packs/PackLocationInfo;)Lnet/minecraft/server/packs/VanillaPackResources;"))
    private static VanillaPackResources betaAtmosphere$resources(VanillaPackResourcesBuilder builder, PackLocationInfo info) {
        builder.pushLayer().exposeNamespace("beta_atmosphere");
        var mod = FabricLoader.getInstance().getModContainer("beta_atmosphere").orElseThrow();
        for (var root : mod.getRootPaths()) {
            var assets = root.resolve("assets");
            if (Files.isDirectory(assets)) builder.pushAssetPath(PackType.CLIENT_RESOURCES, assets);
        }
        return builder.build(info);
    }
}
