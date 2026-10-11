package dev.meldrite.command;
import java.util.*;
public record CommandKey(String owner,String namespace,String name) {
    public CommandKey {Objects.requireNonNull(owner);namespace=normalize(namespace);name=normalize(name);if(owner.isBlank())throw new IllegalArgumentException("Empty owner");}
    public static String normalize(String value){Objects.requireNonNull(value);String normalized=value.trim().toLowerCase(Locale.ROOT);if(!normalized.matches("[^\\s:]+"))throw new IllegalArgumentException("Invalid command label: "+value);return normalized;}
    public String route(){return namespace+":"+name;}
}
