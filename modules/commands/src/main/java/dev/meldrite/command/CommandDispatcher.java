package dev.meldrite.command;
import dev.meldrite.adventure.AudienceService;
import dev.meldrite.bridge.*;
import java.util.*;
import java.util.function.*;
import net.kyori.adventure.text.Component;
public final class CommandDispatcher {
    public record Result(boolean found,boolean executed,boolean accepted){}
    public record Failure(String owner,CommandSource source,String line,Throwable cause){}
    private final CommandRegistry registry;private final ExecutionPort execution;private final AudienceService audiences;
    private final BiFunction<CommandSource,String,Optional<String>> ingress;private final Consumer<Failure> errors;
    public CommandDispatcher(CommandRegistry registry,ExecutionPort execution,AudienceService audiences,BiFunction<CommandSource,String,Optional<String>> ingress,Consumer<Failure> errors){this.registry=Objects.requireNonNull(registry);this.execution=Objects.requireNonNull(execution);this.audiences=Objects.requireNonNull(audiences);this.ingress=Objects.requireNonNull(ingress);this.errors=Objects.requireNonNull(errors);}
    private void primary(){if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_COMMAND_DISPATCH");}
    public Result dispatch(CommandSource source,String line){
        primary();Objects.requireNonNull(source);Objects.requireNonNull(line);
        if(source.origin()!=CommandSource.Origin.API){var processed=Objects.requireNonNull(ingress.apply(source,line));if(processed.isEmpty())return new Result(false,false,false);line=processed.get();}
        var words=Arrays.stream(line.split(" ")).filter(word->!word.isEmpty()).toList();if(words.isEmpty())return new Result(false,false,false);String label=words.get(0).toLowerCase(Locale.ROOT);
        var resolved=registry.resolve(label);if(resolved.isEmpty())return new Result(false,false,false);var registration=resolved.get();var command=registration.definition();
        if(!command.permission().permits(source)){Component denial=Objects.requireNonNull(command.permission().denial().get());if(!denial.equals(Component.empty()))audiences.send(source.audience(),denial,AudienceMessage.Channel.SYSTEM_MESSAGE);return new Result(true,false,false);}
        if(!registration.active())return new Result(false,false,false);
        try{return new Result(true,true,command.executor().execute(source,label,words.subList(1,words.size())));}catch(Throwable failure){throw report(command,source,line,failure);}
    }
    public Optional<List<String>> suggest(CommandSource source,String line){
        primary();Objects.requireNonNull(source);Objects.requireNonNull(line);int space=line.indexOf(' ');
        if(space<0){String prefix=line.toLowerCase(Locale.ROOT);return Optional.of(registry.routes().entrySet().stream().filter(entry->entry.getKey().startsWith(prefix)&&entry.getValue().definition().permission().permits(source)).map(Map.Entry::getKey).sorted(String.CASE_INSENSITIVE_ORDER).toList());}
        String label=line.substring(0,space);var resolved=registry.resolve(label);if(resolved.isEmpty())return Optional.empty();var registration=resolved.get();var command=registration.definition();if(!command.permission().permits(source)||!registration.active())return Optional.empty();List<String> args=Arrays.asList(line.substring(space+1).split(" ",-1));
        try{return Objects.requireNonNull(command.completer().complete(source,label,List.copyOf(args))).map(List::copyOf);}catch(Throwable failure){throw report(command,source,line,failure);}
    }
    private RuntimeException report(CommandRegistry.Definition command,CommandSource source,String line,Throwable failure){
        if(failure instanceof InterruptedException)Thread.currentThread().interrupt();try{errors.accept(new Failure(command.key().owner(),source,line,failure));}catch(Throwable loggingFailure){if(loggingFailure!=failure)failure.addSuppressed(loggingFailure);}return new IllegalStateException("COMMAND_CALLBACK_FAILED: "+command.key().route(),failure);
    }
}
