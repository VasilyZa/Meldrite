package dev.meldrite.v1201.hook;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerCombatAccessMixin {
    @Accessor("joinInvulnerabilityTicks") void meldrite$setJoinInvulnerabilityTicks(int ticks);
}
