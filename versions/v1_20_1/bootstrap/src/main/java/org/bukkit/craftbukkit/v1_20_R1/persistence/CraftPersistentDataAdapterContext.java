package org.bukkit.craftbukkit.v1_20_R1.persistence;

import java.util.Objects;
import org.bukkit.persistence.PersistentDataAdapterContext;

public final class CraftPersistentDataAdapterContext implements PersistentDataAdapterContext {
    private final CraftPersistentDataTypeRegistry registry;

    public CraftPersistentDataAdapterContext(CraftPersistentDataTypeRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    @Override public CraftPersistentDataContainer newPersistentDataContainer() {
        return new CraftPersistentDataContainer(registry);
    }
}
