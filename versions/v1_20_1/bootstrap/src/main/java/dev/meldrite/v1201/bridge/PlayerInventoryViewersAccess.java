package dev.meldrite.v1201.bridge;

import java.util.List;
import net.minecraft.entity.player.PlayerEntity;

/** Runtime bridge for the viewer list exposed by Bukkit's player inventory. */
public interface PlayerInventoryViewersAccess {
    List<PlayerEntity> meldrite$getViewers();
}
