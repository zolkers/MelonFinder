package fr.riege.pathfinder.engine;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.registry.IRegistry;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.heuristic.IHeuristic;

public record PathfinderContext(IWorldLayer worldLayer, IBlockPhysicsLayer blockPhysicsLayer,
                                IEntityPhysicsLayer entityPhysicsLayer, ICollisionLayer collisionLayer,
                                IRegistry<IMovementEvaluator> evaluatorRegistry, IHeuristic heuristic, int maxNodes,
                                int maxSegmentLength, long randomSeed) {

}
