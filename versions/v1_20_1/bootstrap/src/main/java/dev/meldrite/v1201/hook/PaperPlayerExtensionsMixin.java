package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public abstract class PaperPlayerExtensionsMixin implements dev.meldrite.v1201.compat.PaperPlayerAccess {
    @org.spongepowered.asm.mixin.Unique private dev.meldrite.v1201.compat.NativePlayerState meldrite$playerState = new dev.meldrite.v1201.compat.NativePlayerState();
    @Override public dev.meldrite.v1201.compat.NativePlayerState meldrite$playerState() { return meldrite$playerState; }
    @org.spongepowered.asm.mixin.injection.Inject(method = "copyFrom", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void meldrite$copySession(ServerPlayerEntity oldPlayer, boolean alive, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        meldrite$playerState = ((dev.meldrite.v1201.compat.PaperPlayerAccess) oldPlayer).meldrite$playerState();
        ((dev.meldrite.v1201.compat.PaperLivingPickupAccess) (Object) this).meldrite$setPickupOverride(
                ((dev.meldrite.v1201.compat.PaperLivingPickupAccess) oldPlayer).meldrite$getPickupOverride());
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "setClientSettings", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void meldrite$clientSettings(net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket settings, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        meldrite$playerState.locale = settings.language();
        meldrite$playerState.clientViewDistance = settings.viewDistance();
        meldrite$playerState.skinParts = settings.playerModelBitMask();
    }
    @org.spongepowered.asm.mixin.injection.Inject(method = "readCustomDataFromNbt", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void meldrite$readSession(net.minecraft.nbt.NbtCompound nbt, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        var bukkit = nbt.getCompound("bukkit"); var paper = nbt.getCompound("Paper");
        meldrite$playerState.playedBefore = !nbt.isEmpty();
        if (bukkit.contains("firstPlayed")) meldrite$playerState.firstPlayed = bukkit.getLong("firstPlayed");
        if (bukkit.contains("lastPlayed")) meldrite$playerState.lastPlayed = bukkit.getLong("lastPlayed");
        if (paper.contains("LastSeen")) meldrite$playerState.lastSeen = paper.getLong("LastSeen");
    }
    @org.spongepowered.asm.mixin.injection.Inject(method = "writeCustomDataToNbt", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void meldrite$writeSession(net.minecraft.nbt.NbtCompound nbt, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        var bukkit = nbt.getCompound("bukkit"); var paper = nbt.getCompound("Paper");
        bukkit.putLong("firstPlayed", meldrite$playerState.firstPlayed);
        bukkit.putLong("lastPlayed", System.currentTimeMillis());
        bukkit.putString("lastKnownName", ((ServerPlayerEntity) (Object) this).getGameProfile().getName());
        paper.putLong("LastLogin", meldrite$playerState.lastLogin); paper.putLong("LastSeen", System.currentTimeMillis());
        nbt.put("bukkit", bukkit); nbt.put("Paper", paper);
    }
    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer) NativeEntityExtensions.bukkit((ServerPlayerEntity) (Object) this);
    }
}
