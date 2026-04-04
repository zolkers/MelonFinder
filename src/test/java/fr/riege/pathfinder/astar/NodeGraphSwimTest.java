package fr.riege.pathfinder.astar;

import fr.riege.api.goal.BlockGoal;
import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.ICollisionLayer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.AABB;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.Direction;
import fr.riege.api.math.FluidType;
import fr.riege.api.path.PathStatus;
import fr.riege.api.registry.MovementKeys;
import fr.riege.pathfinder.evaluator.FallEvaluator;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.JumpEvaluator;
import fr.riege.pathfinder.evaluator.SwimEvaluator;
import fr.riege.pathfinder.evaluator.WalkEvaluator;
import fr.riege.pathfinder.registry.OrderedRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NodeGraphSwimTest {

    private IWorldLayer world(Set<BlockPos> fluid, Set<BlockPos> walkable, Set<BlockPos> solid) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return walkable.contains(pos); }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return solid.contains(pos); }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) {
                return fluid.contains(pos) ? FluidType.WATER : FluidType.NONE;
            }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
        };
    }

    private IBlockPhysicsLayer normalBlock() {
        return new IBlockPhysicsLayer() {
            @Override public float getSpeedMultiplier(@NonNull BlockPos pos) { return 1.0f; }
            @Override public float getSlipperiness(@NonNull BlockPos pos) { return 0.6f; }
            @Override public boolean isPassable(@NonNull BlockPos pos) { return true; }
            @Override public double getStandingY(@NonNull BlockPos pos) { return pos.y(); }
            @Override public float getDragFactor(@NonNull BlockPos pos) { return 1.5f; }
            @Override public float getBlockDamage(@NonNull BlockPos pos) { return 0.0f; }
        };
    }

    private IEntityPhysicsLayer standardEntity() {
        return new IEntityPhysicsLayer() {
            @Override public double getHitboxWidth() { return 0.6; }
            @Override public double getHitboxHeight() { return 1.8; }
            @Override public double getStepHeight() { return 0.6; }
            @Override public double getJumpVelocity() { return 0.42; }
            @Override public float evaluateFallDamage(int blocks) { return Math.max(0, blocks - 3); }
            @Override public double getSwimSpeed() { return 0.2; }
            @Override public double getSprintMultiplier() { return 1.3; }
            @Override public double getSneakSpeedMultiplier() { return 0.3; }
        };
    }

    private ICollisionLayer noCollision() {
        return new ICollisionLayer() {
            @Override public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos pos) { return Collections.emptyList(); }
            @Override public boolean hasCollisionAt(@NonNull AABB box) { return false; }
            @Override public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double hitboxHalf) { return 5.0; }
        };
    }

    private NodeGraph buildGraph(Set<BlockPos> fluid, Set<BlockPos> walkable, Set<BlockPos> solid) {
        IWorldLayer w = world(fluid, walkable, solid);
        IBlockPhysicsLayer b = normalBlock();
        IEntityPhysicsLayer e = standardEntity();
        ICollisionLayer c = noCollision();
        OrderedRegistry<IMovementEvaluator> registry = new OrderedRegistry<>();
        registry.register(MovementKeys.WALK, new WalkEvaluator(w, b));
        registry.register(MovementKeys.JUMP, new JumpEvaluator(w, b, e, c));
        registry.register(MovementKeys.FALL, new FallEvaluator(w, e));
        registry.register(MovementKeys.SWIM, new SwimEvaluator(w, b, e));
        return new NodeGraph(registry, w);
    }

    @Test
    void inFluid_swimNeighborGenerated() {
        Set<BlockPos> fluid = Set.of(new BlockPos(0, 64, 0), new BlockPos(1, 64, 0));
        NodeGraph graph = buildGraph(fluid, Set.of(), Set.of());

        List<NeighborMove> neighbors = graph.getNeighbors(new BlockPos(0, 64, 0));

        boolean foundSwim = neighbors.stream().anyMatch(m ->
            m.to().equals(new BlockPos(1, 64, 0)) && m.movementKey().equals(MovementKeys.SWIM));
        assertTrue(foundSwim, "Swim neighbor must be generated when in fluid");
    }

    @Test
    void inFluid_swimUpNeighborGenerated() {
        BlockPos current = new BlockPos(0, 64, 0);
        BlockPos above   = new BlockPos(0, 65, 0);
        Set<BlockPos> fluid = Set.of(current, above);
        NodeGraph graph = buildGraph(fluid, Set.of(), Set.of());

        List<NeighborMove> neighbors = graph.getNeighbors(current);

        boolean foundUp = neighbors.stream().anyMatch(m -> m.to().equals(above));
        assertTrue(foundUp, "Swim-up neighbor must be generated");
    }

    @Test
    void inFluid_swimDownNeighborGenerated() {
        BlockPos current = new BlockPos(0, 65, 0);
        BlockPos below   = new BlockPos(0, 64, 0);
        Set<BlockPos> fluid = Set.of(current, below);
        NodeGraph graph = buildGraph(fluid, Set.of(), Set.of());

        List<NeighborMove> neighbors = graph.getNeighbors(current);

        boolean foundDown = neighbors.stream().anyMatch(m -> m.to().equals(below));
        assertTrue(foundDown, "Swim-down neighbor must be generated");
    }

    @Test
    void notInFluid_noSwimNeighbors() {
        BlockPos current = new BlockPos(0, 64, 0);
        Set<BlockPos> walkable = Set.of(current, new BlockPos(1, 64, 0));
        Set<BlockPos> solid    = Set.of(new BlockPos(0, 63, 0), new BlockPos(1, 63, 0));
        NodeGraph graph = buildGraph(Set.of(), walkable, solid);

        List<NeighborMove> neighbors = graph.getNeighbors(current);

        boolean hasSwim = neighbors.stream().anyMatch(m -> m.movementKey().equals(MovementKeys.SWIM));
        assertFalse(hasSwim, "No swim neighbors when not in fluid");
    }

    @Test
    void astar_throughWater_findsPath() {
        BlockPos start = new BlockPos(0, 64, 0);
        BlockPos goal  = new BlockPos(3, 64, 0);
        Set<BlockPos> fluid = Set.of(
            new BlockPos(0, 64, 0),
            new BlockPos(1, 64, 0),
            new BlockPos(2, 64, 0),
            new BlockPos(3, 64, 0)
        );
        Set<BlockPos> solid = Set.of(
            new BlockPos(0, 63, 0),
            new BlockPos(1, 63, 0),
            new BlockPos(2, 63, 0),
            new BlockPos(3, 63, 0)
        );
        Set<BlockPos> walkable = Set.of(start, goal, new BlockPos(1, 64, 0), new BlockPos(2, 64, 0));
        IWorldLayer w = world(fluid, walkable, solid);
        IBlockPhysicsLayer b = normalBlock();
        IEntityPhysicsLayer e = standardEntity();
        ICollisionLayer c = noCollision();
        OrderedRegistry<IMovementEvaluator> registry = new OrderedRegistry<>();
        registry.register(MovementKeys.WALK, new WalkEvaluator(w, b));
        registry.register(MovementKeys.JUMP, new JumpEvaluator(w, b, e, c));
        registry.register(MovementKeys.FALL, new FallEvaluator(w, e));
        registry.register(MovementKeys.SWIM, new SwimEvaluator(w, b, e));
        AStarSearch search = new AStarSearch(new NodeGraph(registry, w), 5_000L);

        List<BlockPos> path = search.search(start, new BlockGoal(goal));

        assertEquals(PathStatus.FOUND, search.getLastStatus());
        assertEquals(goal, path.getLast());
    }

    @Test
    void astar_swimUpThroughWaterColumn_findsPath() {
        BlockPos start = new BlockPos(0, 64, 0);
        BlockPos goal  = new BlockPos(0, 67, 0);
        Set<BlockPos> fluid = Set.of(
            new BlockPos(0, 64, 0),
            new BlockPos(0, 65, 0),
            new BlockPos(0, 66, 0),
            new BlockPos(0, 67, 0)
        );
        Set<BlockPos> solid = Set.of(new BlockPos(0, 63, 0));
        Set<BlockPos> walkable = Set.of(start);
        IWorldLayer w = world(fluid, walkable, solid);
        IBlockPhysicsLayer b = normalBlock();
        IEntityPhysicsLayer e = standardEntity();
        ICollisionLayer c = noCollision();
        OrderedRegistry<IMovementEvaluator> registry = new OrderedRegistry<>();
        registry.register(MovementKeys.SWIM, new SwimEvaluator(w, b, e));
        AStarSearch search = new AStarSearch(new NodeGraph(registry, w), 5_000L);

        List<BlockPos> path = search.search(start, new BlockGoal(goal));

        assertEquals(PathStatus.FOUND, search.getLastStatus());
        assertEquals(goal, path.getLast());
    }
}
