package dev.meldrite.paper.scheduler;
public final class SchedulerAdapter implements org.bukkit.scheduler.BukkitScheduler {
  private final dev.meldrite.scheduler.TickScheduler ticks; private final dev.meldrite.scheduler.AsyncScheduler async;
  private final java.util.function.Function<org.bukkit.plugin.Plugin,dev.meldrite.plugin.spi.PluginScope> scopes;
  private final java.util.function.Function<String,org.bukkit.plugin.Plugin> plugins;
  private final dev.meldrite.plugin.classloading.PluginLoaderEngine engine;
  private final java.util.Map<org.bukkit.plugin.Plugin,dev.meldrite.scheduler.TaskOwner> owners=new java.util.IdentityHashMap<>();
  public SchedulerAdapter(dev.meldrite.scheduler.TickScheduler ticks,dev.meldrite.scheduler.AsyncScheduler async,java.util.function.Function<org.bukkit.plugin.Plugin,dev.meldrite.plugin.spi.PluginScope> scopes,java.util.function.Function<String,org.bukkit.plugin.Plugin> plugins,dev.meldrite.plugin.classloading.PluginLoaderEngine engine){this.ticks=java.util.Objects.requireNonNull(ticks);this.async=java.util.Objects.requireNonNull(async);this.scopes=java.util.Objects.requireNonNull(scopes);this.plugins=java.util.Objects.requireNonNull(plugins);this.engine=java.util.Objects.requireNonNull(engine);}
  private synchronized dev.meldrite.scheduler.TaskOwner owner(org.bukkit.plugin.Plugin plugin){
    java.util.Objects.requireNonNull(plugin);if(!plugin.isEnabled())throw new org.bukkit.plugin.IllegalPluginAccessException("Plugin is not enabled: "+plugin.getName());
    var existing=owners.get(plugin);if(existing!=null)return existing;
    var scope=java.util.Objects.requireNonNull(scopes.apply(plugin),"Unknown plugin scope");var owner=new dev.meldrite.scheduler.TaskOwner(plugin.getName(),scope);
    scope.register(()->{synchronized(this){owners.remove(plugin,owner);}});owners.put(plugin,owner);return owner;
  }
  private dev.meldrite.plugin.classloading.PluginRealm realm(org.bukkit.plugin.Plugin plugin){var loader=plugin.getClass().getClassLoader();if(loader instanceof dev.meldrite.plugin.classloading.PluginRealm realm)return realm;throw new UnsupportedOperationException("UNSUPPORTED_PLUGIN_REALM: "+loader);}
  private <T> T owned(org.bukkit.plugin.Plugin plugin,java.util.concurrent.Callable<T> callback)throws Exception{return engine.withContext(realm(plugin),callback);}
  private org.bukkit.scheduler.BukkitTask task(dev.meldrite.scheduler.TaskHandle handle){org.bukkit.plugin.Plugin plugin=plugins.apply(handle.owner().identity());if(plugin==null)throw new IllegalStateException("TASK_OWNER_UNAVAILABLE: "+handle.owner().identity());return new org.bukkit.scheduler.BukkitTask(){public int getTaskId(){return handle.id();}public org.bukkit.plugin.Plugin getOwner(){return plugin;}public boolean isSync(){return handle.synchronous();}public boolean isCancelled(){return handle.cancelled();}public void cancel(){handle.cancel();}};}
  private org.bukkit.scheduler.BukkitTask schedule(org.bukkit.plugin.Plugin plugin,Runnable runnable,long delay,long period,boolean asynchronous){java.util.Objects.requireNonNull(runnable);return schedule(plugin,task->runnable.run(),delay,period,asynchronous);}
  private org.bukkit.scheduler.BukkitTask schedule(org.bukkit.plugin.Plugin plugin,java.util.function.Consumer<org.bukkit.scheduler.BukkitTask> callback,long delay,long period,boolean asynchronous){
    java.util.Objects.requireNonNull(callback);var owner=owner(plugin);long normalizedDelay=Math.max(0,delay),normalizedPeriod=period==0?1:Math.max(-1,period);
    java.util.function.Consumer<dev.meldrite.scheduler.TaskHandle> operation=handle->{var task=task(handle);try{owned(plugin,()->{callback.accept(task);return task;});}catch(RuntimeException|Error failure){throw failure;}catch(Exception failure){throw new java.util.concurrent.CompletionException(failure);}};
    var handle=asynchronous?ticks.scheduleAsync(owner,normalizedDelay,normalizedPeriod,operation,async):ticks.schedule(owner,normalizedDelay,normalizedPeriod,operation);return task(handle);
  }
  private <T> java.util.concurrent.Future<T> syncCall(org.bukkit.plugin.Plugin plugin,java.util.concurrent.Callable<T> callback){java.util.Objects.requireNonNull(callback);return ticks.callSync(owner(plugin),()->owned(plugin,callback));}
  private void cancel(int id){for(var handle:ticks.tasks())if(handle.id()==id)handle.cancel();}
  private void cancelOwned(org.bukkit.plugin.Plugin plugin){java.util.Objects.requireNonNull(plugin);for(var handle:ticks.tasks())if(handle.owner().identity().equals(plugin.getName()))handle.cancel();}
  private java.util.List<org.bukkit.scheduler.BukkitTask> pendingTasks(){return ticks.tasks().stream().map(this::task).toList();}
  private java.util.List<org.bukkit.scheduler.BukkitWorker> activeWorkers(){return async.workers().stream().map(worker->{var task=task(worker.task());return (org.bukkit.scheduler.BukkitWorker)new org.bukkit.scheduler.BukkitWorker(){public int getTaskId(){return task.getTaskId();}public org.bukkit.plugin.Plugin getOwner(){return task.getOwner();}public Thread getThread(){return worker.thread();}};}).toList();}
  private java.util.concurrent.Executor executor(org.bukkit.plugin.Plugin plugin){java.util.Objects.requireNonNull(plugin);return command->schedule(plugin,java.util.Objects.requireNonNull(command),0,-1,false);}
  @Override public <T extends java.lang.Object> java.util.concurrent.Future<T> callSyncMethod(org.bukkit.plugin.Plugin p0, java.util.concurrent.Callable<T> p1) { return syncCall(p0,p1); }
  @Override public void cancelTask(int p0) { cancel(p0); }
  @Override public void cancelTasks(org.bukkit.plugin.Plugin p0) { cancelOwned(p0); }
  @Override public java.util.List<org.bukkit.scheduler.BukkitWorker> getActiveWorkers() { return activeWorkers(); }
  @Override public java.util.concurrent.Executor getMainThreadExecutor(org.bukkit.plugin.Plugin p0) { return executor(p0); }
  @Override public java.util.List<org.bukkit.scheduler.BukkitTask> getPendingTasks() { return pendingTasks(); }
  @Override public boolean isCurrentlyRunning(int p0) { return ticks.running(p0); }
  @Override public boolean isQueued(int p0) { return ticks.queued(p0); }
  @Override public org.bukkit.scheduler.BukkitTask runTask(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1) throws java.lang.IllegalArgumentException { return schedule(p0,p1,0,-1,false); }
  @Override public void runTask(org.bukkit.plugin.Plugin p0, java.util.function.Consumer<org.bukkit.scheduler.BukkitTask> p1) throws java.lang.IllegalArgumentException { schedule(p0,p1,0,-1,false); }
  @Override public org.bukkit.scheduler.BukkitTask runTask(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1) throws java.lang.IllegalArgumentException { return schedule(p0,p1,0,-1,false); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskAsynchronously(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1) throws java.lang.IllegalArgumentException { return schedule(p0,p1,0,-1,true); }
  @Override public void runTaskAsynchronously(org.bukkit.plugin.Plugin p0, java.util.function.Consumer<org.bukkit.scheduler.BukkitTask> p1) throws java.lang.IllegalArgumentException { schedule(p0,p1,0,-1,true); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskAsynchronously(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1) throws java.lang.IllegalArgumentException { return schedule(p0,p1,0,-1,true); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskLater(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,-1,false); }
  @Override public void runTaskLater(org.bukkit.plugin.Plugin p0, java.util.function.Consumer<org.bukkit.scheduler.BukkitTask> p1, long p2) throws java.lang.IllegalArgumentException { schedule(p0,p1,p2,-1,false); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskLater(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1, long p2) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,-1,false); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskLaterAsynchronously(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,-1,true); }
  @Override public void runTaskLaterAsynchronously(org.bukkit.plugin.Plugin p0, java.util.function.Consumer<org.bukkit.scheduler.BukkitTask> p1, long p2) throws java.lang.IllegalArgumentException { schedule(p0,p1,p2,-1,true); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskLaterAsynchronously(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1, long p2) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,-1,true); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskTimer(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2, long p3) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,p3,false); }
  @Override public void runTaskTimer(org.bukkit.plugin.Plugin p0, java.util.function.Consumer<org.bukkit.scheduler.BukkitTask> p1, long p2, long p3) throws java.lang.IllegalArgumentException { schedule(p0,p1,p2,p3,false); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskTimer(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1, long p2, long p3) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,p3,false); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskTimerAsynchronously(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2, long p3) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,p3,true); }
  @Override public void runTaskTimerAsynchronously(org.bukkit.plugin.Plugin p0, java.util.function.Consumer<org.bukkit.scheduler.BukkitTask> p1, long p2, long p3) throws java.lang.IllegalArgumentException { schedule(p0,p1,p2,p3,true); }
  @Override public org.bukkit.scheduler.BukkitTask runTaskTimerAsynchronously(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1, long p2, long p3) throws java.lang.IllegalArgumentException { return schedule(p0,p1,p2,p3,true); }
  @Override public int scheduleAsyncDelayedTask(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1) { return schedule(p0,p1,0,-1,true).getTaskId(); }
  @Override public int scheduleAsyncDelayedTask(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2) { return schedule(p0,p1,p2,-1,true).getTaskId(); }
  @Override public int scheduleAsyncRepeatingTask(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2, long p3) { return schedule(p0,p1,p2,p3,true).getTaskId(); }
  @Override public int scheduleSyncDelayedTask(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1) { return schedule(p0,p1,0,-1,false).getTaskId(); }
  @Override public int scheduleSyncDelayedTask(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2) { return schedule(p0,p1,p2,-1,false).getTaskId(); }
  @Override public int scheduleSyncDelayedTask(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1) { return schedule(p0,p1,0,-1,false).getTaskId(); }
  @Override public int scheduleSyncDelayedTask(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1, long p2) { return schedule(p0,p1,p2,-1,false).getTaskId(); }
  @Override public int scheduleSyncRepeatingTask(org.bukkit.plugin.Plugin p0, java.lang.Runnable p1, long p2, long p3) { return schedule(p0,p1,p2,p3,false).getTaskId(); }
  @Override public int scheduleSyncRepeatingTask(org.bukkit.plugin.Plugin p0, org.bukkit.scheduler.BukkitRunnable p1, long p2, long p3) { return schedule(p0,p1,p2,p3,false).getTaskId(); }
}
