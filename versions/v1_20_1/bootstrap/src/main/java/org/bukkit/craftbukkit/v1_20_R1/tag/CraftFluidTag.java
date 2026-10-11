package org.bukkit.craftbukkit.v1_20_R1.tag;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class CraftFluidTag extends CraftTag<Fluid, org.bukkit.Fluid> {
    public CraftFluidTag(Registry<Fluid> registry, TagKey<Fluid> tag) { super(registry, tag); }
    @Override protected Fluid toNative(org.bukkit.Fluid fluid) {
        return registry.getOrEmpty(new Identifier(fluid.getKey().getNamespace(), fluid.getKey().getKey())).orElse(null);
    }
    @Override protected org.bukkit.Fluid toBukkit(Fluid fluid) {
        var id = registry.getId(fluid);
        var value = org.bukkit.Registry.FLUID.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        if (value == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_FLUID_TAG_MEMBER: " + id);
        return value;
    }
}
