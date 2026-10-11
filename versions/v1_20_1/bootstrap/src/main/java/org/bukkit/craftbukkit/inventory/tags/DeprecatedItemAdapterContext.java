package org.bukkit.craftbukkit.inventory.tags;

import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagAdapterContext;
import org.bukkit.persistence.PersistentDataAdapterContext;

/** Adapter context preserving the legacy item-tag API on top of persistent data. */
public final class DeprecatedItemAdapterContext implements ItemTagAdapterContext {
    private final PersistentDataAdapterContext context;

    public DeprecatedItemAdapterContext(PersistentDataAdapterContext context) {
        this.context = java.util.Objects.requireNonNull(context, "context");
    }

    @Override public CustomItemTagContainer newTagContainer() {
        return new DeprecatedCustomTagContainer(context.newPersistentDataContainer());
    }
}
