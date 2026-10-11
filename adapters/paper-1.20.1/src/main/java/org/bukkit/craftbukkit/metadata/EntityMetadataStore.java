package org.bukkit.craftbukkit.metadata;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataStoreBase;

public class EntityMetadataStore extends MetadataStoreBase<Entity> {
    @Override protected String disambiguate(Entity entity, String key) { return entity.getUniqueId() + ":" + key; }
}
