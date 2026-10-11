package dev.meldrite.service;

import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;

/** One neutral permission graph and attachment order; native operator state is an external port. */
public final class PermissionService implements AutoCloseable {
    public enum Default {TRUE,FALSE,OP,NOT_OP;public boolean value(boolean op){return this==TRUE||this==OP&&op||this==NOT_OP&&!op;}}
    public interface OperatorState {boolean isOperator();void setOperator(boolean operator);}
    public record Definition(String name,Default defaultValue,Map<String,Boolean> children){public Definition{name=normalize(name);Objects.requireNonNull(defaultValue);children=values(children);}}
    public record Effective(boolean value,Optional<UUID> attachment){public Effective{Objects.requireNonNull(attachment);}}
    private final ExecutionPort execution;private final Runnable changed;private final Map<String,Definition> definitions=new LinkedHashMap<>();private final Map<String,Subject> subjects=new LinkedHashMap<>();private boolean closed;
    public PermissionService(ExecutionPort execution,Runnable changed){this.execution=Objects.requireNonNull(execution);this.changed=Objects.requireNonNull(changed);}
    private static String normalize(String name){Objects.requireNonNull(name);return name.toLowerCase(Locale.ROOT);}
    private static Map<String,Boolean> values(Map<String,Boolean> input){var result=new LinkedHashMap<String,Boolean>();Objects.requireNonNull(input).forEach((name,value)->result.put(normalize(name),Objects.requireNonNull(value)));return Collections.unmodifiableMap(result);}
    private void primary(){if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_PERMISSION_MUTATION");}
    private void open(){if(closed)throw new IllegalStateException("PERMISSION_SERVICE_CLOSED");}
    public void define(Definition definition){
        synchronized(this){primary();open();Objects.requireNonNull(definition);var candidate=new LinkedHashMap<>(definitions);candidate.put(definition.name(),definition);for(String name:candidate.keySet())acyclic(candidate,name,new LinkedHashSet<>());definitions.clear();definitions.putAll(candidate);}changed.run();
    }
    private static void acyclic(Map<String,Definition> graph,String name,Set<String> path){
        if(!path.add(name))throw new IllegalArgumentException("CYCLIC_PERMISSION: "+path+" -> "+name);if(path.size()>512)throw new IllegalArgumentException("PERMISSION_GRAPH_TOO_DEEP");Definition definition=graph.get(name);if(definition!=null)for(String child:definition.children().keySet())acyclic(graph,child,path);path.remove(name);
    }
    public synchronized Subject subject(String identity,OperatorState operator){
        primary();open();Objects.requireNonNull(identity);Objects.requireNonNull(operator);if(identity.isBlank())throw new IllegalArgumentException("Empty permission subject");if(subjects.containsKey(identity))throw new IllegalStateException("PERMISSION_SUBJECT_ALREADY_REGISTERED: "+identity);var subject=new Subject(identity,operator);subjects.put(identity,subject);return subject;
    }
    public final class Attachment implements AutoCloseable {
        private final UUID id=UUID.randomUUID();private final Subject subject;private final String owner;private final PluginScope scope;
        private Map<String,Boolean> values;private Optional<Runnable> removed;private boolean closed;
        private Attachment(Subject subject,String owner,PluginScope scope,Map<String,Boolean> values,Runnable removed){this.subject=subject;this.owner=owner;this.scope=scope;this.values=PermissionService.values(values);this.removed=Optional.of(removed);}
        public UUID id(){return id;}
        public void replace(Map<String,Boolean> replacement){synchronized(PermissionService.this){primary();subject.live();if(closed||scope.isClosed())throw new IllegalStateException("OWNER_DISABLED: "+owner);values=PermissionService.values(replacement);}subject.recalculate();}
        @Override public void close(){Optional<Runnable> callback;synchronized(PermissionService.this){
            primary();if(closed)return;closed=true;subject.attachments.remove(id);values=Map.of();callback=removed;removed=Optional.empty();
        }Throwable failure=null;try{callback.ifPresent(Runnable::run);}catch(Throwable caught){failure=caught;}try{changed.run();}catch(Throwable caught){if(failure==null)failure=caught;else if(failure!=caught)failure.addSuppressed(caught);}if(failure!=null)throw propagate(failure);}
    }
    private static RuntimeException propagate(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();if(failure instanceof Error error)throw error;return failure instanceof RuntimeException runtime?runtime:new IllegalStateException("PERMISSION_CALLBACK_FAILED",failure);}
    public final class Subject implements AutoCloseable {
        private final String identity;private final OperatorState operator;private final Map<UUID,Attachment> attachments=new LinkedHashMap<>();private boolean closed;
        private Subject(String identity,OperatorState operator){this.identity=identity;this.operator=operator;}
        private void live(){open();if(closed)throw new IllegalStateException("PERMISSION_SUBJECT_CLOSED");}
        public boolean isOperator(){synchronized(PermissionService.this){live();return operator.isOperator();}}
        public void requirePrimaryMutation(){synchronized(PermissionService.this){primary();live();}}
        public void setOperator(boolean value){requirePrimaryMutation();operator.setOperator(value);recalculate();}
        public boolean has(String name){synchronized(PermissionService.this){live();String key=normalize(name);Definition definition=definitions.get(key);return has(key,definition==null?Default.OP:definition.defaultValue());}}
        public boolean has(String name,Default fallback){synchronized(PermissionService.this){live();Objects.requireNonNull(fallback);Effective value=calculate().get(normalize(name));return value==null?fallback.value(operator.isOperator()):value.value();}}
        public boolean isSet(String name){synchronized(PermissionService.this){live();return calculate().containsKey(normalize(name));}}
        public Map<String,Effective> effective(){synchronized(PermissionService.this){live();return Collections.unmodifiableMap(calculate());}}
        private Map<String,Effective> calculate(){
            var result=new LinkedHashMap<String,Effective>();boolean op=operator.isOperator();
            for(Definition definition:definitions.values())if(definition.defaultValue().value(op)){result.put(definition.name(),new Effective(true,Optional.empty()));expand(result,definition.children(),false,Optional.empty());}
            for(Attachment attachment:attachments.values())if(!attachment.closed&&!attachment.scope.isClosed())expand(result,attachment.values,false,Optional.of(attachment.id));return result;
        }
        private void expand(Map<String,Effective> result,Map<String,Boolean> children,boolean invert,Optional<UUID> attachment){
            for(var entry:children.entrySet()){boolean value=entry.getValue()^invert;result.put(entry.getKey(),new Effective(value,attachment));Definition definition=definitions.get(entry.getKey());if(definition!=null)expand(result,definition.children(),!value,attachment);}
        }
        public Attachment attach(String owner,PluginScope scope,Map<String,Boolean> values,Runnable removed){Attachment attachment;synchronized(PermissionService.this){
            primary();live();Objects.requireNonNull(owner);Objects.requireNonNull(scope);Objects.requireNonNull(removed);if(owner.isBlank())throw new IllegalArgumentException("Empty attachment owner");if(scope.isClosed())throw new IllegalStateException("OWNER_DISABLED");
            attachment=new Attachment(this,owner,scope,values,removed);attachments.put(attachment.id,attachment);
        }try{scope.register(attachment);recalculate();return attachment;}catch(RuntimeException|Error failure){try{attachment.close();}catch(RuntimeException|Error cleanup){if(cleanup!=failure)failure.addSuppressed(cleanup);}throw failure;}}
        public void recalculate(){synchronized(PermissionService.this){primary();live();calculate();}changed.run();}
        @Override public void close(){List<Attachment> remaining;synchronized(PermissionService.this){primary();if(closed)return;closed=true;subjects.remove(identity,this);remaining=List.copyOf(attachments.values());}
            RuntimeException aggregate=null;for(Attachment attachment:remaining)try{attachment.close();}catch(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();if(aggregate==null)aggregate=new IllegalStateException("PERMISSION_CLEANUP_FAILED");aggregate.addSuppressed(failure);}if(aggregate!=null)throw aggregate;
        }
    }
    @Override public void close(){List<Subject> remaining;synchronized(this){primary();if(closed)return;closed=true;remaining=List.copyOf(subjects.values());definitions.clear();subjects.clear();}RuntimeException aggregate=null;for(Subject subject:remaining)try{subject.close();}catch(RuntimeException|Error failure){if(aggregate==null)aggregate=new IllegalStateException("PERMISSION_CLEANUP_FAILED");aggregate.addSuppressed(failure);}if(aggregate!=null)throw aggregate;}
}
