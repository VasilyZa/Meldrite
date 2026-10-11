package dev.meldrite.v1201.hook;

import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;

/** Official 1.20.1 NBT string factory under legacy method names. */
@Mixin(NbtString.class)
public abstract class LegacyNbtStringAliasesMixin {
}
