package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.v1201.compat.NativeBlockEntityPersistentDataAccess;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;

/** Snapshot-backed Bukkit state for native block entities. */
public class CraftBlockEntityState<T extends BlockEntity> extends CraftBlockState implements TileState {
    protected final T tileEntity;
    protected final T snapshot;
    private final NbtCompound unhandledItemData = new NbtCompound();

    @SuppressWarnings("unchecked")
    public CraftBlockEntityState(ServerWorld world, BlockPos position, BlockState state, T tileEntity, boolean useSnapshot) {
        super(world, position, state);
        this.tileEntity = Objects.requireNonNull(tileEntity, "tileEntity");
        if (useSnapshot) {
            NbtCompound nbt = tileEntity.createNbtWithIdentifyingData().copy();
            BlockEntity copy = BlockEntity.createFromNbt(position, state, nbt);
            if (copy == null || copy.getClass() != tileEntity.getClass()) {
                throw new IllegalStateException("Cannot create block entity snapshot for " + tileEntity.getType());
            }
            this.snapshot = (T) copy;
            copyPersistentData(tileEntity, this.snapshot);
        } else {
            this.snapshot = tileEntity;
        }
    }

    public T getTileEntity() { return tileEntity; }
    public T getSnapshot() { return snapshot; }
    public void preserveUnhandledItemData(NbtCompound original) {
        NbtCompound serialized = snapshot.createNbt();
        for (String key : original.getKeys()) if (!serialized.contains(key)) unhandledItemData.put(key, original.get(key).copy());
    }
    public NbtCompound getSnapshotNBT() {
        NbtCompound result = unhandledItemData.copy();
        result.copyFrom(snapshot.createNbt());
        return result;
    }
    public BlockPos getPosition() { return position; }
    protected T liveTile() {
        if (!isPlaced()) return snapshot;
        BlockEntity live = world.getBlockEntity(position);
        if (live == null || live.getClass() != tileEntity.getClass()) throw new IllegalStateException("Block entity has been replaced");
        @SuppressWarnings("unchecked") T result = (T) live;
        return result;
    }
    protected void requirePlaced() {
        if (!isPlaced()) throw new IllegalStateException("BlockState is not placed in a world");
        if (!world.getServer().isOnThread()) throw new IllegalStateException("ASYNC_BLOCK_WRITE");
    }

    @Override public PersistentDataContainer getPersistentDataContainer() {
        return ((NativeBlockEntityPersistentDataAccess) snapshot).meldrite$getPersistentDataContainer();
    }

    @Override public boolean isSnapshot() { return snapshot != tileEntity; }

    @Override public boolean update(boolean force, boolean applyPhysics) {
        if (!super.update(force, applyPhysics)) return false;
        if (!isPlaced()) return true;
        BlockEntity live = world.getBlockEntity(position);
        if (live == null || live.getClass() != tileEntity.getClass()) return true;
        if (live != snapshot) {
            live.readNbt(snapshot.createNbt().copy());
            copyPersistentData(snapshot, live);
        }
        live.markDirty();
        BlockState current = world.getBlockState(position);
        world.updateListeners(position, current, current, 3);
        return true;
    }

    private static void copyPersistentData(BlockEntity from, BlockEntity to) {
        if (from instanceof NativeBlockEntityPersistentDataAccess source
                && to instanceof NativeBlockEntityPersistentDataAccess target) {
            target.meldrite$loadPersistentData(source.meldrite$getPersistentDataContainer().toTagCompound().copy());
        }
    }
}
