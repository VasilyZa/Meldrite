package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.FoxEntity;

public class CraftFox extends CraftAnimals implements org.bukkit.entity.Fox {
    public CraftFox(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public FoxEntity getHandle() { return (FoxEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin foxState() { return (dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin) getHandle(); }
    @Override public Type getFoxType() { return Type.values()[getHandle().getVariant().ordinal()]; }
    @Override public void setFoxType(Type type) { requireWritable(); getHandle().setVariant(FoxEntity.Type.values()[java.util.Objects.requireNonNull(type, "type").ordinal()]); }
    @Override public boolean isCrouching() { return getHandle().isInSneakingPose(); }
    @Override public void setCrouching(boolean flag) { requireWritable(); getHandle().setCrouching(flag); }
    @Override public boolean isSitting() { return getHandle().isSitting(); }
    @Override public void setSitting(boolean flag) { requireWritable(); getHandle().setSitting(flag); }
    @Override public boolean isSleeping() { return getHandle().isSleeping(); }
    @Override public void setSleeping(boolean flag) { requireWritable(); foxState().meldrite$sleeping(flag); }
    private org.bukkit.entity.AnimalTamer trusted(net.minecraft.entity.data.TrackedData<java.util.Optional<java.util.UUID>> data) {
        var uuid = getHandle().getDataTracker().get(data).orElse(null); if (uuid == null) return null;
        var player = getServer().getPlayer(uuid); return player == null ? getServer().getOfflinePlayer(uuid) : player;
    }
    @Override public org.bukkit.entity.AnimalTamer getFirstTrustedPlayer() { return trusted(dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin.meldrite$firstTrusted()); }
    @Override public org.bukkit.entity.AnimalTamer getSecondTrustedPlayer() { return trusted(dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin.meldrite$secondTrusted()); }
    @Override public void setFirstTrustedPlayer(org.bukkit.entity.AnimalTamer player) {
        requireWritable(); var tracker = getHandle().getDataTracker();
        if (player == null && tracker.get(dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin.meldrite$secondTrusted()).isPresent()) throw new IllegalStateException("Must remove second trusted player first");
        tracker.set(dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin.meldrite$firstTrusted(), player == null ? java.util.Optional.empty() : java.util.Optional.of(player.getUniqueId()));
    }
    @Override public void setSecondTrustedPlayer(org.bukkit.entity.AnimalTamer player) {
        requireWritable(); var tracker = getHandle().getDataTracker();
        if (player != null && tracker.get(dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin.meldrite$firstTrusted()).isEmpty()) throw new IllegalStateException("Must add first trusted player first");
        tracker.set(dev.meldrite.v1201.hook.FoxEntityPaperAccessMixin.meldrite$secondTrusted(), player == null ? java.util.Optional.empty() : java.util.Optional.of(player.getUniqueId()));
    }
    @Override public boolean isFaceplanted() { return getHandle().isWalking(); }
    @Override public void setFaceplanted(boolean flag) { requireWritable(); foxState().meldrite$faceplanted(flag); }
    @Override public boolean isInterested() { return getHandle().isRollingHead(); }
    @Override public void setInterested(boolean flag) { requireWritable(); getHandle().setRollingHead(flag); }
    @Override public boolean isLeaping() { return getHandle().isChasing(); }
    @Override public void setLeaping(boolean flag) { requireWritable(); getHandle().setChasing(flag); }
    @Override public boolean isDefending() { return foxState().meldrite$isDefending(); }
    @Override public void setDefending(boolean flag) { requireWritable(); foxState().meldrite$defending(flag); }
}
