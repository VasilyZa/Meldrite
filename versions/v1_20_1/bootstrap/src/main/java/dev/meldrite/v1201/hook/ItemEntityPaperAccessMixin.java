package dev.meldrite.v1201.hook;

import java.util.UUID;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityPaperAccessMixin {
    @Accessor("itemAge") int meldrite$getItemAge();
    @Accessor("itemAge") void meldrite$setItemAge(int value);
    @Accessor("pickupDelay") int meldrite$getPickupDelay();
    @Accessor("pickupDelay") void meldrite$setPickupDelay(int value);
    @Accessor("health") int meldrite$getHealth();
    @Accessor("health") void meldrite$setHealth(int value);
    @Accessor("owner") UUID meldrite$getOwner();
    @Accessor("owner") void meldrite$setOwner(UUID value);
    @Accessor("thrower") UUID meldrite$getThrower();
    @Accessor("thrower") void meldrite$setThrower(UUID value);
}
