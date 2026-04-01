package fr.riege.layer.adapter;

import fr.riege.api.annotation.Layer;
import fr.riege.api.layer.IWorldLayer;
import fr.riege.api.math.BlockPos;
import fr.riege.api.math.FluidType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

@Layer
public final class FabricWorldLayer implements IWorldLayer {

    private final Level level;

    public FabricWorldLayer(@NotNull Level level) {
        this.level = level;
    }

    @Override
    public boolean isWalkable(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        net.minecraft.core.BlockPos below = mcPos.below();
        return level.getBlockState(mcPos).isAir() && level.getBlockState(below).isSolid();
    }

    @Override
    public boolean isSolid(@NotNull BlockPos pos) {
        net.minecraft.core.BlockPos mcPos = toMc(pos);
        return level.getBlockState(mcPos).isSolid();
    }

    @Override
    public @NotNull FluidType getFluidType(@NotNull BlockPos pos) {
        FluidState state = level.getFluidState(toMc(pos));
        if (state.is(Fluids.WATER) || state.is(Fluids.FLOWING_WATER)) return FluidType.WATER;
        if (state.is(Fluids.LAVA) || state.is(Fluids.FLOWING_LAVA)) return FluidType.LAVA;
        return FluidType.NONE;
    }

    @Override
    public int getLightLevel(@NotNull BlockPos pos) {
        return level.getLightEmission(toMc(pos));
    }

    private @NotNull net.minecraft.core.BlockPos toMc(@NotNull BlockPos pos) {
        return new net.minecraft.core.BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }
}
