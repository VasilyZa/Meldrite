package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.InteractionEntityPaperAccessMixin;
import net.minecraft.entity.decoration.InteractionEntity;
import org.bukkit.entity.Interaction;
public class CraftInteraction extends CraftEntity implements Interaction {
    public CraftInteraction(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public InteractionEntity getHandle() { return (InteractionEntity) super.getHandle(); }
    private InteractionEntityPaperAccessMixin access() { return (InteractionEntityPaperAccessMixin) getHandle(); }
    @Override public float getInteractionWidth() { return access().meldrite$getWidth(); }
    @Override public void setInteractionWidth(float value) { delegate.requireWritable(); access().meldrite$setWidth(value); }
    @Override public float getInteractionHeight() { return access().meldrite$getHeight(); }
    @Override public void setInteractionHeight(float value) { delegate.requireWritable(); access().meldrite$setHeight(value); }
    @Override public boolean isResponsive() { return access().meldrite$getResponse(); }
    @Override public void setResponsive(boolean value) { delegate.requireWritable(); access().meldrite$setResponse(value); }
    @Override public PreviousInteraction getLastAttack() { var action = access().meldrite$attack(); return action == null ? null : new CraftPreviousInteraction(action.player(), action.timestamp()); }
    @Override public PreviousInteraction getLastInteraction() { var action = access().meldrite$interaction(); return action == null ? null : new CraftPreviousInteraction(action.player(), action.timestamp()); }
    private static class CraftPreviousInteraction implements PreviousInteraction {
        private final java.util.UUID uuid; private final long timestamp;
        public CraftPreviousInteraction(java.util.UUID uuid, long timestamp) { this.uuid = uuid; this.timestamp = timestamp; }
        @Override public org.bukkit.OfflinePlayer getPlayer() { return org.bukkit.Bukkit.getOfflinePlayer(uuid); }
        @Override public long getTimestamp() { return timestamp; }
    }
}
