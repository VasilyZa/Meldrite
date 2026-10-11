package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.BatEntityPaperAccessMixin;
import net.minecraft.entity.passive.BatEntity;
import org.bukkit.Location;
public class CraftBat extends CraftAmbient implements org.bukkit.entity.Bat {
    public CraftBat(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public BatEntity getHandle() { return (BatEntity) super.getHandle(); }
    @Override public boolean isAwake() { return !getHandle().isRoosting(); }
    @Override public void setAwake(boolean awake) { requireWritable(); getHandle().setRoosting(!awake); }
    @Override public Location getTargetLocation() { var pos = ((BatEntityPaperAccessMixin) getHandle()).meldrite$target(); return pos == null ? null : new Location(getWorld(), pos.getX(), pos.getY(), pos.getZ()); }
    @Override public void setTargetLocation(Location location) { requireWritable(); ((BatEntityPaperAccessMixin) getHandle()).meldrite$target(location == null ? null : new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())); }
}
