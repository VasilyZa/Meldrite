package dev.meldrite.v1201.hook;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.util.UserCache$Entry")
public interface UserCacheEntryPaperAccessMixin {
    @Accessor("profile") GameProfile meldrite$profile();
    @Accessor("lastAccessed") void meldrite$lastAccessed(long value);
}
