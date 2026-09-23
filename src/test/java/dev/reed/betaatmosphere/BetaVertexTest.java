package dev.reed.betaatmosphere;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BetaVertexTest {
    @Test void blendsBrightnessBeforeInterpolation() {
        float dark = BetaLightMath.brightness(0, 0.05F);
        float bright = BetaLightMath.brightness(15, 0.05F);
        assertEquals(0.525F, BetaVertexMath.corner(dark, bright, dark, bright, false, false), 1e-6);
        assertTrue(0.525F - BetaLightMath.brightness(8, 0.05F) > 0.25F);
    }

    @Test void blockedDiagonalUsesFirstEdgeOnlyWhenBothEdgesBlock() {
        assertEquals(0.4F, BetaVertexMath.corner(0.2F, 0.4F, 0.6F, 1, true, true), 1e-6);
        assertEquals(0.55F, BetaVertexMath.corner(0.2F, 0.4F, 0.6F, 1, true, false), 1e-6);
        assertEquals(0.55F, BetaVertexMath.corner(0.2F, 0.4F, 0.6F, 1, false, true), 1e-6);
    }

    @Test void partialFaceInterpolatesFourCorners() {
        assertEquals(0.1F, BetaVertexMath.bilinear(0.1F, 0.3F, 0.5F, 0.9F, 0, 0), 1e-6);
        assertEquals(0.9F, BetaVertexMath.bilinear(0.1F, 0.3F, 0.5F, 0.9F, 1, 1), 1e-6);
        assertEquals(0.45F, BetaVertexMath.bilinear(0.1F, 0.3F, 0.5F, 0.9F, 0.5F, 0.5F), 1e-6);
    }

    @Test void encodingHasBoundedErrorAndDistinctMarker() {
        for (float ambient : new float[]{0.05F, 0.1F}) for (int i = 0; i <= 10000; i++) {
            float value = ambient + (1 - ambient) * i / 10000;
            int packed = BetaVertexMath.encode(value, ambient);
            assertTrue(BetaVertexMath.encoded(packed));
            assertEquals(value, BetaVertexMath.decode(packed, ambient), 0.00199);
        }
        for (int sky = 0; sky < 16; sky++) assertFalse(BetaVertexMath.encoded(sky << 20));
    }

    @Test void atlasPreservesAllOrdinaryLightSamplesIncludingEffects() {
        var atlas = new BetaLightAtlas();
        for (float ambient : new float[]{0.05F, 0.1F}) for (int dark = 0; dark <= 15; dark++) {
            atlas.update(dark, ambient);
            for (int sky = 0; sky < 16; sky++) for (int block = 0; block < 16; block++) {
                int expected = BetaLightMath.pixel(block, sky, dark, ambient, 0.3F, 0.1F, 0.2F);
                for (int dy = 7; dy <= 8; dy++) for (int dx = 7; dx <= 8; dx++)
                    assertEquals(expected, BetaLightMath.applyEffects(atlas.brightness(block * 16 + dx, sky * 16 + dy), 0.3F, 0.1F, 0.2F));
            }
        }
    }

    @Test void atlasProducesLinearMidFaceBrightness() {
        var atlas = new BetaLightAtlas();
        atlas.update(11, 0.05F);
        for (int coordinate = 1; coordinate < 240; coordinate++) {
            float sampled = (atlas.brightness(coordinate + 7, 15) + atlas.brightness(coordinate + 8, 16)) * 0.5F;
            assertEquals(0.05F + 0.95F * coordinate / 240, sampled, 1e-6);
        }
        assertEquals(0.525F, (atlas.brightness(127, 15) + atlas.brightness(128, 16)) * 0.5F, 1e-6);
    }
}
