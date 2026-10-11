package org.bukkit.craftbukkit.command;

import java.util.UUID;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.util.CraftChatMessage;

/** Bukkit command sender backed by a native command block. */
public class CraftBlockCommandSender extends ServerCommandSender implements BlockCommandSender {
    private final ServerCommandSource block;
    private final BlockEntity tile;

    public CraftBlockCommandSender(ServerCommandSource block, BlockEntity tile) {
        this.block = java.util.Objects.requireNonNull(block, "block");
        this.tile = java.util.Objects.requireNonNull(tile, "tile");
    }

    public ServerCommandSource getWrapper() { return block; }

    @Override public Block getBlock() {
        if (tile.getWorld() == null) throw new IllegalStateException("Command block is not attached to a world");
        return CraftBlock.at(tile.getWorld(), tile.getPos());
    }

    @Override public void sendMessage(String message) {
        for (net.minecraft.text.Text component : CraftChatMessage.fromString(message)) block.sendMessage(component);
    }

    @Override public void sendMessage(String... messages) {
        if (messages != null) for (String message : messages) sendMessage(message);
    }

    @Override public void sendMessage(UUID sender, String message) { sendMessage(message); }
    @Override public void sendMessage(UUID sender, String... messages) { sendMessage(messages); }
    @Override public String getName() { return block.getName(); }
    @Override public net.kyori.adventure.text.Component name() {
        return net.kyori.adventure.text.Component.text(block.getDisplayName().getString());
    }
    @Override public boolean isOp() { return true; }
    @Override public void setOp(boolean value) { throw new UnsupportedOperationException("Cannot change operator status of a block"); }
}
