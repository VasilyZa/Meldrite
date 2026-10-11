package dev.meldrite.v1201.hook;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Official 1.20.1 NBT entry points used by legacy plugin reflection. */
@Mixin(NbtCompound.class)
public abstract class LegacyNbtCompoundAliasesMixin {
    public String m_() { return ((NbtCompound) (Object) this).asString(); }
    public byte b() { return ((NbtCompound) (Object) this).getType(); }
    public NbtElement d() { return ((NbtCompound) (Object) this).copy(); }
    public Set<String> e() { return ((NbtCompound) (Object) this).getKeys(); }
    public int f() { return ((NbtCompound) (Object) this).getSize(); }
    public boolean g() { return ((NbtCompound) (Object) this).isEmpty(); }
    public NbtCompound h() { return ((NbtCompound) (Object) this).copy(); }

    public UUID a(String key) { return ((NbtCompound) (Object) this).getUuid(key); }
    public boolean b(String key) { return ((NbtCompound) (Object) this).containsUuid(key); }
    public boolean b(String key, int type) { return ((NbtCompound) (Object) this).contains(key, type); }
    public NbtElement c(String key) { return ((NbtCompound) (Object) this).get(key); }
    public NbtList c(String key, int elementType) { return ((NbtCompound) (Object) this).getList(key, elementType); }
    public byte d(String key) { return ((NbtCompound) (Object) this).getType(key); }
    public boolean e(String key) { return ((NbtCompound) (Object) this).contains(key); }
    public boolean hasKey(String key) { return ((NbtCompound) (Object) this).contains(key); }
    public byte f(String key) { return ((NbtCompound) (Object) this).getByte(key); }
    public short g(String key) { return ((NbtCompound) (Object) this).getShort(key); }
    public int h(String key) { return ((NbtCompound) (Object) this).getInt(key); }
    public long i(String key) { return ((NbtCompound) (Object) this).getLong(key); }
    public float j(String key) { return ((NbtCompound) (Object) this).getFloat(key); }
    public double k(String key) { return ((NbtCompound) (Object) this).getDouble(key); }
    public String l(String key) { return ((NbtCompound) (Object) this).getString(key); }
    public byte[] m(String key) { return ((NbtCompound) (Object) this).getByteArray(key); }
    public int[] n(String key) { return ((NbtCompound) (Object) this).getIntArray(key); }
    public long[] o(String key) { return ((NbtCompound) (Object) this).getLongArray(key); }
    public NbtCompound p(String key) { return ((NbtCompound) (Object) this).getCompound(key); }
    public boolean q(String key) { return ((NbtCompound) (Object) this).getBoolean(key); }

    /**
     * Legacy optional getter used by recent Paper and plugin NBT code.  NBT
     * booleans are byte tags; a present tag of another type must use the
     * supplied fallback just like a missing tag.
     */
    public boolean getBooleanOr(String key, boolean fallback) {
        NbtCompound compound = (NbtCompound) (Object) this;
        return compound.contains(key, NbtElement.BYTE_TYPE) ? compound.getBoolean(key) : fallback;
    }

    /** Returns the stored compound, or a fresh empty compound for non-compound values. */
    public NbtCompound getCompoundOrEmpty(String key) {
        NbtElement value = ((NbtCompound) (Object) this).get(key);
        return value instanceof NbtCompound compound ? compound : new NbtCompound();
    }

    /** Returns the stored list without element filtering, or a fresh empty list. */
    public NbtList getListOrEmpty(String key) {
        NbtElement value = ((NbtCompound) (Object) this).get(key);
        return value instanceof NbtList list ? list : new NbtList();
    }

    public void r(String key) { ((NbtCompound) (Object) this).remove(key); }

    /** Legacy Mojang signature that returns the tag removed by the native mutator. */
    public NbtElement remove(String key) {
        return dev.meldrite.v1201.compat.NativeNbtIo.remove((NbtCompound) (Object) this, key);
    }

    public int getIntOr(String key, int fallback) {
        return ((NbtCompound) (Object) this).contains(key) ? ((NbtCompound) (Object) this).getInt(key) : fallback;
    }
    public long getLongOr(String key, long fallback) {
        return ((NbtCompound) (Object) this).contains(key) ? ((NbtCompound) (Object) this).getLong(key) : fallback;
    }
    public void setByte(String key, byte value) { ((NbtCompound) (Object) this).putByte(key, value); }
    public void setInt(String key, int value) { ((NbtCompound) (Object) this).putInt(key, value); }

    public NbtElement a(String key, NbtElement value) { return ((NbtCompound) (Object) this).put(key, value); }
    public NbtElement set(String key, NbtElement value) { return ((NbtCompound) (Object) this).put(key, value); }
    public void a(String key, boolean value) { ((NbtCompound) (Object) this).putBoolean(key, value); }
    public void a(String key, byte value) { ((NbtCompound) (Object) this).putByte(key, value); }
    public void a(String key, short value) { ((NbtCompound) (Object) this).putShort(key, value); }
    public void a(String key, int value) { ((NbtCompound) (Object) this).putInt(key, value); }
    public void a(String key, long value) { ((NbtCompound) (Object) this).putLong(key, value); }
    public void a(String key, float value) { ((NbtCompound) (Object) this).putFloat(key, value); }
    public void a(String key, double value) { ((NbtCompound) (Object) this).putDouble(key, value); }
    public void a(String key, String value) { ((NbtCompound) (Object) this).putString(key, value); }
    public void a(String key, UUID value) { ((NbtCompound) (Object) this).putUuid(key, value); }
    public void a(String key, byte[] value) { ((NbtCompound) (Object) this).putByteArray(key, value); }
    public void a(String key, int[] value) { ((NbtCompound) (Object) this).putIntArray(key, value); }
    public void a(String key, long[] value) { ((NbtCompound) (Object) this).putLongArray(key, value); }
    public void a(String key, List<Byte> value) { ((NbtCompound) (Object) this).putByteArray(key, value); }
    public void b(String key, List<Integer> value) { ((NbtCompound) (Object) this).putIntArray(key, value); }
    public void c(String key, List<Long> value) { ((NbtCompound) (Object) this).putLongArray(key, value); }
    public NbtCompound a(NbtCompound value) { return ((NbtCompound) (Object) this).copyFrom(value); }
}
