package org.bukkit.craftbukkit.scheduler;

import org.bukkit.scheduler.BukkitScheduler;

/** Async scheduler ABI alias sharing the profile scheduler implementation. */
public class CraftAsyncScheduler extends CraftScheduler {
    public CraftAsyncScheduler() { super(); }
    public CraftAsyncScheduler(BukkitScheduler delegate) { super(delegate); }
}
