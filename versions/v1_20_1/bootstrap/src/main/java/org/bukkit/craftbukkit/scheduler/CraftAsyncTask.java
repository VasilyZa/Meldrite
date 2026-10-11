package org.bukkit.craftbukkit.scheduler;

import java.util.Map;
import org.bukkit.plugin.Plugin;

/** Async task type retained for Paper scheduler reflection. */
class CraftAsyncTask extends CraftTask {
    CraftAsyncTask(Map<Integer, CraftTask> ignored, Plugin plugin, Object task, int id, long delay) { super(plugin, task, id, delay); }
    @Override public boolean isSync() { return false; }
}
