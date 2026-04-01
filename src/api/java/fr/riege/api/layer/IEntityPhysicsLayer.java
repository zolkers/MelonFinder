package fr.riege.api.layer;

public interface IEntityPhysicsLayer {
    double getHitboxWidth();
    double getHitboxHeight();
    double getStepHeight();
    double getJumpVelocity();
    float evaluateFallDamage(int blocks);
    double getSwimSpeed();
    double getSprintMultiplier();
    double getSneakSpeedMultiplier();
}
