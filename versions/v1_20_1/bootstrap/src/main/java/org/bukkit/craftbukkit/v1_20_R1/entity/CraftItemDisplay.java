package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.ItemDisplayEntityPaperAccessMixin;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
public class CraftItemDisplay extends CraftDisplay implements org.bukkit.entity.ItemDisplay {
    public CraftItemDisplay(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity getHandle() { return (net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity) super.getHandle(); }
    private ItemDisplayEntityPaperAccessMixin access() { return (ItemDisplayEntityPaperAccessMixin) getHandle(); }
    @Override public org.bukkit.inventory.ItemStack getItemStack() { return CraftItemStack.asBukkitCopy(access().meldrite$item()); }
    @Override public void setItemStack(org.bukkit.inventory.ItemStack value) { delegate.requireWritable(); access().meldrite$item(CraftItemStack.asNMSCopy(value)); }
    @Override public ItemDisplayTransform getItemDisplayTransform() { return ItemDisplayTransform.values()[access().meldrite$mode().getIndex()]; }
    @Override public void setItemDisplayTransform(ItemDisplayTransform value) { java.util.Objects.requireNonNull(value, "transform"); delegate.requireWritable(); access().meldrite$mode(net.minecraft.client.render.model.json.ModelTransformationMode.FROM_INDEX.apply(value.ordinal())); }
}
