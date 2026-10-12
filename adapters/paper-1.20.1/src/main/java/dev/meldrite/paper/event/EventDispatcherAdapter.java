package dev.meldrite.paper.event;
import dev.meldrite.event.*;
import dev.meldrite.plugin.spi.PluginScope;
import dev.meldrite.plugin.classloading.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
/** HandlerList owns ordered snapshots; common code supplies cancellation and failure handling. */
public final class EventDispatcherAdapter {
    private final PluginLoaderEngine engine;private final EventPolicy policy;private final Consumer<ListenerRegistry.Failure> failures;
    private final ListenerRegistry.OrderedListenerAccess<Event,RegisteredListener> listenerAccess=new ListenerRegistry.OrderedListenerAccess<>() {
        @Override public String owner(RegisteredListener listener){return listener.getPlugin().getName();}
        @Override public boolean enabled(RegisteredListener listener){return listener.getPlugin().isEnabled();}
        @Override public boolean ignoreCancelled(RegisteredListener listener){return listener.isIgnoringCancelled();}
        @Override public boolean cancelled(Event event){return event instanceof Cancellable cancellable&&cancellable.isCancelled();}
        @Override public void call(RegisteredListener listener,Event event)throws Exception {callOwned(listener,event);}
    };
    public EventDispatcherAdapter(EventPolicy policy,Consumer<ListenerRegistry.Failure> failures,PluginLoaderEngine engine){this.engine=Objects.requireNonNull(engine);this.policy=Objects.requireNonNull(policy);this.failures=Objects.requireNonNull(failures);}
    public void callEvent(Event event){
        policy.check(event.isAsynchronous());
        ListenerRegistry.dispatchOrdered(event,event.getHandlers().getRegisteredListeners(),listenerAccess,failures);
    }
    private void callOwned(RegisteredListener listener,Event event)throws Exception {
        ClassLoader loader=listener.getPlugin().getClass().getClassLoader();
        if(!(loader instanceof PluginRealm realm))throw new UnsupportedOperationException("UNSUPPORTED_PLUGIN_REALM: "+loader);
        engine.withContext(realm,()->{listener.callEvent(event);return event;});
    }
    public AutoCloseable registerEvents(Listener listener,Plugin plugin,PluginScope scope){
        if(!plugin.isEnabled())throw new IllegalStateException("PLUGIN_NOT_ENABLED: "+plugin.getName());
        var registered=new ArrayList<Map.Entry<HandlerList,RegisteredListener>>();
        try{for(var entry:createRegisteredListeners(listener,plugin).entrySet()){
            HandlerList handlers=handlerList(entry.getKey());for(RegisteredListener registration:entry.getValue()){handlers.register(registration);registered.add(Map.entry(handlers,registration));}
        }}catch(RuntimeException|Error failure){for(var entry:registered)entry.getKey().unregister(entry.getValue());throw failure;}
        return scope.register(()->{for(var entry:registered)entry.getKey().unregister(entry.getValue());});
    }
    public AutoCloseable registerEvent(Class<? extends Event> eventType,Listener listener,EventPriority priority,
                                       EventExecutor executor,Plugin plugin,boolean ignoreCancelled,PluginScope scope){
        Objects.requireNonNull(eventType);Objects.requireNonNull(listener);Objects.requireNonNull(priority);
        Objects.requireNonNull(executor);Objects.requireNonNull(plugin);Objects.requireNonNull(scope);
        RegisteredListener registered=new RegisteredListener(listener,executor,priority,plugin,ignoreCancelled);
        HandlerList handlers=handlerList(eventType);handlers.register(registered);
        return scope.register(()->handlers.unregister(registered));
    }
    private static HandlerList handlerList(Class<? extends Event> event){
        try{Method method=event.getMethod("getHandlerList");if(!Modifier.isStatic(method.getModifiers())||!HandlerList.class.isAssignableFrom(method.getReturnType()))throw new IllegalArgumentException("Invalid event handler list: "+event);if(!method.trySetAccessible())throw new IllegalAccessException("Event handler list is inaccessible");return Objects.requireNonNull((HandlerList)method.invoke(null));}
        catch(InvocationTargetException failure){throw new IllegalArgumentException("Event handler list failed: "+event,failure.getCause());}
        catch(ReflectiveOperationException failure){throw new IllegalArgumentException("Event has no usable static handler list: "+event,failure);}
    }
    public static Map<Class<? extends Event>,Set<RegisteredListener>> createRegisteredListeners(Listener listener,Plugin plugin){
        Objects.requireNonNull(listener);Objects.requireNonNull(plugin);Map<Class<? extends Event>,Set<RegisteredListener>> result=new LinkedHashMap<>();Set<String> signatures=new HashSet<>();
        for(Class<?> type=listener.getClass();type!=Object.class;type=type.getSuperclass()){
            Method[] methods=type.getDeclaredMethods();Arrays.sort(methods,Comparator.comparing(Method::toGenericString));
            for(Method method:methods){if(method.isBridge()||method.isSynthetic()||!signatures.add(method.getName()+Arrays.toString(method.getParameterTypes())))continue;EventHandler annotation=method.getAnnotation(EventHandler.class);if(annotation==null)continue;
                if(method.getParameterCount()!=1||!Event.class.isAssignableFrom(method.getParameterTypes()[0])||method.getReturnType()!=void.class||Modifier.isStatic(method.getModifiers()))throw new IllegalArgumentException("Invalid annotated event handler: "+method);
                if(!method.trySetAccessible())throw new IllegalArgumentException("Inaccessible event handler: "+method);
                Class<? extends Event> eventType=method.getParameterTypes()[0].asSubclass(Event.class);
                MethodHandle callback;
                try{callback=MethodHandles.lookup().unreflect(method).asType(MethodType.methodType(void.class,Listener.class,Event.class));}
                catch(IllegalAccessException failure){throw new IllegalArgumentException("Inaccessible event handler: "+method,failure);}
                EventExecutor executor=(target,event)->{if(!eventType.isInstance(event))return;try{callback.invokeExact(target,event);}catch(Throwable failure){throw new EventException(failure);}};
                result.computeIfAbsent(eventType,key->new LinkedHashSet<>()).add(new RegisteredListener(listener,executor,annotation.priority(),plugin,annotation.ignoreCancelled()));
            }
        }
        return result;
    }
}
