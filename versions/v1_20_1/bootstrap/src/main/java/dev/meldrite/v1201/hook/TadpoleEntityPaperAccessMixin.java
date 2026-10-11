package dev.meldrite.v1201.hook;
import net.minecraft.entity.passive.TadpoleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(TadpoleEntity.class)
public interface TadpoleEntityPaperAccessMixin { @Accessor("tadpoleAge") int meldrite$age(); @Accessor("tadpoleAge") void meldrite$age(int ticks); }
