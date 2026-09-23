package dev.reed.betaatmosphere;

import java.io.IOException;
import com.mojang.blaze3d.platform.NativeImage;

/** Immutable original Beta color maps, safe to sample from Sodium mesh workers. */
public final class BetaColorMaps {
    private static final int[] GRASS = read("grass");
    private static final int[] FOLIAGE = read("foliage");
    private BetaColorMaps() {}
    public static int grass(double t, double rain) { return GRASS[BetaAtmosphereMath.colormapIndex(t, rain)] & 0xFFFFFF; }
    public static int foliage(double t, double rain) { return FOLIAGE[BetaAtmosphereMath.colormapIndex(t, rain)] & 0xFFFFFF; }
    private static int[] read(String name) {
        try (var stream = BetaColorMaps.class.getResourceAsStream("/assets/beta_atmosphere/textures/colormap/" + name + ".png");
             var image = NativeImage.read(stream)) {
            int[] pixels = new int[65536];
            for (int y = 0; y < 256; y++) for (int x = 0; x < 256; x++) pixels[y * 256 + x] = image.getPixel(x, y);
            return pixels;
        } catch (IOException e) { throw new IllegalStateException("Cannot load Beta " + name + " colors", e); }
    }
}
