package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeTraderState;
import net.minecraft.entity.passive.WanderingTraderEntity;
import org.bukkit.Location;
public class CraftWanderingTrader extends CraftAbstractVillager implements org.bukkit.entity.WanderingTrader {
    public CraftWanderingTrader(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WanderingTraderEntity getHandle() { return (WanderingTraderEntity) super.getHandle(); }
    private NativeTraderState state() { return (NativeTraderState) getHandle(); }
    @Override public int getDespawnDelay() { return getHandle().getDespawnDelay(); }
    @Override public void setDespawnDelay(int ticks) { requireWritable(); getHandle().setDespawnDelay(ticks); }
    @Override public boolean canDrinkPotion() { return state().meldrite$canDrinkPotion(); }
    @Override public void setCanDrinkPotion(boolean value) { requireWritable(); state().meldrite$canDrinkPotion(value); }
    @Override public boolean canDrinkMilk() { return state().meldrite$canDrinkMilk(); }
    @Override public void setCanDrinkMilk(boolean value) { requireWritable(); state().meldrite$canDrinkMilk(value); }
    @Override public Location getWanderingTowards() { var pos = ((dev.meldrite.v1201.hook.WanderingTraderEntityPaperAccessMixin) getHandle()).meldrite$wanderTarget(); return pos == null ? null : new Location(getWorld(), pos.getX(), pos.getY(), pos.getZ()); }
    @Override public void setWanderingTowards(Location location) { requireWritable(); getHandle().setWanderTarget(location == null ? null : new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())); }
}
