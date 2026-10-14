package dev.meldrite.v1201.compat;

import io.netty.channel.Channel;

/** Native channel and disconnect lifecycle state. */
public interface NativeConnectionAccess {
    Channel meldrite$channel();
    boolean meldrite$disconnectionHandled();
}
