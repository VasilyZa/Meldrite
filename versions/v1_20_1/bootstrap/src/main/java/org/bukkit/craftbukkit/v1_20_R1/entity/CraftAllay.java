package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeAllayState;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.AllayEntityPaperAccessMixin;
import net.minecraft.entity.passive.AllayEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
public class CraftAllay extends CraftCreature implements org.bukkit.entity.Allay {
    public CraftAllay(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AllayEntity getHandle() { return (AllayEntity) super.getHandle(); }
    private AllayEntityPaperAccessMixin access() { return (AllayEntityPaperAccessMixin) getHandle(); }
    @Override public org.bukkit.inventory.Inventory getInventory() { return new CraftInventory(getHandle().getInventory(), this); }
    @Override public boolean canDuplicate() { return access().meldrite$canDuplicateNow(); }
    @Override public void setCanDuplicate(boolean value) { requireWritable(); getHandle().getDataTracker().set(AllayEntityPaperAccessMixin.meldrite$canDuplicate(), value); }
    @Override public long getDuplicationCooldown() { return access().meldrite$cooldown(); }
    @Override public void setDuplicationCooldown(long ticks) { requireWritable(); access().meldrite$cooldown(ticks); }
    @Override public void resetDuplicationCooldown() { requireWritable(); access().meldrite$resetCooldown(); }
    @Override public boolean isDancing() { return getHandle().isDancing(); }
    @Override public void startDancing(Location location) {
        java.util.Objects.requireNonNull(location, "location"); if (location.getBlock().getType() != org.bukkit.Material.JUKEBOX) throw new IllegalArgumentException("Location must contain a jukebox");
        requireWritable(); getHandle().updateJukeboxPos(new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), true);
    }
    @Override public void startDancing() { requireWritable(); ((NativeAllayState) getHandle()).meldrite$forceDancing(true); getHandle().setDancing(true); }
    @Override public void stopDancing() { requireWritable(); ((NativeAllayState) getHandle()).meldrite$forceDancing(false); access().meldrite$jukebox(null); getHandle().setDancing(false); }
    @Override public org.bukkit.entity.Allay duplicateAllay() {
        requireWritable(); var duplicate = dev.meldrite.v1201.compat.NativeAllayBehavior.duplicate(getHandle());
        return duplicate == null ? null : (org.bukkit.entity.Allay) NativeEntityExtensions.bukkit(duplicate);
    }
    @Override public Location getJukebox() { var pos = access().meldrite$jukebox(); return pos == null ? null : new Location(getWorld(), pos.getX(), pos.getY(), pos.getZ()); }
}
