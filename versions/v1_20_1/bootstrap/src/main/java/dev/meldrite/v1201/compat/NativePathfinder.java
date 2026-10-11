package dev.meldrite.v1201.compat;

import com.destroystokyo.paper.entity.Pathfinder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

/** Projects the native navigation path and node evaluator directly. */
public final class NativePathfinder implements Pathfinder {
    private final CraftMob mob;
    public NativePathfinder(CraftMob mob) { this.mob = java.util.Objects.requireNonNull(mob); }
    @Override public Mob getEntity() { return mob; }
    @Override public void stopPathfinding() { mob.requireWritable(); mob.getHandle().getNavigation().stop(); }
    @Override public boolean hasPath() { Path path = mob.getHandle().getNavigation().getCurrentPath(); return path != null && !path.isFinished(); }
    @Override public PathResult getCurrentPath() { Path path = mob.getHandle().getNavigation().getCurrentPath(); return path == null || path.isFinished() ? null : new Result(path); }
    @Override public PathResult findPath(Location location) { java.util.Objects.requireNonNull(location).checkFinite(); if (location.getWorld() != mob.getWorld()) throw new IllegalArgumentException("PATH_IN_DIFFERENT_WORLD"); mob.requireWritable(); var path = mob.getHandle().getNavigation().findPathTo(location.getX(), location.getY(), location.getZ(), 0); return path == null ? null : new Result(path); }
    @Override public PathResult findPath(LivingEntity target) { if (!(target instanceof CraftLivingEntity craft) || craft.getWorld() != mob.getWorld()) throw new IllegalArgumentException("FOREIGN_PATH_TARGET"); mob.requireWritable(); var path = mob.getHandle().getNavigation().findPathTo(craft.getHandle(), 0); return path == null ? null : new Result(path); }
    @Override public boolean moveTo(PathResult path, double speed) { if (!(path instanceof NativePathfinder.Result result) || result.world() != mob.getWorld()) throw new IllegalArgumentException("FOREIGN_PATH_RESULT"); if (!Double.isFinite(speed) || speed < 0) throw new IllegalArgumentException("Invalid path speed"); mob.requireWritable(); return mob.getHandle().getNavigation().startMovingAlong(result.path, speed); }
    @Override public boolean canOpenDoors() { return mob.getHandle().getNavigation().getNodeMaker().canOpenDoors(); }
    @Override public void setCanOpenDoors(boolean value) { mob.requireWritable(); mob.getHandle().getNavigation().getNodeMaker().setCanOpenDoors(value); }
    @Override public boolean canPassDoors() { return mob.getHandle().getNavigation().getNodeMaker().canEnterOpenDoors(); }
    @Override public void setCanPassDoors(boolean value) { mob.requireWritable(); mob.getHandle().getNavigation().getNodeMaker().setCanEnterOpenDoors(value); }
    @Override public boolean canFloat() { return mob.getHandle().getNavigation().getNodeMaker().canSwim(); }
    @Override public void setCanFloat(boolean value) { mob.requireWritable(); mob.getHandle().getNavigation().getNodeMaker().setCanSwim(value); }
    private final class Result implements PathResult {
        private final Path path;
        private Result(Path path) { this.path = path; }
        private org.bukkit.World world() { return mob.getWorld(); }
        private Location location(PathNode node) { return node == null ? null : new Location(world(), node.x, node.y, node.z); }
        @Override public List<Location> getPoints() { var points = new ArrayList<Location>(path.getLength()); for (int index = 0; index < path.getLength(); index++) points.add(location(path.getNode(index))); return points; }
        @Override public int getNextPointIndex() { return path.getCurrentNodeIndex(); }
        @Override public Location getNextPoint() { return path.isFinished() ? null : location(path.getCurrentNode()); }
        @Override public Location getFinalPoint() { return location(path.getEnd()); }
    }
}
