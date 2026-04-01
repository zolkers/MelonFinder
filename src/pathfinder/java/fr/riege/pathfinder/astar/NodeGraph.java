package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import fr.riege.api.path.MovementType;
import fr.riege.api.path.Node;
import fr.riege.api.registry.IRegistry;
import fr.riege.api.registry.MovementKeys;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.MovementResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NodeGraph {

    private static final int[] DX = {1, -1, 0, 0, 1, -1, 1, -1};
    private static final int[] DZ = {0, 0, 1, -1, 1, 1, -1, -1};

    private final IRegistry<IMovementEvaluator> evaluatorRegistry;

    public NodeGraph(@NotNull IRegistry<IMovementEvaluator> evaluatorRegistry) {
        this.evaluatorRegistry = evaluatorRegistry;
    }

    public @NotNull List<Node> getNeighbors(@NotNull Node current) {
        List<Node> neighbors = new ArrayList<>();
        BlockPos pos = current.pos();
        double parentGCost = current.gCost();

        addHorizontalNeighbors(pos, parentGCost, neighbors);
        addVerticalNeighbors(pos, parentGCost, neighbors);

        return neighbors;
    }

    private void addHorizontalNeighbors(@NotNull BlockPos pos, double parentGCost,
            @NotNull List<Node> neighbors) {
        Optional<IMovementEvaluator> walkOpt = evaluatorRegistry.get(MovementKeys.WALK);
        if (walkOpt.isEmpty()) {
            return;
        }
        IMovementEvaluator walkEval = walkOpt.get();
        for (int i = 0; i < DX.length; i++) {
            BlockPos neighborPos = pos.offset(DX[i], 0, DZ[i]);
            MovementResult result = walkEval.evaluate(pos, neighborPos);
            if (result.isPossible()) {
                MovementType type = new MovementType(MovementKeys.WALK);
                neighbors.add(new Node(neighborPos, type, parentGCost + result.getCost(), 0));
            }
        }
    }

    private void addVerticalNeighbors(@NotNull BlockPos pos, double parentGCost,
            @NotNull List<Node> neighbors) {
        addJumpNeighbor(pos, parentGCost, neighbors);
        addFallNeighbor(pos, parentGCost, neighbors);
    }

    private void addJumpNeighbor(@NotNull BlockPos pos, double parentGCost,
            @NotNull List<Node> neighbors) {
        Optional<IMovementEvaluator> jumpOpt = evaluatorRegistry.get(MovementKeys.JUMP);
        if (jumpOpt.isEmpty()) {
            return;
        }
        IMovementEvaluator jumpEval = jumpOpt.get();
        BlockPos upPos = pos.offset(0, 1, 0);
        MovementResult result = jumpEval.evaluate(pos, upPos);
        if (result.isPossible()) {
            MovementType type = new MovementType(MovementKeys.JUMP);
            neighbors.add(new Node(upPos, type, parentGCost + result.getCost(), 0));
        }
    }

    private void addFallNeighbor(@NotNull BlockPos pos, double parentGCost,
            @NotNull List<Node> neighbors) {
        Optional<IMovementEvaluator> fallOpt = evaluatorRegistry.get(MovementKeys.FALL);
        if (fallOpt.isEmpty()) {
            return;
        }
        IMovementEvaluator fallEval = fallOpt.get();
        BlockPos downPos = pos.offset(0, -1, 0);
        MovementResult result = fallEval.evaluate(pos, downPos);
        if (result.isPossible()) {
            MovementType type = new MovementType(MovementKeys.FALL);
            neighbors.add(new Node(downPos, type, parentGCost + result.getCost(), 0));
        }
    }
}
