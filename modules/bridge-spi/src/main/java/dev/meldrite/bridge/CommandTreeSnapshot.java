package dev.meldrite.bridge;
import java.util.*;
/** Logical command roots; the version bridge owns Brigadier and client refresh. */
public record CommandTreeSnapshot(long revision,List<Node> nodes) {
    public record Node(String route,String owner,List<String> anyPermission){public Node{Objects.requireNonNull(route);Objects.requireNonNull(owner);anyPermission=List.copyOf(anyPermission);}}
    public CommandTreeSnapshot{if(revision<0)throw new IllegalArgumentException("Negative command revision");nodes=List.copyOf(nodes);if(nodes.stream().map(Node::route).distinct().count()!=nodes.size())throw new IllegalArgumentException("Duplicate command roots");}
}
