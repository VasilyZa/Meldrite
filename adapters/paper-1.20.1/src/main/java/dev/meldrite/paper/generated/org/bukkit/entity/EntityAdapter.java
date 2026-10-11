package dev.meldrite.paper.generated.org.bukkit.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.paper.entity.EntityProjection;
/** Exact selected root bindings share the existing facade/delegate identity path. */
public final class EntityAdapter extends dev.meldrite.paper.facade.EntityAdapter {
    public EntityAdapter(EntityDelegate delegate,FacadeContext context){super(delegate,context);}
    @Override public int getEntityId(){return super.getEntityId();}
    @Override public java.util.UUID getUniqueId(){return super.getUniqueId();}
    @Override public String getName(){return delegate.identity().name();}
    @Override public org.bukkit.World getWorld(){return super.getWorld();}
    @Override public boolean isValid(){return super.isValid();}
    @Override public void remove(){super.remove();}
    @Override public org.bukkit.Location getLocation(){return EntityProjection.location(delegate,context);}
    @Override public org.bukkit.Location getLocation(org.bukkit.Location location){if(location==null)return null;var value=getLocation();location.setWorld(value.getWorld());location.setX(value.getX());location.setY(value.getY());location.setZ(value.getZ());location.setYaw(value.getYaw());location.setPitch(value.getPitch());return location;}
    @Override public boolean teleport(org.bukkit.Location location){return EntityProjection.teleport(delegate,context,location);}
    @Override public org.bukkit.entity.EntityType getType(){return EntityProjection.type(delegate);}
}
