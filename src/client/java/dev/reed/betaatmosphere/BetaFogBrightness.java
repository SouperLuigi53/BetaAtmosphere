package dev.reed.betaatmosphere;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.attribute.EnvironmentAttributes;

/** Beta's ten-percent-per-tick eye adaptation, used only to darken distance fog. */
public final class BetaFogBrightness {
    private static ClientLevel lastLevel;
    private static float previous = 1.0F;
    private static float current = 1.0F;

    private BetaFogBrightness() {}

    public static boolean supports(ClientLevel level) {
        return level.dimension() == Level.OVERWORLD || level.dimension() == Level.NETHER;
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;
        if (level == null || !supports(level) || minecraft.getCameraEntity() == null) {
            lastLevel = null;
            previous = current = 1.0F;
            return;
        }
        var position = minecraft.getCameraEntity().blockPosition();
        float ambient = level.dimension() == Level.NETHER ? 0.1F : 0.05F;
        float angle = minecraft.gameRenderer.mainCamera().attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, 1.0F);
        int darkening = level.dimension() == Level.NETHER ? 15 : BetaLightMath.skyDarkening(angle,
                level.getRainLevel(1.0F), level.getThunderLevel(1.0F));
        float local = BetaLightMath.combined(level.getBrightness(LightLayer.BLOCK, position),
                level.getBrightness(LightLayer.SKY, position), darkening, ambient);
        float target = BetaFogMath.localBrightness(minecraft.options.getEffectiveRenderDistance() * 16.0F, local);
        if (lastLevel != level) {
            lastLevel = level;
            previous = current = target;
        } else {
            previous = current;
            current += (target - current) * 0.1F;
        }
    }

    public static float interpolated(float partialTick) {
        return previous + (current - previous) * Math.clamp(partialTick, 0.0F, 1.0F);
    }
}
