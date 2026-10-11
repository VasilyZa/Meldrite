package dev.meldrite.v1201.compat;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;

/** The explicit Bukkit profile API does not alter entity UUIDs or replace native players. */
public final class NativePlayerProfiles {
    private NativePlayerProfiles() {}
    public static GameProfile copy(GameProfile current, com.destroystokyo.paper.profile.PlayerProfile value) {
        if (value == null) throw new IllegalArgumentException("Player profile is required");
        var source = com.destroystokyo.paper.profile.CraftPlayerProfile.asAuthlibCopy(value);
        var result = new GameProfile(current.getId(), source.getName() == null ? current.getName() : source.getName());
        result.getProperties().putAll(source.getProperties());
        return result;
    }
    public static void replace(CraftPlayer facade, com.destroystokyo.paper.profile.PlayerProfile value) {
        var player = facade.getHandle();
        var profile = copy(player.getGameProfile(), value);
        if (player.networkHandler == null) {
            ((PaperPlayerProfileAccess) player).meldrite$setGameProfile(profile);
            return;
        }
        var viewers = facade.getServer().getOnlinePlayers().stream()
                .filter(viewer -> viewer.canSee(facade)).filter(CraftPlayer.class::isInstance)
                .map(CraftPlayer.class::cast).toList();
        for (var viewer : viewers) {
            if (viewer == facade) player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket(java.util.List.of(player.getUuid())));
            else NativePlayerVisibility.update(viewer.getHandle(), player, false);
        }
        ((PaperPlayerProfileAccess) player).meldrite$setGameProfile(profile);
        for (var viewer : viewers) {
            if (viewer == facade) player.networkHandler.sendPacket(net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.entryFromPlayer(java.util.List.of(player)));
            else NativePlayerVisibility.update(viewer.getHandle(), player, true);
        }
        refresh(player);
        facade.meldrite$restoreClientState();
    }
    private static void refresh(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        var connection = player.networkHandler;
        connection.sendPacket(new net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket(
                world.getDimensionKey(), world.getRegistryKey(), net.minecraft.world.biome.source.BiomeAccess.hashSeed(world.getSeed()),
                player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(),
                world.isDebugWorld(), world.isFlat(), (byte) 3, player.getLastDeathPos(), player.getPortalCooldown()));
        player.sendAbilitiesUpdate();
        connection.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), java.util.Set.of());
        var manager = player.server.getPlayerManager();
        manager.sendCommandTree(player);
        manager.sendWorldInfo(player, world);
        manager.sendPlayerStatus(player);
        connection.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(player.getInventory().selectedSlot));
        connection.sendPacket(new net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        for (var effect : player.getStatusEffects()) connection.sendPacket(new net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket(player.getId(), effect));
    }
}
