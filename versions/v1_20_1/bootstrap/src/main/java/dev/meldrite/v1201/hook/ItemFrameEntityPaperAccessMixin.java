package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ItemFrameEntity.class)
public interface ItemFrameEntityPaperAccessMixin {
    @Accessor("itemDropChance") float meldrite$getDropChance();
    @Accessor("itemDropChance") void meldrite$setDropChance(float value);
    @Accessor("fixed") boolean meldrite$isFixed();
    @Accessor("fixed") void meldrite$setFixed(boolean value);
}
