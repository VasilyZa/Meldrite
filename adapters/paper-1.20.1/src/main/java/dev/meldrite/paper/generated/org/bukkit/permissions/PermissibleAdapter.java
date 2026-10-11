package dev.meldrite.paper.generated.org.bukkit.permissions;

import dev.meldrite.service.PermissionService;
import dev.meldrite.plugin.spi.PluginScope;
import dev.meldrite.plugin.classloading.*;
import java.util.*;
import java.util.function.Function;
import org.bukkit.permissions.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.*;

/** Exact upstream attachment objects delegate their recalculation to one neutral permission service. */
public final class PermissibleAdapter implements Permissible {
    private record Binding(PermissionService.Attachment handle,PluginScope scope){}
    private final PermissionService.Subject subject;private final Function<Plugin,PluginScope> scopes;private final BukkitScheduler scheduler;private final PluginLoaderEngine engine;
    private final Map<PermissionAttachment,Binding> attachments=new IdentityHashMap<>();private final Map<PermissionAttachment,BukkitTask> timers=new IdentityHashMap<>();
    public PermissibleAdapter(PermissionService.Subject subject,Function<Plugin,PluginScope> scopes,BukkitScheduler scheduler,PluginLoaderEngine engine){this.subject=Objects.requireNonNull(subject);this.scopes=Objects.requireNonNull(scopes);this.scheduler=Objects.requireNonNull(scheduler);this.engine=Objects.requireNonNull(engine);}
    private static <T> T argument(T value,String name){if(value==null)throw new IllegalArgumentException(name+" cannot be null");return value;}
    private static PermissionService.Default defaultValue(PermissionDefault value){return PermissionService.Default.valueOf(value.name());}
    private synchronized void writable(PermissionAttachment attachment){subject.requirePrimaryMutation();Binding binding=attachments.get(attachment);if(binding==null||binding.scope.isClosed())throw new IllegalStateException("PERMISSION_ATTACHMENT_CLOSED");}
    private final class ScopedAttachment extends PermissionAttachment {
        ScopedAttachment(Plugin plugin){super(plugin,PermissibleAdapter.this);}
        @Override public void setPermission(String name,boolean value){writable(this);super.setPermission(name,value);}
        @Override public void setPermission(Permission permission,boolean value){writable(this);super.setPermission(permission,value);}
        @Override public void unsetPermission(String name){writable(this);super.unsetPermission(name);}
        @Override public void unsetPermission(Permission permission){writable(this);super.unsetPermission(permission);}
    }
    public boolean isOp(){return subject.isOperator();}public void setOp(boolean value){subject.setOperator(value);}
    public boolean hasPermission(String name){return subject.has(argument(name,"Permission name"));}
    public boolean hasPermission(Permission permission){argument(permission,"Permission");return subject.has(permission.getName(),defaultValue(permission.getDefault()));}
    public boolean isPermissionSet(String name){return subject.isSet(argument(name,"Permission name"));}
    public boolean isPermissionSet(Permission permission){return isPermissionSet(argument(permission,"Permission").getName());}
    public synchronized Set<PermissionAttachmentInfo> getEffectivePermissions(){
        var result=new LinkedHashSet<PermissionAttachmentInfo>();subject.effective().forEach((name,effective)->{PermissionAttachment attachment=effective.attachment().map(id->attachments.entrySet().stream().filter(entry->entry.getValue().handle.id().equals(id)).map(Map.Entry::getKey).findFirst().orElseThrow(()->new IllegalStateException("ATTACHMENT_SOURCE_UNBOUND: "+id))).orElse(null);result.add(new PermissionAttachmentInfo(this,name,attachment,effective.value()));});return Collections.unmodifiableSet(result);
    }
    public synchronized PermissionAttachment addAttachment(Plugin plugin){
        argument(plugin,"Plugin");if(!plugin.isEnabled())throw new IllegalArgumentException("Plugin is disabled: "+plugin.getName());PluginScope scope=Objects.requireNonNull(scopes.apply(plugin));
        subject.requirePrimaryMutation();PermissionAttachment attachment=new ScopedAttachment(plugin);var handle=subject.attach(plugin.getName(),scope,attachment.getPermissions(),()->removed(attachment));attachments.put(attachment,new Binding(handle,scope));return attachment;
    }
    public synchronized PermissionAttachment addAttachment(Plugin plugin,String name,boolean value){argument(name,"Permission name");var attachment=addAttachment(plugin);try{attachment.setPermission(name,value);return attachment;}catch(RuntimeException|Error failure){try{removeAttachment(attachment);}catch(RuntimeException|Error cleanup){if(cleanup!=failure)failure.addSuppressed(cleanup);}throw failure;}}
    public synchronized PermissionAttachment addAttachment(Plugin plugin,int ticks){
        var attachment=addAttachment(plugin);try{BukkitTask timer=scheduler.runTaskLater(plugin,()->{synchronized(this){if(attachments.containsKey(attachment))removeAttachment(attachment);}},ticks);timers.put(attachment,timer);return attachment;}catch(RuntimeException|Error failure){try{removeAttachment(attachment);}catch(RuntimeException|Error cleanup){if(cleanup!=failure)failure.addSuppressed(cleanup);}throw failure;}
    }
    public synchronized PermissionAttachment addAttachment(Plugin plugin,String name,boolean value,int ticks){argument(name,"Permission name");var attachment=addAttachment(plugin,ticks);try{attachment.setPermission(name,value);return attachment;}catch(RuntimeException|Error failure){try{removeAttachment(attachment);}catch(RuntimeException|Error cleanup){if(cleanup!=failure)failure.addSuppressed(cleanup);}throw failure;}}
    public synchronized void removeAttachment(PermissionAttachment attachment){argument(attachment,"Attachment");Binding binding=attachments.get(attachment);if(binding==null)throw new IllegalArgumentException("Attachment is not registered to this permissible");binding.handle.close();}
    private synchronized void removed(PermissionAttachment attachment){
        attachments.remove(attachment);BukkitTask timer=timers.remove(attachment);if(timer!=null)timer.cancel();PermissionRemovedExecutor callback=attachment.getRemovalCallback();if(callback!=null){ClassLoader loader=callback.getClass().getClassLoader();if(loader instanceof PluginRealm realm)new PermissionRemovedExecutorAdapter(engine,realm,callback).attachmentRemoved(attachment);else callback.attachmentRemoved(attachment);}
    }
    public synchronized void recalculatePermissions(){for(var entry:List.copyOf(attachments.entrySet()))if(!entry.getValue().scope.isClosed())entry.getValue().handle.replace(entry.getKey().getPermissions());subject.recalculate();}
}
