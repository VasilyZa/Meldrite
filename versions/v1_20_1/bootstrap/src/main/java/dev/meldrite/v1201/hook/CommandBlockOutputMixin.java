package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeCommandBlockOutput;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.block.entity.CommandBlockBlockEntity$1")
public abstract class CommandBlockOutputMixin implements NativeCommandBlockOutput {
    @Shadow @Final private CommandBlockBlockEntity field_11921;

    @Override public CommandBlockBlockEntity meldrite$commandBlock() { return field_11921; }
}
