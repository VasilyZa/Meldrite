package org.bukkit.craftbukkit.conversations;

import java.util.LinkedList;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ManuallyAbandonedConversationCanceller;

/** Serializes console conversations in the same order as CraftBukkit. */
public class ConversationTracker {
    private LinkedList<Conversation> queue = new LinkedList<>();
    public synchronized boolean beginConversation(Conversation conversation) {
        if (conversation == null || queue.contains(conversation)) return true;
        queue.addLast(conversation);
        if (queue.getFirst() == conversation) { conversation.begin(); conversation.outputNextPrompt(); }
        return true;
    }
    public synchronized void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        if (queue.isEmpty()) return;
        if (queue.getFirst() == conversation) conversation.abandon(details);
        queue.remove(conversation);
        if (!queue.isEmpty()) queue.getFirst().outputNextPrompt();
    }
    public synchronized void abandonAllConversations() {
        LinkedList<Conversation> old = queue;
        queue = new LinkedList<>();
        for (Conversation conversation : old) try {
            conversation.abandon(new ConversationAbandonedEvent(conversation, new ManuallyAbandonedConversationCanceller()));
        } catch (Throwable failure) { Bukkit.getLogger().log(Level.SEVERE, "Unexpected exception while abandoning a conversation", failure); }
    }
    public synchronized void acceptConversationInput(String input) {
        if (!queue.isEmpty()) try { queue.getFirst().acceptInput(input); }
        catch (Throwable failure) { Bukkit.getLogger().log(Level.WARNING, "Conversation input failed", failure); }
    }
    public synchronized boolean isConversing() { return !queue.isEmpty(); }
    public synchronized boolean isConversingModaly() { return !queue.isEmpty() && queue.getFirst().isModal(); }
}
