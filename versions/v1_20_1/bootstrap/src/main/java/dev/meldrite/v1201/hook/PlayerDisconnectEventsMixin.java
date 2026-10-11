package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.meldrite.v1201.compat.NativePlayerSessionEvents;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class PlayerDisconnectEventsMixin {
    @Shadow public ServerPlayerEntity player;
    @WrapMethod(method = "onDisconnected")
    private void meldrite$disconnectEvent(Text reason, Operation<Void> original) {
        NativePlayerSessionEvents.disconnect(player, () -> original.call(reason));
    }
}
