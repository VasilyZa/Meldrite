package dev.meldrite.v1201.hook;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(LightningEntity.class)
public interface LightningEntityPaperAccessMixin {
    @Accessor("cosmetic") boolean meldrite$isCosmetic();
    @Accessor("remainingActions") int meldrite$getRemainingActions();
    @Accessor("remainingActions") void meldrite$setRemainingActions(int value);
    @Accessor("ambientTick") int meldrite$getAmbientTick();
    @Accessor("ambientTick") void meldrite$setAmbientTick(int value);
}
