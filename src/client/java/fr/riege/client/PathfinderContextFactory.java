package fr.riege.client;

import fr.riege.api.registry.IRegistry;
import fr.riege.api.registry.MovementKeys;
import fr.riege.layer.adapter.FabricBlockPhysicsLayer;
import fr.riege.layer.adapter.FabricCollisionLayer;
import fr.riege.layer.adapter.FabricEntityPhysicsLayer;
import fr.riege.layer.adapter.FabricWorldLayer;
import fr.riege.pathfinder.engine.PathfinderContext;
import fr.riege.pathfinder.evaluator.ClimbEvaluator;
import fr.riege.pathfinder.evaluator.FallEvaluator;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.JumpEvaluator;
import fr.riege.pathfinder.evaluator.ParkourEvaluator;
import fr.riege.pathfinder.evaluator.SneakEvaluator;
import fr.riege.pathfinder.evaluator.SprintEvaluator;
import fr.riege.pathfinder.evaluator.SwimEvaluator;
import fr.riege.pathfinder.evaluator.WalkEvaluator;
import fr.riege.pathfinder.heuristic.Euclidean3DHeuristic;
import fr.riege.pathfinder.registry.OrderedRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class PathfinderContextFactory {

    private static final int DEFAULT_MAX_NODES = 100_000;
    private static final int DEFAULT_MAX_SEGMENT_LENGTH = 12;
    private static final long DEFAULT_RANDOM_SEED = 42L;

    private PathfinderContextFactory() {}

    public static @NotNull PathfinderContext create(@NotNull LocalPlayer player) {
        Level level = player.level();
        FabricWorldLayer world = new FabricWorldLayer(level);
        FabricBlockPhysicsLayer blockPhysics = new FabricBlockPhysicsLayer(level);
        FabricEntityPhysicsLayer entityPhysics = new FabricEntityPhysicsLayer(player);
        FabricCollisionLayer collision = new FabricCollisionLayer(level);
        IRegistry<IMovementEvaluator> registry = buildRegistry(world, blockPhysics, entityPhysics, collision);
        return new PathfinderContext(
            world, blockPhysics, entityPhysics, collision,
            registry, new Euclidean3DHeuristic(),
            DEFAULT_MAX_NODES, DEFAULT_MAX_SEGMENT_LENGTH, DEFAULT_RANDOM_SEED);
    }

    private static @NotNull IRegistry<IMovementEvaluator> buildRegistry(
            @NotNull FabricWorldLayer world,
            @NotNull FabricBlockPhysicsLayer blockPhysics,
            @NotNull FabricEntityPhysicsLayer entityPhysics,
            @NotNull FabricCollisionLayer collision) {
        OrderedRegistry<IMovementEvaluator> registry = new OrderedRegistry<>();
        registry.register(MovementKeys.WALK,    new WalkEvaluator(world, blockPhysics, entityPhysics));
        registry.register(MovementKeys.JUMP,    new JumpEvaluator(world, blockPhysics, entityPhysics, collision));
        registry.register(MovementKeys.FALL,    new FallEvaluator(world, entityPhysics));
        registry.register(MovementKeys.SWIM,    new SwimEvaluator(world, blockPhysics, entityPhysics));
        registry.register(MovementKeys.CLIMB,   new ClimbEvaluator(blockPhysics));
        registry.register(MovementKeys.SPRINT,  new SprintEvaluator(world, blockPhysics, entityPhysics));
        registry.register(MovementKeys.SNEAK,   new SneakEvaluator(world, blockPhysics, entityPhysics));
        registry.register(MovementKeys.PARKOUR, new ParkourEvaluator(world));
        return registry;
    }
}
