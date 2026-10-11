package dev.meldrite.v1201.compat;

import net.minecraft.item.ItemStack;

/** Paper state and private native fields exposed for the 1.20.1 trident facade. */
public interface TridentNativeAccess {
    ItemStack meldrite$getTridentStack();
    void meldrite$setTridentStack(ItemStack stack);
    boolean meldrite$hasDealtDamage();
    void meldrite$setDealtDamage(boolean value);
    boolean meldrite$hasGlint();
    void meldrite$setGlint(boolean value);
    int meldrite$getLoyalty();
    void meldrite$setLoyalty(int value);
}
