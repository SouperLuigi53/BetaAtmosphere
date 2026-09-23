package dev.reed.betaatmosphere;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BetaAtmosphereTest {
    @Test void celestialClockKeepsBetaNoonMidnightAndDailyWrap() {
        assertEquals(0, BetaAtmosphereMath.celestialAngle(6000, 0));
        assertEquals(0.5F, BetaAtmosphereMath.celestialAngle(18000, 0));
        for (int time = 0; time < 24000; time += 37)
            assertEquals(BetaAtmosphereMath.celestialAngle(time, 0.25F), BetaAtmosphereMath.celestialAngle(time + 24000, 0.25F));
    }
    @Test void sunsetOnlyExistsNearHorizon() {
        assertEquals(0, BetaAtmosphereMath.sunset(0)[3]);
        assertEquals(0, BetaAtmosphereMath.sunset(0.5F)[3]);
        assertArrayEquals(new float[]{0.85F, 0.375F, 0.2F, 1F}, BetaAtmosphereMath.sunset(0.25F), 0.0001F);
    }
    @Test void cloudsKeepBetaNightPaletteAndWeatherDimming() {
        assertArrayEquals(new float[]{1, 1, 1}, BetaAtmosphereMath.cloud(0, 0, 0), 0.00001F);
        assertArrayEquals(new float[]{0.1F, 0.1F, 0.15F}, BetaAtmosphereMath.cloud(0.5F, 0, 0), 0.00001F);
        assertTrue(BetaAtmosphereMath.cloud(0, 1, 1)[0] < BetaAtmosphereMath.cloud(0, 1, 0)[0]);
        assertEquals(0.5F, BetaAtmosphereMath.stars(0.5F, 0));
        assertEquals(0, BetaAtmosphereMath.stars(0.5F, 1));
    }
    @Test void climateLookupMultipliesRainfallByTemperature() {
        assertEquals(0, BetaAtmosphereMath.colormapIndex(1, 1));
        assertEquals(65535, BetaAtmosphereMath.colormapIndex(0, 1));
        assertEquals(0xBF7F, BetaAtmosphereMath.colormapIndex(0.5, 0.5));
        assertEquals(0, BetaAtmosphereMath.colormapIndex(2, 2));
        assertNotEquals(BetaAtmosphereMath.skyBase(0), BetaAtmosphereMath.skyBase(1));
    }
    @Test void fluidsStartAtOriginalPaletteMinimumAndRemainInRange() {
        for (var kind : BetaFluidSimulation.Kind.values()) {
            var simulation = new BetaFluidSimulation(kind, 42);
            boolean water = kind.ordinal() < 2;
            simulation.tick();
            for (int color : simulation.pixels()) assertEquals(water ? 0x922032FF : 0xFF9B0000, color);
            for (int t = 0; t < 500; t++) {
                simulation.tick();
                for (int color : simulation.pixels()) {
                    int r = color >>> 16 & 255, a = color >>> 24, b = color & 255;
                    if (water) { assertTrue(r >= 32 && r <= 64); assertEquals(255, b); assertTrue(a >= 146 && a <= 196); }
                    else { assertTrue(r >= 155); assertEquals(255, a); assertTrue(b <= 128); }
                }
            }
            assertTrue(Arrays.stream(simulation.pixels()).distinct().count() > 1, kind + " should animate");
        }
    }
    @Test void flowingLavaUsesSameSimulationWithOneRowScrollEveryThreeTicks() {
        var still = new BetaFluidSimulation(BetaFluidSimulation.Kind.LAVA_STILL, 10842);
        var flow = new BetaFluidSimulation(BetaFluidSimulation.Kind.LAVA_FLOW, 10842);
        for (int tick = 1; tick <= 200; tick++) {
            still.tick(); flow.tick();
            for (int i = 0; i < 256; i++) assertEquals(still.pixels()[(i - tick / 3 * 16) & 255], flow.pixels()[i]);
        }
    }
}
