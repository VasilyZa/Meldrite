package org.bukkit.craftbukkit.util.permissions;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;
/** Registers the vanilla command permission tree used by Bukkit's operator checks. */
public final class CommandPermissions {
    private static final String ROOT = "minecraft.command"; private static final String PREFIX = ROOT + ".";
    private static final String[] OP_COMMANDS = { "kill", "say", "give", "teleport", "kick", "stop", "list", "experience", "defaultgamemode", "seed", "effect", "selector", "attribute", "advancement", "ban", "ban-ip", "banlist", "bossbar", "clear", "clone", "damage", "data", "datapack", "debug", "deop", "difficulty", "enchant", "execute", "fill", "fillbiome", "forceload", "function", "gamerule", "jfr", "locate", "loot", "op", "pardon", "pardon-ip", "particle", "perf", "playsound", "ride", "recipe", "reload", "item", "save-all", "save-off", "save-on", "schedule", "scoreboard", "setblock", "setidletimeout", "setworldspawn", "spawnpoint", "spectate", "spreadplayers", "stopsound", "summon", "tag", "team", "tellraw", "time", "title", "weather", "whitelist", "worldborder", "place", "return" };
    private CommandPermissions() {}
    public static Permission registerPermissions(Permission parent) {
        Permission commands = DefaultPermissions.registerPermission(ROOT, "Gives the user the ability to use all vanilla minecraft commands", parent);
        for (String command : OP_COMMANDS) DefaultPermissions.registerPermission(PREFIX + command, "Allows the user to use the /" + command + " command", PermissionDefault.OP, commands);
        for (String command : new String[]{"me", "msg", "help", "trigger", "teammsg"}) DefaultPermissions.registerPermission(PREFIX + command, "Allows the user to use the /" + command + " command", PermissionDefault.TRUE, commands);
        DefaultPermissions.registerPermission("minecraft.admin.command_feedback", "Receive command broadcasts when sendCommandFeedback is true", PermissionDefault.OP, commands);
        commands.recalculatePermissibles(); return commands;
    }
}
