package org.bukkit.craftbukkit.v1_20_R1;

import net.minecraft.sound.BlockSoundGroup;
import org.bukkit.Sound;

/** Paper's sound-only block group projection. */
public final class CraftBlockSoundGroup implements com.destroystokyo.paper.block.BlockSoundGroup {
    private final CraftSoundGroup delegate;
    public CraftBlockSoundGroup(BlockSoundGroup handle) { delegate = CraftSoundGroup.getSoundGroup(handle); }
    @Override public Sound getBreakSound() { return delegate.getBreakSound(); }
    @Override public Sound getStepSound() { return delegate.getStepSound(); }
    @Override public Sound getPlaceSound() { return delegate.getPlaceSound(); }
    @Override public Sound getHitSound() { return delegate.getHitSound(); }
    @Override public Sound getFallSound() { return delegate.getFallSound(); }
}
