package fr.riege.pathfinder.registry;

import fr.riege.api.registry.RegistryKey;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegistryKeyTest {
    @Test void parse_validKey_returnsKey() {
        RegistryKey key = RegistryKey.of("fr.riege:walk");
        assertEquals("fr.riege", key.getNamespace());
        assertEquals("walk", key.getPath());
    }
    @Test void equals_sameKey_returnsTrue() {
        assertEquals(RegistryKey.of("fr.riege:walk"), RegistryKey.of("fr.riege:walk"));
    }
}
