package dev.meldrite.v1201.hook;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityIdPaperAccessMixin {
    @Accessor("CURRENT_ID") static AtomicInteger meldrite$getCurrentId() { throw new AssertionError(); }
}
