package dev.meldrite.command;
import dev.meldrite.adventure.AudienceHandle;
import dev.meldrite.foundation.WorldKey;
import java.util.*;
import java.util.function.Predicate;
public record CommandSource(Kind kind,Origin origin,String identity,Locale locale,AudienceHandle audience,Predicate<String> permissions,Optional<Target> target) {
    public enum Kind {PLAYER,CONSOLE,API,NATIVE}
    public enum Origin {PLAYER_INPUT,CONSOLE_INPUT,API,NATIVE}
    public record Target(WorldKey world,double x,double y,double z){public Target{Objects.requireNonNull(world);if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Non-finite command target");}}
    public CommandSource {Objects.requireNonNull(kind);Objects.requireNonNull(origin);Objects.requireNonNull(identity);Objects.requireNonNull(locale);Objects.requireNonNull(audience);Objects.requireNonNull(permissions);Objects.requireNonNull(target);}
    public CommandSource withOrigin(Origin value){return new CommandSource(kind,value,identity,locale,audience,permissions,target);}
    public CommandSource withTarget(Optional<Target> value){return new CommandSource(kind,origin,identity,locale,audience,permissions,value);}
}
