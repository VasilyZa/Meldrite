package org.bukkit.craftbukkit.inventory;

import dev.meldrite.paper.facade.ItemFactoryAdapter;
import dev.meldrite.paper.facade.ItemNbtData;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaBlockState extends CraftMetaItem implements BlockStateMeta {
    private final Material material;
    public CraftMetaBlockState(Material material) { this.material = Objects.requireNonNull(material); }
    private CraftMetaBlockState(CraftMetaBlockState source) { super(source); material = source.material; }
    public Material material() { return material; }
    @Override public boolean hasBlockState() { return mutableTag().get("BlockEntityTag") instanceof Map<?, ?>; }
    @Override public BlockState getBlockState() { return ItemFactoryAdapter.blockState(material, ItemNbtData.copy(ItemNbtData.compound(mutableTag().get("BlockEntityTag")))); }
    @Override public void setBlockState(BlockState state) { value("BlockEntityTag", ItemFactoryAdapter.blockStateData(material, Objects.requireNonNull(state))); }
    @Override public CraftMetaBlockState clone() { return new CraftMetaBlockState(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("TILE_ENTITY"); result.put("blockMaterial", material.name()); return result; }
}
