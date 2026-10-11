package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeMerchantAccess;
import dev.meldrite.v1201.hook.MerchantEntityPaperAccessMixin;
import java.util.List;
import net.minecraft.entity.passive.MerchantEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchant;

public class CraftAbstractVillager extends CraftAgeable implements org.bukkit.entity.AbstractVillager {
    public CraftAbstractVillager(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public MerchantEntity getHandle() { return (MerchantEntity) super.getHandle(); }
    private CraftMerchant merchant() { return ((NativeMerchantAccess) getHandle()).getCraftMerchant(); }
    @Override public org.bukkit.inventory.Inventory getInventory() { return new CraftInventory(getHandle().getInventory(), this); }
    @Override public List<MerchantRecipe> getRecipes() { return merchant().getRecipes(); }
    @Override public void setRecipes(List<MerchantRecipe> recipes) { requireWritable(); merchant().setRecipes(recipes); }
    @Override public MerchantRecipe getRecipe(int index) { return merchant().getRecipe(index); }
    @Override public void setRecipe(int index, MerchantRecipe recipe) { requireWritable(); merchant().setRecipe(index, recipe); }
    @Override public int getRecipeCount() { return merchant().getRecipeCount(); }
    @Override public boolean isTrading() { return merchant().isTrading(); }
    @Override public HumanEntity getTrader() { return merchant().getTrader(); }
    @Override public void resetOffers() { requireWritable(); ((MerchantEntityPaperAccessMixin) getHandle()).meldrite$setOffers(null); }
}
