package dev.meldrite.v1201.hook;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;

/** Official 1.20.1 NBT list entry points under legacy method names. */
@Mixin(NbtList.class)
public abstract class LegacyNbtListAliasesMixin {
    public NbtElement k(int index) {
        return ((NbtList) (Object) this).get(index);
    }

    public boolean b(int index, NbtElement value) {
        return ((NbtList) (Object) this).addElement(index, value);
    }

    /** Matches the modern Mojang helper: invalid or non-compound entries yield a fresh empty compound. */
    public net.minecraft.nbt.NbtCompound getCompoundOrEmpty(int index) {
        return ((NbtList) (Object) this).getCompound(index);
    }
}
