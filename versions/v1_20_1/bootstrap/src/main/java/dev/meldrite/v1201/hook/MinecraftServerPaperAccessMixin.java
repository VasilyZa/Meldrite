package dev.meldrite.v1201.hook;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ApiServices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerPaperAccessMixin {
    @Accessor("apiServices") ApiServices meldrite$apiServices();
}
