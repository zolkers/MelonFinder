package fr.riege.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MelonFinderClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("melonfinder-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("MelonFinder client initialized");
    }
}
