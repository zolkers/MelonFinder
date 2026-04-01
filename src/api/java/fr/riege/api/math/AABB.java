package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

public final class AABB {

    private final Vec3 min;
    private final Vec3 max;

    public AABB(@NotNull Vec3 min, @NotNull Vec3 max) {
        this.min = min;
        this.max = max;
    }

    @NotNull
    public Vec3 getMin() {
        return min;
    }

    @NotNull
    public Vec3 getMax() {
        return max;
    }

    public boolean intersects(@NotNull AABB other) {
        boolean noOverlapX = this.max.getX() <= other.min.getX() || other.max.getX() <= this.min.getX();
        boolean noOverlapY = this.max.getY() <= other.min.getY() || other.max.getY() <= this.min.getY();
        boolean noOverlapZ = this.max.getZ() <= other.min.getZ() || other.max.getZ() <= this.min.getZ();
        return !(noOverlapX || noOverlapY || noOverlapZ);
    }

    @NotNull
    public AABB expand(double amount) {
        Vec3 newMin = min.add(-amount, -amount, -amount);
        Vec3 newMax = max.add(amount, amount, amount);
        return new AABB(newMin, newMax);
    }

    @NotNull
    public AABB offset(@NotNull Vec3 delta) {
        Vec3 newMin = min.add(delta.getX(), delta.getY(), delta.getZ());
        Vec3 newMax = max.add(delta.getX(), delta.getY(), delta.getZ());
        return new AABB(newMin, newMax);
    }

    @Override
    public String toString() {
        return "AABB{min=" + min + ", max=" + max + "}";
    }
}
