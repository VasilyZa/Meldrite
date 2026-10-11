package dev.meldrite.v1201.hook;

import java.util.Map;
import java.util.UUID;
import net.minecraft.server.PlayerManager;
import net.minecraft.stat.ServerStatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerManager.class)
public interface PlayerManagerStatisticsAccess {
    @Accessor("statisticsMap") Map<UUID, ServerStatHandler> meldrite$statistics();
}
