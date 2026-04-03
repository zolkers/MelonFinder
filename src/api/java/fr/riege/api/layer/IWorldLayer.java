package fr.riege.api.layer;

import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import org.jetbrains.annotations.NotNull;

public interface IWorldLayer {
    boolean isWalkable(@NotNull BlockPos pos);
    boolean isSolid(@NotNull BlockPos pos);
    @NotNull FluidType getFluidType(@NotNull BlockPos pos);
    int getLightLevel(@NotNull BlockPos pos);

    default boolean isPassable(@NotNull BlockPos pos) {
        return !isSolid(pos);
    }
}
