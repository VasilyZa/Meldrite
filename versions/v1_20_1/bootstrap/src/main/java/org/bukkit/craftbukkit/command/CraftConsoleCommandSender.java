package org.bukkit.craftbukkit.command;

import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.craftbukkit.conversations.ConversationTracker;

/** Console command sender backed by the process logger. */
public class CraftConsoleCommandSender extends ServerCommandSender implements ConsoleCommandSender {
    protected final ConversationTracker conversationTracker = new ConversationTracker();
    public CraftConsoleCommandSender() { super(); }
    @Override public void sendMessage(String message) { if (message != null) for (String line : message.split("\\n", -1)) sendRawMessage(line); }
    @Override public void sendMessage(String... messages) { if (messages != null) for (String message : messages) sendMessage(message); }
    @Override public void sendRawMessage(String message) { if (message != null) Logger.getLogger("Minecraft").info(ChatColor.stripColor(message)); }
    @Override public void sendRawMessage(UUID sender, String message) { sendRawMessage(message); }
    @Override public String getName() { return "CONSOLE"; }
    @Override public boolean isOp() { return true; }
    @Override public void setOp(boolean value) { throw new UnsupportedOperationException("Cannot change operator status of server console"); }
    @Override public boolean beginConversation(Conversation conversation) { return conversationTracker.beginConversation(conversation); }
    @Override public void abandonConversation(Conversation conversation) { conversationTracker.abandonConversation(conversation, new ConversationAbandonedEvent(conversation, new org.bukkit.conversations.ManuallyAbandonedConversationCanceller())); }
    @Override public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) { conversationTracker.abandonConversation(conversation, details); }
    @Override public void acceptConversationInput(String input) { conversationTracker.acceptConversationInput(input); }
    @Override public boolean isConversing() { return conversationTracker.isConversing(); }
    @Override public void sendMessage(net.kyori.adventure.identity.Identity identity, net.kyori.adventure.text.Component message, net.kyori.adventure.audience.MessageType type) {
        sendMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(message));
    }
    @Override public boolean hasPermission(String name) { return true; }
    @Override public boolean hasPermission(org.bukkit.permissions.Permission permission) { return true; }
}
