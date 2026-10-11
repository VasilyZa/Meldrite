package org.bukkit.craftbukkit.help;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.MultipleCommandAlias;
import org.bukkit.help.HelpTopic;

public class MultipleCommandAliasHelpTopic extends HelpTopic {
    private final MultipleCommandAlias alias;

    public MultipleCommandAliasHelpTopic(MultipleCommandAlias alias) {
        this.alias = alias;
        name = "/" + alias.getLabel();
        StringBuilder text = new StringBuilder();
        for (Command command : alias.getCommands()) {
            if (text.length() > 0) text.append(ChatColor.GOLD).append(" > ").append(ChatColor.WHITE);
            text.append('/').append(command.getLabel());
        }
        shortText = text.toString();
        fullText = ChatColor.GOLD + "Alias for: " + ChatColor.WHITE + shortText;
    }

    @Override public boolean canSee(CommandSender sender) {
        if (amendedPermission != null) return sender.hasPermission(amendedPermission);
        if (sender instanceof ConsoleCommandSender) return true;
        for (Command command : alias.getCommands()) if (!command.testPermissionSilent(sender)) return false;
        return true;
    }
}
