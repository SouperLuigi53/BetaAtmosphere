package dev.reed.betaatmosphere;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BetaMathTest {
    @Test void betaSunAndWeatherDarkenOnlySkylight() {
        assertEquals(0, BetaLightMath.skyDarkening(0, 0, 0));
        assertEquals(11, BetaLightMath.skyDarkening(180, 0, 0));
        assertEquals(5, BetaLightMath.skyDarkening(90, 0, 0));
        assertEquals(3, BetaLightMath.skyDarkening(0, 1, 0));
        assertEquals(5, BetaLightMath.skyDarkening(0, 1, 1));
    }
    @Test void betaOverworldReferenceValues() {
        assertEquals(0.05F, BetaLightMath.brightness(0, 0.05F), 0.000001F);
        assertEquals(0.12916667F, BetaLightMath.brightness(4, 0.05F), 0.000001F);
        assertEquals(0.78888889F, BetaLightMath.brightness(14, 0.05F), 0.000001F);
        assertEquals(1.0F, BetaLightMath.brightness(15, 0.05F), 0.000001F);
    }

    @Test void netherHasBetaAmbientFloor() {
        assertEquals(0.1F, BetaLightMath.combined(0, 0, 11, 0.1F));
        assertEquals(1.0F, BetaLightMath.brightness(15, 0.1F), 0.000001F);
    }

    @Test void overlappingTorchAndSkyDoNotAddBrightness() {
        float torch = BetaLightMath.brightness(14, 0.05F);
        assertEquals(torch, BetaLightMath.combined(14, 14, 0, 0.05F));
        assertEquals(torch, BetaLightMath.combined(14, 15, 11, 0.05F));
    }

    @Test void midnightSkylightIsLevelFourAndCavesStayDark() {
        assertEquals(0.12916667F, BetaLightMath.combined(0, 15, 11, 0.05F), 0.000001F);
        assertEquals(0.05F, BetaLightMath.combined(0, 0, 11, 0.05F));
    }

    @Test void ordinaryLightmapIsGrayscaleMonotonicAndOpaque() {
        for (int skyDarkening = 0; skyDarkening <= 11; skyDarkening++) {
            for (int sky = 0; sky < 16; sky++) {
                int previous = 0;
                for (int block = 0; block < 16; block++) {
                    int pixel = BetaLightMath.pixel(block, sky, skyDarkening, 0.05F, 0, 0, 0);
                    int red = pixel >>> 16 & 255;
                    assertEquals(255, pixel >>> 24);
                    assertEquals(red, pixel >>> 8 & 255);
                    assertEquals(red, pixel & 255);
                    assertTrue(red >= previous);
                    previous = red;
                }
            }
        }
    }

    @Test void nightVisionAndDarknessStillWork() {
        assertEquals(0xFFFFFFFF, BetaLightMath.pixel(0, 0, 11, 0.05F, 1, 0, 0));
        assertEquals(0xFF000000, BetaLightMath.pixel(0, 0, 11, 0.05F, 0, 0.45F, 0));
    }

    @Test void fogRespectsShortDistancesAndBetaFarLimit() {
        assertEquals(32, BetaFogMath.distance(32));
        assertEquals(128, BetaFogMath.distance(128));
        assertEquals(256, BetaFogMath.distance(1024));
        assertEquals(0, BetaFogMath.skyMix(32));
        assertEquals(1.0 - Math.pow(0.25, 0.25), BetaFogMath.skyMix(256), 0.000001);
    }

    @Test void nearFogAdaptsToCavesWhileFarFogDoesNot() {
        assertEquals(0.05F, BetaFogMath.localBrightness(32, 0.05F));
        assertEquals(1.0F, BetaFogMath.localBrightness(256, 0.05F));
    }
}
