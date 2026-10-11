package dev.meldrite.v1201.compat;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
public final class NativeItemFrameItems {
    private static final ThreadLocal<Boolean> SOUND = new ThreadLocal<>();
    private NativeItemFrameItems() {}
    public static void set(ItemFrameEntity frame, ItemStack item, boolean sound) {
        Boolean previous = SOUND.get(); SOUND.set(sound);
        try { frame.setHeldItemStack(item); } finally { if (previous == null) SOUND.remove(); else SOUND.set(previous); }
    }
    public static boolean playSound() { return !Boolean.FALSE.equals(SOUND.get()); }
}
