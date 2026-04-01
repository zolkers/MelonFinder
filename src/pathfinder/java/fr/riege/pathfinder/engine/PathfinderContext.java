package fr.riege.pathfinder.engine;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.registry.IRegistry;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.heuristic.IHeuristic;
import org.jetbrains.annotations.NotNull;

public final class PathfinderContext {

    private final IWorldLayer worldLayer;
    private final IBlockPhysicsLayer blockPhysicsLayer;
    private final IEntityPhysicsLayer entityPhysicsLayer;
    private final ICollisionLayer collisionLayer;
    private final IRegistry<IMovementEvaluator> evaluatorRegistry;
    private final IHeuristic heuristic;
    private final int maxNodes;
    private final int maxSegmentLength;

    public PathfinderContext(
            @NotNull IWorldLayer worldLayer,
            @NotNull IBlockPhysicsLayer blockPhysicsLayer,
            @NotNull IEntityPhysicsLayer entityPhysicsLayer,
            @NotNull ICollisionLayer collisionLayer,
            @NotNull IRegistry<IMovementEvaluator> evaluatorRegistry,
            @NotNull IHeuristic heuristic,
            int maxNodes,
            int maxSegmentLength) {
        this.worldLayer = worldLayer;
        this.blockPhysicsLayer = blockPhysicsLayer;
        this.entityPhysicsLayer = entityPhysicsLayer;
        this.collisionLayer = collisionLayer;
        this.evaluatorRegistry = evaluatorRegistry;
        this.heuristic = heuristic;
        this.maxNodes = maxNodes;
        this.maxSegmentLength = maxSegmentLength;
    }

    public @NotNull IWorldLayer getWorldLayer() { return worldLayer; }
    public @NotNull IBlockPhysicsLayer getBlockPhysicsLayer() { return blockPhysicsLayer; }
    public @NotNull IEntityPhysicsLayer getEntityPhysicsLayer() { return entityPhysicsLayer; }
    public @NotNull ICollisionLayer getCollisionLayer() { return collisionLayer; }
    public @NotNull IRegistry<IMovementEvaluator> getEvaluatorRegistry() { return evaluatorRegistry; }
    public @NotNull IHeuristic getHeuristic() { return heuristic; }
    public int getMaxNodes() { return maxNodes; }
    public int getMaxSegmentLength() { return maxSegmentLength; }
}
