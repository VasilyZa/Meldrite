package org.bukkit.craftbukkit.util;
import java.io.File;
import java.io.FilenameFilter;
public final class DatFileFilter implements FilenameFilter {
    @Override public boolean accept(File directory, String name) { return name != null && name.endsWith(".dat"); }
}
