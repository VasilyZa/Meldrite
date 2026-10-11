package dev.meldrite.v1201.hook;

import com.mojang.authlib.GameProfile;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stable ServerPlayer entry points shared by the supported 1.20.1 plugin branches. */
@Mixin(ServerPlayerEntity.class)
public abstract class LegacyServerPlayerAliasesMixin implements dev.meldrite.v1201.compat.PaperPlayerNameAccess {
    public ServerPlayNetworkHandler b;
    public ServerPlayNetworkHandler f;
    public ServerPlayNetworkHandler g;
    public volatile Text listName;
    public volatile String displayName;
    public volatile net.kyori.adventure.text.Component adventure$displayName;
    @Inject(method = "<init>", at = @At("TAIL"))
    private void meldrite$initialDisplayName(org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback) {
        displayName = ((ServerPlayerEntity) (Object) this).getGameProfile().getName();
        adventure$displayName = net.kyori.adventure.text.Component.text(displayName);
    }
    @Override public net.kyori.adventure.text.Component meldrite$getDisplayName() {
        return adventure$displayName == null ? net.kyori.adventure.text.Component.text(((ServerPlayerEntity) (Object) this).getGameProfile().getName()) : adventure$displayName;
    }
    @Override public void meldrite$setDisplayName(net.kyori.adventure.text.Component name, String legacy) {
        adventure$displayName = name;
        displayName = legacy == null ? net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(name) : legacy;
    }
    @Override public void meldrite$setPlayerListName(Text name) { listName = name; }
    @Override public Names meldrite$names() { return new Names(adventure$displayName, displayName, listName); }
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void meldrite$copyPlayerNames(ServerPlayerEntity previous, boolean alive,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback) {
        var names = ((dev.meldrite.v1201.compat.PaperPlayerNameAccess) previous).meldrite$names();
        adventure$displayName = names.display();
        displayName = names.legacyDisplay();
        listName = names.list();
    }

    @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
    private void meldrite$legacyListName(CallbackInfoReturnable<Text> callback) {
        if (listName != null) callback.setReturnValue(listName);
    }

    public ServerWorld y() {
        return ((ServerPlayerEntity) (Object) this).getServerWorld();
    }

    public ServerWorld z() {
        return ((ServerPlayerEntity) (Object) this).getServerWorld();
    }

    public ServerWorld A() {
        return ((ServerPlayerEntity) (Object) this).getServerWorld();
    }

    /** Modern Mojang-mapped name for the player's server level. */
    public ServerWorld level() {
        return ((ServerPlayerEntity) (Object) this).getServerWorld();
    }

    public PlayerAdvancementTracker Q() {
        return ((ServerPlayerEntity) (Object) this).getAdvancementTracker();
    }

    public PlayerAdvancementTracker R() {
        return ((ServerPlayerEntity) (Object) this).getAdvancementTracker();
    }

    public PlayerAdvancementTracker S() {
        return ((ServerPlayerEntity) (Object) this).getAdvancementTracker();
    }

    public PlayerAdvancementTracker U() {
        return ((ServerPlayerEntity) (Object) this).getAdvancementTracker();
    }

    public GameProfile gh() {
        return ((ServerPlayerEntity) (Object) this).getGameProfile();
    }

    public GameProfile gz() {
        return ((ServerPlayerEntity) (Object) this).getGameProfile();
    }

    public GameProfile gr() {
        return ((ServerPlayerEntity) (Object) this).getGameProfile();
    }

    public GameProfile gb() {
        return ((ServerPlayerEntity) (Object) this).getGameProfile();
    }

    public PlayerInventory gj() {
        return ((ServerPlayerEntity) (Object) this).getInventory();
    }

    public PlayerInventory gc() {
        return ((ServerPlayerEntity) (Object) this).getInventory();
    }

    public PlayerInventory gs() {
        return ((ServerPlayerEntity) (Object) this).getInventory();
    }

    public ItemStack a(EquipmentSlot slot) {
        return ((ServerPlayerEntity) (Object) this).getEquippedStack(slot);
    }

    public ItemStack b(EquipmentSlot slot) {
        return ((ServerPlayerEntity) (Object) this).getEquippedStack(slot);
    }

    /** Legacy Paper/NMS teleport shape; native 1.20.1 performs the actual transfer. */
    public boolean teleportTo(ServerWorld world, double x, double y, double z,
            java.util.Set<PositionFlag> flags, float yaw, float pitch, boolean resetCamera,
            org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        return ((ServerPlayerEntity) (Object) this).teleport(world, x, y, z, flags, yaw, pitch);
    }
}
