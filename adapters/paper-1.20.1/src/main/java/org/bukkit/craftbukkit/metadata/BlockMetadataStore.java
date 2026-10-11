package org.bukkit.craftbukkit.metadata;

import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataStoreBase;

public class BlockMetadataStore extends MetadataStoreBase<Block> {
    @Override protected String disambiguate(Block block, String key) {
        return block.getWorld().getUID() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ() + ":" + key;
    }
}
