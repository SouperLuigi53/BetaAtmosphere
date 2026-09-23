package dev.reed.betaatmosphere;

/** The pre-Beta-1.8 brightness table, evaluated independently of world light propagation. */
public final class BetaLightMath {
    private BetaLightMath() {}

    public static int skyDarkening(float sunAngleDegrees, float rain, float thunder) {
        float daylight = Math.clamp((float) Math.cos(Math.toRadians(sunAngleDegrees)) * 2.0F + 0.5F, 0.0F, 1.0F);
        daylight *= 1.0F - rain * 5.0F / 16.0F;
        daylight *= 1.0F - thunder * 5.0F / 16.0F;
        return (int) ((1.0F - daylight) * 11.0F);
    }

    public static float brightness(int lightLevel, float ambient) {
        float light = Math.clamp(lightLevel, 0, 15) / 15.0F;
        return ambient + (1.0F - ambient) * light / (4.0F - 3.0F * light);
    }

    public static float combined(int blockLight, int skyLight, int skyDarkening, float ambient) {
        return brightness(Math.max(blockLight, skyLight - skyDarkening), ambient);
    }

    public static int pixel(int blockLight, int skyLight, int skyDarkening, float ambient,
                            float nightVision, float darkness, float bossDarkening) {
        float value = combined(blockLight, skyLight, skyDarkening, ambient);
        return applyEffects(value, nightVision, darkness, bossDarkening);
    }

    public static int applyEffects(float value, float nightVision, float darkness, float bossDarkening) {
        value += (1.0F - value) * Math.clamp(nightVision, 0.0F, 1.0F);
        float boss = Math.clamp(bossDarkening, 0.0F, 1.0F);
        int red = channel(value * (1.0F - 0.3F * boss) - darkness);
        int greenBlue = channel(value * (1.0F - 0.4F * boss) - darkness);
        return 0xFF000000 | red << 16 | greenBlue << 8 | greenBlue;
    }

    private static int channel(float value) {
        return Math.round(Math.clamp(value, 0.0F, 1.0F) * 255.0F);
    }
}
