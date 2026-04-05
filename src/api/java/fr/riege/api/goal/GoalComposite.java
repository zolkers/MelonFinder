package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An {@link IGoal} implementation that combines multiple constituent goals
 * using a logical operator.
 *
 * <p>A {@code GoalComposite} allows for complex search criteria by grouping
 * multiple goals together. For example:
 * <ul>
 *   <li>{@link Logic#OR}: Reaching <em>any</em> of the sub-goals satisfies the
 *       composite. The heuristic uses the minimum of all sub-goal estimates.</li>
 *   <li>{@link Logic#AND}: Reaching a position that satisfies <em>all</em>
 *       sub-goals simultaneously. The heuristic uses the maximum of all
 *       sub-goal estimates to remain admissible.</li>
 * </ul>
 *
 * @see IGoal
 */
public final class GoalComposite implements IGoal {

    /**
     * Defines the logical operator used to combine constituent goals.
     */
    public enum Logic {
        /**
         * Satisfied if any constituent goal is reached.
         */
        OR,
        /**
         * Satisfied only if all constituent goals are reached simultaneously.
         */
        AND
    }

    private final List<IGoal> goals;
    private final Logic logic;

    /**
     * Constructs a {@code GoalComposite} from a collection of goals.
     *
     * @param logic the logical operator to apply
     * @param goals the constituent goals; must not be empty or {@code null}
     * @throws IllegalArgumentException if the goal collection is empty
     */
    public GoalComposite(@NotNull Logic logic, @NotNull Collection<? extends IGoal> goals) {
        if (goals.isEmpty()) {
            throw new IllegalArgumentException("GoalComposite requires at least one sub-goal");
        }
        this.logic = logic;
        this.goals = List.copyOf(goals);
    }

    /**
     * Constructs a {@code GoalComposite} from an array of goals.
     *
     * @param logic the logical operator to apply
     * @param goals the constituent goals; must not be empty
     * @throws IllegalArgumentException if no goals are provided
     */
    public GoalComposite(@NotNull Logic logic, @NotNull IGoal... goals) {
        this(logic, Arrays.asList(goals));
    }

    /**
     * Returns {@code true} if the constituent goals satisfy the logical
     * condition at the current position.
     *
     * @param current the block position currently being evaluated; must not
     *                be {@code null}
     * @return {@code true} if the logical condition is met; {@code false} otherwise
     */
    @Override
    public boolean isReached(@NotNull BlockPos current) {
        return switch (logic) {
            case OR -> {
                for (IGoal goal : goals) {
                    if (goal.isReached(current)) yield true;
                }
                yield false;
            }
            case AND -> {
                for (IGoal goal : goals) {
                    if (!goal.isReached(current)) yield false;
                }
                yield true;
            }
        };
    }

    /**
     * Returns the target for the first constituent goal as a representative
     * heuristic reference point.
     *
     * @return the first sub-goal's heuristic target; never {@code null}
     */
    @Override
    public @NotNull BlockPos getTargetForHeuristic() {
        return goals.get(0).getTargetForHeuristic();
    }

    /**
     * Returns a combined heuristic cost estimate based on the logical operator.
     *
     * <ul>
     *   <li>For {@link Logic#OR}, returns the <strong>minimum</strong> estimate
     *       among all sub-goals.</li>
     *   <li>For {@link Logic#AND}, returns the <strong>maximum</strong> estimate
     *       among all sub-goals.</li>
     * </ul>
     *
     * @param from the position from which to estimate; never {@code null}
     * @return the combined heuristic estimate; always {@code >= 0.0}
     */
    @Override
    public double heuristicCost(@NotNull BlockPos from) {
        return switch (logic) {
            case OR -> {
                double min = Double.MAX_VALUE;
                for (IGoal goal : goals) {
                    min = Math.min(min, goal.heuristicCost(from));
                }
                yield min;
            }
            case AND -> {
                double max = 0.0;
                for (IGoal goal : goals) {
                    max = Math.max(max, goal.heuristicCost(from));
                }
                yield max;
            }
        };
    }

    /**
     * Returns the logical operator used by this composite.
     *
     * @return the logic type; never {@code null}
     */
    public @NotNull Logic getLogic() {
        return logic;
    }

    /**
     * Returns an unmodifiable list of the constituent goals.
     *
     * @return the sub-goals; never {@code null}
     */
    public @NotNull List<IGoal> getGoals() {
        return goals;
    }
}
