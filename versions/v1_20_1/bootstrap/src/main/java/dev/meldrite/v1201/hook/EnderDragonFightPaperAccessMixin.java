package dev.meldrite.v1201.hook;

import java.util.List;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonSpawnState;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnderDragonFight.class)
public interface EnderDragonFightPaperAccessMixin {
    @Accessor("world") ServerWorld meldrite$world();
    @Accessor("bossBar") ServerBossBar meldrite$bossBar();
    @Accessor("exitPortalLocation") BlockPos meldrite$portalLocation();
    @Accessor("dragonSpawnState") EnderDragonSpawnState meldrite$respawnState();
    @Accessor("crystals") List<EndCrystalEntity> meldrite$respawnCrystals();
    @Accessor("gateways") it.unimi.dsi.fastutil.objects.ObjectArrayList<Integer> meldrite$gateways();
    @Invoker("findEndPortal") BlockPattern.Result meldrite$findEndPortal();
    @Invoker("generateEndPortal") void meldrite$generateEndPortal(boolean portals);
    @Invoker("setSpawnState") void meldrite$setRespawnState(EnderDragonSpawnState state);
    @Invoker("respawnDragon") void meldrite$respawnDragon(List<EndCrystalEntity> crystals);
    @Invoker("generateNewEndGateway") void meldrite$generateNewEndGateway();
    @Invoker("generateEndGateway") void meldrite$generateEndGateway(BlockPos position);
}
