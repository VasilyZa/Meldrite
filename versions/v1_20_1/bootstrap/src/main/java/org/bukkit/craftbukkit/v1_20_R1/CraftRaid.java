package org.bukkit.craftbukkit.v1_20_R1;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.RaidPaperAccessMixin;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.entity.Raider;

public final class CraftRaid implements Raid {
    private final net.minecraft.village.raid.Raid handle;
    public CraftRaid(net.minecraft.village.raid.Raid handle) { this.handle = java.util.Objects.requireNonNull(handle, "handle"); }
    public net.minecraft.village.raid.Raid getHandle() { return handle; }
    private RaidPaperAccessMixin state() { return (RaidPaperAccessMixin) handle; }
    @Override public boolean isStarted() { return handle.hasStarted(); }
    @Override public long getActiveTicks() { return state().meldrite$activeTicks(); }
    @Override public int getBadOmenLevel() { return handle.getBadOmenLevel(); }
    @Override public void setBadOmenLevel(int level) {
        if (level < 0 || level > handle.getMaxAcceptableBadOmenLevel()) throw new IllegalArgumentException("Invalid bad omen level");
        NativeEntityExtensions.requireThread((ServerWorld) handle.getWorld()); handle.setBadOmenLevel(level);
    }
    @Override public Location getLocation() { var pos = handle.getCenter(); return new Location(NativeEntityExtensions.world((ServerWorld) handle.getWorld()), pos.getX(), pos.getY(), pos.getZ()); }
    @Override public RaidStatus getStatus() { return handle.hasStopped() ? RaidStatus.STOPPED : handle.hasWon() ? RaidStatus.VICTORY : handle.hasLost() ? RaidStatus.LOSS : RaidStatus.ONGOING; }
    @Override public int getSpawnedGroups() { return handle.getGroupsSpawned(); }
    @Override public int getTotalGroups() { return getTotalWaves() + (getBadOmenLevel() > 1 ? 1 : 0); }
    @Override public int getTotalWaves() { return state().meldrite$waveCount(); }
    @Override public float getTotalHealth() { return handle.getCurrentRaiderHealth(); }
    @Override public Set<UUID> getHeroes() { return java.util.Collections.unmodifiableSet(state().meldrite$heroes()); }
    @Override public List<Raider> getRaiders() { return handle.getAllRaiders().stream().map(entity -> (Raider) NativeEntityExtensions.bukkit(entity)).toList(); }
}
