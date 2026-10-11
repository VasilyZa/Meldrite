package dev.meldrite.v1201.hook;

import net.minecraft.nbt.StringNbtReader;
import org.spongepowered.asm.mixin.Mixin;

/** Official 1.20.1 SNBT parser under legacy method names. */
@Mixin(StringNbtReader.class)
public abstract class LegacyStringNbtReaderAliasesMixin {
}
