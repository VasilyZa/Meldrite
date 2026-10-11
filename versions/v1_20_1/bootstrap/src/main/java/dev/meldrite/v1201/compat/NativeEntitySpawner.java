package dev.meldrite.v1201.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Consumer;

/** Bukkit creation rules followed by one native, cancellable insertion boundary. */
public final class NativeEntitySpawner {
    private NativeEntitySpawner() {}

    public static org.bukkit.entity.EntityType type(Class<? extends org.bukkit.entity.Entity> requested) {
        if (requested == null) throw new IllegalArgumentException("Entity class cannot be null");
        if (org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity.class.isAssignableFrom(requested)) {
            throw new IllegalArgumentException("Use a Bukkit entity class instead of its CraftBukkit implementation: " + requested.getName());
        }
        Class<?> concrete = requested;
        if (concrete == org.bukkit.entity.AbstractArrow.class || concrete == org.bukkit.entity.TippedArrow.class) concrete = org.bukkit.entity.Arrow.class;
        else if (concrete == org.bukkit.entity.AbstractHorse.class) concrete = org.bukkit.entity.Horse.class;
        else if (concrete == org.bukkit.entity.Fireball.class) concrete = org.bukkit.entity.LargeFireball.class;
        else if (concrete == org.bukkit.entity.SplashPotion.class || concrete == org.bukkit.entity.LingeringPotion.class) concrete = org.bukkit.entity.ThrownPotion.class;
        else if (concrete == org.bukkit.entity.Minecart.class) concrete = org.bukkit.entity.minecart.RideableMinecart.class;
        for (var type : org.bukkit.entity.EntityType.values()) {
            if (type.getEntityClass() == concrete) { validateType(type); return type; }
        }
        throw new IllegalArgumentException("Cannot spawn an entity for " + requested.getName());
    }

    public static void validateType(org.bukkit.entity.EntityType type) {
        if (type == null || type == org.bukkit.entity.EntityType.UNKNOWN || type == org.bukkit.entity.EntityType.PLAYER
                || type == org.bukkit.entity.EntityType.FISHING_HOOK) {
            throw new IllegalArgumentException("Cannot spawn entity type " + type);
        }
    }

