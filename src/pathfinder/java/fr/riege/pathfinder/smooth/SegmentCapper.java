package fr.riege.pathfinder.smooth;

import fr.riege.api.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class SegmentCapper {

    private final int maxSegmentLength;

    public SegmentCapper(int maxSegmentLength) {
        this.maxSegmentLength = maxSegmentLength;
    }

    public @NotNull List<BlockPos> cap(@NotNull List<BlockPos> path) {
        if (path.size() <= 1) {
            return new ArrayList<>(path);
        }
        List<BlockPos> result = new ArrayList<>();
        result.add(path.get(0));
        for (int i = 0; i < path.size() - 1; i++) {
            insertMidpoints(path.get(i), path.get(i + 1), result);
            result.add(path.get(i + 1));
        }
        return result;
    }

    private void insertMidpoints(@NotNull BlockPos from, @NotNull BlockPos to,
            @NotNull List<BlockPos> out) {
        if (from.distanceTo(to) <= maxSegmentLength) {
            return;
        }
        BlockPos mid = midpoint(from, to);
        insertMidpoints(from, mid, out);
        out.add(mid);
        insertMidpoints(mid, to, out);
    }

    private @NotNull BlockPos midpoint(@NotNull BlockPos from, @NotNull BlockPos to) {
        int mx = Math.round((from.getX() + to.getX()) / 2.0f);
        int my = Math.round((from.getY() + to.getY()) / 2.0f);
        int mz = Math.round((from.getZ() + to.getZ()) / 2.0f);
        return new BlockPos(mx, my, mz);
    }
}
