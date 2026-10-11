package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.WorldBukkitAccess;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public abstract class PaperWorldExtensionsMixin implements WorldBukkitAccess {
    @Unique private volatile CraftWorld meldrite$bukkitWorld;

    @Override public CraftWorld meldrite$getBukkitWorld() {
        return meldrite$bukkitWorld;
    }

    @Override public void meldrite$setBukkitWorld(CraftWorld world) {
        meldrite$bukkitWorld = java.util.Objects.requireNonNull(world, "world");
    }

    public CraftWorld getWorld() {
        if (!((Object) this instanceof ServerWorld world)) throw new IllegalStateException("NOT_SERVER_WORLD");
        CraftWorld current = meldrite$bukkitWorld;
        if (current == null) {
            current = NativeEntityExtensions.world(world);
            meldrite$bukkitWorld = current;
        }
        return current;
    }
}
