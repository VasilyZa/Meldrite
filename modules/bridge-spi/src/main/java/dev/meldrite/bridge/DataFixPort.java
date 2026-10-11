package dev.meldrite.bridge;
import dev.meldrite.foundation.ResourceKey;
public interface DataFixPort { byte[] update(ResourceKey dataType, int fromVersion, int toVersion, byte[] input) throws Exception; }
