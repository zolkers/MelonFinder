package fr.riege.layer.adapter;

import fr.riege.api.annotation.Layer;
import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.math.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

@Layer
public final class FabricBlockPhysicsLayer implements IBlockPhysicsLayer {

    private final Level level;

    public FabricBlockPhysicsLayer(@NotNull Level level) {
        this.level = level;
    }

    @Override
    public float getSpeedMultiplier(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        BlockState state = level.getBlockState(mcPos);
        return state.getBlock().getSpeedFactor();
    }

    @Override
    public float getSlipperiness(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        BlockState state = level.getBlockState(mcPos);
        return state.getBlock().getFriction();
    }

    @Override
    public boolean isPassable(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        BlockState state = level.getBlockState(mcPos);
        return state.getCollisionShape(level, mcPos).isEmpty();
    }

    @Override
    public double getStandingY(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        BlockState state = level.getBlockState(mcPos);
        double maxY = state.getCollisionShape(level, mcPos).max(Direction.Axis.Y);
        return Double.isInfinite(maxY) ? pos.getY() : pos.getY() + maxY;
    }

    @Override
    public float getDragFactor(@NotNull BlockPos pos) {
        FluidState fluid = level.getFluidState(toMc(pos));
        return fluid.isEmpty() ? 1.0f : 0.5f;
    }

    @Override
    public float getBlockDamage(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        BlockState state = level.getBlockState(mcPos);
        float jumpFactor = state.getBlock().getJumpFactor();
        return jumpFactor < 1.0f ? 1.0f - jumpFactor : 0.0f;
    }

    private @NotNull net.minecraft.core.BlockPos toMc(@NotNull BlockPos pos) {
        return new net.minecraft.core.BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }
}
