package org.bukkit.craftbukkit.scheduler;

import org.bukkit.plugin.Plugin;

/** Linked debug record used by scheduler diagnostics. */
class CraftAsyncDebugger {
    private CraftAsyncDebugger next;
    private final int expiry;
    private final Plugin plugin;
    private final Class<?> taskClass;
    CraftAsyncDebugger(int expiry, Plugin plugin, Class<?> taskClass) { this.expiry = expiry; this.plugin = plugin; this.taskClass = taskClass; }
    final CraftAsyncDebugger getNextHead(int tick) { CraftAsyncDebugger current = this; while (current.next != null && tick > current.expiry) current = current.next; return current; }
    final CraftAsyncDebugger setNext(CraftAsyncDebugger value) { return next = value; }
    StringBuilder debugTo(StringBuilder out) { for (CraftAsyncDebugger current = this; current != null; current = current.next) if (current.plugin != null && current.taskClass != null) out.append(current.plugin.getName()).append(':').append(current.taskClass.getName()).append('@').append(current.expiry).append(','); return out; }
}
