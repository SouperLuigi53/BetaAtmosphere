import dev.reed.betaatmosphere.BetaAtmosphereMath;

public class SkyReferenceCheck {
    public static void main(String[] args) throws Exception {
        Class<?> dimension = Class.forName("xa");
        Object original = Class.forName("rh").getConstructor().newInstance();
        var angle = dimension.getMethod("a", long.class, float.class);
        var sunset = dimension.getMethod("a", float.class, float.class);
        int checks = 0;
        for (long tick = 0; tick < 48000; tick++) {
            for (float partial : new float[]{0, 0.25F, 0.5F, 0.75F}) {
                float expected = (float) angle.invoke(original, tick, partial);
                float actual = BetaAtmosphereMath.celestialAngle(tick, partial);
                if (Float.floatToIntBits(expected) != Float.floatToIntBits(actual)) throw new AssertionError("celestial angle at " + tick + ": " + expected + " vs " + actual);
                float[] expectedSunset = (float[]) sunset.invoke(original, expected, partial);
                float[] actualSunset = BetaAtmosphereMath.sunset(actual);
                if (expectedSunset == null) {
                    if (actualSunset[3] != 0) throw new AssertionError("unexpected sunset at " + tick);
                } else for (int i = 0; i < 4; i++) {
                    if (Float.floatToIntBits(expectedSunset[i]) != Float.floatToIntBits(actualSunset[i])) throw new AssertionError("sunset at " + tick + ", channel " + i);
                }
                checks++;
            }
        }
        System.out.println("PASS " + checks + " celestial-angle and sunset comparisons against original Beta 1.7.3 bytecode, with exact float equality");
    }
}
