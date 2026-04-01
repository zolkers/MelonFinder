package fr.riege.api.goal;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public interface IGoal {

    /** Vérifie si la position actuelle satisfait le goal. */
    boolean isReached(@NotNull BlockPos current);

    /** Retourne la cible courante pour le calcul heuristique. Peut changer entre deux appels (entity goal). */
    @NotNull BlockPos getTargetForHeuristic();

    /**
     * Coût heuristique estimé depuis `from` vers ce goal.
     * Implémentation par défaut : distance euclidienne 3D vers getTargetForHeuristic().
     */
    default double heuristicCost(@NotNull BlockPos from) {
        return from.distanceTo(getTargetForHeuristic());
    }
}
