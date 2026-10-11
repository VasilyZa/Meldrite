package org.bukkit.craftbukkit.help;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.help.HelpTopic;

public class CustomHelpTopic extends HelpTopic {
    private final String permissionNode;

    public CustomHelpTopic(String name, String shortText, String fullText, String permissionNode) {
        this.name = name;
        this.shortText = shortText;
        this.fullText = shortText + "\n" + fullText;
        this.permissionNode = permissionNode;
    }

    @Override public boolean canSee(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || permissionNode.isEmpty() || sender.hasPermission(permissionNode);
    }
}
