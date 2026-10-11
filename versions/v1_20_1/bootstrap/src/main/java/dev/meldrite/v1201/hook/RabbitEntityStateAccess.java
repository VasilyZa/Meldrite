package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.RabbitEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RabbitEntity.class)
public interface RabbitEntityStateAccess {
    @Accessor("moreCarrotTicks") int meldrite$carrotTicks();
    @Accessor("moreCarrotTicks") void meldrite$carrotTicks(int ticks);
}
