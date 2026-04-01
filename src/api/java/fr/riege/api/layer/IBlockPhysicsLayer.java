package fr.riege.api.layer;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IBlockPhysicsLayer {
    float getSpeedMultiplier(@NotNull BlockPos pos);
    float getSlipperiness(@NotNull BlockPos pos);
    boolean isPassable(@NotNull BlockPos pos);
    double getStandingY(@NotNull BlockPos pos);
    float getDragFactor(@NotNull BlockPos pos);
    float getBlockDamage(@NotNull BlockPos pos);
}
