package dev.reed.betaatmosphere;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

/** One reusable sampler per lighter; no access to the live client world from mesh workers. */
public final class BetaBlockLighting {
    private final BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
    private final float[] samples = new float[9];
    private final boolean[] blocked = new boolean[9];
    private final float[] corners = new float[4];
    private Direction.Axis axis;
    private float ambient;

    public static float shade(Direction direction) {
        return switch (direction) {
            case DOWN -> 0.5F;
            case UP -> 1.0F;
            case NORTH, SOUTH -> 0.8F;
            case WEST, EAST -> 0.6F;
        };
    }

    public void prepare(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction face,
                        BetaLightmapState.Parameters parameters) {
        axis = face.getAxis();
        ambient = parameters.ambient();
        int x = pos.getX() + face.getStepX(), y = pos.getY() + face.getStepY(), z = pos.getZ() + face.getStepZ();
        for (int b = -1; b <= 1; b++) for (int a = -1; a <= 1; a++) {
            // Beta's diagonal fallback uses X edges on Y/Z faces, Z edges on X faces.
            scratch.set(x + (axis == Direction.Axis.X ? 0 : a),
                    y + (axis == Direction.Axis.Y ? 0 : b),
                    z + (axis == Direction.Axis.X ? a : axis == Direction.Axis.Y ? b : 0));
            int index = (b + 1) * 3 + a + 1;
            samples[index] = sample(level, state, scratch, parameters);
            blocked[index] = level.getBlockState(scratch).isSolid();
        }
        for (int b = 0; b < 2; b++) for (int a = 0; a < 2; a++) {
            int ea = 3 + a * 2, eb = 1 + b * 6, diagonal = a * 2 + b * 6;
            corners[b * 2 + a] = BetaVertexMath.corner(samples[4], samples[ea], samples[eb],
                    samples[diagonal], blocked[ea], blocked[eb]);
        }
    }

    public int smooth(float x, float y, float z) {
        float a = axis == Direction.Axis.X ? z : x;
        float b = axis == Direction.Axis.Y ? z : y;
        return BetaVertexMath.encode(BetaVertexMath.bilinear(corners[0], corners[1], corners[2], corners[3], a, b), ambient);
    }

    public int flat(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction face,
                    boolean boundary, BetaLightmapState.Parameters parameters) {
        scratch.set(pos);
        if (boundary) scratch.move(face);
        return BetaVertexMath.encode(sample(level, state, scratch, parameters), parameters.ambient());
    }

    public static boolean boundary(Direction face, double normalPosition) {
        return face.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? normalPosition >= 0.9999F : normalPosition <= 0.0001F;
    }

    private static float sample(BlockAndTintGetter level, BlockState source, BlockPos pos,
                                BetaLightmapState.Parameters parameters) {
        if (source.emissiveRendering()) return 1;
        int block = Math.max(source.getLightEmission(), level.getBrightness(LightLayer.BLOCK, pos));
        int sky = level.getBrightness(LightLayer.SKY, pos);
        return BetaLightMath.combined(block, sky, parameters.skyDarkening(), parameters.ambient());
    }
}
