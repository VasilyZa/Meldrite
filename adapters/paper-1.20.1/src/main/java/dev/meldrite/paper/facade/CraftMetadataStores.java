package dev.meldrite.paper.facade;

/** Process-wide stores matching CraftBukkit's metadata ownership semantics. */
public final class CraftMetadataStores {
    public static final org.bukkit.craftbukkit.metadata.BlockMetadataStore BLOCKS = new org.bukkit.craftbukkit.metadata.BlockMetadataStore();
    public static final org.bukkit.craftbukkit.metadata.EntityMetadataStore ENTITIES = new org.bukkit.craftbukkit.metadata.EntityMetadataStore();
    public static final org.bukkit.craftbukkit.metadata.PlayerMetadataStore PLAYERS = new org.bukkit.craftbukkit.metadata.PlayerMetadataStore();
    public static final org.bukkit.craftbukkit.metadata.WorldMetadataStore WORLDS = new org.bukkit.craftbukkit.metadata.WorldMetadataStore();
    private CraftMetadataStores() { }
}
