package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

public record AABB(Vec3 min, Vec3 max) {

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

    @Override
    public @NotNull String toString() {
        return "AABB{min=" + min + ", max=" + max + "}";
    }
}
