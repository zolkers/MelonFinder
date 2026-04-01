package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

public record Vec3(double x, double y, double z) {

    public double distanceTo(@NotNull Vec3 other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @NotNull
    public Vec3 add(double dx, double dy, double dz) {
        return new Vec3(x + dx, y + dy, z + dz);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vec3(double x1, double y1, double z1))) {
            return false;
        }
        return Double.compare(x, x1) == 0
                && Double.compare(y, y1) == 0
                && Double.compare(z, z1) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
    }

    @Override
    public @NotNull String toString() {
        return "Vec3{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
