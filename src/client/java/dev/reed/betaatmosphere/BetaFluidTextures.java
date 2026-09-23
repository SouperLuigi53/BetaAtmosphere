package dev.reed.betaatmosphere;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.reed.betaatmosphere.mixin.TextureAtlasSpriteAccessor;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

/** Updates the shared block atlas, so vanilla and Sodium see the same live simulation. */
public final class BetaFluidTextures implements AutoCloseable {
    private final TextureAtlas atlas;
    private final Sprite[] sprites = new Sprite[4];

    public BetaFluidTextures(TextureAtlas atlas, int mipLevel) {
        this.atlas = atlas;
        String[] names = {"water_still", "water_flow", "lava_still", "lava_flow"};
        for (int i = 0; i < names.length; i++) {
            var sprite = atlas.getSprite(Identifier.withDefaultNamespace("block/" + names[i]));
            sprites[i] = new Sprite(sprite, BetaFluidSimulation.Kind.values()[i], mipLevel);
        }
        tick();
    }

    public void tick() {
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        for (Sprite sprite : sprites) {
            sprite.update();
            for (int mip = 0; mip < sprite.images.length; mip++)
                encoder.writeToTexture(atlas.getTexture(), sprite.images[mip], mip, 0,
                        sprite.sprite.getX() >> mip, sprite.sprite.getY() >> mip);
        }
    }

    @Override public void close() { for (Sprite sprite : sprites) for (var image : sprite.images) image.close(); }

    private static final class Sprite {
        final TextureAtlasSprite sprite;
        final BetaFluidSimulation simulation;
        final NativeImage[] images;
        final int padding, originalSize;

        Sprite(TextureAtlasSprite sprite, BetaFluidSimulation.Kind kind, int mipLevel) {
            this.sprite = sprite;
            padding = ((TextureAtlasSpriteAccessor) sprite).betaAtmosphere$padding();
            originalSize = kind == BetaFluidSimulation.Kind.WATER_FLOW || kind == BetaFluidSimulation.Kind.LAVA_FLOW ? 32 : 16;
            simulation = new BetaFluidSimulation(kind, System.nanoTime() + kind.ordinal());
            images = new NativeImage[mipLevel + 1];
            for (int mip = 0; mip <= mipLevel; mip++) images[mip] = new NativeImage(
                    Math.max(1, (sprite.contents().width() + 2 * padding) >> mip),
                    Math.max(1, (sprite.contents().height() + 2 * padding) >> mip), false);
            for (int i = 0; i < 64; i++) simulation.tick();
        }

        void update() {
            simulation.tick();
            var base = images[0];
            int width = sprite.contents().width(), height = sprite.contents().height();
            int[] pixels = simulation.pixels();
            for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
                int sx = Math.floorDiv((x - padding) * originalSize, width) & 15;
                int sy = Math.floorDiv((y - padding) * originalSize, height) & 15;
                base.setPixel(x, y, pixels[sx + sy * 16]);
            }
            for (int mip = 1; mip < images.length; mip++) {
                var from = images[mip - 1]; var to = images[mip];
                for (int y = 0; y < to.getHeight(); y++) for (int x = 0; x < to.getWidth(); x++) {
                    int a = from.getPixel(x * 2, y * 2), b = from.getPixel(x * 2 + 1, y * 2);
                    int c = from.getPixel(x * 2, y * 2 + 1), d = from.getPixel(x * 2 + 1, y * 2 + 1);
                    int color = 0;
                    for (int shift = 0; shift <= 24; shift += 8)
                        color |= (((a >>> shift & 255) + (b >>> shift & 255) + (c >>> shift & 255) + (d >>> shift & 255)) / 4) << shift;
                    to.setPixel(x, y, color);
                }
            }
        }
    }
}
