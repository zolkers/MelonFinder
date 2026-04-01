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
        result.add(path.getFirst());
        for (int i = 1; i < path.size() - 1; i++) {
            if (isImportantNode(path, i)) {
                result.add(path.get(i));
            }
        }
        result.add(path.getLast());
        return result;
    }

    private boolean isImportantNode(@NotNull List<BlockPos> path, int i) {
        BlockPos prev = path.get(i - 1);
        BlockPos curr = path.get(i);
        BlockPos next = path.get(i + 1);

        int dxIn = curr.x() - prev.x();
        int dzIn = curr.z() - prev.z();
        int dxOut = next.x() - curr.x();
        int dzOut = next.z() - curr.z();
        int dyIn = curr.y() - prev.y();
        int dyOut = next.y() - curr.y();

        boolean horizontalChange = (dxIn != dxOut) || (dzIn != dzOut);
        boolean verticalChange = (dyIn != dyOut);

        return horizontalChange || verticalChange;
    }
}
