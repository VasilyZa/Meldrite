package dev.meldrite.v1201.hook;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.WardenAngerManager;
import net.minecraft.entity.mob.WardenEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WardenEntity.class)
public interface WardenEntityPaperAccessMixin {
    @Accessor("angerManager") WardenAngerManager meldrite$angerManager();
}
