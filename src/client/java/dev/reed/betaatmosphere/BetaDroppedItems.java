package dev.reed.betaatmosphere;

import dev.reed.betaatmosphere.mixin.ItemLayerAccessor;
import dev.reed.betaatmosphere.mixin.ItemStackRenderStateAccessor;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/** Changes only item-entity render states; the shared baked models stay untouched. */
public final class BetaDroppedItems {
    private static final ItemTransform SPRITE_TRANSFORM = new ItemTransform(
            new Vector3f(), new Vector3f(0, 0.125F, 0), new Vector3f(0.5F));
    private static final Matrix4f IDENTITY = new Matrix4f();
    private record FlatSprite(ItemQuads quads, Vector3fc[] extents) {}
    private static final Map<ItemQuads, FlatSprite> FLAT_QUADS = new WeakHashMap<>();

    private BetaDroppedItems() {}

    public static int renderedCopies(int stackCount) {
        if (stackCount > 20) return 4;
        if (stackCount > 5) return 3;
        return stackCount > 1 ? 2 : 1;
    }

    public static boolean flatten(ItemStackRenderState item) {
        var state = (ItemStackRenderStateAccessor) item;
        int count = state.betaAtmosphere$layerCount();
        if (count == 0) return false;
        var layers = state.betaAtmosphere$layers();
        // Only generated sprite models: blocks and special models keep their native geometry.
        for (int i = 0; i < count; i++) {
            var layer = (ItemLayerAccessor) layers[i];
            if (layer.betaAtmosphere$usesBlockLight() || layer.betaAtmosphere$specialRenderer() != null
                    || layer.betaAtmosphere$quads().all().stream().noneMatch(q -> q.direction() == Direction.SOUTH)) return false;
        }
        for (int i = 0; i < count; i++) {
            var layer = layers[i];
            var quads = ((ItemLayerAccessor) layer).betaAtmosphere$quads();
            var sprite = FLAT_QUADS.computeIfAbsent(quads, BetaDroppedItems::frontFaces);
            layer.setQuads(sprite.quads);
            layer.setExtents(() -> sprite.extents);
            layer.setItemTransform(SPRITE_TRANSFORM);
            layer.setLocalTransform(IDENTITY);
        }
        return true;
    }

    private static FlatSprite frontFaces(ItemQuads original) {
        var faces = new ArrayList<BakedQuad>();
        for (var q : original.all()) if (q.direction() == Direction.SOUTH) {
            faces.add(new BakedQuad(flat(q.position0()), flat(q.position1()), flat(q.position2()), flat(q.position3()),
                    q.packedUV0(), q.packedUV1(), q.packedUV2(), q.packedUV3(), q.direction(), q.materialInfo()));
        }
        var extents = new Vector3fc[faces.size() * 4];
        for (int i = 0; i < faces.size(); i++) for (int v = 0; v < 4; v++) extents[i * 4 + v] = faces.get(i).position(v);
        return new FlatSprite(ItemQuads.split(faces), extents);
    }

    private static Vector3fc flat(Vector3fc vertex) { return new Vector3f(vertex.x(), vertex.y(), 0.5F); }
}
