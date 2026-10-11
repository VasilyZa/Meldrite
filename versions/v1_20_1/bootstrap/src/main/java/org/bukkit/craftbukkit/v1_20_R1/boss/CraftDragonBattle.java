package org.bukkit.craftbukkit.v1_20_R1.boss;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.EnderDragonFightPaperAccessMixin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonSpawnState;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEnderCrystal;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;

public class CraftDragonBattle implements DragonBattle {
    private final EnderDragonFight handle;
    public CraftDragonBattle(EnderDragonFight handle) { this.handle = Objects.requireNonNull(handle, "handle"); }
    public EnderDragonFight getHandle() { return handle; }
    private EnderDragonFightPaperAccessMixin state() { return (EnderDragonFightPaperAccessMixin) handle; }
    private void requireWritable() { NativeEntityExtensions.requireThread(state().meldrite$world()); }
    @Override public EnderDragon getEnderDragon() {
        var uuid = handle.getDragonUuid();
        var entity = uuid == null ? null : state().meldrite$world().getEntity(uuid);
        return entity == null ? null : (EnderDragon) NativeEntityExtensions.bukkit(entity);
    }
    @Override public BossBar getBossBar() { return new CraftBossBar(state().meldrite$bossBar()); }
    @Override public Location getEndPortalLocation() {
        var pos = state().meldrite$portalLocation();
        return pos == null ? null : new Location(NativeEntityExtensions.world(state().meldrite$world()), pos.getX(), pos.getY(), pos.getZ());
    }
    @Override public boolean generateEndPortal(boolean withPortals) {
        requireWritable();
        if (state().meldrite$portalLocation() != null || state().meldrite$findEndPortal() != null) return false;
        state().meldrite$generateEndPortal(withPortals);
        return true;
    }
    @Override public boolean hasBeenPreviouslyKilled() { return handle.hasPreviouslyKilled(); }
    @Override public void initiateRespawn() { requireWritable(); handle.respawnDragon(); }
    @Override public boolean initiateRespawn(Collection<EnderCrystal> crystals) {
        requireWritable();
        if (!hasBeenPreviouslyKilled() || getRespawnPhase() != RespawnPhase.NONE) return false;
        if (state().meldrite$portalLocation() == null && state().meldrite$findEndPortal() == null) state().meldrite$generateEndPortal(true);
        var nativeCrystals = new ArrayList<EndCrystalEntity>();
        if (crystals != null) for (EnderCrystal crystal : crystals) {
            if (crystal == null) continue;
            if (!(crystal instanceof CraftEnderCrystal craft)) throw new IllegalArgumentException("Crystal is not a native entity");
            if (craft.getHandle().getWorld() == state().meldrite$world()) nativeCrystals.add(craft.getHandle());
        }
        state().meldrite$respawnDragon(nativeCrystals);
        return state().meldrite$respawnState() != null;
    }
    @Override public RespawnPhase getRespawnPhase() {
        var phase = state().meldrite$respawnState();
        return phase == null ? RespawnPhase.NONE : RespawnPhase.values()[phase.ordinal()];
    }
    @Override public boolean setRespawnPhase(RespawnPhase phase) {
        if (phase == null || phase == RespawnPhase.NONE) throw new IllegalArgumentException("Invalid respawn phase");
        requireWritable();
        if (state().meldrite$respawnState() == null) return false;
        state().meldrite$setRespawnState(EnderDragonSpawnState.values()[phase.ordinal()]);
        return true;
    }
    @Override public void resetCrystals() { requireWritable(); handle.resetEndCrystals(); }
    @Override public int getGatewayCount() { return 20 - state().meldrite$gateways().size(); }
    @Override public boolean spawnNewGateway() {
        requireWritable();
        if (state().meldrite$gateways().isEmpty()) return false;
        state().meldrite$generateNewEndGateway();
        return true;
    }
    @Override public void spawnNewGateway(io.papermc.paper.math.Position position) {
        Objects.requireNonNull(position, "position"); requireWritable();
        state().meldrite$generateEndGateway(new BlockPos(position.blockX(), position.blockY(), position.blockZ()));
    }
    private List<EnderCrystal> project(Collection<EndCrystalEntity> crystals) {
        if (crystals == null) return List.of();
        return crystals.stream().filter(crystal -> crystal.isAlive() && !crystal.isRemoved()
                && NativeEntityExtensions.state(crystal).meldrite$isTracked())
                .map(crystal -> (EnderCrystal) NativeEntityExtensions.bukkit(crystal)).toList();
    }
    @Override public List<EnderCrystal> getRespawnCrystals() { return project(state().meldrite$respawnCrystals()); }
    @Override public List<EnderCrystal> getHealingCrystals() {
        var crystals = new ArrayList<EndCrystalEntity>();
        var world = state().meldrite$world();
        for (var spike : EndSpikeFeature.getSpikes(world)) crystals.addAll(world.getNonSpectatingEntities(EndCrystalEntity.class, spike.getBoundingBox()));
        return project(crystals);
    }
    @Override public boolean equals(Object value) { return value instanceof CraftDragonBattle battle && battle.handle == handle; }
    @Override public int hashCode() { return handle.hashCode(); }
}
