package dev.meldrite.v1201.compat;

import net.minecraft.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

/** Paper mob state and target dispatch carried by each real native mob. */
public interface MobNativeAccess {
    boolean meldrite$isAware();
    void meldrite$setAware(boolean aware);
    boolean meldrite$setTarget(LivingEntity target, EntityTargetEvent.TargetReason reason, boolean fireEvent);
}
