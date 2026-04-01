package fr.riege.pathfinder.evaluator;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IMovementEvaluator {
    @NotNull MovementResult evaluate(@NotNull BlockPos from, @NotNull BlockPos to);
}
