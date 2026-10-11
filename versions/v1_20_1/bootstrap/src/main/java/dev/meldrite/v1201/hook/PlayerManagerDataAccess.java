package dev.meldrite.v1201.hook;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(PlayerManager.class)
public interface PlayerManagerDataAccess {
    @Accessor("saveHandler") WorldSaveHandler meldrite$saveHandler();
}
