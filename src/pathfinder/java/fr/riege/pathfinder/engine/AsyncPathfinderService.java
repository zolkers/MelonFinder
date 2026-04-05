package fr.riege.pathfinder.engine;

import fr.riege.api.goal.IGoal;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.PathResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class AsyncPathfinderService {

    private final PathfinderEngine engine = new PathfinderEngine();
    private final AtomicReference<CompletableFuture<PathResult>> activeFuture = new AtomicReference<>();

    public @NotNull CompletableFuture<PathResult> requestPath(
            @NotNull BlockPos from, @NotNull IGoal goal, @NotNull PathfinderContext ctx) {
        CompletableFuture<PathResult> future = new CompletableFuture<>();
        CompletableFuture<PathResult> previous = activeFuture.getAndSet(future);
        if (previous != null) {
            previous.cancel(true);
        }
        engine.cancel();

        Thread.ofVirtual().start(() -> {
            if (Thread.currentThread().isInterrupted()) {
                future.cancel(true);
                return;
            }
            try {
                PathResult result = engine.compute(from, goal, ctx);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                activeFuture.compareAndSet(future, null);
            }
        });

        return future;
    }

    public void cancel() {
        CompletableFuture<PathResult> current = activeFuture.getAndSet(null);
        if (current != null) {
            current.cancel(true);
        }
        engine.cancel();
    }

    public boolean isRunning() {
        CompletableFuture<PathResult> current = activeFuture.get();
        return current != null && !current.isDone();
    }

    public @NotNull Map<BlockPos, Double> getLastExploredCosts() {
        return engine.getLastExploredCosts();
    }

    public @NotNull Map<BlockPos, BlockPos> getLastParentMap() {
        return engine.getLastParentMap();
    }
}
