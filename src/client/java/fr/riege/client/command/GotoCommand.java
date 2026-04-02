package fr.riege.client.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.riege.api.goal.BlockGoal;
import fr.riege.api.math.BlockPos;
import fr.riege.api.path.PathResult;
import fr.riege.client.MelonFinderClient;
import fr.riege.client.PathfinderContextFactory;
import fr.riege.api.event.MelonFinderEvents;
import fr.riege.api.event.events.PathCompleteEvent;
import fr.riege.pathfinder.engine.PathfinderContext;
import fr.riege.pathfinder.engine.PathfinderEngine;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GotoCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger("melonfinder-goto");
    private static final PathfinderEngine ENGINE = new PathfinderEngine();

    private GotoCommand() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("goto")
                .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
                    .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
                        .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
                            .executes(GotoCommand::executeGoto))))
                .then(ClientCommandManager.literal("cancel")
                    .executes(GotoCommand::executeCancel))
                .then(ClientCommandManager.literal("status")
                    .executes(GotoCommand::executeStatus)))
        );
    }

    private static int executeGoto(@NotNull CommandContext<FabricClientCommandSource> ctx) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;

        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");

        BlockPos from = new BlockPos(
            (int) Math.floor(player.getX()),
            (int) Math.floor(player.getY()),
            (int) Math.floor(player.getZ())
        );
        BlockPos goal = new BlockPos(x, y, z);

        MelonFinderClient.displayStatus("Computing path to " + x + " " + y + " " + z + "...");
        player.displayClientMessage(Component.literal("[MelonFinder] Computing path to " + x + " " + y + " " + z + "..."), false);

        Thread.ofVirtual().name("melonfinder-pathfinder").start(() -> {
            try {
                PathfinderContext pathCtx = PathfinderContextFactory.create(player);
                PathResult result = ENGINE.compute(from, new BlockGoal(goal), pathCtx);
                MelonFinderEvents.BUS.post(new PathCompleteEvent(result));
                player.displayClientMessage(Component.literal("[MelonFinder] " + result.path().status()), false);
            } catch (Exception e) {
                LOGGER.error("Pathfinder error", e);
                player.displayClientMessage(Component.literal("[MelonFinder] Internal error — see log"), false);
                MelonFinderClient.displayStatus("Error");
            }
        });

        return 1;
    }

    private static int executeCancel(@NotNull CommandContext<FabricClientCommandSource> ctx) {
        ENGINE.cancel();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.literal("[MelonFinder] Path cancelled"), false);
        }
        MelonFinderClient.displayStatus(null);
        return 1;
    }

    private static int executeStatus(@NotNull CommandContext<FabricClientCommandSource> ctx) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;
        String status = ENGINE.isRunning() ? "Computing..." : "Idle";
        player.displayClientMessage(Component.literal("[MelonFinder] Status: " + status), false);
        return 1;
    }
}
