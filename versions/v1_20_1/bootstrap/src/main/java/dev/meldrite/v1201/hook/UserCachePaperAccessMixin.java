package dev.meldrite.v1201.hook;

import java.util.Map;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UserCache.class)
public interface UserCachePaperAccessMixin {
    @Accessor("byName") Map<String, ?> meldrite$profilesByName();
    @Invoker("incrementAndGetAccessCount") long meldrite$nextProfileAccess();
}
