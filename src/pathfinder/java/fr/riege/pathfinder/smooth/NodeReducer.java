package fr.riege.pathfinder.smooth;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class NodeReducer {

    public @NotNull List<BlockPos> reduce(@NotNull List<BlockPos> path) {
        if (path.size() <= 2) {
            return new ArrayList<>(path);
        }
        List<BlockPos> result = new ArrayList<>();
        result.add(path.get(0));
        for (int i = 1; i < path.size() - 1; i++) {
            if (isImportantNode(path, i)) {
                result.add(path.get(i));
            }
        }
        result.add(path.get(path.size() - 1));
        return result;
    }

    private boolean isImportantNode(@NotNull List<BlockPos> path, int i) {
        BlockPos prev = path.get(i - 1);
        BlockPos curr = path.get(i);
        BlockPos next = path.get(i + 1);

        int dxIn = curr.getX() - prev.getX();
        int dzIn = curr.getZ() - prev.getZ();
        int dxOut = next.getX() - curr.getX();
        int dzOut = next.getZ() - curr.getZ();
        int dyIn = curr.getY() - prev.getY();
        int dyOut = next.getY() - curr.getY();

        boolean horizontalChange = (dxIn != dxOut) || (dzIn != dzOut);
        boolean verticalChange = (dyIn != dyOut);

        return horizontalChange || verticalChange;
    }
}
