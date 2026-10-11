package dev.meldrite.v1201.hook;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.SaddledComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(PigEntity.class)
public interface PigEntityPaperAccessMixin { @Accessor("saddledComponent") SaddledComponent meldrite$steering(); }
