package dev.reed.betaatmosphere;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** Keep the requested modern biome's palette, including server/data-pack overrides. */
public final class BetaBiomeColors {
    private record Palette(RegistryAccess registry, Biome dappledForest) {}
    private static volatile Palette palette;

    private BetaBiomeColors() {}

    public static boolean preserves(Biome biome) {
        var level = Minecraft.getInstance().level;
        if (level == null) return false;
        var registry = level.registryAccess();
        var current = palette;
        if (current == null || current.registry != registry) {
            current = new Palette(registry, registry.lookupOrThrow(Registries.BIOME).getValue(Biomes.DAPPLED_FOREST));
            palette = current;
        }
        return biome == current.dappledForest;
    }
}