    public static Entity create(ServerWorld world, Location location, org.bukkit.entity.EntityType type,
            Class<? extends org.bukkit.entity.Entity> requestedClass, boolean randomizeData) {
        validateType(type);
        NativeEntityExtensions.requireThread(world);
        var key = new Identifier(type.getKey().toString());
        var nativeType = Registries.ENTITY_TYPE.getOrEmpty(key)
                .orElseThrow(() -> new IllegalArgumentException("No native entity type for " + key));
        if (!nativeType.getRequiredFeatures().isSubsetOf(world.getEnabledFeatures())) {
            throw new IllegalArgumentException("Entity type is not enabled: " + key);
        }
        BlockPos position = BlockPos.ofFloored(location.getX(), location.getY(), location.getZ());
        Entity entity = switch (type) {
            case DROPPED_ITEM -> {
                // Bukkit's generic Item spawn starts with stone, which the consumer may replace.
                var item = new ItemEntity(world, location.getX(), location.getY(), location.getZ(), new ItemStack(Items.STONE));
                item.setToDefaultPickupDelay();
                yield item;
            }
            case EXPERIENCE_ORB -> new net.minecraft.entity.ExperienceOrbEntity(world, location.getX(), location.getY(), location.getZ(), 0);
            case PRIMED_TNT -> new net.minecraft.entity.TntEntity(world, location.getX(), location.getY(), location.getZ(), null);
            case FALLING_BLOCK -> fallingBlock(world, location, world.getBlockState(position));
            case FIREWORK -> {
                var rocket = new net.minecraft.entity.projectile.FireworkRocketEntity(world, location.getX(), location.getY(), location.getZ(), new ItemStack(Items.FIREWORK_ROCKET));
                if (!randomizeData) {
                    rocket.setVelocity(0, 0.05, 0);
                    ((dev.meldrite.v1201.hook.FireworkRocketEntityAccess) rocket).meldrite$setLifeTime(16);
                }
                yield rocket;
            }
            case EVOKER_FANGS -> new net.minecraft.entity.mob.EvokerFangsEntity(world, location.getX(), location.getY(), location.getZ(),
                    (float) Math.toRadians(location.getYaw()), 0, null);
            case LEASH_HITCH -> new net.minecraft.entity.decoration.LeashKnotEntity(world, position);
            case PAINTING, ITEM_FRAME, GLOW_ITEM_FRAME -> hanging(world, position, type, randomizeData);
            default -> nativeType.create(world);
        };
        if (entity == null) throw new IllegalArgumentException("Native entity factory cannot create " + key);
        // Hanging constructors anchor to a block; preserve their computed attachment position.
        if (!(entity instanceof AbstractDecorationEntity)) {
            float yaw = location.getYaw(), pitch = location.getPitch();
            if (type == org.bukkit.entity.EntityType.ARROW || type == org.bukkit.entity.EntityType.SPECTRAL_ARROW
                    || type == org.bukkit.entity.EntityType.TRIDENT || type == org.bukkit.entity.EntityType.ENDER_PEARL
                    || type == org.bukkit.entity.EntityType.THROWN_EXP_BOTTLE || type == org.bukkit.entity.EntityType.ENDER_CRYSTAL
                    || type == org.bukkit.entity.EntityType.LIGHTNING) { yaw = 0; pitch = 0; }
            entity.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), yaw, pitch);
            if (entity instanceof LivingEntity living) living.setHeadYaw(location.getYaw());
        }
        if (entity instanceof ExplosiveProjectileEntity projectile) {
            var direction = location.getDirection();
            projectile.powerX = direction.getX() * 0.1;
            projectile.powerY = direction.getY() * 0.1;
            projectile.powerZ = direction.getZ() * 0.1;
        }
        if (entity instanceof PotionEntity potion) {
            var item = new ItemStack(requestedClass == org.bukkit.entity.LingeringPotion.class ? Items.LINGERING_POTION : Items.SPLASH_POTION);
            potion.setItem(PotionUtil.setPotion(item, Potions.WATER));
        }
        if (entity instanceof net.minecraft.entity.projectile.ArrowEntity arrow && requestedClass == org.bukkit.entity.TippedArrow.class) {
            arrow.initFromStack(PotionUtil.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.WATER));
        }
        if (!randomizeData && (entity instanceof ItemEntity || entity instanceof net.minecraft.entity.ExperienceOrbEntity
                || entity instanceof net.minecraft.entity.TntEntity)) entity.setVelocity(Vec3d.ZERO);
        return entity;
    }

    public static FallingBlockEntity fallingBlock(ServerWorld world, Location location, net.minecraft.block.BlockState state) {
        // The native constructor preserves the state and never removes the source block.
        var falling = new FallingBlockEntity(world, location.getX(), location.getY(), location.getZ(), state);
        falling.timeFalling = 1;
        return falling;
    }

    private static Entity hanging(ServerWorld world, BlockPos position, org.bukkit.entity.EntityType type, boolean randomize) {
        boolean frame = type != org.bukkit.entity.EntityType.PAINTING;
        Direction facing = Direction.NORTH;
        boolean attached = false;
        Direction[] supports = frame ? new Direction[]{Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP, Direction.DOWN}
                : new Direction[]{Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH};
        for (var support : supports) {
            var state = world.getBlockState(position.offset(support));
            if (!state.isSolidBlock(world, position.offset(support)) && !(state.getBlock() instanceof net.minecraft.block.AbstractRedstoneGateBlock)) continue;
            AbstractDecorationEntity candidate = frame ? new ItemFrameEntity(world, position, support.getOpposite())
                    : new PaintingEntity(world, position, support.getOpposite(), Registries.PAINTING_VARIANT.getEntry(net.minecraft.entity.decoration.painting.PaintingVariants.KEBAB).orElseThrow());
            if (!candidate.canStayAttached()) continue;
            facing = support.getOpposite(); attached = true; break;
        }
        if (type == org.bukkit.entity.EntityType.GLOW_ITEM_FRAME) return new net.minecraft.entity.decoration.GlowItemFrameEntity(world, position, facing);
        if (type == org.bukkit.entity.EntityType.ITEM_FRAME) return new ItemFrameEntity(world, position, facing);
        if (randomize && attached) {
            var randomized = PaintingEntity.placePainting(world, position, facing);
            if (randomized.isPresent()) return randomized.get();
        }
        return new PaintingEntity(world, position, facing, Registries.PAINTING_VARIANT.getEntry(net.minecraft.entity.decoration.painting.PaintingVariants.KEBAB).orElseThrow());
    }

    public static <T extends org.bukkit.entity.Entity> T add(ServerWorld world, Entity entity, T api,
            CreatureSpawnEvent.SpawnReason reason, boolean randomizeData, Consumer<T> consumer, PluginManager events) {
        NativeEntityExtensions.requireThread(world);
        if (reason == null) throw new IllegalArgumentException("Spawn reason cannot be null");
        if (randomizeData && entity instanceof MobEntity mob) {
            mob.initialize(world, world.getLocalDifficulty(entity.getBlockPos()), net.minecraft.entity.SpawnReason.COMMAND, null, null);
        }
        if (consumer != null) consumer.accept(api);
        // The consumer may remove the detached entity itself.
        if (entity.isRemoved()) return api;
        var event = spawnEvent(api, reason);
        events.callEvent(event);
        if (((org.bukkit.event.Cancellable) event).isCancelled() || entity.isRemoved()) { discardWithPassengers(entity); return api; }
        // Native insertion installs the tracker and keeps every entity's original native identity.
        var members = entity.streamSelfAndPassengers().toList();
        for (var member : members) {
            if (member != entity) {
                var passengerApi = NativeEntityExtensions.bukkit(member);
                var passengerEvent = spawnEvent(passengerApi, reason);
                events.callEvent(passengerEvent);
                if (((org.bukkit.event.Cancellable) passengerEvent).isCancelled() || member.isRemoved()) { discardWithPassengers(entity); return api; }
            }
        }
        for (var member : members) {
            if (!world.spawnEntity(member)) { discardWithPassengers(entity); break; }
        }
        return api;
    }

    private static org.bukkit.event.Event spawnEvent(org.bukkit.entity.Entity api, CreatureSpawnEvent.SpawnReason reason) {
        if (api instanceof org.bukkit.entity.LivingEntity living) return new CreatureSpawnEvent(living, reason);
        if (api instanceof org.bukkit.entity.Item item) return new org.bukkit.event.entity.ItemSpawnEvent(item);
        if (api instanceof org.bukkit.entity.Projectile projectile) return new org.bukkit.event.entity.ProjectileLaunchEvent(projectile);
        if (api instanceof org.bukkit.entity.Vehicle vehicle) return new org.bukkit.event.vehicle.VehicleCreateEvent(vehicle);
        if (api instanceof org.bukkit.entity.LightningStrike lightning) {
            var cause = switch (reason) {
                case COMMAND -> org.bukkit.event.weather.LightningStrikeEvent.Cause.COMMAND;
                case CUSTOM -> org.bukkit.event.weather.LightningStrikeEvent.Cause.CUSTOM;
                case SPAWNER -> org.bukkit.event.weather.LightningStrikeEvent.Cause.SPAWNER;
                default -> org.bukkit.event.weather.LightningStrikeEvent.Cause.UNKNOWN;
            };
            return new org.bukkit.event.weather.LightningStrikeEvent(lightning.getWorld(), lightning, cause);
        }
        return new org.bukkit.event.entity.EntitySpawnEvent(api);
    }

    private static void discardWithPassengers(Entity entity) {
        var members = entity.streamSelfAndPassengers().toList();
        for (var member : members) if (!member.isRemoved()) member.discard();
    }
}
