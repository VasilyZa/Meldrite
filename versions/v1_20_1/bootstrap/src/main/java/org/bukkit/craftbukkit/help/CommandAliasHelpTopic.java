package org.bukkit.craftbukkit.help;

import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;

public class CommandAliasHelpTopic extends HelpTopic {
    private final String aliasFor;
    private final HelpMap helpMap;

    public CommandAliasHelpTopic(String alias, String aliasFor, HelpMap helpMap) {
        this.aliasFor = aliasFor.startsWith("/") ? aliasFor : "/" + aliasFor;
        this.helpMap = helpMap;
        this.name = alias.startsWith("/") ? alias : "/" + alias;
        Preconditions.checkArgument(!name.equals(this.aliasFor), "Command %s cannot be alias for itself", name);
        this.shortText = ChatColor.YELLOW + "Alias for " + ChatColor.WHITE + this.aliasFor;
    }

    @Override public String getFullText(CommandSender sender) {
        Preconditions.checkArgument(sender != null, "CommandSender cannot be null");
        StringBuilder result = new StringBuilder(shortText);
        HelpTopic topic = helpMap.getHelpTopic(aliasFor);
        if (topic != null) result.append('\n').append(topic.getFullText(sender));
        return result.toString();
    }

    @Override public boolean canSee(CommandSender sender) {
        Preconditions.checkArgument(sender != null, "CommandSender cannot be null");
        if (amendedPermission != null) return sender.hasPermission(amendedPermission);
        HelpTopic topic = helpMap.getHelpTopic(aliasFor);
        return topic != null && topic.canSee(sender);
    }
}
