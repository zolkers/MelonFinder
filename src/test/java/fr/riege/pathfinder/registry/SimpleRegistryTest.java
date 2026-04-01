package fr.riege.pathfinder.registry;

import fr.riege.api.registry.RegistryKey;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleRegistryTest {
    @Test void register_thenGet_returnsValue() {
        SimpleRegistry<String> r = new SimpleRegistry<>();
        RegistryKey key = RegistryKey.of("fr.riege:test");
        r.register(key, "hello");
        assertEquals("hello", r.get(key).orElseThrow());
    }
    @Test void get_unknownKey_returnsEmpty() {
        assertTrue(new SimpleRegistry<String>().get(RegistryKey.of("fr.riege:missing")).isEmpty());
    }
    @Test void register_duplicateKey_throws() {
        SimpleRegistry<String> r = new SimpleRegistry<>();
        RegistryKey key = RegistryKey.of("fr.riege:dup");
        r.register(key, "first");
        assertThrows(IllegalArgumentException.class, () -> r.register(key, "second"));
    }
}
