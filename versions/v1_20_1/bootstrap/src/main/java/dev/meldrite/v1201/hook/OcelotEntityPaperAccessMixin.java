package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.OcelotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(OcelotEntity.class)
public interface OcelotEntityPaperAccessMixin {
    @Invoker("isTrusting") boolean meldrite$isTrusting();
    @Invoker("setTrusting") void meldrite$setTrusting(boolean value);
}
