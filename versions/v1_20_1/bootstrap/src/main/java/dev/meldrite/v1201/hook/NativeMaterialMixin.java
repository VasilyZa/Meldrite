package dev.meldrite.v1201.hook;

import org.spongepowered.asm.mixin.Mixin;

/** The config plugin applies the native registry API projection before Material is defined. */
@Mixin(targets = "org.bukkit.Material", remap = false)
public abstract class NativeMaterialMixin { }
