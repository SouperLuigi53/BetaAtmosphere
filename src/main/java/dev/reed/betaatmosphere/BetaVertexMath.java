package dev.reed.betaatmosphere;

/** Scalar brightness, not an average of sky/block light levels. */
public final class BetaVertexMath {
    public static final int LINEAR_ROW = 8 << 16;

    private BetaVertexMath() {}

    public static float corner(float center, float edgeA, float edgeB, float diagonal,
                               boolean blockedA, boolean blockedB) {
        return (center + edgeA + edgeB + (blockedA && blockedB ? edgeA : diagonal)) * 0.25F;
    }

    public static float bilinear(float nn, float pn, float np, float pp, float a, float b) {
        a = Math.clamp(a, 0, 1);
        b = Math.clamp(b, 0, 1);
        return (nn + (pn - nn) * a) * (1 - b) + (np + (pp - np) * a) * b;
    }

    // The reserved row lies between ordinary integer sky-light coordinates. A linear ramp
    // preserves Beta's interpolation across triangles, including modern effect overlays.
    public static int encode(float brightness, float ambient) {
        return LINEAR_ROW | Math.round(Math.clamp((brightness - ambient) / (1 - ambient), 0, 1) * 240);
    }

    public static boolean encoded(int light) { return (light >>> 16) == 8; }

    public static float decode(int light, float ambient) {
        return ambient + (1 - ambient) * (light & 255) / 240.0F;
    }
}
