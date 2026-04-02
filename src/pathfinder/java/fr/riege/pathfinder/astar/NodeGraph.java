package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import fr.riege.api.path.MovementType;
import fr.riege.api.path.Node;
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

    public @NotNull List<Node> getNeighbors(@NotNull Node current) {
        List<Node> neighbors = new ArrayList<>();
        BlockPos pos = current.pos();
        double parentGCost = current.gCost();

        addHorizontalNeighbors(pos, parentGCost, neighbors);
        addFallNeighbors(pos, parentGCost, neighbors);

        return neighbors;
    }

    private void addHorizontalNeighbors(@NotNull BlockPos pos, double parentGCost,
            @NotNull List<Node> neighbors) {
        Optional<IMovementEvaluator> walkOpt = evaluatorRegistry.get(MovementKeys.WALK);
        Optional<IMovementEvaluator> jumpOpt = evaluatorRegistry.get(MovementKeys.JUMP);

        for (int i = 0; i < DX.length; i++) {
            int dx = DX[i];
            int dz = DZ[i];

            walkOpt.ifPresent(walkEval -> {
                tryMove(pos, dx, 0, dz, walkEval, MovementKeys.WALK, parentGCost, neighbors);
                tryMove(pos, dx, -1, dz, walkEval, MovementKeys.WALK, parentGCost, neighbors);
            });

            jumpOpt.ifPresent(jumpEval ->
                tryMove(pos, dx, 1, dz, jumpEval, MovementKeys.JUMP, parentGCost, neighbors)
            );
        }
    }

    private void addFallNeighbors(@NotNull BlockPos pos, double parentGCost,
            @NotNull List<Node> neighbors) {
        Optional<IMovementEvaluator> fallOpt = evaluatorRegistry.get(MovementKeys.FALL);
        if (fallOpt.isEmpty()) return;

        IMovementEvaluator fallEval = fallOpt.get();
        for (int i = 0; i < DX.length; i++) {
            int dx = DX[i];
            int dz = DZ[i];
            for (int dy = 2; dy <= MAX_FALL_DEPTH + 1; dy++) {
                MovementResult result = fallEval.evaluate(pos, pos.offset(dx, -dy, dz));
                if (!result.isPossible()) continue;
                MovementType type = new MovementType(MovementKeys.FALL);
                neighbors.add(new Node(pos.offset(dx, -dy, dz), type, parentGCost + result.getCost(), 0));
                break;
            }
        }
    }

    private void tryMove(@NotNull BlockPos pos, int dx, int dy, int dz,
            @NotNull IMovementEvaluator evaluator, @NotNull RegistryKey key,
            double parentGCost, @NotNull List<Node> neighbors) {
        BlockPos to = pos.offset(dx, dy, dz);
        MovementResult result = evaluator.evaluate(pos, to);
        if (!result.isPossible()) return;
        neighbors.add(new Node(to, new MovementType(key), parentGCost + result.getCost(), 0));
    }
}
