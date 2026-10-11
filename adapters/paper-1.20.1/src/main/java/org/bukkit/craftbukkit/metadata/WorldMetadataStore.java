package org.bukkit.craftbukkit.metadata;

import org.bukkit.World;
import org.bukkit.metadata.MetadataStoreBase;

public class WorldMetadataStore extends MetadataStoreBase<World> {
    @Override protected String disambiguate(World world, String key) { return world.getUID() + ":" + key; }
}
