package dev.reed.betaatmosphere;

/** Enlarged lightmap: ordinary light levels keep their original samples; row 16 holds
 * linear brightness for Beta block vertices. No terrain shader replacement is needed. */
public final class BetaLightAtlas {
    public static final int SIZE = 256;
    private final float[] values = new float[SIZE * SIZE];
    private int darkening = -1;
    private float ambient = -1;

    public void update(int darkening, float ambient) {
        if (this.darkening == darkening && this.ambient == ambient) return;
        this.darkening = darkening;
        this.ambient = ambient;
        float[] table = new float[256];
        for (int sky = 0; sky < 16; sky++) for (int block = 0; block < 16; block++)
            table[sky * 16 + block] = BetaLightMath.combined(block, sky, darkening, ambient);
        for (int y = 0; y < SIZE; y++) for (int x = 0; x < SIZE; x++) {
            float value;
            if (y == 15 || y == 16) {
                value = ambient + (1 - ambient) * Math.clamp((x + 0.5F - 8) / 240, 0, 1);
            } else {
                float bx = tableCoordinate(x), sy = tableCoordinate(y);
                int b0 = (int) bx, s0 = (int) sy, b1 = Math.min(b0 + 1, 15), s1 = Math.min(s0 + 1, 15);
                value = BetaVertexMath.bilinear(table[s0 * 16 + b0], table[s0 * 16 + b1],
                        table[s1 * 16 + b0], table[s1 * 16 + b1], bx - b0, sy - s0);
            }
            values[y * SIZE + x] = value;
        }
    }

    private static float tableCoordinate(int pixel) {
        // Duplicate each old texel's exact value around its sample center.
        int offset = Math.floorMod(pixel - 8, 16);
        float p = pixel + 0.5F;
        if (offset == 0) p = pixel;
        else if (offset == 15) p = pixel + 1;
        return Math.clamp((p - 8) / 16, 0, 15);
    }

    public float brightness(int x, int y) { return values[y * SIZE + x]; }
}
