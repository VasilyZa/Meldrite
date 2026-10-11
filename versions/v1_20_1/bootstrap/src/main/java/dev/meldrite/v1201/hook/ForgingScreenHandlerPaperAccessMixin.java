package dev.meldrite.v1201.hook;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ForgingScreenHandler.class)
public interface ForgingScreenHandlerPaperAccessMixin { @Accessor("context") ScreenHandlerContext meldrite$getContext(); @Accessor("player") PlayerEntity meldrite$getPlayer(); }
