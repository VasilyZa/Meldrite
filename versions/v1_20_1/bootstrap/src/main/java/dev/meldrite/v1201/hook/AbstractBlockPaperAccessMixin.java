package dev.meldrite.v1201.hook;
import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(AbstractBlock.class)
public interface AbstractBlockPaperAccessMixin { @Accessor("collidable") boolean meldrite$isCollidable(); }
