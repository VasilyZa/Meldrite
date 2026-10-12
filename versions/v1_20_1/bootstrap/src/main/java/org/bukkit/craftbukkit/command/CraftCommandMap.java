package org.bukkit.craftbukkit.command;

import java.util.Map;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.PluginsCommand;

/** Exposes the same known-command map that CraftBukkit plugins inspect. */
public class CraftCommandMap extends SimpleCommandMap {
    public CraftCommandMap(Server server) {
        super(server);
        register("bukkit", new PluginsCommand("plugins"));
    }
    @Override public void clearCommands() {
        super.clearCommands();
        register("bukkit", new PluginsCommand("plugins"));
    }
    public Map<String, Command> getKnownCommands() { return knownCommands; }
}
