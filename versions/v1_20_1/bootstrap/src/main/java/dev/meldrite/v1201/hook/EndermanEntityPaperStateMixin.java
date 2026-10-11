package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EndermanEntity.class)
public class EndermanEntityPaperStateMixin {
    @Unique private boolean meldrite$screaming;
    @Unique private boolean meldrite$staredAt;
    public boolean meldrite$isScreaming() { return meldrite$screaming; }
    public void meldrite$setScreaming(boolean value) { meldrite$screaming = value; }
    public boolean meldrite$hasBeenStaredAt() { return meldrite$staredAt; }
    public void meldrite$setHasBeenStaredAt(boolean value) { meldrite$staredAt = value; }
}
