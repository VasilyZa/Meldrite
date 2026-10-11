package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeProjectileSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileSourceMixin implements NativeProjectileSource {
    @Unique private org.bukkit.projectiles.ProjectileSource meldrite$source;
    @Unique private BlockPos meldrite$blockSource;
    @Override public org.bukkit.projectiles.ProjectileSource meldrite$getSource() {
        if (meldrite$source == null && meldrite$blockSource != null) {
            var tile = ((ProjectileEntity) (Object) this).getWorld().getBlockEntity(meldrite$blockSource);
            if (tile instanceof net.minecraft.block.entity.DispenserBlockEntity dispenser) meldrite$source = new org.bukkit.craftbukkit.v1_20_R1.projectiles.CraftBlockProjectileSource(dispenser);
        }
        return meldrite$source;
    }
    @Override public void meldrite$setSource(org.bukkit.projectiles.ProjectileSource value) {
        meldrite$source = value;
        meldrite$blockSource = value instanceof org.bukkit.projectiles.BlockProjectileSource block ? new BlockPos(block.getBlock().getX(), block.getBlock().getY(), block.getBlock().getZ()) : null;
    }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo callback) { if (meldrite$blockSource != null) nbt.putLong("Paper.BlockProjectileSource", meldrite$blockSource.asLong()); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo callback) {
        meldrite$source = null; meldrite$blockSource = nbt.contains("Paper.BlockProjectileSource") ? BlockPos.fromLong(nbt.getLong("Paper.BlockProjectileSource")) : null;
    }
}
