import dev.reed.betaatmosphere.BetaFluidSimulation;
import java.util.Random;

public class FluidReferenceCheck {
    public static void main(String[] args) throws Exception {
        var field = Class.forName("java.lang.Math$RandomNumberGeneratorHolder").getDeclaredField("randomNumberGenerator");
        field.setAccessible(true);
        var random = (Random) field.get(null);
        String[] classes = {"vs", "oh", "cg", "if"};
        int comparisons = 0;
        for (int kind = 0; kind < 4; kind++) {
            Class<?> originalClass = Class.forName(classes[kind]);
            for (long seed : new long[]{0, 42, 10842}) {
                Object original = originalClass.getConstructor().newInstance();
                var update = originalClass.getMethod("a");
                random.setSeed(seed);
                var port = new BetaFluidSimulation(BetaFluidSimulation.Kind.values()[kind], seed);
                for (int tick = 1; tick <= 1000; tick++) {
                    update.invoke(original);
                    port.tick();
                    byte[] bytes = ((aw) original).a;
                    for (int i = 0; i < 256; i++) {
                        int expected = (bytes[i * 4 + 3] & 255) << 24 | (bytes[i * 4] & 255) << 16
                                | (bytes[i * 4 + 1] & 255) << 8 | (bytes[i * 4 + 2] & 255);
                        if (expected != port.pixels()[i]) throw new AssertionError("kind=" + kind + " seed=" + seed + " tick=" + tick + " pixel=" + i
                                + " expected=" + Integer.toHexString(expected) + " got=" + Integer.toHexString(port.pixels()[i]));
                        comparisons++;
                    }
                }
            }
            System.out.println("PASS original Beta 1.7.3 bytecode parity: " + BetaFluidSimulation.Kind.values()[kind] + ", three seeds, 1000 ticks each");
        }
        System.out.println("PASS " + comparisons + " complete RGBA pixel comparisons against the original Beta client classes");
    }
}
