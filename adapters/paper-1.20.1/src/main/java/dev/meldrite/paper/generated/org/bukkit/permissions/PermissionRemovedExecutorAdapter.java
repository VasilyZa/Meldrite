package dev.meldrite.paper.generated.org.bukkit.permissions;
import dev.meldrite.plugin.classloading.*;
import java.util.Objects;
import org.bukkit.permissions.*;
public final class PermissionRemovedExecutorAdapter implements PermissionRemovedExecutor {
    private final PluginLoaderEngine engine;private final PluginRealm realm;private final PermissionRemovedExecutor callback;
    public PermissionRemovedExecutorAdapter(PluginLoaderEngine engine,PluginRealm realm,PermissionRemovedExecutor callback){this.engine=Objects.requireNonNull(engine);this.realm=Objects.requireNonNull(realm);this.callback=Objects.requireNonNull(callback);}
    public void attachmentRemoved(PermissionAttachment attachment){Objects.requireNonNull(attachment);try{engine.withContext(realm,()->{callback.attachmentRemoved(attachment);return attachment;});}catch(RuntimeException|Error failure){throw failure;}catch(Exception failure){throw new IllegalStateException("PERMISSION_REMOVAL_CALLBACK_FAILED",failure);}}
}
