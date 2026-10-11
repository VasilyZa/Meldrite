package org.bukkit.craftbukkit.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;

/**
 * Legacy unversioned owner for plugins which cast ArmorStand facades before
 * invoking the Bukkit methods. The version-owned implementation remains the
 * concrete class and supplies all behavior through normal virtual dispatch.
 */
public abstract class CraftArmorStand
        extends org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity
        implements org.bukkit.entity.ArmorStand {
    protected CraftArmorStand(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) {
        super(delegate, context, entities);
    }
}
