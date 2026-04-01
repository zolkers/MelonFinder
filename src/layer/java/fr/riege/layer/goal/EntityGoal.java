package fr.riege.layer.goal;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class EntityGoal implements IGoal {

    private final Entity entity;
    private final double radius;

    public EntityGoal(@NotNull Entity entity, double radius) {
        this.entity = entity;
        this.radius = radius;
    }

    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return current.distanceTo(getTargetForHeuristic()) <= radius;
    }

    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        net.minecraft.core.BlockPos pos = entity.blockPosition();
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public double getRadius() {
        return radius;
    }
}
