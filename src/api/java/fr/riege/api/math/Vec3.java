package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

public final class Vec3 {

    private final double x;
    private final double y;
    private final double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

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

    @NotNull
    public Vec3 subtract(@NotNull Vec3 other) {
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    @NotNull
    public Vec3 normalize() {
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length == 0.0) {
            return new Vec3(0, 0, 0);
        }
        return new Vec3(x / length, y / length, z / length);
    }

    @NotNull
    public Vec3 scale(double factor) {
        return new Vec3(x * factor, y * factor, z * factor);
    }

    @NotNull
    public BlockPos toBlockPos() {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vec3)) {
            return false;
        }
        Vec3 other = (Vec3) obj;
        return Double.compare(x, other.x) == 0
                && Double.compare(y, other.y) == 0
                && Double.compare(z, other.z) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
    }

    @Override
    public String toString() {
        return "Vec3{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
