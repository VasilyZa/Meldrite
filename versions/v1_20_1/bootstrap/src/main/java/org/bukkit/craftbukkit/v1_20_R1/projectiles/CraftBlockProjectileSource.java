package org.bukkit.craftbukkit.v1_20_R1.projectiles;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointerImpl;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class CraftBlockProjectileSource implements org.bukkit.projectiles.BlockProjectileSource {
    private final DispenserBlockEntity dispenserBlock;
    public CraftBlockProjectileSource(DispenserBlockEntity dispenser) { dispenserBlock = java.util.Objects.requireNonNull(dispenser); }
    @Override public org.bukkit.block.Block getBlock() { return org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock.at(dispenserBlock.getWorld(), dispenserBlock.getPos()); }
    @Override public <T extends Projectile> T launchProjectile(Class<? extends T> projectile) { return launchProjectile(projectile, null, null); }
    @Override public <T extends Projectile> T launchProjectile(Class<? extends T> projectile, Vector velocity) { return launchProjectile(projectile, velocity, null); }
    @SuppressWarnings("unchecked")
    @Override public <T extends Projectile> T launchProjectile(Class<? extends T> type, Vector velocity, org.bukkit.util.Consumer<T> consumer) {
        java.util.Objects.requireNonNull(type, "projectile");
        if (!(dispenserBlock.getWorld() instanceof ServerWorld world) || !world.getServer().isOnThread()) throw new IllegalStateException("Projectile launch requires the server thread");
        if (world.getBlockState(dispenserBlock.getPos()).getBlock() != net.minecraft.block.Blocks.DISPENSER || world.getBlockEntity(dispenserBlock.getPos()) != dispenserBlock) throw new IllegalStateException("Block is no longer this dispenser");
        var pointer = new BlockPointerImpl(world, dispenserBlock.getPos());
        var pos = DispenserBlock.getOutputLocation(pointer);
        var facing = pointer.getBlockState().get(DispenserBlock.FACING);
        ProjectileEntity launch;
        if (Snowball.class.isAssignableFrom(type)) launch = new SnowballEntity(world, pos.getX(), pos.getY(), pos.getZ());
        else if (Egg.class.isAssignableFrom(type)) launch = new EggEntity(world, pos.getX(), pos.getY(), pos.getZ());
        else if (EnderPearl.class.isAssignableFrom(type)) launch = new EnderPearlEntity(EntityType.ENDER_PEARL, world);
        else if (ThrownExpBottle.class.isAssignableFrom(type)) launch = new ExperienceBottleEntity(world, pos.getX(), pos.getY(), pos.getZ());
        else if (ThrownPotion.class.isAssignableFrom(type)) {
            var potion = new PotionEntity(world, pos.getX(), pos.getY(), pos.getZ());
            potion.setItem(new net.minecraft.item.ItemStack(LingeringPotion.class.isAssignableFrom(type) ? net.minecraft.item.Items.LINGERING_POTION : net.minecraft.item.Items.SPLASH_POTION));
            launch = potion;
        } else if (AbstractArrow.class.isAssignableFrom(type)) {
            PersistentProjectileEntity arrow = SpectralArrow.class.isAssignableFrom(type) ? new SpectralArrowEntity(world, pos.getX(), pos.getY(), pos.getZ()) : new ArrowEntity(world, pos.getX(), pos.getY(), pos.getZ());
            if (TippedArrow.class.isAssignableFrom(type) && arrow instanceof ArrowEntity tipped) {
                var potion = (dev.meldrite.v1201.hook.ArrowEntityAccess) tipped;
                potion.meldrite$setPotion(net.minecraft.potion.Potions.WATER);
                potion.meldrite$initColor();
            }
            arrow.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
            launch = arrow;
        } else if (Fireball.class.isAssignableFrom(type)) {
            double x = facing.getOffsetX() + world.random.nextGaussian() * 0.05;
            double y = facing.getOffsetY() + world.random.nextGaussian() * 0.05;
            double z = facing.getOffsetZ() + world.random.nextGaussian() * 0.05;
            launch = SmallFireball.class.isAssignableFrom(type) ? new SmallFireballEntity(world, pos.getX(), pos.getY(), pos.getZ(), x, y, z)
                    : WitherSkull.class.isAssignableFrom(type) ? new WitherSkullEntity(EntityType.WITHER_SKULL, world)
                    : new FireballEntity(EntityType.FIREBALL, world);
            var fireball = (ExplosiveProjectileEntity) launch;
            double length = Math.sqrt(x * x + y * y + z * z);
            fireball.powerX = x / length * 0.1;
            fireball.powerY = y / length * 0.1;
            fireball.powerZ = z / length * 0.1;
        } else throw new IllegalArgumentException("Projectile not supported: " + type.getName());
        launch.setPosition(pos.getX(), pos.getY(), pos.getZ());
        if (launch instanceof ExplosiveProjectileEntity) launch.setPosition(pos.getX() + facing.getOffsetX() * 0.3,
                pos.getY() + facing.getOffsetY() * 0.3, pos.getZ() + facing.getOffsetZ() * 0.3);
        ((dev.meldrite.v1201.compat.NativeProjectileSource) launch).meldrite$setSource(this);
        float speed = 1.1f, spread = 6;
        if (launch instanceof PotionEntity || launch instanceof ExperienceBottleEntity) { speed *= 1.25f; spread *= 0.5f; }
        launch.setVelocity(facing.getOffsetX(), facing.getOffsetY() + 0.1, facing.getOffsetZ(), speed, spread);
        if (velocity != null) { velocity.checkFinite(); launch.setVelocity(velocity.getX(), velocity.getY(), velocity.getZ()); }
        T result = (T) NativeEntityExtensions.bukkit(launch);
        if (!type.isInstance(result)) throw new IllegalArgumentException("Projectile not supported: " + type.getName());
        if (consumer != null) consumer.accept(result);
        if (!world.spawnEntity(launch)) throw new IllegalStateException("Projectile spawn was rejected");
        return result;
    }
}
