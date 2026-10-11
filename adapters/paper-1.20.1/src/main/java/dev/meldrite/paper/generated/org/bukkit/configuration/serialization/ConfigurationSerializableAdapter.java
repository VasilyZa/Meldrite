package dev.meldrite.paper.generated.org.bukkit.configuration.serialization;
import dev.meldrite.plugin.classloading.*;
import java.util.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
public final class ConfigurationSerializableAdapter implements ConfigurationSerializable {
    private final PluginLoaderEngine engine;private final PluginRealm realm;private final ConfigurationSerializable value;
    public ConfigurationSerializableAdapter(PluginLoaderEngine engine,PluginRealm realm,ConfigurationSerializable value){this.engine=Objects.requireNonNull(engine);this.realm=Objects.requireNonNull(realm);this.value=Objects.requireNonNull(value);}
    public Map<String,Object> serialize(){try{return engine.withContext(realm,()->Objects.requireNonNull(value.serialize(),"Serializable returned null map"));}catch(RuntimeException|Error failure){throw failure;}catch(Exception failure){throw new IllegalStateException("SERIALIZABLE_CALLBACK_FAILED",failure);}}
}
