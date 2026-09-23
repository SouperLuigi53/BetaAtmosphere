package dev.reed.betaatmosphere;

import java.awt.Color;

/** Beta 1.7.3 celestial, weather, cloud and colormap calculations. */
public final class BetaAtmosphereMath {
    private static final float[] SINE = new float[65536];
    public static final float EXPONENTIAL_FOG = -4096;
    public static final float FOG_OPAQUE_EXPONENT = 6.9077554F;
    static { for (int i = 0; i < SINE.length; i++) SINE[i] = (float) Math.sin(i * Math.PI * 2 / 65536); }
    private BetaAtmosphereMath() {}
    public static float sin(float radians) { return SINE[(int) (radians * 10430.378F) & 65535]; }
    public static float cos(float radians) { return SINE[(int) (radians * 10430.378F + 16384) & 65535]; }

    public static float celestialAngle(long time, float partialTick) {
        float phase = (Math.floorMod(time, 24000) + partialTick) / 24000F - 0.25F;
        if (phase < 0) phase++;
        if (phase > 1) phase--;
        float eased = 1 - (float) ((Math.cos(phase * Math.PI) + 1) / 2);
        return phase + (eased - phase) / 3;
    }

    public static float daylight(float angle) { return Math.clamp(cos(angle * (float) Math.PI * 2) * 2 + 0.5F, 0, 1); }

    public static int skyBase(float temperature) {
        float t = Math.clamp(temperature / 3, -1, 1);
        return Color.HSBtoRGB(0.62222224F - t * 0.05F, 0.5F + t * 0.1F, 1);
    }

    public static float[] sky(float temperature, float angle, float rain, float thunder, float flash) {
        int base = skyBase(temperature);
        float day = daylight(angle);
        float[] rgb = {(base >>> 16 & 255) / 255F * day, (base >>> 8 & 255) / 255F * day, (base & 255) / 255F * day};
        desaturate(rgb, rain * 0.75F, 0.6F);
        desaturate(rgb, thunder * 0.75F, 0.2F);
        float lightning = Math.clamp(flash, 0, 1) * 0.45F;
        rgb[0] += (0.8F - rgb[0]) * lightning;
        rgb[1] += (0.8F - rgb[1]) * lightning;
        rgb[2] += (1 - rgb[2]) * lightning;
        return rgb;
    }

    public static float[] cloud(float angle, float rain, float thunder) {
        float[] rgb = {1, 1, 1};
        desaturate(rgb, rain * 0.95F, 0.6F);
        float day = daylight(angle);
        rgb[0] *= day * 0.9F + 0.1F;
        rgb[1] *= day * 0.9F + 0.1F;
        rgb[2] *= day * 0.85F + 0.15F;
        desaturate(rgb, thunder * 0.95F, 0.2F);
        return rgb;
    }

    private static void desaturate(float[] rgb, float strength, float shade) {
        float gray = (rgb[0] * 0.3F + rgb[1] * 0.59F + rgb[2] * 0.11F) * shade;
        for (int i = 0; i < 3; i++) rgb[i] += (gray - rgb[i]) * strength;
    }

    public static float[] sunset(float angle) {
        float horizon = cos(angle * (float) Math.PI * 2);
        if (horizon < -0.4F || horizon > 0.4F) return new float[4];
        float blend = horizon / 0.4F * 0.5F + 0.5F;
        float alpha = 1 - (1 - sin(blend * (float) Math.PI)) * 0.99F;
        return new float[]{blend * 0.3F + 0.7F, blend * blend * 0.7F + 0.2F, 0.2F, alpha * alpha};
    }

    public static float stars(float angle, float rain) {
        float brightness = Math.clamp(1 - (cos(angle * (float) Math.PI * 2) * 2 + 0.75F), 0, 1);
        return brightness * brightness * 0.5F * (1 - rain);
    }

    public static int colormapIndex(double temperature, double rainfall) {
        double t = Math.clamp(temperature, 0, 1), humidity = Math.clamp(rainfall, 0, 1) * t;
        return (int) ((1 - humidity) * 255) << 8 | (int) ((1 - t) * 255);
    }
}
