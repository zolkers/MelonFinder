package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import fr.riege.api.registry.IRegistry;
import fr.riege.api.registry.MovementKeys;
import fr.riege.api.registry.RegistryKey;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.MovementResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NodeGraph {

    private static final int[] DX = {1, -1, 0, 0, 1, -1, 1, -1};
    private static final int[] DZ = {0, 0, 1, -1, 1, 1, -1, -1};
    private static final int MAX_FALL_DEPTH = 3;

    private final IRegistry<IMovementEvaluator> evaluatorRegistry;

    public NodeGraph(@NotNull IRegistry<IMovementEvaluator> evaluatorRegistry) {
        this.evaluatorRegistry = evaluatorRegistry;
    }

    public @NotNull List<NeighborMove> getNeighbors(@NotNull BlockPos pos) {
        List<NeighborMove> neighbors = new ArrayList<>();
        addHorizontalNeighbors(pos, neighbors);
        addFallNeighbors(pos, neighbors);
        return neighbors;
    }

    private void addHorizontalNeighbors(@NotNull BlockPos pos, @NotNull List<NeighborMove> neighbors) {
        Optional<IMovementEvaluator> walkOpt = evaluatorRegistry.get(MovementKeys.WALK);
        Optional<IMovementEvaluator> jumpOpt = evaluatorRegistry.get(MovementKeys.JUMP);

        for (int i = 0; i < DX.length; i++) {
            int dx = DX[i];
            int dz = DZ[i];
            walkOpt.ifPresent(eval -> {
                tryMove(pos, dx, 0, dz, eval, MovementKeys.WALK, neighbors);
                tryMove(pos, dx, -1, dz, eval, MovementKeys.WALK, neighbors);
            });
            jumpOpt.ifPresent(eval -> tryMove(pos, dx, 1, dz, eval, MovementKeys.JUMP, neighbors));
        }
    }

    private void addFallNeighbors(@NotNull BlockPos pos, @NotNull List<NeighborMove> neighbors) {
        Optional<IMovementEvaluator> fallOpt = evaluatorRegistry.get(MovementKeys.FALL);
        if (fallOpt.isEmpty()) return;

        IMovementEvaluator fallEval = fallOpt.get();
        for (int i = 0; i < DX.length; i++) {
            int dx = DX[i];
            int dz = DZ[i];
            for (int dy = 2; dy <= MAX_FALL_DEPTH + 1; dy++) {
                BlockPos to = pos.offset(dx, -dy, dz);
                MovementResult result = fallEval.evaluate(pos, to);
                if (!result.isPossible()) continue;
                neighbors.add(new NeighborMove(to, MovementKeys.FALL, result.getCost()));
                break;
            }
        }
    }

    private void tryMove(@NotNull BlockPos pos, int dx, int dy, int dz,
            @NotNull IMovementEvaluator evaluator, @NotNull RegistryKey key,
            @NotNull List<NeighborMove> neighbors) {
        BlockPos to = pos.offset(dx, dy, dz);
        MovementResult result = evaluator.evaluate(pos, to);
        if (result.isPossible()) {
            neighbors.add(new NeighborMove(to, key, result.getCost()));
        }
    }
}
