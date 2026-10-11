package org.bukkit.craftbukkit.v1_20_R1.tag;

import net.minecraft.block.Block;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.bukkit.Material;

public final class CraftBlockTag extends CraftTag<Block, Material> {
    public CraftBlockTag(Registry<Block> registry, TagKey<Block> tag) { super(registry, tag); }
    @Override protected Block toNative(Material material) {
        if (material.isLegacy() || !material.isBlock()) return null;
        return registry.getOrEmpty(new Identifier(material.getKey().getNamespace(), material.getKey().getKey())).orElse(null);
    }
    @Override protected Material toBukkit(Block block) {
        var id = registry.getId(block);
        var material = dev.meldrite.v1201.compat.NativeMaterialProjection.material(block);
        if (material == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_BLOCK_TAG_MEMBER: " + id);
        return material;
    }
}
