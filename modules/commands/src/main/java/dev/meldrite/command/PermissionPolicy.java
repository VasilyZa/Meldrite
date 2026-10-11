package dev.meldrite.command;
import java.util.*;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
public record PermissionPolicy(Supplier<List<String>> anyOf,Supplier<Component> denial) {
    public PermissionPolicy{Objects.requireNonNull(anyOf);Objects.requireNonNull(denial);}
    public List<String> requirements(){return List.copyOf(anyOf.get());}
    public boolean permits(CommandSource source){var permissions=requirements();return permissions.isEmpty()||permissions.stream().anyMatch(source.permissions());}
}
