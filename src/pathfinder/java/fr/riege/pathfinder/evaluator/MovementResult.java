package fr.riege.pathfinder.evaluator;

import org.jetbrains.annotations.NotNull;

public final class MovementResult {

    private final boolean possible;
    private final double cost;

    private MovementResult(boolean possible, double cost) {
        this.possible = possible;
        this.cost = cost;
    }

    public static @NotNull MovementResult possible(double cost) {
        return new MovementResult(true, cost);
    }

    private static final MovementResult IMPOSSIBLE = new MovementResult(false, Double.MAX_VALUE);

    public static @NotNull MovementResult impossible() {
        return IMPOSSIBLE;
    }

    public boolean isPossible() { return possible; }
    public double getCost() { return cost; }
}
