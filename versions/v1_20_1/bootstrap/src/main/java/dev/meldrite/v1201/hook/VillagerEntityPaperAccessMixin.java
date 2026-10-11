package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VillagerEntity.class)
public interface VillagerEntityPaperAccessMixin {
    @Accessor("restocksToday") int meldrite$getRestocksToday();
    @Accessor("restocksToday") void meldrite$setRestocksToday(int restocks);
    @Invoker("levelUp") void meldrite$levelUp();
    @Invoker("sayNo") void meldrite$shakeHead();
    @Invoker("releaseAllTickets") void meldrite$releaseAllTickets();
}
