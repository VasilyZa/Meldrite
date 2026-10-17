package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeRecipes;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;

/** Paper's recipe-only reload sends native recipes and each player's initial recipe book. */
@Mixin(PlayerManager.class)
public abstract class PlayerManagerRecipeReloadMixin {
    public void reloadRecipeData() {
        NativeRecipes.synchronize((PlayerManager) (Object) this);
    }
}
