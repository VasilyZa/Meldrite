package dev.meldrite.paper.command;

import dev.meldrite.command.*;
import dev.meldrite.plugin.classloading.*;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;
import net.kyori.adventure.text.*;
import org.bukkit.Location;
import org.bukkit.command.*;

/** Profile conversions only; routing, ownership, permission and dispatch algorithms are shared. */
public final class CommandMapAdapter implements CommandMap {
    public record Ownership(String identity,PluginScope scope,Optional<PluginRealm> realm){public Ownership{Objects.requireNonNull(identity);Objects.requireNonNull(scope);Objects.requireNonNull(realm);}}
    private final CommandRegistry registry;private final CommandDispatcher dispatcher;
    private final Function<CommandSender,CommandSource> sources;private final Function<CommandSource,CommandSender> senders;
    private final Function<Command,Ownership> owners;private final PluginLoaderEngine engine;
    private final Function<Location,CommandSource.Target> targetInput;private final Function<CommandSource.Target,Location> targetOutput;
    private final Supplier<Component> defaultDenial;
    private final Map<CommandRegistry.Registration,Command> commands=Collections.synchronizedMap(new IdentityHashMap<>());
    public CommandMapAdapter(CommandRegistry registry,CommandDispatcher dispatcher,Function<CommandSender,CommandSource> sources,Function<CommandSource,CommandSender> senders,Function<Command,Ownership> owners,PluginLoaderEngine engine,Function<Location,CommandSource.Target> targetInput,Function<CommandSource.Target,Location> targetOutput,Supplier<Component> defaultDenial){this.registry=Objects.requireNonNull(registry);this.dispatcher=Objects.requireNonNull(dispatcher);this.sources=Objects.requireNonNull(sources);this.senders=Objects.requireNonNull(senders);this.owners=Objects.requireNonNull(owners);this.engine=Objects.requireNonNull(engine);this.targetInput=Objects.requireNonNull(targetInput);this.targetOutput=Objects.requireNonNull(targetOutput);this.defaultDenial=Objects.requireNonNull(defaultDenial);}
    private <T> T owned(Ownership owner,Callable<T> callback)throws Exception{return owner.realm().isPresent()?engine.withContext(owner.realm().get(),callback):callback.call();}
    private PermissionPolicy permission(Command command){return new PermissionPolicy(()->{String permission=command.getPermission();return permission==null||permission.isEmpty()?List.of():Arrays.asList(permission.split(";"));},()->{Component message=command.permissionMessage();if(message==null)message=Objects.requireNonNull(defaultDenial.get());String permission=Objects.requireNonNullElse(command.getPermission(),"");return message.replaceText(TextReplacementConfig.builder().matchLiteral("<permission>").replacement(permission).build());});}
    @Override public boolean register(String fallbackPrefix,Command command){return register(Objects.requireNonNull(command).getName(),fallbackPrefix,command);}
    @Override public boolean register(String label,String fallbackPrefix,Command command){
        Objects.requireNonNull(command);if(command.isRegistered())throw new IllegalStateException("COMMAND_ALREADY_REGISTERED: "+command.getName());String normalized=CommandKey.normalize(label),namespace=CommandKey.normalize(fallbackPrefix);var owner=Objects.requireNonNull(owners.apply(command));
        var definition=new CommandRegistry.Definition(new CommandKey(owner.identity(),namespace,normalized),command.getAliases(),!(command instanceof org.bukkit.command.defaults.BukkitCommand),permission(command),(source,route,args)->owned(owner,()->command.execute(Objects.requireNonNull(senders.apply(source)),route,args.toArray(String[]::new))),(source,route,args)->owned(owner,()->Optional.ofNullable(command.tabComplete(Objects.requireNonNull(senders.apply(source)),route,args.toArray(String[]::new),source.target().map(target->Objects.requireNonNull(targetOutput.apply(target))).orElse(null)))));
        var registration=registry.register(owner.scope(),definition);boolean bare=registry.resolve(normalized).filter(value->value==registration).isPresent();
        command.setLabel(bare?normalized:namespace+":"+normalized);command.setAliases(definition.aliases().stream().filter(alias->registry.resolve(alias).filter(value->value==registration).isPresent()).toList());
        if(!command.register(this)){registration.close();throw new IllegalStateException("COMMAND_REGISTER_REJECTED: "+normalized);}commands.put(registration,command);
        owner.scope().register(()->{commands.remove(registration);if(!command.unregister(this))throw new IllegalStateException("COMMAND_UNREGISTER_REJECTED: "+normalized);});return bare;
    }
    @Override public void registerAll(String prefix,List<Command> batch){Objects.requireNonNull(batch);for(Command command:batch)register(prefix,command);}
    @Override public Command getCommand(String label){return registry.resolve(Objects.requireNonNull(label)).map(this::command).orElse(null);}
    private Command command(CommandRegistry.Registration registration){return Objects.requireNonNull(commands.get(registration),"UNBOUND_COMMAND_ADAPTER: "+registration.definition().key().route());}
    /** A read-only snapshot; direct mutable-map compatibility is deliberately not advertised. */
    @Override public Map<String,Command> getKnownCommands(){var result=new LinkedHashMap<String,Command>();registry.routes().forEach((route,registration)->result.put(route,command(registration)));return Collections.unmodifiableMap(result);}
    @Override public boolean dispatch(CommandSender sender,String line)throws CommandException{
        Objects.requireNonNull(sender);Objects.requireNonNull(line);try{return dispatcher.dispatch(Objects.requireNonNull(sources.apply(sender)).withOrigin(CommandSource.Origin.API),line).found();}catch(IllegalStateException failure){throw commandFailure(line,failure);}
    }
    @Override public List<String> tabComplete(CommandSender sender,String line){return tabComplete(sender,line,null);}
    @Override public List<String> tabComplete(CommandSender sender,String line,Location location){
        if(sender==null||line==null)throw new IllegalArgumentException("Sender and command line are required");var source=Objects.requireNonNull(sources.apply(sender)).withOrigin(CommandSource.Origin.API).withTarget(location==null?Optional.empty():Optional.of(Objects.requireNonNull(targetInput.apply(location))));
        try{var suggestions=dispatcher.suggest(source,line);if(suggestions.isEmpty())return null;return line.indexOf(' ')<0&&source.kind()==CommandSource.Kind.PLAYER?suggestions.get().stream().map(value->"/"+value).toList():suggestions.get();}catch(IllegalStateException failure){throw commandFailure(line,failure);}
    }
    private CommandException commandFailure(String line,IllegalStateException failure){Throwable cause=Objects.requireNonNullElse(failure.getCause(),failure);return cause instanceof CommandException command?command:new CommandException("Command callback failed: "+line,cause);}
    @Override public void clearCommands(){
        registry.requirePrimaryMutation();List<Command> values;synchronized(commands){values=List.copyOf(commands.values());}Throwable failure=null;
        try{registry.clear();}catch(RuntimeException|Error caught){failure=caught;}
        for(Command command:values)try{if(!command.unregister(this))throw new IllegalStateException("COMMAND_UNREGISTER_REJECTED: "+command.getName());}catch(RuntimeException|Error caught){if(failure==null)failure=caught;else if(failure!=caught)failure.addSuppressed(caught);}
        commands.clear();if(failure instanceof RuntimeException runtime)throw runtime;if(failure instanceof Error error)throw error;
    }
}
