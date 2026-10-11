package dev.meldrite.v1201.hook;
import java.util.UUID;
import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(AnimalEntity.class)
public interface AnimalEntityPaperAccessMixin { @Accessor("lovingPlayer") UUID meldrite$getLoveCause(); @Accessor("lovingPlayer") void meldrite$setLoveCause(UUID uuid); }
