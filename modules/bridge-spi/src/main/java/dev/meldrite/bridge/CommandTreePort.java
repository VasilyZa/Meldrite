package dev.meldrite.bridge;
public interface CommandTreePort {
    void replaceTree(String audienceId, byte[] serializedTree);
    /** Publish the complete logical roots and refresh affected clients; reject before effects on failure. */
    default void publish(CommandTreeSnapshot snapshot){throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: command.logical-tree-publication");}
}
