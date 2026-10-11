package dev.meldrite.plugin.runtime;
import dev.meldrite.foundation.Diagnostic;
import dev.meldrite.plugin.spi.*;
import java.util.*;
/** Serialized lifecycle on the creating thread; failures remain observable and cleanup continues. */
public final class PluginController {
    public enum Step { PREPARE, CONSTRUCT, LOAD, ENABLE, DISABLE, CLEANUP }
    private static final class Entry {
        final PluginHost host;
        final PluginScope scope;
        PluginState state=PluginState.RESOLVED;
        boolean constructed,disableCalled;
        Entry(PluginHost host){this.host=host;this.scope=Objects.requireNonNull(host.scope());}
    }
    private final Thread ownerThread=Thread.currentThread();
    private final LoadPlan plan;
    private final Map<String,Entry> entries=new LinkedHashMap<>();
    private final Set<Step> disableAfterFailure;
    private final List<Diagnostic> diagnostics=new ArrayList<>();
    private final Map<String,Throwable> failures=new LinkedHashMap<>();
    private final Set<String> pendingDisables=new LinkedHashSet<>();
    private final ScopeRegistry scopes;
    private boolean inCallback;
    public PluginController(LoadPlan plan,List<PluginHost> hosts,Set<Step> disableAfterFailure){
        this.plan=Objects.requireNonNull(plan);this.disableAfterFailure=Set.copyOf(disableAfterFailure);Map<String,PluginScope> owned=new LinkedHashMap<>();
        for(PluginHost host:hosts){Objects.requireNonNull(host);String id=host.identity().id();if(entries.putIfAbsent(id,new Entry(host))!=null)throw new IllegalArgumentException("DUPLICATE_PLUGIN_HOST: "+id);owned.put(id,entries.get(id).scope);}
        if(plan.serverOrder().size()!=entries.size()||!new HashSet<>(plan.serverOrder()).equals(entries.keySet()))throw new IllegalArgumentException("HOST_PLAN_MISMATCH");
        for(var dependency:plan.requiredDependents().entrySet()){if(!entries.containsKey(dependency.getKey())||!entries.keySet().containsAll(dependency.getValue()))throw new IllegalArgumentException("UNKNOWN_DEPENDENCY_OWNER");}
        scopes=new ScopeRegistry(owned);
    }
    public ScopeRegistry scopes(){return scopes;}
    public PluginState state(String id){checkThread();return entry(id).state;}
    public List<Diagnostic> diagnostics(){checkThread();return List.copyOf(diagnostics);}
    public Optional<Throwable> failure(String id){checkThread();entry(id);return Optional.ofNullable(failures.get(id));}
    /** Observational only: context-loader ownership cannot identify every thread a plugin creates. */
    public List<Diagnostic> observeLingeringThreads(String id,ClassLoader realm){
        checkThread();entry(id);Objects.requireNonNull(realm);List<Diagnostic> observed=new ArrayList<>();
        for(Thread thread:Thread.getAllStackTraces().keySet())if(thread!=ownerThread&&thread.isAlive()&&thread.getContextClassLoader()==realm){
            Diagnostic diagnostic=new Diagnostic("LINGERING_PLUGIN_THREAD",Optional.empty(),"STOPPING",id+" context-loader thread remains alive: "+thread.getName()+" (id="+thread.getId()+")",null);diagnostics.add(diagnostic);observed.add(diagnostic);
        }
        return List.copyOf(observed);
    }
    public void verifyPreparedHosts(){
        checkOperation();List<String> order=shutdownOrder();Collections.reverse(order);
        for(String id:order){Entry entry=entry(id);if(entry.state!=PluginState.RESOLVED)continue;Optional<Throwable> prepared=entry.host.preparationFailure();if(prepared.isEmpty())continue;
            Throwable failure=prepared.get();record(id,Step.PREPARE,failure);entry.state=PluginState.FAILED;cleanup(id,false,failure);blockDependents(id,failure,new HashSet<>());
        }
    }
    public void loadAll(){verifyPreparedHosts();for(String id:plan.serverOrder())if(state(id)==PluginState.RESOLVED)construct(id);for(String id:plan.serverOrder())if(state(id)==PluginState.CONSTRUCTED)load(id);}
    public void enableAll(){checkThread();for(String id:plan.serverOrder())if(state(id)==PluginState.LOADED)enable(id);}
    public void construct(String id){transition(id,PluginState.RESOLVED,PluginState.CONSTRUCTED,Step.CONSTRUCT,()->entry(id).host.construct());}
    public void load(String id){transition(id,PluginState.CONSTRUCTED,PluginState.LOADED,Step.LOAD,()->entry(id).host.load());}
    public void enable(String id){transition(id,PluginState.LOADED,PluginState.ENABLED,Step.ENABLE,()->entry(id).host.enable());}
    private void transition(String id,PluginState before,PluginState after,Step step,Callback callback){
        checkOperation();Entry entry=entry(id);if(entry.state!=before)throw new IllegalStateException("ILLEGAL_PLUGIN_STATE: "+id+" "+entry.state+" -> "+step);
        try{invoke(callback);if(step==Step.CONSTRUCT)entry.constructed=true;entry.state=after;drainPendingDisables();}
        catch(Throwable failure){record(id,step,failure);entry.state=PluginState.FAILED;cleanup(id,entry.constructed&&disableAfterFailure.contains(step),failure);blockDependents(id,failure,new HashSet<>());}
    }
    private void blockDependents(String provider,Throwable failure,Set<String> visited){
        if(!visited.add(provider))return;
        for(String id:plan.requiredDependents().getOrDefault(provider,Set.of())){
            Entry dependent=entry(id);if(dependent.state==PluginState.FAILED||dependent.state==PluginState.BLOCKED_DEPENDENCY||dependent.state==PluginState.DISABLED)continue;
            boolean disable=dependent.state==PluginState.ENABLED;dependent.state=PluginState.BLOCKED_DEPENDENCY;
            diagnostics.add(new Diagnostic("BLOCKED_DEPENDENCY",Optional.empty(),"DEPENDENCY",id+" requires failed "+provider,failure));cleanup(id,disable,failure);blockDependents(id,failure,visited);
        }
    }
    public void disableAll(){checkOperation();List<String> order=shutdownOrder();for(String id:order)disable(id);}
    private List<String> shutdownOrder(){
        List<String> order=new ArrayList<>();Set<String> seen=new HashSet<>();List<String> reverse=new ArrayList<>(plan.serverOrder());Collections.reverse(reverse);for(String id:reverse)shutdownVisit(id,seen,order);return order;
    }
    private void shutdownVisit(String id,Set<String> seen,List<String> order){if(!seen.add(id))return;for(String dependent:plan.requiredDependents().getOrDefault(id,Set.of()))shutdownVisit(dependent,seen,order);order.add(id);}
    public void disable(String id){
        checkOperation();Entry entry=entry(id);if(entry.state==PluginState.DISABLED||entry.state==PluginState.FAILED||entry.state==PluginState.BLOCKED_DEPENDENCY)return;
        boolean callback=entry.state==PluginState.ENABLED;entry.state=PluginState.DISABLING;Throwable error=cleanup(id,callback,null);entry.state=error==null?PluginState.DISABLED:PluginState.FAILED;
    }
    /** Bukkit permits a plugin to disable itself from onEnable; queue that request until the callback returns. */
    public void disableFromPlugin(String id){
        checkThread();
        if(inCallback){ pendingDisables.add(id); return; }
        disable(id);
    }
    private void drainPendingDisables(){
        if(inCallback || pendingDisables.isEmpty())return;
        var pending=new ArrayList<>(pendingDisables); pendingDisables.clear();
        for(String id:pending)disable(id);
    }
    private Throwable cleanup(String id,boolean disable,Throwable original){
        Entry entry=entry(id);Throwable error=original;
        if(disable&&!entry.disableCalled){entry.disableCalled=true;try{invoke(entry.host::disable);}catch(Throwable failure){record(id,Step.DISABLE,failure);error=combine(error,failure);}}
        try{entry.scope.close();}catch(Throwable failure){record(id,Step.CLEANUP,failure);error=combine(error,failure);}return error;
    }
    private static Throwable combine(Throwable original,Throwable failure){if(original==null)return failure;if(original!=failure)original.addSuppressed(failure);return original;}
    private void record(String id,Step phase,Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();failures.putIfAbsent(id,failure);diagnostics.add(new Diagnostic("PLUGIN_"+phase+"_FAILED",Optional.empty(),phase.name(),id+" failed during "+phase,failure));}
    private Entry entry(String id){Entry entry=entries.get(id);if(entry==null)throw new IllegalArgumentException("UNKNOWN_PLUGIN_OWNER: "+id);return entry;}
    private void checkThread(){if(Thread.currentThread()!=ownerThread)throw new IllegalStateException("LIFECYCLE_WRONG_THREAD");}
    private void checkOperation(){checkThread();if(inCallback)throw new IllegalStateException("LIFECYCLE_REENTRANT_CALL");}
    private void invoke(Callback callback)throws Exception {inCallback=true;try{callback.call();}finally{inCallback=false;}}
    @FunctionalInterface private interface Callback {void call()throws Exception;}
}
