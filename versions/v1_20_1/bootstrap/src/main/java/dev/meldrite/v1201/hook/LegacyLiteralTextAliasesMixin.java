package dev.meldrite.v1201.hook;

import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;

/** Installs the legacy String constructor without replacing native text identity. */
@Mixin(MutableText.class)
public abstract class LegacyLiteralTextAliasesMixin { }
