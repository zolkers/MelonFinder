package fr.riege.layer.adapter;

import fr.riege.api.annotation.Layer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Layer
public final class FabricCollisionLayer implements ICollisionLayer {

    private final Level level;

    public FabricCollisionLayer(@NotNull Level level) {
        this.level = level;
    }

    @Override
    public @NotNull List<AABB> getCollisionBoxes(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        BlockState state = level.getBlockState(mcPos);
        VoxelShape shape = state.getCollisionShape(level, mcPos);
        List<AABB> boxes = new ArrayList<>();
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
            boxes.add(new AABB(
                new Vec3(pos.x() + x1, pos.y() + y1, pos.z() + z1),
                new Vec3(pos.x() + x2, pos.y() + y2, pos.z() + z2)
            ))
        );
        return boxes;
    }

    @Override
    public boolean hasCollisionAt(@NotNull AABB box) {
        net.minecraft.world.phys.AABB mcBox = toMcBox(box);
        return !level.getBlockCollisions(null, mcBox).iterator().hasNext();
    }

    @Override
    public double getMaxReach(@NotNull BlockPos from, @NotNull Direction dir, double hitboxHalf) {
        net.minecraft.core.BlockPos mcPos = toMc(from);
        net.minecraft.core.Direction mcDir = toMcDir(dir);
        net.minecraft.core.BlockPos neighbor = mcPos.relative(mcDir);
        BlockState state = level.getBlockState(neighbor);
        VoxelShape shape = state.getCollisionShape(level, neighbor);
        if (shape.isEmpty()) return 1.0;
        return 1.0 - shape.min(mcDir.getAxis());
    }

    private @NotNull net.minecraft.core.BlockPos toMc(@NotNull BlockPos pos) {
        return new net.minecraft.core.BlockPos(pos.x(), pos.y(), pos.z());
    }

    private @NotNull net.minecraft.world.phys.AABB toMcBox(@NotNull AABB box) {
        return new net.minecraft.world.phys.AABB(
            box.min().getX(), box.min().getY(), box.min().getZ(),
            box.max().getX(), box.max().getY(), box.max().getZ()
        );
    }

    private @NotNull net.minecraft.core.Direction toMcDir(@NotNull Direction dir) {
        return switch (dir) {
            case NORTH -> net.minecraft.core.Direction.NORTH;
            case SOUTH -> net.minecraft.core.Direction.SOUTH;
            case EAST -> net.minecraft.core.Direction.EAST;
            case WEST -> net.minecraft.core.Direction.WEST;
            case UP -> net.minecraft.core.Direction.UP;
            case DOWN -> net.minecraft.core.Direction.DOWN;
        };
    }
}
