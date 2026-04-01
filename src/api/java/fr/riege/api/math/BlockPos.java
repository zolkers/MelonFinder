package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

public record BlockPos(int x, int y, int z) {

    public double distanceTo(@NotNull BlockPos other) {
        double dx = (double) this.x - other.x;
        double dy = (double) this.y - other.y;
        double dz = (double) this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @NotNull
    public BlockPos offset(int dx, int dy, int dz) {
        return new BlockPos(x + dx, y + dy, z + dz);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlockPos(int x1, int y1, int z1))) {
            return false;
        }
        return x == x1 && y == y1 && z == z1;
    }

    @Override
    public @NotNull String toString() {
        return "BlockPos{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
