package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.ScreenHandlerQuickCraftAccess;
import java.util.Set;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerQuickCraftStateMixin implements ScreenHandlerQuickCraftAccess {
    @Override @Accessor("quickCraftButton") public abstract int meldrite$getQuickCraftButton();
    @Override @Accessor("quickCraftButton") public abstract void meldrite$setQuickCraftButton(int value);
    @Override @Accessor("quickCraftStage") public abstract int meldrite$getQuickCraftStage();
    @Override @Accessor("quickCraftStage") public abstract void meldrite$setQuickCraftStage(int value);
    @Override @Accessor("quickCraftSlots") public abstract Set<Slot> meldrite$getQuickCraftSlots();
    @Override @Invoker("endQuickCraft") public abstract void meldrite$endQuickCraft();
}
