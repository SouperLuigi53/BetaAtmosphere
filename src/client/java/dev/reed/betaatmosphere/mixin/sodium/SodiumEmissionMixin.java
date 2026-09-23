package dev.reed.betaatmosphere.mixin.sodium;

import dev.reed.betaatmosphere.*;
import net.caffeinemc.mods.sodium.client.render.helper.ColorHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = ColorHelper.class, remap = false)
abstract class SodiumEmissionMixin {
    @Inject(method = "maxBrightness", at = @At("HEAD"), cancellable = true)
    private static void betaAtmosphere$emission(int a, int b, CallbackInfoReturnable<Integer> cir) {
        var p = BetaVisualLighting.current();
        if (p == null || (!BetaVertexMath.encoded(a) && !BetaVertexMath.encoded(b))) return;
        cir.setReturnValue(BetaVertexMath.encode(Math.max(betaAtmosphere$value(a, p), betaAtmosphere$value(b, p)), p.ambient()));
    }
    @Unique private static float betaAtmosphere$value(int light, BetaLightmapState.Parameters p) {
        return BetaVertexMath.encoded(light) ? BetaVertexMath.decode(light, p.ambient())
                : BetaLightMath.combined((light & 255) / 16, (light >>> 16 & 255) / 16, p.skyDarkening(), p.ambient());
    }
}
