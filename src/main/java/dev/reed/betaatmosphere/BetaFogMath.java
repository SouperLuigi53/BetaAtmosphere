package dev.reed.betaatmosphere;

public final class BetaFogMath {
    private BetaFogMath() {}

    /** Beta's Far setting ended at 256 blocks. Respect shorter modern render distances. */
    public static float distance(float renderDistanceBlocks) {
        return Math.clamp(renderDistanceBlocks, 16.0F, 256.0F);
    }

    /** Interpolate between Beta's 32/64/128/256-block distance presets. */
    public static float distanceSetting(float renderDistanceBlocks) {
        return Math.clamp((float) (Math.log(distance(renderDistanceBlocks) / 32.0) / Math.log(2.0)), 0.0F, 3.0F);
    }

    public static float skyMix(float renderDistanceBlocks) {
        return 1.0F - (float) Math.pow(1.0F / (distanceSetting(renderDistanceBlocks) + 1.0F), 0.25);
    }

    public static float localBrightness(float renderDistanceBlocks, float localLight) {
        float farWeight = distanceSetting(renderDistanceBlocks) / 3.0F;
        return localLight * (1.0F - farWeight) + farWeight;
    }
}
