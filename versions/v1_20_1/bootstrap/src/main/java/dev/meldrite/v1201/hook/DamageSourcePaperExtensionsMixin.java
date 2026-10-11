package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.PaperDamageSourceAccess;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public abstract class DamageSourcePaperExtensionsMixin implements PaperDamageSourceAccess {
    @Unique private boolean meldrite$sweep, meldrite$melting, meldrite$poison, meldrite$critical;
    public org.bukkit.block.BlockState explodedBlockState;
    @Override public boolean isSweep() { return meldrite$sweep; }
    public DamageSource sweep() { meldrite$sweep = true; return (DamageSource) (Object) this; }
    @Override public boolean isMelting() { return meldrite$melting; }
    public DamageSource melting() { meldrite$melting = true; return (DamageSource) (Object) this; }
    @Override public boolean isPoison() { return meldrite$poison; }
    public DamageSource poison() { meldrite$poison = true; return (DamageSource) (Object) this; }
    @Override public void meldrite$setPoison() { meldrite$poison = true; }
    @Override public boolean isCritical() { return meldrite$critical; }
    public DamageSource critical() { return critical(true); }
    public DamageSource critical(boolean value) { meldrite$critical = value; return (DamageSource) (Object) this; }
    @Override public org.bukkit.block.BlockState meldrite$getExplodedBlockState() { return explodedBlockState; }
}
