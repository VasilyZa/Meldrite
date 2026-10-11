package dev.meldrite.v1201.hook;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.passive.StriderEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(StriderEntity.class)
public interface StriderEntityPaperAccessMixin { @Accessor("saddledComponent") SaddledComponent meldrite$steering(); }
