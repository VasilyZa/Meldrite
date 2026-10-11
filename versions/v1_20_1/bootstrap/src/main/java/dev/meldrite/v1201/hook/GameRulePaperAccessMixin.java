package dev.meldrite.v1201.hook;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.Rule.class)
public interface GameRulePaperAccessMixin {
    @Invoker("deserialize") void meldrite$deserialize(String value);
    @Invoker("changed") void meldrite$changed(MinecraftServer server);
}
