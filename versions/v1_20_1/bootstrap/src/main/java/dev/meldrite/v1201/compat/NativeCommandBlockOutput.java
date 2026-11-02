package dev.meldrite.v1201.compat;

import net.minecraft.block.entity.CommandBlockBlockEntity;

/** The output keeps its original block identity even if that block has been replaced. */
public interface NativeCommandBlockOutput {
    CommandBlockBlockEntity meldrite$commandBlock();
}
