package dev.meldrite.plugin.classloading;

import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Resolves legacy server names to the actual host classes exposed by native routes. */
public final class LegacyClassLookup {
    @FunctionalInterface public interface MemberNames {
        List<String> resolve(Class<?> owner, String name, boolean method, Class<?>[] parameters, boolean declared);
    }
    private static volatile UnaryOperator<String> classNames = name -> name;
    private static volatile MemberNames memberNames = (owner, name, method, parameters, declared) -> List.of();
    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport", "net.minecraft.class_2777"),
            Map.entry("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket", "net.minecraft.class_2777"),
            Map.entry("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity", "net.minecraft.class_2604"),
            Map.entry("net.minecraft.network.protocol.game.ClientboundAddEntityPacket", "net.minecraft.class_2604"),
            Map.entry("net.minecraft.world.entity.EntityLiving", "net.minecraft.class_1309"),
            Map.entry("net.minecraft.world.entity.LivingEntity", "net.minecraft.class_1309"),
            Map.entry("net.minecraft.world.entity.player.EntityHuman", "net.minecraft.class_1657"),
            Map.entry("net.minecraft.world.entity.player.Player", "net.minecraft.class_1657"),
            Map.entry("net.minecraft.world.entity.AreaEffectCloud", "net.minecraft.class_1295"),
            Map.entry("net.minecraft.world.entity.EntityAreaEffectCloud", "net.minecraft.class_1295"),
            Map.entry("net.minecraft.world.entity.Entity", "net.minecraft.class_1297"),
            Map.entry("net.minecraft.world.item.ItemStack", "net.minecraft.class_1799"),
            Map.entry("net.minecraft.nbt.CompoundTag", "net.minecraft.class_2487"),
            Map.entry("net.minecraft.nbt.NBTTagCompound", "net.minecraft.class_2487"),
            Map.entry("net.minecraft.nbt.NBTBase", "net.minecraft.class_2520"),
            Map.entry("net.minecraft.nbt.NBTTagList", "net.minecraft.class_2499"),
            Map.entry("net.minecraft.world.level.Level", "net.minecraft.class_1937"),
            Map.entry("net.minecraft.world.level.World", "net.minecraft.class_1937"),
            Map.entry("net.minecraft.server.level.ServerLevel", "net.minecraft.class_3218"),
            Map.entry("net.minecraft.server.level.WorldServer", "net.minecraft.class_3218"),
            Map.entry("net.minecraft.server.network.ServerGamePacketListenerImpl", "net.minecraft.class_3244"),
            Map.entry("net.minecraft.server.network.PlayerConnection", "net.minecraft.class_3244"),
            Map.entry("net.minecraft.network.protocol.Packet", "net.minecraft.class_2596"),
            Map.entry("net.minecraft.network.Packet", "net.minecraft.class_2596"),
            Map.entry("net.minecraft.server.level.ServerPlayer", "net.minecraft.class_3222"),
            Map.entry("net.minecraft.server.level.EntityPlayer", "net.minecraft.class_3222"),
            Map.entry("net.minecraft.server.MinecraftServer", "net.minecraft.server.MinecraftServer"),
            Map.entry("org.bukkit.craftbukkit.CraftServer", "org.bukkit.craftbukkit.v1_20_R1.CraftServer"),
            Map.entry("org.bukkit.craftbukkit.CraftWorld", "org.bukkit.craftbukkit.v1_20_R1.CraftWorld"),
            Map.entry("org.bukkit.craftbukkit.entity.CraftPlayer", "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer"),
            Map.entry("org.bukkit.craftbukkit.entity.CraftEntity", "org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity"),
            Map.entry("org.bukkit.craftbukkit.inventory.CraftItemStack", "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack"));

    private LegacyClassLookup() {}

    public static void bindMappings(UnaryOperator<String> names, MemberNames members) {
        classNames = Objects.requireNonNull(names);
        memberNames = Objects.requireNonNull(members);
    }

    public static Field getField(Class<?> owner, String name) throws NoSuchFieldException {
        return field(owner, name, false);
    }

    public static Field getDeclaredField(Class<?> owner, String name) throws NoSuchFieldException {
        return field(owner, name, true);
    }

    private static Field field(Class<?> owner, String name, boolean declared) throws NoSuchFieldException {
        try { return declared ? owner.getDeclaredField(name) : owner.getField(name); }
        catch (NoSuchFieldException original) {
            for (String mapped : memberNames.resolve(owner, name, false, null, declared)) {
                try { return declared ? owner.getDeclaredField(mapped) : owner.getField(mapped); }
                catch (NoSuchFieldException ignored) { }
            }
            throw original;
        }
    }

    public static Method getMethod(Class<?> owner, String name, Class<?>... parameters) throws NoSuchMethodException {
        return method(owner, name, parameters, false);
    }

    public static Method getDeclaredMethod(Class<?> owner, String name, Class<?>... parameters) throws NoSuchMethodException {
        return method(owner, name, parameters, true);
    }

    private static Method method(Class<?> owner, String name, Class<?>[] parameters, boolean declared) throws NoSuchMethodException {
        try { return declared ? owner.getDeclaredMethod(name, parameters) : owner.getMethod(name, parameters); }
        catch (NoSuchMethodException original) {
            for (String mapped : memberNames.resolve(owner, name, true, parameters, declared)) {
                try { return declared ? owner.getDeclaredMethod(mapped, parameters) : owner.getMethod(mapped, parameters); }
                catch (NoSuchMethodException ignored) { }
            }
            throw original;
        }
    }

    public static Class<?> forName(String name) throws ClassNotFoundException {
        return forName(name, true, callerLoader());
    }

    public static Class<?> forName(String name, boolean initialize, ClassLoader requestedLoader)
            throws ClassNotFoundException {
        String mapped = classNames.apply(name);
        if (mapped.equals(name)) mapped = classNames.apply(NAMES.getOrDefault(name, name));
        // The three-argument Class.forName contract treats a null loader as the
        // bootstrap loader.  Only the one-argument compatibility shim should
        // infer the caller's plugin loader.
        ClassLoader loader = requestedLoader;
        if (!mapped.equals(name)) {
            try {
                return Class.forName(mapped, initialize, LegacyClassLookup.class.getClassLoader());
            } catch (ClassNotFoundException ignored) {
                // Continue with the caller loader for plugin-owned aliases.
            }
        }
        try {
            // Preserve Class.forName semantics for plugin-owned and relocated classes,
            // including the bootstrap-only behavior when loader is null.
            return Class.forName(name, initialize, loader);
        } catch (ClassNotFoundException original) {
            if (mapped.equals(name)) {
                throw original;
            }
            try {
                return Class.forName(mapped, initialize, loader);
            } catch (ClassNotFoundException ignored) {
                return Class.forName(mapped, initialize, LegacyClassLookup.class.getClassLoader());
            }
        }
    }

    private static ClassLoader callerLoader() {
        return java.lang.StackWalker.getInstance(java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .filter(frame -> frame.getDeclaringClass() != LegacyClassLookup.class)
                        .findFirst()
                        .map(frame -> frame.getDeclaringClass().getClassLoader())
                        .orElse(LegacyClassLookup.class.getClassLoader()));
    }
}
