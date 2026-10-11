package dev.meldrite.v1201.hook;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Enchantment.class)
public interface EnchantmentSlotsAccessor {
    @Accessor("slotTypes") EquipmentSlot[] meldrite$getSlotTypes();
}
