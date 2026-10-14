package dev.meldrite.v1201.compat;

import net.minecraft.network.ClientConnection;

/** Access to the native play connection without reflective NMS aliases. */
public interface NativePlayerConnectionAccess {
    ClientConnection meldrite$connection();
}
