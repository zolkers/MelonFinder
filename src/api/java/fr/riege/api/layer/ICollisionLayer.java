package fr.riege.api.layer;

import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICollisionLayer {
    @NotNull List<AABB> getCollisionBoxes(@NotNull BlockPos pos);
    boolean hasCollisionAt(@NotNull AABB box);
    double getMaxReach(@NotNull BlockPos from, @NotNull Direction dir, double hitboxHalf);
}
