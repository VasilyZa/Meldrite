package dev.meldrite.v1201.hook;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(BatEntity.class)
public interface BatEntityPaperAccessMixin { @Accessor("hangingPosition") BlockPos meldrite$target(); @Accessor("hangingPosition") void meldrite$target(BlockPos pos); }
