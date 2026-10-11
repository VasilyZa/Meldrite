package org.bukkit.craftbukkit.event;

import org.bukkit.Location;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

/** Immutable portal-event data used by the server portal search code. */
public class CraftPortalEvent {
    private final Location to;
    private final int searchRadius;
    private final int creationRadius;
    private final boolean canCreatePortal;
    private final boolean cancelled;

    public CraftPortalEvent(EntityPortalEvent portalEvent) {
        this.to = portalEvent.getTo();
        this.searchRadius = portalEvent.getSearchRadius();
        this.creationRadius = 0;
        this.canCreatePortal = false;
        this.cancelled = portalEvent.isCancelled();
    }

    public CraftPortalEvent(PlayerPortalEvent portalEvent) {
        this.to = portalEvent.getTo();
        this.searchRadius = portalEvent.getSearchRadius();
        this.creationRadius = portalEvent.getCreationRadius();
        this.canCreatePortal = portalEvent.getCanCreatePortal();
        this.cancelled = portalEvent.isCancelled();
    }

    public Location getTo() { return to; }
    public int getSearchRadius() { return searchRadius; }
    public int getCreationRadius() { return creationRadius; }
    public boolean getCanCreatePortal() { return canCreatePortal; }
    public boolean isCancelled() { return cancelled; }
}
