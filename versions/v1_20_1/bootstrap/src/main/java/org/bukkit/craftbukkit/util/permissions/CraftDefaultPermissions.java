package org.bukkit.craftbukkit.util.permissions;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;
/** Installs the vanilla Bukkit permission roots once during server bootstrap. */
public final class CraftDefaultPermissions {
    private static final String ROOT = "minecraft"; public static final String GALE_ROOT = "gale"; public static final String writeBooks = GALE_ROOT + ".writebooks";
    private CraftDefaultPermissions() {}
    public static void registerCorePermissions() {
        Permission parent = DefaultPermissions.registerPermission(ROOT, "Gives the user the ability to use all vanilla utilities and commands");
        CommandPermissions.registerPermissions(parent);
        DefaultPermissions.registerPermission(ROOT + ".nbt.place", "Gives the user the ability to place restricted blocks with NBT in creative", PermissionDefault.OP, parent);
        DefaultPermissions.registerPermission(ROOT + ".nbt.copy", "Gives the user the ability to copy NBT in creative", PermissionDefault.TRUE, parent);
        DefaultPermissions.registerPermission(ROOT + ".debugstick", "Gives the user the ability to use the debug stick in creative", PermissionDefault.OP, parent);
        DefaultPermissions.registerPermission(ROOT + ".debugstick.always", "Gives the user the ability to use the debug stick in all game modes", PermissionDefault.FALSE);
        DefaultPermissions.registerPermission(ROOT + ".commandblock", "Gives the user the ability to use command blocks", PermissionDefault.OP, parent);
        DefaultPermissions.registerPermission(writeBooks, "Gives the user the ability to write books", PermissionDefault.OP);
        parent.recalculatePermissibles();
    }
}
