package dev.meldrite.v1201.hook;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerDamageTiltAccessor {
    @Accessor("damageTiltYaw") void meldrite$setDamageTiltYaw(float yaw);
}
