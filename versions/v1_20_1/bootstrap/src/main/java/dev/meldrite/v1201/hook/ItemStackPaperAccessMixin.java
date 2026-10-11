package dev.meldrite.v1201.hook;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface ItemStackPaperAccessMixin {
    @Mutable @Accessor("item") void meldrite$setItem(Item value);
}
