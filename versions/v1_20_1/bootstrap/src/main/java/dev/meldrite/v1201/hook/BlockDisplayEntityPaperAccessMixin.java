package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(DisplayEntity.BlockDisplayEntity.class)
public interface BlockDisplayEntityPaperAccessMixin {
    @Invoker("getBlockState") BlockState meldrite$block();
    @Invoker("setBlockState") void meldrite$block(BlockState value);
}
