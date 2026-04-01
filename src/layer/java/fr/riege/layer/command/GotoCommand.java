package fr.riege.layer.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.PathResult;
import fr.riege.api.path.PathStatus;
import fr.riege.layer.PathfinderContextFactory;
import fr.riege.pathfinder.engine.PathfinderContext;
import fr.riege.pathfinder.engine.PathfinderEngine;
import fr.riege.pathfinder.goal.BlockPosGoal;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class GotoCommand {

    private static final PathfinderEngine ENGINE = new PathfinderEngine();

    private GotoCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(GotoCommand::registerCommands);
    }

    private static void registerCommands(
            @NotNull CommandDispatcher<CommandSourceStack> dispatcher,
            @NotNull CommandBuildContext buildContext,
            @NotNull Commands.CommandSelection selection) {
        dispatcher.register(
            Commands.literal("goto")
                .then(Commands.argument("x", IntegerArgumentType.integer())
                    .then(Commands.argument("y", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer())
                            .executes(ctx -> executeGoto(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "x"),
                                IntegerArgumentType.getInteger(ctx, "y"),
                                IntegerArgumentType.getInteger(ctx, "z"))))))
                .then(Commands.literal("cancel")
                    .executes(ctx -> executeCancel(ctx.getSource())))
                .then(Commands.literal("status")
                    .executes(ctx -> executeStatus(ctx.getSource())))
        );
    }

    private static int executeGoto(
            @NotNull CommandSourceStack source, int x, int y, int z) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be a player"));
            return 0;
        }
        BlockPos from = toApiPos(player.blockPosition());
        BlockPosGoal goal = new BlockPosGoal(new BlockPos(x, y, z));
        PathfinderContext ctx = PathfinderContextFactory.create(player);
        PathResult result = ENGINE.compute(from, goal, ctx);
        sendResult(source, result);
        return 1;
    }

    private static int executeCancel(@NotNull CommandSourceStack source) {
        ENGINE.cancel();
        source.sendSuccess(() -> Component.literal("Pathfinder cancelled"), false);
        return 1;
    }

    private static int executeStatus(@NotNull CommandSourceStack source) {
        String status = ENGINE.isRunning() ? "Running" : "Idle";
        source.sendSuccess(() -> Component.literal("Pathfinder: " + status), false);
        return 1;
    }

    private static void sendResult(
            @NotNull CommandSourceStack source, @NotNull PathResult result) {
        PathStatus status = result.getPath().getStatus();
        String message = buildResultMessage(status, result);
        source.sendSuccess(() -> Component.literal(message), false);
    }

    private static @NotNull String buildResultMessage(
            @NotNull PathStatus status, @NotNull PathResult result) {
        return switch (status) {
            case FOUND -> "Path found — " + result.getPath().getSegments().size()
                + " segments, " + result.getNodesExplored() + " nodes explored, "
                + result.getComputeMs() + "ms";
            case UNREACHABLE -> "Destination unreachable";
            case TIMEOUT -> "Search timed out (too far or complex)";
            case CANCELLED -> "Cancelled";
        };
    }

    private static @NotNull BlockPos toApiPos(@NotNull net.minecraft.core.BlockPos pos) {
        return new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    }
}
