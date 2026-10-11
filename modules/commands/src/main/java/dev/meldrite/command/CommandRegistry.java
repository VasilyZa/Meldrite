package dev.meldrite.command;
import dev.meldrite.bridge.*;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
public final class CommandRegistry implements AutoCloseable {
    @FunctionalInterface public interface Executor {boolean execute(CommandSource source,String label,List<String> arguments)throws Exception;}
    @FunctionalInterface public interface Completer {Optional<List<String>> complete(CommandSource source,String label,List<String> arguments)throws Exception;}
    public record Definition(CommandKey key,List<String> aliases,boolean primaryOverridesAlias,PermissionPolicy permission,Executor executor,Completer completer){public Definition{Objects.requireNonNull(key);aliases=aliases.stream().map(CommandKey::normalize).distinct().toList();Objects.requireNonNull(permission);Objects.requireNonNull(executor);Objects.requireNonNull(completer);}}
    public final class Registration implements AutoCloseable {
        private final Definition definition;private final PluginScope scope;private boolean removed;
        private Registration(Definition definition,PluginScope scope){this.definition=definition;this.scope=scope;}
        public Definition definition(){return definition;}public boolean active(){synchronized(CommandRegistry.this){return !removed&&!scope.isClosed();}}
        public void close(){remove(this);}
    }
    private record Route(Registration registration,boolean alias){}
    private final ExecutionPort execution;private final CommandTreePort tree;private final Map<String,Route> routes=new LinkedHashMap<>();
    private long revision;private boolean closed,publishing;
    public CommandRegistry(ExecutionPort execution,CommandTreePort tree){this.execution=Objects.requireNonNull(execution);this.tree=Objects.requireNonNull(tree);}
    private void mutation(){if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_COMMAND_MUTATION");if(publishing)throw new IllegalStateException("REENTRANT_COMMAND_PUBLICATION");}
    public synchronized void requirePrimaryMutation(){mutation();}
    private CommandTreeSnapshot snapshot(Map<String,Route> candidate,long nextRevision){return new CommandTreeSnapshot(nextRevision,candidate.entrySet().stream().filter(entry->entry.getValue().registration.active()).map(entry->new CommandTreeSnapshot.Node(entry.getKey(),entry.getValue().registration.definition.key().owner(),entry.getValue().registration.definition.permission().requirements())).toList());}
    private void publish(Map<String,Route> candidate,long nextRevision){publishing=true;try{tree.publish(snapshot(candidate,nextRevision));}finally{publishing=false;}}
    public synchronized Registration register(PluginScope scope,Definition definition){
        mutation();Objects.requireNonNull(scope);Objects.requireNonNull(definition);if(closed)throw new IllegalStateException("COMMAND_REGISTRY_CLOSED");if(scope.isClosed())throw new IllegalStateException("OWNER_DISABLED");
        var registration=new Registration(definition,scope);var candidate=new LinkedHashMap<>(routes);var key=definition.key();
        route(candidate,key.namespace(),key.name(),registration,false,definition.primaryOverridesAlias());for(String alias:definition.aliases())route(candidate,key.namespace(),alias,registration,true,false);
        long nextRevision=Math.addExact(revision,1);publish(candidate,nextRevision);routes.clear();routes.putAll(candidate);revision=nextRevision;scope.register(registration);return registration;
    }
    private void route(Map<String,Route> candidate,String namespace,String label,Registration registration,boolean alias,boolean primaryOverridesAlias){
        candidate.put(namespace+":"+label,new Route(registration,alias));Route previous=candidate.get(label);
        if(previous==null||(!alias&&primaryOverridesAlias&&previous.alias))candidate.put(label,new Route(registration,alias));
    }
    private synchronized void remove(Registration registration){
        mutation();if(registration.removed)return;registration.removed=true;routes.entrySet().removeIf(entry->entry.getValue().registration==registration);revision=Math.addExact(revision,1);publish(routes,revision);
    }
    public synchronized Optional<Registration> resolve(String route){Objects.requireNonNull(route);Route result=routes.get(route.toLowerCase(Locale.ROOT));return result==null||!result.registration.active()?Optional.empty():Optional.of(result.registration);}
    public synchronized Map<String,Registration> routes(){var result=new LinkedHashMap<String,Registration>();routes.forEach((name,route)->{if(route.registration.active())result.put(name,route.registration);});return Collections.unmodifiableMap(result);}
    public synchronized void refresh(){mutation();if(closed)throw new IllegalStateException("COMMAND_REGISTRY_CLOSED");long nextRevision=Math.addExact(revision,1);publish(routes,nextRevision);revision=nextRevision;}
    public synchronized void clear(){mutation();for(var route:routes.values())route.registration.removed=true;routes.clear();revision=Math.addExact(revision,1);publish(routes,revision);}
    public synchronized void close(){mutation();if(closed)return;clear();closed=true;}
}
