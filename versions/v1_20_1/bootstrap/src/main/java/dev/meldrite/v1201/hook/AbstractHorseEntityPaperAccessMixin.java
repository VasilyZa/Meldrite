package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.SimpleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractHorseEntity.class)
public interface AbstractHorseEntityPaperAccessMixin {
    @Accessor("items") SimpleInventory meldrite$getInventory();
    @Invoker("onChestedStatusChanged") void meldrite$resizeInventory();
    @Invoker("getHorseFlag") boolean meldrite$getHorseFlag(int flag);
    @Invoker("setHorseFlag") void meldrite$setHorseFlag(int flag, boolean value);
    @Accessor("eatingTicks") void meldrite$setEatingTicks(int ticks);
}
