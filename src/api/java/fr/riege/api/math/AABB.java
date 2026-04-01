package fr.riege.api.math;

import org.jetbrains.annotations.NotNull;

public record AABB(Vec3 min, Vec3 max) {

    public boolean intersects(@NotNull AABB other) {
        boolean noOverlapX = this.max.x() <= other.min.x() || other.max.x() <= this.min.x();
        boolean noOverlapY = this.max.y() <= other.min.y() || other.max.y() <= this.min.y();
        boolean noOverlapZ = this.max.z() <= other.min.z() || other.max.z() <= this.min.z();
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
