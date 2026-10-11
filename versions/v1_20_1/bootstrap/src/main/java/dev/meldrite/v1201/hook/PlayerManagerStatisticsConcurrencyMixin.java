package dev.meldrite.v1201.hook;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.PlayerManager;
import net.minecraft.stat.ServerStatHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native collections shared by asynchronous API reads and status queries. */
@Mixin(PlayerManager.class)
public abstract class PlayerManagerStatisticsConcurrencyMixin {
    @Shadow @Final @Mutable private Map<UUID, ServerStatHandler> statisticsMap;
    @Shadow @Final @Mutable private java.util.List<net.minecraft.server.network.ServerPlayerEntity> players;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void meldrite$publishStatisticsConcurrently(CallbackInfo callback) {
        statisticsMap = new ConcurrentHashMap<>(statisticsMap);
        // Login/logout are infrequent writes; Netty status queries traverse a stable roster.
        players = new java.util.concurrent.CopyOnWriteArrayList<>(players);
    }
    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "createStatHandler")
    private ServerStatHandler meldrite$serializeStatisticsCreation(net.minecraft.entity.player.PlayerEntity player,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<ServerStatHandler> original) {
        // Preserve vanilla legacy-file migration, but share its publication lock with offline reads.
        synchronized (statisticsMap) { return original.call(player); }
    }
}
