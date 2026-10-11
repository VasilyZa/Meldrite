package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;

public class CraftCommandBlock extends CraftBlockEntityState<CommandBlockBlockEntity> implements org.bukkit.block.CommandBlock {
    public CraftCommandBlock(ServerWorld world, BlockPos pos, BlockState state, CommandBlockBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    public net.minecraft.world.CommandBlockExecutor getCommandBlockHandle() { return snapshot.getCommandExecutor(); }
    @Override public String getCommand() { return getCommandBlockHandle().getCommand(); }
    @Override public void setCommand(String command) { getCommandBlockHandle().setCommand(command == null ? "" : command); }
    @Override public String getName() { return CraftChatMessage.fromComponent(getCommandBlockHandle().getCustomName()); }
    @Override public void setName(String name) { getCommandBlockHandle().setCustomName(CraftChatMessage.fromStringOrNull(name == null ? "@" : name)); }
    @Override public Component name() { return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(getCommandBlockHandle().getCustomName())); }
    @Override public void name(Component name) { getCommandBlockHandle().setCustomName(name == null ? Text.literal("@") : Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(name))); }
    @Override public Component lastOutput() { var output = getCommandBlockHandle().getLastOutput(); return output == null ? Component.empty() : GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(output)); }
    @Override public void lastOutput(Component value) { getCommandBlockHandle().setLastOutput(value == null ? null : Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(value))); }
    @Override public int getSuccessCount() { return getCommandBlockHandle().getSuccessCount(); }
    @Override public void setSuccessCount(int value) { getCommandBlockHandle().setSuccessCount(value); }
}
