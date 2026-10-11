package org.bukkit.craftbukkit.v1_20_R1.block;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;

public abstract class CraftNameableBlockEntityState<T extends BlockEntity & Nameable>
        extends CraftBlockEntityState<T> implements org.bukkit.Nameable {
    protected CraftNameableBlockEntityState(ServerWorld world, BlockPos position, BlockState state, T tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    protected abstract void setNativeCustomName(Text name);
    protected abstract void setLiveCustomName(T tile, Text name);

    @Override public Component customName() {
        Text name = snapshot.getCustomName();
        return name == null ? null : GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(name));
    }

    @Override public void customName(Component name) {
        setNativeCustomName(name == null ? null : Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(name)));
    }

    @Override public String getCustomName() {
        Text name = snapshot.getCustomName();
        return name == null ? null : CraftChatMessage.fromComponent(name);
    }

    @Override public void setCustomName(String name) { setNativeCustomName(CraftChatMessage.fromStringOrNull(name)); }

    @SuppressWarnings("unchecked")
    @Override public boolean update(boolean force, boolean applyPhysics) {
        if (!super.update(force, applyPhysics)) return false;
        if (!isPlaced()) return true;
        BlockEntity live = world.getBlockEntity(position);
        if (live != null && live.getClass() == tileEntity.getClass()) setLiveCustomName((T) live, snapshot.getCustomName());
        return true;
    }
}
