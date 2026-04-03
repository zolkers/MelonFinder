package fr.riege.client;

import fr.riege.api.math.BlockPos;
import fr.riege.api.path.Path;
import fr.riege.client.command.GotoCommand;
import fr.riege.client.event.EventBridge;
import fr.riege.client.render.DebugOverlay;
import fr.riege.client.render.MelonFinderMeshRenderer;
import fr.riege.client.render.PathRenderer;
import fr.riege.client.event.MelonFinderEvents;
import fr.riege.api.event.events.PathCompleteEvent;
import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class MelonFinderClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("melonfinder-client");

    private static MelonFinderClient instance;

    private final MelonFinderMeshRenderer meshRenderer = new MelonFinderMeshRenderer();
    private final PathRenderer pathRenderer = new PathRenderer(meshRenderer);
    private final DebugOverlay debugOverlay = new DebugOverlay();

    @Override
    public void onInitializeClient() {
        instance = this;

        GotoCommand.register();
        new EventBridge(MelonFinderEvents.BUS).register();
        pathRenderer.register(MelonFinderEvents.BUS);
        debugOverlay.register(MelonFinderEvents.BUS);

        MelonFinderEvents.BUS.subscribe(PathCompleteEvent.class, this, event ->
            displayPath(event.getResult().path())
        );

        LOGGER.info("MelonFinder client initialized");
    }

    public static void displayPath(@NotNull Path path) {
        if (instance == null) return;
        instance.pathRenderer.setPath(path);
        instance.debugOverlay.setPath(path);
    }

    public static void displayStatus(@Nullable String text) {
        if (instance == null) return;
        instance.debugOverlay.setStatus(text != null ? text : "");
    }

    public static void displayExploredCosts(@NotNull Map<BlockPos, Double> costs) {
        if (instance == null) return;
        instance.debugOverlay.setExploredCosts(costs);
    }
}
