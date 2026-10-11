package dev.meldrite.v1201.hook;

import net.minecraft.advancement.AdvancementManager;
import net.minecraft.server.ServerAdvancementLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the live advancement manager used by the Bukkit dynamic-advancement API. */
@Mixin(ServerAdvancementLoader.class)
public interface ServerAdvancementLoaderAccessMixin {
    @Accessor("manager") AdvancementManager meldrite$getManager();
}
