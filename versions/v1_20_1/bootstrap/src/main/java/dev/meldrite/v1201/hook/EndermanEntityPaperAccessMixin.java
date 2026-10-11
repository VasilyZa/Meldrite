package dev.meldrite.v1201.hook;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EndermanEntity.class)
public interface EndermanEntityPaperAccessMixin {
    @Invoker("teleportRandomly") boolean meldrite$teleportRandomly();
    @Invoker("teleportTo") boolean meldrite$teleportTo(Entity entity);
}
