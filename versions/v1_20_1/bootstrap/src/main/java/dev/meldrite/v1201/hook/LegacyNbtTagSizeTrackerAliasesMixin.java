package dev.meldrite.v1201.hook;

import net.minecraft.nbt.NbtTagSizeTracker;
import org.spongepowered.asm.mixin.Mixin;

/** Marker for the modern unlimited NBT tracker factory. */
@Mixin(NbtTagSizeTracker.class)
public abstract class LegacyNbtTagSizeTrackerAliasesMixin {
}
