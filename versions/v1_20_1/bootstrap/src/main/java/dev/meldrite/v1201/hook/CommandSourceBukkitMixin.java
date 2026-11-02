package dev.meldrite.v1201.hook;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.CraftEntityCommandSender;
import org.bukkit.craftbukkit.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.command.CraftRemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerCommandSource.class)
public abstract class CommandSourceBukkitMixin implements BukkitBrigadierCommandSource {
    @Shadow @Final private CommandOutput output;

    @Override public Entity getBukkitEntity() {
        var source = (ServerCommandSource) (Object) this;
        return source.getEntity() == null ? null : NativeEntityExtensions.bukkit(source.getEntity());
    }

    @Override public World getBukkitWorld() {
        var source = (ServerCommandSource) (Object) this;
        return source.getWorld() == null ? null : NativeEntityExtensions.world(source.getWorld());
    }

    @Override public Location getBukkitLocation() {
        var source = (ServerCommandSource) (Object) this;
        World world = getBukkitWorld();
        var position = source.getPosition();
        if (world == null || position == null) return null;
        var rotation = source.getRotation();
        return new Location(world, position.x, position.y, position.z,
                rotation == null ? 0 : rotation.y, rotation == null ? 0 : rotation.x);
    }

    @Override public CommandSender getBukkitSender() {
        var source = (ServerCommandSource) (Object) this;
        // Preserve the output owner when an /execute command changes the source entity.
        if (output instanceof net.minecraft.entity.Entity entity) return NativeEntityExtensions.bukkit(entity);
        if (output instanceof MinecraftServer) return Bukkit.getConsoleSender();
        if (output instanceof net.minecraft.server.rcon.RconCommandOutput) return new CraftRemoteConsoleCommandSender(source, output);
        if (output instanceof net.minecraft.world.CommandBlockExecutor block) {
            var owner = block.getSource().getEntity();
            if (owner != null) return NativeEntityExtensions.bukkit(owner);
            if (output instanceof dev.meldrite.v1201.compat.NativeCommandBlockOutput access) {
                return new CraftBlockCommandSender(source, access.meldrite$commandBlock(), output);
            }
        }
        // Mod-provided outputs still receive messages through their real native source.
        return new CraftEntityCommandSender(source, output);
    }
}
