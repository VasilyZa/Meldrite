package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeChatTypes;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.Registerable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageType.class)
public abstract class NativeChatTypesMixin {
    @Inject(method = "bootstrap", at = @At("TAIL"), require = 1)
    private static void meldrite$registerRawChat(Registerable<MessageType> registry, CallbackInfo callback) {
        NativeChatTypes.bootstrap(registry);
    }
}
