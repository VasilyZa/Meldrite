package org.bukkit.craftbukkit.command;

import java.util.Map;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

/** Exposes the same known-command map that CraftBukkit plugins inspect. */
public class CraftCommandMap extends SimpleCommandMap {
    public CraftCommandMap(Server server) { super(server); }
    public Map<String, Command> getKnownCommands() { return knownCommands; }
}
