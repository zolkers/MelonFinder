package fr.riege.pathfinder.astar;

import fr.riege.api.layer.IBlockPhysicsLayer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import fr.riege.api.registry.MovementKeys;
import fr.riege.pathfinder.evaluator.IMovementEvaluator;
import fr.riege.pathfinder.evaluator.WalkEvaluator;
import fr.riege.pathfinder.registry.OrderedRegistry;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NodeGraphChunkGuardTest {

    private static final int Y = 64;

    private IWorldLayer worldWithUnloaded(Set<BlockPos> walkable, Set<BlockPos> unloaded) {
        return new IWorldLayer() {
            @Override public boolean isWalkable(@NonNull BlockPos pos) { return walkable.contains(pos); }
            @Override public boolean isSolid(@NonNull BlockPos pos) { return false; }
            @Override public @NonNull FluidType getFluidType(@NonNull BlockPos pos) { return FluidType.NONE; }
            @Override public int getLightLevel(@NonNull BlockPos pos) { return 15; }
            @Override public boolean isChunkLoaded(@NonNull BlockPos pos) { return !unloaded.contains(pos); }
        };
    }

    private IBlockPhysicsLayer normalBlock() {
        return new IBlockPhysicsLayer() {
            @Override public float getSpeedMultiplier(@NonNull BlockPos pos) { return 1.0f; }
            @Override public float getSlipperiness(@NonNull BlockPos pos) { return 0.6f; }
            @Override public boolean isPassable(@NonNull BlockPos pos) { return true; }
            @Override public double getStandingY(@NonNull BlockPos pos) { return pos.y(); }
            @Override public float getDragFactor(@NonNull BlockPos pos) { return 1.0f; }
            @Override public float getBlockDamage(@NonNull BlockPos pos) { return 0.0f; }
        };
    }

    @Test
    void neighborInUnloadedChunk_notGenerated() {
        BlockPos from    = new BlockPos(0, Y, 0);
        BlockPos blocked = new BlockPos(1, Y, 0);  // walkable but chunk unloaded

        IWorldLayer world = worldWithUnloaded(Set.of(from, blocked), Set.of(blocked));
        IBlockPhysicsLayer block = normalBlock();
        OrderedRegistry<IMovementEvaluator> reg = new OrderedRegistry<>();
        reg.register(MovementKeys.WALK, new WalkEvaluator(world, block));
        NodeGraph graph = new NodeGraph(reg, world);

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertFalse(
            neighbors.stream().anyMatch(m -> m.to().equals(blocked)),
            "Neighbor in unloaded chunk must not be generated"
        );
    }

    @Test
    void allChunksLoaded_neighborsGeneratedNormally() {
        BlockPos from   = new BlockPos(0, Y, 0);
        BlockPos target = new BlockPos(1, Y, 0);

        IWorldLayer world = worldWithUnloaded(Set.of(from, target), Set.of());
        IBlockPhysicsLayer block = normalBlock();
        OrderedRegistry<IMovementEvaluator> reg = new OrderedRegistry<>();
        reg.register(MovementKeys.WALK, new WalkEvaluator(world, block));
        NodeGraph graph = new NodeGraph(reg, world);

        List<NeighborMove> neighbors = graph.getNeighbors(from);

        assertTrue(
            neighbors.stream().anyMatch(m -> m.to().equals(target)),
            "Neighbor must be generated when chunk is loaded"
        );
    }
}
