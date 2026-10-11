package dev.meldrite.v1201.hook;
import net.minecraft.entity.mob.EndermiteEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(EndermiteEntity.class)
public interface EndermiteEntityPaperAccessMixin { @Accessor("lifeTime") int meldrite$lifetime(); @Accessor("lifeTime") void meldrite$lifetime(int ticks); }
