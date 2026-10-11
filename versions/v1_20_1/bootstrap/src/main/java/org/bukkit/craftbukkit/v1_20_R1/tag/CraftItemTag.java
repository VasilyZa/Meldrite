package org.bukkit.craftbukkit.v1_20_R1.tag;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.bukkit.Material;

public final class CraftItemTag extends CraftTag<Item, Material> {
    public CraftItemTag(Registry<Item> registry, TagKey<Item> tag) { super(registry, tag); }
    @Override protected Item toNative(Material material) {
        if (material.isLegacy() || !material.isItem()) return null;
        return registry.getOrEmpty(new Identifier(material.getKey().getNamespace(), material.getKey().getKey())).orElse(null);
    }
    @Override protected Material toBukkit(Item item) {
        var id = registry.getId(item);
        var material = dev.meldrite.v1201.compat.NativeMaterialProjection.material(item);
        if (material == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ITEM_TAG_MEMBER: " + id);
        return material;
    }
}
