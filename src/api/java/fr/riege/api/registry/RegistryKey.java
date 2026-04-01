package fr.riege.api.registry;

import org.jetbrains.annotations.NotNull;

public final class RegistryKey {

    private final String namespace;
    private final String path;

    private RegistryKey(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    @NotNull
    public static RegistryKey of(@NotNull String key) {
        int colonIndex = key.indexOf(':');
        if (colonIndex < 0) {
            throw new IllegalArgumentException("Invalid registry key (missing ':'): " + key);
        }
        String namespace = key.substring(0, colonIndex);
        String path = key.substring(colonIndex + 1);
        return new RegistryKey(namespace, path);
    }

    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegistryKey)) {
            return false;
        }
        RegistryKey other = (RegistryKey) obj;
        return namespace.equals(other.namespace) && path.equals(other.path);
    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
