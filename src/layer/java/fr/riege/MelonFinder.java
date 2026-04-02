package fr.riege;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MelonFinder implements ModInitializer {

    public static final String MOD_ID = "melonfinder";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("MelonFinder initialized");
    }
}
