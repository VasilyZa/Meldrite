package dev.meldrite.v1201.hook;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtType;
import org.spongepowered.asm.mixin.Mixin;

/** Base NBT aliases inherited by compound/list/string values. */
@Mixin(NbtElement.class)
public interface LegacyNbtElementAliasesMixin {
    default int a() { return ((NbtElement) (Object) this).getSizeInBytes(); }
    default byte b() { return ((NbtElement) (Object) this).getType(); }
    default NbtType<?> c() { return ((NbtElement) (Object) this).getNbtType(); }
    default NbtElement d() { return ((NbtElement) (Object) this).copy(); }
    default String m_() { return ((NbtElement) (Object) this).asString(); }
}
