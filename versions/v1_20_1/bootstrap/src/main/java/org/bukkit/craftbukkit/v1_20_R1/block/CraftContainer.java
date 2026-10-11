package org.bukkit.craftbukkit.v1_20_R1.block;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.bukkit.block.Container;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;

public class CraftContainer extends CraftBlockEntityState<LockableContainerBlockEntity> implements Container {
    public CraftContainer(ServerWorld world, BlockPos position, BlockState state,
                          LockableContainerBlockEntity container, boolean useSnapshot) {
        super(world, position, state, container, useSnapshot);
    }

    @Override public org.bukkit.inventory.Inventory getInventory() {
        LockableContainerBlockEntity live = isPlaced() && world.getBlockEntity(position) instanceof LockableContainerBlockEntity current
                ? current : snapshot;
        Inventory inventory = live;
        BlockState currentState = isPlaced() ? world.getBlockState(position) : state;
        if (isPlaced() && live instanceof ChestBlockEntity && currentState.getBlock() instanceof ChestBlock chest) {
            Inventory combined = ChestBlock.getInventory(chest, currentState, world, position, true);
            if (combined != null) inventory = combined;
        }
        return new CraftInventory(inventory, this);
    }

    @Override public org.bukkit.inventory.Inventory getSnapshotInventory() {
        return new CraftInventory(snapshot, this);
    }

    @Override public boolean isLocked() { return !getLock().isEmpty(); }

    @Override public String getLock() {
        NbtCompound nbt = snapshot.createNbt();
        return nbt.contains("Lock") ? nbt.getString("Lock") : "";
    }

    @Override public void setLock(String key) {
        NbtCompound nbt = snapshot.createNbt();
        if (key == null || key.isEmpty()) nbt.remove("Lock");
        else nbt.putString("Lock", key);
        snapshot.readNbt(nbt);
    }

    @Override public Component customName() {
        Text name = snapshot.getCustomName();
        return name == null ? null : GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(name));
    }

    @Override public void customName(Component customName) {
        snapshot.setCustomName(customName == null ? null
                : Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(customName)));
    }

    @Override public String getCustomName() {
        Text name = snapshot.getCustomName();
        return name == null ? null : CraftChatMessage.fromComponent(name);
    }

    @Override public void setCustomName(String name) {
        snapshot.setCustomName(CraftChatMessage.fromStringOrNull(name));
    }

    @Override public boolean update(boolean force, boolean applyPhysics) {
        if (!super.update(force, applyPhysics)) return false;
        if (!isPlaced()) return true;
        if (world.getBlockEntity(position) instanceof LockableContainerBlockEntity live && live.getClass() == tileEntity.getClass()) {
            live.setCustomName(snapshot.getCustomName());
        }
        return true;
    }
}
