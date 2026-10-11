package dev.meldrite.v1201.hook;

import com.mojang.authlib.GameProfile;
import dev.meldrite.v1201.compat.PaperPlayerProfileAccess;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerProfileExtensionsMixin implements PaperPlayerProfileAccess {
    @Shadow @Final @Mutable private GameProfile gameProfile;
    @Override public void meldrite$setGameProfile(GameProfile profile) { gameProfile = profile; }
}
