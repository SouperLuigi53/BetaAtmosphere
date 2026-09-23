package dev.reed.betaatmosphere;

import java.util.Random;

/** Original 16x16 procedural fluid rules, with independent state for each of the four sprites. */
public final class BetaFluidSimulation {
    public enum Kind { WATER_STILL, WATER_FLOW, LAVA_STILL, LAVA_FLOW }
    private final Kind kind;
    private final Random random;
    private float[] height = new float[256], next = new float[256];
    private final float[] heat = new float[256], impulse = new float[256];
    private final int[] pixels = new int[256];
    private int tick;

    public BetaFluidSimulation(Kind kind, long seed) { this.kind = kind; random = new Random(seed); }
    public int[] pixels() { return pixels; }
    public int ticks() { return tick; }

    public void tick() {
        tick++;
        boolean lava = kind == Kind.LAVA_STILL || kind == Kind.LAVA_FLOW;
        boolean flow = kind == Kind.WATER_FLOW || kind == Kind.LAVA_FLOW;
        if (lava) updateLava(); else updateWater(flow);
        float[] swap = height; height = next; next = swap;
        int scroll = flow ? (lava ? tick / 3 : tick) * 16 : 0;
        for (int i = 0; i < 256; i++) {
            float v = Math.clamp(height[(i - scroll) & 255] * (lava ? 2 : 1), 0, 1);
            float square = v * v;
            int red, green, blue, alpha;
            if (lava) {
                red = (int) (v * 100 + 155); green = (int) (square * 255);
                blue = (int) (v * v * v * v * 128); alpha = 255;
            } else {
                red = (int) (32 + square * 32); green = (int) (50 + square * 64);
                blue = 255; alpha = (int) (146 + square * 50);
            }
            pixels[i] = alpha << 24 | red << 16 | green << 8 | blue;
        }
    }

    private void updateWater(boolean flow) {
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) {
            float sum = 0;
            for (int step = 0; step < 3; step++) sum += flow
                    ? height[x + ((y - 2 + step) & 15) * 16] : height[((x - 1 + step) & 15) + y * 16];
            int i = x + y * 16;
            next[i] = sum / (flow ? 3.2F : 3.3F) + heat[i] * 0.8F;
        }
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) {
            int i = x + y * 16;
            heat[i] = Math.max(0, heat[i] + impulse[i] * 0.05F);
            impulse[i] -= flow ? 0.3F : 0.1F;
            if (random.nextDouble() < (flow ? 0.2 : 0.05)) impulse[i] = 0.5F;
        }
    }

    private void updateLava() {
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) {
            int shiftX = (int) (BetaAtmosphereMath.sin(y * (float) Math.PI * 2 / 16) * 1.2F);
            int shiftY = (int) (BetaAtmosphereMath.sin(x * (float) Math.PI * 2 / 16) * 1.2F);
            float sum = 0;
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++)
                sum += height[((x + dx + shiftX) & 15) + ((y + dy + shiftY) & 15) * 16];
            int i = x + y * 16;
            float localHeat = heat[i] + heat[((x + 1) & 15) + y * 16]
                    + heat[((x + 1) & 15) + ((y + 1) & 15) * 16] + heat[x + ((y + 1) & 15) * 16];
            next[i] = sum / 10 + localHeat / 4 * 0.8F;
            heat[i] = Math.max(0, heat[i] + impulse[i] * 0.01F);
            impulse[i] -= 0.06F;
            if (random.nextDouble() < 0.005) impulse[i] = 1.5F;
        }
    }
}
