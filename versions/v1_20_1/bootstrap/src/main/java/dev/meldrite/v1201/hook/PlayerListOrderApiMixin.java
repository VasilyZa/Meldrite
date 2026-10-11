package dev.meldrite.v1201.hook;

import org.spongepowered.asm.mixin.Mixin;

/** The config plugin supplements the public interface before any plugin can link or inspect it. */
@Mixin(targets = "org.bukkit.entity.Player", remap = false)
public interface PlayerListOrderApiMixin { }
