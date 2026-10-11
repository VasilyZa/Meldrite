package dev.meldrite.v1201.hook;

import net.minecraft.nbt.NbtIo;
import org.spongepowered.asm.mixin.Mixin;

/** Marker for static NBT aliases installed by the mixin plugin. */
@Mixin(NbtIo.class)
public abstract class LegacyNbtIoAliasesMixin {
}
