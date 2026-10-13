package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Advertises the distribution through the native brand used by the login payload. */
@Mixin(MinecraftServer.class)
public abstract class ServerBrandMixin {
    @ModifyReturnValue(method = "getServerModName", at = @At("RETURN"))
    private String meldrite$serverBrand(String original) {
        return "Meldrite";
    }
}
