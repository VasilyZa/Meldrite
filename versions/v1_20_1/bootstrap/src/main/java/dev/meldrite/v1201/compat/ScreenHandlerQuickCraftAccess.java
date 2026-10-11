package dev.meldrite.v1201.compat;

import java.util.Set;
import net.minecraft.screen.slot.Slot;

/** Narrow access to the native quick-craft state used by the drag event bridge. */
public interface ScreenHandlerQuickCraftAccess {
    int meldrite$getQuickCraftButton();
    void meldrite$setQuickCraftButton(int value);
    int meldrite$getQuickCraftStage();
    void meldrite$setQuickCraftStage(int value);
    Set<Slot> meldrite$getQuickCraftSlots();
    void meldrite$endQuickCraft();
}
