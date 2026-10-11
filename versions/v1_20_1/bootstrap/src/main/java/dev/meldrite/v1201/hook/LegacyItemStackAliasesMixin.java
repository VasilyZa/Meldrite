package dev.meldrite.v1201.hook;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Keeps the official 1.20.1 ItemStack NBT entry points available to
 * libraries that were compiled against the obfuscated server jar.
 */
@Mixin(ItemStack.class)
public abstract class LegacyItemStackAliasesMixin {
    /** Official getNbt() */
    public NbtCompound v() {
        return ((ItemStack) (Object) this).getNbt();
    }

    public NbtCompound s() { return ((ItemStack) (Object) this).getNbt(); }
    public NbtCompound t() { return ((ItemStack) (Object) this).getNbt(); }

    /** Official hasNbt() */
    public boolean u() {
        return ((ItemStack) (Object) this).hasNbt();
    }

    /** Official getOrCreateNbt() */
    public NbtCompound w() {
        return ((ItemStack) (Object) this).getOrCreateNbt();
    }

    public void c(NbtCompound nbt) {
        ((ItemStack) (Object) this).setNbt(nbt);
    }

    public NbtCompound b(NbtCompound nbt) {
        return ((ItemStack) (Object) this).writeNbt(nbt);
    }
}
