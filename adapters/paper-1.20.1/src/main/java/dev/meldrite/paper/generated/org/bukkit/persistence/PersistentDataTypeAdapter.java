package dev.meldrite.paper.generated.org.bukkit.persistence;
import dev.meldrite.plugin.classloading.*;
import java.util.*;
import java.util.concurrent.Callable;
import org.bukkit.persistence.*;
/** Custom converters enter their actual plugin realm; primitive API providers are shared. */
public final class PersistentDataTypeAdapter<T,Z> implements PersistentDataType<T,Z> {
    private final PluginLoaderEngine engine;private final PersistentDataType<T,Z> delegate;
    public PersistentDataTypeAdapter(PluginLoaderEngine engine,PersistentDataType<T,Z> delegate){this.engine=Objects.requireNonNull(engine);this.delegate=Objects.requireNonNull(delegate);}
    private <R> R owned(Callable<R> operation){
        try{
            ClassLoader loader=delegate.getClass().getClassLoader();
            if(!(loader instanceof PluginRealm)&&loader!=PersistentDataType.class.getClassLoader())
                throw new UnsupportedOperationException("UNOWNED_PERSISTENT_CONVERTER: "+delegate.getClass().getName());
            return Objects.requireNonNull(loader instanceof PluginRealm realm?engine.withContext(realm,operation):operation.call(),"Persistent converter returned null");
        }catch(RuntimeException|Error failure){throw failure;}catch(Exception failure){throw new IllegalStateException("PERSISTENT_CONVERTER_FAILED",failure);}
    }
    public Class<T> getPrimitiveType(){return owned(delegate::getPrimitiveType);}
    public Class<Z> getComplexType(){return owned(delegate::getComplexType);}
    public T toPrimitive(Z value,PersistentDataAdapterContext context){Objects.requireNonNull(value);Objects.requireNonNull(context);return owned(()->getPrimitiveType().cast(delegate.toPrimitive(getComplexType().cast(value),context)));}
    public Z fromPrimitive(T value,PersistentDataAdapterContext context){Objects.requireNonNull(value);Objects.requireNonNull(context);return owned(()->getComplexType().cast(delegate.fromPrimitive(getPrimitiveType().cast(value),context)));}
}
