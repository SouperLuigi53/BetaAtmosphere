package dev.reed.betaatmosphere.qa;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.reed.betaatmosphere.*;
import dev.reed.betaatmosphere.mixin.*;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biomes;
import org.joml.Vector3f;

public final class DropFixtures {
    public static void run(Minecraft mc, List<String> results) {
        var biomes = mc.level.registryAccess().lookupOrThrow(Registries.BIOME);
        var dappled = biomes.getValueOrThrow(Biomes.DAPPLED_FOREST);
        check((dappled.getGrassColor(0, 0) & 0xFFFFFF) == 14641191, "Dappled grass orange");
        check((dappled.getFoliageColor() & 0xFFFFFF) == 15109680, "Dappled foliage orange");
        check(dappled.getGrassColor(0, 0) == dappled.getSpecialEffects().grassColorOverride().orElseThrow(), "preserve registered grass override");
        check(dappled.getFoliageColor() == dappled.getSpecialEffects().foliageColorOverride().orElseThrow(), "preserve registered leaf override");
        var forest = biomes.getValueOrThrow(Biomes.FOREST);
        check(!BetaBiomeColors.preserves(forest), "ordinary forest still uses Beta palette");
        var badlands = biomes.getValueOrThrow(Biomes.BADLANDS);
        check(badlands.getGrassColor(0, 0) != badlands.getSpecialEffects().grassColorOverride().orElseThrow(), "non-Dappled override behavior unchanged");
        results.add("PASS Dappled grass/foliage registered orange overrides; ordinary forest and badlands retain previous Beta behavior");

        var diamondEntity = new ItemEntity(mc.level, 0, 102, 0, new ItemStack(Items.DIAMOND));
        diamondEntity.setId(1000099);
        var diamondRenderer = (ItemEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(diamondEntity);
        int[] counts = {1, 2, 5, 6, 20, 21, 64};
        int[] copies = {1, 2, 2, 3, 3, 4, 4};
        for (int i = 0; i < counts.length; i++) {
            var entity = new ItemEntity(mc.level, 0, 102, 0, new ItemStack(Items.DIAMOND, counts[i]));
            entity.setId(1000100 + i);
            var state = diamondRenderer.createRenderState();
            diamondRenderer.extractRenderState(entity, state, 0);
            check(((BetaDroppedItemState) state).betaAtmosphere$isFlat(), "count fixture uses flat sprite: " + counts[i]);
            check(state.count == copies[i], "Beta stack copies for count " + counts[i] + ": " + state.count);
        }
        results.add("PASS Beta stack-copy thresholds for item-sprite counts 1, 2, 5, 6, 20, 21 and 64");

        int angles = 0;
        for (Item type : new Item[]{Items.DIAMOND, Items.IRON_SWORD, Items.APPLE, Items.POTION, Items.LEATHER_CHESTPLATE, Items.COMPASS}) {
            var stack = new ItemStack(type, type == Items.DIAMOND ? 32 : 1);
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            var entity = new ItemEntity(mc.level, 0, 102, 0, stack);
            entity.setId(1000000);
            var renderer = (ItemEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(entity);
            var original = new ItemStackRenderState();
            mc.getItemModelResolver().updateForNonLiving(original, stack, ItemDisplayContext.GROUND, entity);
            var originalAccess = (ItemStackRenderStateAccessor) original;
            var firstOriginal = ((ItemLayerAccessor) originalAccess.betaAtmosphere$layers()[0]).betaAtmosphere$quads();
            int originalSize = firstOriginal.all().size();
            var originalTints = new HashMap<net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo, Integer>();
            for (int layerIndex = 0; layerIndex < originalAccess.betaAtmosphere$layerCount(); layerIndex++) {
                var layer = originalAccess.betaAtmosphere$layers()[layerIndex];
                var colors = layer.tintLayers();
                for (var q : ((ItemLayerAccessor) layer).betaAtmosphere$quads().all()) {
                    int index = q.materialInfo().tintIndex();
                    originalTints.put(q.materialInfo(), index >= 0 && index < colors.size() ? colors.getInt(index) : -1);
                }
            }
            var state = renderer.createRenderState();
            renderer.extractRenderState(entity, state, 0);
            check(((BetaDroppedItemState) state).betaAtmosphere$isFlat(), "sprite classification: " + type);
            check(state.item.getModelBoundingBox().getZsize() == 0, "truly flat geometry: " + type);
            check(firstOriginal.all().size() == originalSize && original.getModelBoundingBox().getZsize() > 0, "shared/inventory model unchanged: " + type);

            for (float yaw : new float[]{0, 90, 180, 270}) for (float pitch : new float[]{-75, 0, 75}) for (float age : new float[]{0, 83}) {
                var camera = new CameraRenderState();
                camera.yRot = yaw; camera.xRot = pitch;
                camera.orientation.rotationYXZ((float) Math.PI - yaw * (float) Math.PI / 180, -pitch * (float) Math.PI / 180, 0);
                var expected = new Vector3f(0, 0, 1).rotate(camera.orientation);
                int[] submissions = {0};
                SubmitNodeCollector collector = (SubmitNodeCollector) Proxy.newProxyInstance(DropFixtures.class.getClassLoader(),
                        new Class[]{SubmitNodeCollector.class}, (proxy, method, args) -> {
                            if (method.getName().equals("order")) return proxy;
                            if (method.getName().equals("submitItem")) {
                                submissions[0]++;
                                var pose = ((PoseStack) args[0]).last().pose();
                                var facing = new Vector3f(0, 0, 1).mulDirection(pose).normalize();
                                check(facing.dot(expected) > 0.99999F, "camera facing at " + yaw + "/" + pitch + "/" + age);
                                var quads = (ItemQuads) args[6];
                                for (var q : quads.all()) {
                                    check(q.direction() == Direction.SOUTH, "no extruded side/back faces");
                                    for (int v = 0; v < 4; v++) check(q.position(v).z() == 0.5F, "flat vertex plane");
                                    int tintIndex = q.materialInfo().tintIndex();
                                    int[] colors = (int[]) args[5];
                                    int actualTint = tintIndex >= 0 && tintIndex < colors.length ? colors[tintIndex] : -1;
                                    check(actualTint == originalTints.get(q.materialInfo()), "original tint or white fallback preserved: " + type);
                                }
                                check(args[7] != ItemStackRenderState.FoilType.NONE, "glint preserved");
                            }
                            return null;
                        });
                state.ageInTicks = age;
                renderer.submit(state, new PoseStack(), collector, camera);
                check(submissions[0] > 0, "rendered sprite " + type);
                angles++;
            }
            entity.setItem(new ItemStack(Items.STONE));
            renderer.extractRenderState(entity, state, 0);
            check(!((BetaDroppedItemState) state).betaAtmosphere$isFlat(), "pooled render state resets for block");
        }
        for (Item type : new Item[]{Items.STONE, Items.OAK_STAIRS, Items.GLASS, Items.SHIELD}) {
            var entity = new ItemEntity(mc.level, 0, 102, 0, new ItemStack(type));
            entity.setId(1000001);
            var renderer = (ItemEntityRenderer) mc.getEntityRenderDispatcher().getRenderer(entity);
            var state = renderer.createRenderState();
            renderer.extractRenderState(entity, state, 0);
            check(!((BetaDroppedItemState) state).betaAtmosphere$isFlat(), "3D block/special model retained: " + type);
        }
        results.add("PASS " + angles + " actual item-renderer submissions across yaw, pitch and age; six sprite types, tint/glint, stack copies, unchanged shared models, pooled state reset and 3D blocks/special models");
    }

    static void check(boolean condition, String message) { if (!condition) throw new AssertionError("BETA_DROP_SMOKE_FAIL: " + message); }
}
