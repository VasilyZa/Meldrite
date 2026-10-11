package dev.meldrite.v1201.hook;

import net.minecraft.entity.ItemEntity;
import net.kyori.adventure.util.TriState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Paper-only item state that has no vanilla 1.20.1 field. */
@Mixin(ItemEntity.class)
public abstract class PaperItemEntityMixin implements dev.meldrite.v1201.compat.PaperItemAccess {
    @Unique private boolean meldrite$canMobPickup = true;
    @Unique private TriState meldrite$frictionState = TriState.NOT_SET;

    @Override public boolean meldrite$canMobPickup() { return meldrite$canMobPickup; }
    @Override public void meldrite$setCanMobPickup(boolean value) { meldrite$canMobPickup = value; }
    @Override public TriState meldrite$getFrictionState() { return meldrite$frictionState; }
    @Override public void meldrite$setFrictionState(TriState value) { meldrite$frictionState = java.util.Objects.requireNonNull(value, "state"); }
}
