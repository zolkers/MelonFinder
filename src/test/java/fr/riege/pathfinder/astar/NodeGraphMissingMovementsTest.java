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
import fr.riege.pathfinder.evaluator.ClimbEvaluator;
import fr.riege.pathfinder.evaluator.FallEvaluator;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.JumpEvaluator;
import fr.riege.pathfinder.evaluator.ParkourEvaluator;
import fr.riege.pathfinder.evaluator.SprintEvaluator;
import fr.riege.pathfinder.evaluator.WalkEvaluator;
import fr.riege.pathfinder.registry.OrderedRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that NodeGraph consults ClimbEvaluator, ParkourEvaluator, and
 * SprintEvaluator when they are registered — all three were silently ignored
 * before this fix.
 */
class NodeGraphMissingMovementsTest {

    // ── World / layer stubs ──────────────────────────────────────────────────

    private IWorldLayer world(Set<BlockPos> walkable) {
        return world(walkable, Set.of());
    }

    private IWorldLayer world(Set<BlockPos> walkable, Set<BlockPos> climbable) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return walkable.contains(pos); }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
            @Override public boolean isClimbable(@NonNull BlockPos pos) { return climbable.contains(pos); }
        };
    }

    private IBlockPhysicsLayer physics() {
        return new IBlockPhysicsLayer() {
            @Override public float  getSpeedMultiplier(@NonNull BlockPos p) { return 1.0f; }
            @Override public float  getSlipperiness(@NonNull BlockPos p)    { return 0.6f; }
            @Override public boolean isPassable(@NonNull BlockPos p)         { return true; }
            @Override public double getStandingY(@NonNull BlockPos p)        { return p.y(); }
            @Override public float  getDragFactor(@NonNull BlockPos p)       { return 1.0f; }
            @Override public float  getBlockDamage(@NonNull BlockPos p)      { return 0.0f; }
        };
    }

    private IEntityPhysicsLayer entity() {
        return new IEntityPhysicsLayer() {
            @Override public double getHitboxWidth()   { return 0.6; }
            @Override public double getHitboxHeight()  { return 1.8; }
            @Override public double getStepHeight()    { return 0.6; }
            @Override public double getJumpVelocity()  { return 0.42; }
            @Override public float  evaluateFallDamage(int blocks) { return Math.max(0, blocks - 3); }
            @Override public double getSwimSpeed()     { return 0.2; }
            @Override public double getSprintMultiplier()      { return 1.3; }
            @Override public double getSneakSpeedMultiplier()  { return 0.3; }
        };
    }

    private ICollisionLayer noCollision() {
        return new ICollisionLayer() {
            @Override public @NonNull List<AABB> getCollisionBoxes(@NonNull BlockPos p) { return Collections.emptyList(); }
            @Override public boolean hasCollisionAt(@NonNull AABB box) { return false; }
            @Override public double getMaxReach(@NonNull BlockPos from, @NonNull Direction dir, double h) { return 5.0; }
        };
    }

    // ── Graph builders ───────────────────────────────────────────────────────

    private NodeGraph walkOnlyGraph(Set<BlockPos> walkable) {
        IWorldLayer w        = world(walkable);
        IBlockPhysicsLayer b = physics();
        IEntityPhysicsLayer e = entity();
        ICollisionLayer c    = noCollision();

        OrderedRegistry<IMovementEvaluator> reg = new OrderedRegistry<>();
        reg.register(MovementKeys.WALK,  new WalkEvaluator(w, b));
        reg.register(MovementKeys.JUMP,  new JumpEvaluator(w, b, e, c));
        reg.register(MovementKeys.FALL,  new FallEvaluator(w, e));
        return new NodeGraph(reg, w);
    }

    private NodeGraph graphWithClimb(Set<BlockPos> walkable, Set<BlockPos> climbable) {
        IWorldLayer w        = world(walkable, climbable);
        IBlockPhysicsLayer b = physics();
        IEntityPhysicsLayer e = entity();
        ICollisionLayer c    = noCollision();

        OrderedRegistry<IMovementEvaluator> reg = new OrderedRegistry<>();
        reg.register(MovementKeys.WALK,  new WalkEvaluator(w, b));
        reg.register(MovementKeys.JUMP,  new JumpEvaluator(w, b, e, c));
        reg.register(MovementKeys.FALL,  new FallEvaluator(w, e));
        reg.register(MovementKeys.CLIMB, new ClimbEvaluator(w));
        return new NodeGraph(reg, w);
    }

    private NodeGraph graphWithParkour(Set<BlockPos> walkable) {
        IWorldLayer w        = world(walkable);
        IBlockPhysicsLayer b = physics();
        IEntityPhysicsLayer e = entity();
        ICollisionLayer c    = noCollision();

        OrderedRegistry<IMovementEvaluator> reg = new OrderedRegistry<>();
        reg.register(MovementKeys.WALK,    new WalkEvaluator(w, b));
        reg.register(MovementKeys.JUMP,    new JumpEvaluator(w, b, e, c));
        reg.register(MovementKeys.FALL,    new FallEvaluator(w, e));
        reg.register(MovementKeys.PARKOUR, new ParkourEvaluator(w));
        return new NodeGraph(reg, w);
    }

    private NodeGraph graphWithSprint(Set<BlockPos> walkable) {
        IWorldLayer w        = world(walkable);
        IBlockPhysicsLayer b = physics();
        IEntityPhysicsLayer e = entity();
        ICollisionLayer c    = noCollision();

        OrderedRegistry<IMovementEvaluator> reg = new OrderedRegistry<>();
        reg.register(MovementKeys.WALK,   new WalkEvaluator(w, b));
        reg.register(MovementKeys.JUMP,   new JumpEvaluator(w, b, e, c));
        reg.register(MovementKeys.FALL,   new FallEvaluator(w, e));
        reg.register(MovementKeys.SPRINT, new SprintEvaluator(w, b, e));
        return new NodeGraph(reg, w);
    }

    // ── Climb tests ──────────────────────────────────────────────────────────

    @Test
    void climbUp_neighborPresent_whenCurrentBlockIsClimbable() {
        BlockPos from = new BlockPos(0, 64, 0);
        BlockPos to   = new BlockPos(0, 65, 0);
        // from is a ladder (isClimbable=true); to is air (isPassable=true via world stub)
        NodeGraph graph = graphWithClimb(Set.of(), Set.of(from));

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertTrue(
            neighbors.stream().anyMatch(m -> m.to().equals(to) && m.movementKey().equals(MovementKeys.CLIMB)),
            "Climb-up neighbor must be present when standing on a climbable block"
        );
    }

    @Test
    void climbDown_neighborPresent_whenCurrentBlockIsClimbable() {
        BlockPos from = new BlockPos(0, 65, 0);
        BlockPos to   = new BlockPos(0, 64, 0);
        // from is a ladder; to is air below
        NodeGraph graph = graphWithClimb(Set.of(), Set.of(from));

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertTrue(
            neighbors.stream().anyMatch(m -> m.to().equals(to) && m.movementKey().equals(MovementKeys.CLIMB)),
            "Climb-down neighbor must be present when standing on a climbable block"
        );
    }

    @Test
    void climb_notGenerated_whenNoClimbEvaluatorRegistered() {
        BlockPos from = new BlockPos(0, 64, 0);
        NodeGraph graph = walkOnlyGraph(Set.of(from));

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertFalse(
            neighbors.stream().anyMatch(m -> m.movementKey().equals(MovementKeys.CLIMB)),
            "No CLIMB neighbors must appear when ClimbEvaluator is not registered"
        );
    }

    @Test
    void astar_acrossLadder_findsPath() {
        BlockPos start = new BlockPos(0, 64, 0);
        BlockPos goal  = new BlockPos(0, 65, 0);
        // start is climbable (ladder); goal is air above → CLIMB edge start→goal
        NodeGraph graph   = graphWithClimb(Set.of(), Set.of(start));
        AStarSearch astar = new AStarSearch(graph, 5_000L);

        List<BlockPos> path = astar.search(start, new BlockGoal(goal));

        assertEquals(PathStatus.FOUND, astar.getLastStatus(),
            "A* must find a path via CLIMB when ClimbEvaluator is registered");
        assertEquals(goal, path.getLast());
    }

    @Test
    void astar_withoutClimbEvaluator_cannotReachVerticalDestination() {
        BlockPos start = new BlockPos(0, 64, 0);
        BlockPos goal  = new BlockPos(0, 65, 0);
        NodeGraph graph   = walkOnlyGraph(Set.of(start));
        AStarSearch astar = new AStarSearch(graph, 5_000L);

        astar.search(start, new BlockGoal(goal));

        assertNotEquals(PathStatus.FOUND, astar.getLastStatus(),
            "Without ClimbEvaluator, a pure-vertical move must not be reachable");
    }

    // ── Parkour tests ────────────────────────────────────────────────────────

    @Test
    void parkourNeighbor_presentAcrossGap() {
        BlockPos from    = new BlockPos(0, 64, 0);
        BlockPos landing = new BlockPos(2, 64, 0);
        // (1,64,0) is NOT walkable → gap; (2,64,0) IS walkable → landing
        NodeGraph graph = graphWithParkour(Set.of(from, landing));

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertTrue(
            neighbors.stream().anyMatch(m -> m.to().equals(landing) && m.movementKey().equals(MovementKeys.PARKOUR)),
            "Parkour neighbor must be present across a 2-block gap"
        );
    }

    @Test
    void parkourNeighbor_notGenerated_whenGapIsWalkable() {
        BlockPos from       = new BlockPos(0, 64, 0);
        BlockPos intermediate = new BlockPos(1, 64, 0);
        BlockPos landing    = new BlockPos(2, 64, 0);
        // Intermediate IS walkable → ParkourEvaluator returns impossible (no real gap)
        NodeGraph graph = graphWithParkour(Set.of(from, intermediate, landing));

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertFalse(
            neighbors.stream().anyMatch(m -> m.to().equals(landing) && m.movementKey().equals(MovementKeys.PARKOUR)),
            "Parkour must not be generated when the intermediate block is walkable"
        );
    }

    @Test
    void astar_acrossParkourGap_findsPath() {
        BlockPos start   = new BlockPos(0, 64, 0);
        BlockPos goal    = new BlockPos(2, 64, 0);
        // (1,64,0) is the gap (NOT in walkable set)
        NodeGraph graph  = graphWithParkour(Set.of(start, goal));
        AStarSearch astar = new AStarSearch(graph, 5_000L);

        List<BlockPos> path = astar.search(start, new BlockGoal(goal));

        assertEquals(PathStatus.FOUND, astar.getLastStatus(),
            "A* must find a path via PARKOUR across a 2-block gap");
        assertEquals(goal, path.getLast());
    }

    @Test
    void astar_withoutParkourEvaluator_cannotCrossGap() {
        BlockPos start = new BlockPos(0, 64, 0);
        BlockPos goal  = new BlockPos(2, 64, 0);
        // Walk-only graph: (1,64,0) is the gap — no walkable intermediate
        NodeGraph graph  = walkOnlyGraph(Set.of(start, goal));
        AStarSearch astar = new AStarSearch(graph, 5_000L);

        astar.search(start, new BlockGoal(goal));

        assertNotEquals(PathStatus.FOUND, astar.getLastStatus(),
            "Without ParkourEvaluator, a 2-block gap must be unreachable");
    }

    // ── Sprint tests ─────────────────────────────────────────────────────────

    @Test
    void sprintNeighbor_presentAlongsideWalkNeighbor() {
        BlockPos from   = new BlockPos(0, 64, 0);
        BlockPos target = new BlockPos(1, 64, 0);
        NodeGraph graph = graphWithSprint(Set.of(from, target));

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertTrue(
            neighbors.stream().anyMatch(m -> m.to().equals(target) && m.movementKey().equals(MovementKeys.SPRINT)),
            "Sprint neighbor must be present when SprintEvaluator is registered"
        );
    }

    @Test
    void sprintNeighbor_cheaperThanWalkNeighborForSameTarget() {
        BlockPos from   = new BlockPos(0, 64, 0);
        BlockPos target = new BlockPos(1, 64, 0);
        NodeGraph graph = graphWithSprint(Set.of(from, target));

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        double walkCost = neighbors.stream()
            .filter(m -> m.to().equals(target) && m.movementKey().equals(MovementKeys.WALK))
            .mapToDouble(NeighborMove::edgeCost)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Walk neighbor missing for target"));

        double sprintCost = neighbors.stream()
            .filter(m -> m.to().equals(target) && m.movementKey().equals(MovementKeys.SPRINT))
            .mapToDouble(NeighborMove::edgeCost)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Sprint neighbor missing for target"));

        assertTrue(sprintCost < walkCost,
            "Sprint cost (" + sprintCost + ") must be less than walk cost (" + walkCost + ")");
    }
}
