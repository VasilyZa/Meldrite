package dev.meldrite.paper.entity;
import dev.meldrite.foundation.*;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import java.util.Objects;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
/** Parameter/result conversion only; no movement algorithm or native object access. */
public final class EntityProjection {
    private EntityProjection(){}
    public static EntityPosition position(WorldKey world,Location location){Objects.requireNonNull(location);location.checkFinite();return new EntityPosition(world,location.getX(),location.getY(),location.getZ(),location.getYaw(),location.getPitch());}
    public static Location location(EntityDelegate delegate,FacadeContext context){var value=delegate.position();return new Location(context.world(delegate.identity().world()),value.x(),value.y(),value.z(),value.yaw(),value.pitch());}
    public static boolean teleport(EntityDelegate delegate,FacadeContext context,Location location){delegate.requireWritable();Objects.requireNonNull(location);if(location.getWorld()!=context.world(delegate.identity().world()))throw new UnsupportedOperationException("CROSS_WORLD_OR_FOREIGN_LOCATION");return delegate.teleport(position(delegate.identity().world().key(),location));}
    public static EntityType type(EntityDelegate delegate){var key=delegate.type();if(!key.registryNamespace().equals("minecraft")||!key.registryPath().equals("entity_type")||!key.entryNamespace().equals("minecraft"))throw new UnsupportedOperationException("UNREPRESENTABLE_ENTITY_TYPE: "+key);var type=EntityType.fromName(key.entryPath());if(type==null||!type.getKey().equals(new NamespacedKey(key.entryNamespace(),key.entryPath())))throw new UnsupportedOperationException("UNREPRESENTABLE_ENTITY_TYPE: "+key);return type;}
}
