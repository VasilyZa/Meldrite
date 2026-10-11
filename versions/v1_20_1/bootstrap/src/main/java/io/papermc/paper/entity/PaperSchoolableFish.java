package io.papermc.paper.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.SchoolingFishEntityPaperAccessMixin;
import net.minecraft.entity.passive.SchoolingFishEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftFish;

public class PaperSchoolableFish extends CraftFish implements SchoolableFish {
    public PaperSchoolableFish(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SchoolingFishEntity getHandle() { return (SchoolingFishEntity) super.getHandle(); }
    private SchoolingFishEntityPaperAccessMixin state() { return (SchoolingFishEntityPaperAccessMixin) getHandle(); }
    @Override public void startFollowing(SchoolableFish fish) {
        java.util.Objects.requireNonNull(fish, "fish");
        if (!(fish instanceof PaperSchoolableFish target)) throw new IllegalArgumentException("Fish must be native-backed");
        requireWritable();
        if (target.getHandle() == getHandle()) throw new IllegalArgumentException("Fish cannot follow itself");
        if (target.getHandle().getWorld() != getHandle().getWorld()) throw new IllegalArgumentException("Fish must be in the same world");
        stopFollowing();
        getHandle().joinGroupOf(target.getHandle());
    }
    @Override public void stopFollowing() { requireWritable(); if (state().meldrite$getLeader() != null) getHandle().leaveGroup(); }
    @Override public int getSchoolSize() { return state().meldrite$getGroupSize(); }
    @Override public int getMaxSchoolSize() { return getHandle().getMaxGroupSize(); }
    @Override public SchoolableFish getSchoolLeader() {
        var leader = state().meldrite$getLeader();
        return leader == null ? null : (SchoolableFish) NativeEntityExtensions.bukkit(leader);
    }
}
