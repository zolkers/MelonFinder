package fr.riege.layer.adapter;

import fr.riege.api.annotation.Layer;
import fr.riege.api.layer.IEntityPhysicsLayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

@Layer
public final class FabricEntityPhysicsLayer implements IEntityPhysicsLayer {

    private static final double JUMP_VELOCITY = 0.42;
    private static final double STEP_HEIGHT = 0.6;
    private static final double SWIM_SPEED = 0.2;
    private static final double SPRINT_MULTIPLIER = 1.3;
    private static final double SNEAK_MULTIPLIER = 0.3;
    private static final int SAFE_FALL_BLOCKS = 3;

    private final Player player;

    public FabricEntityPhysicsLayer(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public double getHitboxWidth() {
        return player.getBbWidth();
    }

    @Override
    public double getHitboxHeight() {
        return player.getBbHeight();
    }

    @Override
    public double getStepHeight() {
        return STEP_HEIGHT;
    }

    @Override
    public double getJumpVelocity() {
        return JUMP_VELOCITY;
    }

    @Override
    public float evaluateFallDamage(int blocks) {
        int damagingBlocks = blocks - SAFE_FALL_BLOCKS;
        return damagingBlocks > 0 ? (float) damagingBlocks : 0.0f;
    }

    @Override
    public double getSwimSpeed() {
        return SWIM_SPEED;
    }

    @Override
    public double getSprintMultiplier() {
        return SPRINT_MULTIPLIER;
    }

    @Override
    public double getSneakSpeedMultiplier() {
        return SNEAK_MULTIPLIER;
    }
}
