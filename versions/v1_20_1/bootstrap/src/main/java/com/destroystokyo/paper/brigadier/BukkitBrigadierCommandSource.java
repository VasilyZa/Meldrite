/*
 * Source: PaperMC/Paper-archive, 1.20.1 tag,
 * commit 773dd724469bae89d0c2075edc3d1ddc8d5b0b18.
 * Paper inherits the GNU GPL version 3; some contributors license their
 * work under MIT. See the upstream license and author list:
 * https://github.com/PaperMC/Paper-archive/blob/773dd724469bae89d0c2075edc3d1ddc8d5b0b18/LICENSE.md
 */
package com.destroystokyo.paper.brigadier;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface BukkitBrigadierCommandSource {

    @Nullable
    Entity getBukkitEntity();

    @Nullable
    World getBukkitWorld();

    @Nullable
    Location getBukkitLocation();

    CommandSender getBukkitSender();
}
