package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.PhantomEntityPaperAccessMixin;
import net.minecraft.entity.mob.PhantomEntity;
import org.bukkit.Location;

public final class CraftPhantom extends CraftMob implements org.bukkit.entity.Phantom, org.bukkit.entity.Enemy {
    private java.util.UUID spawningEntity;
    private boolean burnInDay = true;
    private Location anchor;
    public CraftPhantom(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PhantomEntity getHandle() { return (PhantomEntity) super.getHandle(); }
    private PhantomEntityPaperAccessMixin access() { return (PhantomEntityPaperAccessMixin) (Object) getHandle(); }
    @Override public int getSize() { return access().meldrite$getPhantomSize(); }
    @Override public void setSize(int size) { if (size < 0) throw new IllegalArgumentException("size must be nonnegative"); requireWritable(); access().meldrite$setPhantomSize(size); }
    @Override public java.util.UUID getSpawningEntity() { return spawningEntity; }
    @Override public boolean shouldBurnInDay() { return burnInDay; }
    @Override public void setShouldBurnInDay(boolean value) { requireWritable(); burnInDay = value; }
    @Override public Location getAnchorLocation() { return anchor == null ? null : anchor.clone(); }
    @Override public void setAnchorLocation(Location location) {
        if (location != null) {
            location.checkFinite();
            if (location.getWorld() != getWorld()) throw new IllegalArgumentException("anchor is in a different world");
        }
        requireWritable();
        anchor = location == null ? null : location.clone();
    }
}
