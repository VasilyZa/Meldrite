package org.bukkit.craftbukkit.v1_20_R1.block;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;

public final class CraftSkull extends CraftBlockEntityState<SkullBlockEntity> implements Skull {
    public CraftSkull(ServerWorld world, BlockPos position, BlockState state, SkullBlockEntity skull, boolean useSnapshot) {
        super(world, position, state, skull, useSnapshot);
    }

    private GameProfile profile() { return snapshot.getOwner(); }

    @Override public boolean hasOwner() { return profile() != null; }
    @Override public String getOwner() { return profile() == null ? null : profile().getName(); }

    @Override public boolean setOwner(String name) {
        if (name == null || name.length() > 16) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
        if (player == null) return false;
        snapshot.setOwner(new GameProfile(player.getUniqueId(), player.getName()));
        return true;
    }

    @Override public OfflinePlayer getOwningPlayer() {
        GameProfile owner = profile();
        if (owner == null) return null;
        if (owner.getId() != null) return Bukkit.getOfflinePlayer(owner.getId());
        return owner.getName() == null ? null : Bukkit.getOfflinePlayer(owner.getName());
    }

    @Override public void setOwningPlayer(OfflinePlayer player) {
        Objects.requireNonNull(player, "player");
        snapshot.setOwner(new GameProfile(player.getUniqueId(), player.getName()));
    }

    @Override public void setPlayerProfile(PlayerProfile playerProfile) {
        Objects.requireNonNull(playerProfile, "playerProfile");
        snapshot.setOwner(toGameProfile(playerProfile));
    }

    @Override public PlayerProfile getPlayerProfile() {
        GameProfile owner = profile();
        if (owner == null) return null;
        PlayerProfile result = Bukkit.createProfile(owner.getId(), owner.getName());
        owner.getProperties().values().forEach(property -> result.setProperty(new ProfileProperty(
                property.getName(), property.getValue(), property.getSignature())));
        return result;
    }

    @Override public org.bukkit.profile.PlayerProfile getOwnerProfile() { return getPlayerProfile(); }

    @Override public void setOwnerProfile(org.bukkit.profile.PlayerProfile ownerProfile) {
        snapshot.setOwner(ownerProfile == null ? null : toGameProfile(ownerProfile));
    }

    private static GameProfile toGameProfile(org.bukkit.profile.PlayerProfile source) {
        GameProfile result = new GameProfile(source.getUniqueId(), source.getName());
        if (source instanceof PlayerProfile paperProfile) {
            for (ProfileProperty property : paperProfile.getProperties()) {
                result.getProperties().put(property.getName(), new Property(
                        property.getName(), property.getValue(), property.getSignature()));
            }
        }
        return result;
    }

    @Override public NamespacedKey getNoteBlockSound() {
        var sound = snapshot.getNoteBlockSound();
        return sound == null ? null : CraftNamespacedKey.fromMinecraft(sound);
    }

    @Override public void setNoteBlockSound(NamespacedKey sound) {
        net.minecraft.nbt.NbtCompound nbt = snapshot.createNbt();
        if (sound == null) nbt.remove(SkullBlockEntity.NOTE_BLOCK_SOUND_KEY);
        else nbt.putString(SkullBlockEntity.NOTE_BLOCK_SOUND_KEY, CraftNamespacedKey.toMinecraft(sound).toString());
        snapshot.readNbt(nbt);
    }

    @Override public BlockFace getRotation() {
        BlockData data = getBlockData();
        return data instanceof Rotatable rotatable ? rotatable.getRotation() : ((Directional) data).getFacing();
    }

    @Override public void setRotation(BlockFace rotation) {
        Objects.requireNonNull(rotation, "rotation");
        BlockData data = getBlockData();
        if (data instanceof Rotatable rotatable) rotatable.setRotation(rotation);
        else ((Directional) data).setFacing(rotation);
        setBlockData(data);
    }

    @Override public SkullType getSkullType() {
        return switch (getType()) {
            case SKELETON_SKULL, SKELETON_WALL_SKULL -> SkullType.SKELETON;
            case WITHER_SKELETON_SKULL, WITHER_SKELETON_WALL_SKULL -> SkullType.WITHER;
            case ZOMBIE_HEAD, ZOMBIE_WALL_HEAD -> SkullType.ZOMBIE;
            case PIGLIN_HEAD, PIGLIN_WALL_HEAD -> SkullType.PIGLIN;
            case PLAYER_HEAD, PLAYER_WALL_HEAD -> SkullType.PLAYER;
            case CREEPER_HEAD, CREEPER_WALL_HEAD -> SkullType.CREEPER;
            case DRAGON_HEAD, DRAGON_WALL_HEAD -> SkullType.DRAGON;
            default -> throw new IllegalArgumentException("Unknown skull type for " + getType());
        };
    }

    @Override public void setSkullType(SkullType skullType) {
        throw new UnsupportedOperationException("Change the block type to change skull type");
    }
}
