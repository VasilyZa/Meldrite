package dev.meldrite.v1201.hook;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(WanderingTraderEntity.class)
public interface WanderingTraderEntityPaperAccessMixin { @Accessor("wanderTarget") BlockPos meldrite$wanderTarget(); }
