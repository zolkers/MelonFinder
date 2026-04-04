package fr.riege.pathfinder.astar;

import fr.riege.api.math.BlockPos;
import fr.riege.api.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

record NeighborMove(@NotNull BlockPos to, @NotNull RegistryKey movementKey, double edgeCost) {}
