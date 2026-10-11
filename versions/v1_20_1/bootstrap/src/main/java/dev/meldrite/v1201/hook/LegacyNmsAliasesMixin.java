package dev.meldrite.v1201.hook;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Compatibility entry points used by 1.20-era Paper plugins. */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class LegacyNmsAliasesMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void meldrite$syncLegacyConnection(MinecraftServer server,
            net.minecraft.network.ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        setLegacyPlayerField(player, "b");
        setLegacyPlayerField(player, "f");
        setLegacyPlayerField(player, "g");
    }

    private void setLegacyPlayerField(ServerPlayerEntity player, String name) {
        try {
            var field = player.getClass().getField(name);
            field.set(player, this);
        } catch (ReflectiveOperationException ignored) {
            // The field is supplied by LegacyServerPlayerAliasesMixin when this alias is active.
        }
    }

    public void a(Packet<?> packet) {
        ((ServerPlayNetworkHandler) (Object) this).sendPacket(packet);
    }

    public void b(Packet<?> packet) {
        ((ServerPlayNetworkHandler) (Object) this).sendPacket(packet);
    }
}
