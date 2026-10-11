package dev.meldrite.paper.configuration;
import dev.meldrite.service.ConfigurationService;
import dev.meldrite.plugin.spi.PluginScope;
import dev.meldrite.plugin.classloading.*;
import dev.meldrite.paper.generated.org.bukkit.configuration.serialization.ConfigurationSerializableAdapter;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
/** Bind actual upstream aliases to owned neutral codecs and release aliases for owned classes. */
public final class ConfigurationCodecRegistry {
    private final ConfigurationService service;private final PluginLoaderEngine engine;private final Function<Plugin,PluginScope> scopes;
    public ConfigurationCodecRegistry(ConfigurationService service,PluginLoaderEngine engine,Function<Plugin,PluginScope> scopes){this.service=Objects.requireNonNull(service);this.engine=Objects.requireNonNull(engine);this.scopes=Objects.requireNonNull(scopes);}
    public <T extends ConfigurationSerializable> AutoCloseable register(Plugin plugin,Class<T> type,String alias){
        if(!(plugin.getClass().getClassLoader() instanceof PluginRealm realm)||type.getClassLoader()!=realm)throw new IllegalArgumentException("SERIALIZABLE_CLASS_OWNER_MISMATCH");
        if(org.bukkit.configuration.serialization.ConfigurationSerialization.getClassByAlias(alias)!=type)throw new IllegalArgumentException("SERIALIZABLE_ALIAS_NOT_REGISTERED_BY_PLUGIN: "+alias);
        Executable factory=factory(type);
        PluginScope owner=scopes.apply(plugin);
        AutoCloseable codec=service.registerCodec(alias,type,owner,new ConfigurationService.Codec<T>(){
            public Map<String,Object> encode(T value){return new ConfigurationSerializableAdapter(engine,realm,value).serialize();}
            public T decode(Map<String,Object> values){try{return engine.withContext(realm,()->{try{Object result=factory instanceof Method method?method.invoke(null,values):((Constructor<?>)factory).newInstance(values);return type.cast(Objects.requireNonNull(result,"Serializable factory returned null"));}catch(InvocationTargetException failure){Throwable cause=failure.getCause();if(cause instanceof Error error)throw error;if(cause instanceof RuntimeException runtime)throw runtime;throw new IllegalStateException("SERIALIZABLE_FACTORY_FAILED",cause);}});}catch(RuntimeException|Error failure){throw failure;}catch(Exception failure){throw new IllegalStateException("SERIALIZABLE_FACTORY_FAILED",failure);}}
        });
        return owner.register(()->{try{org.bukkit.configuration.serialization.ConfigurationSerialization.unregisterClass(type);}finally{codec.close();}});
    }
    private static Executable factory(Class<?> type){
        for(String name:List.of("deserialize","valueOf")){Method method;try{method=type.getDeclaredMethod(name,Map.class);}catch(NoSuchMethodException absent){continue;}if(!Modifier.isStatic(method.getModifiers())||!type.isAssignableFrom(method.getReturnType())||!method.trySetAccessible())throw new IllegalArgumentException("INVALID_SERIALIZABLE_FACTORY: "+method);return method;}
        try{var constructor=type.getDeclaredConstructor(Map.class);if(!constructor.trySetAccessible())throw new IllegalArgumentException("INACCESSIBLE_SERIALIZABLE_FACTORY");return constructor;}catch(NoSuchMethodException missing){throw new IllegalArgumentException("MISSING_SERIALIZABLE_FACTORY: "+type.getName(),missing);}
    }
}
