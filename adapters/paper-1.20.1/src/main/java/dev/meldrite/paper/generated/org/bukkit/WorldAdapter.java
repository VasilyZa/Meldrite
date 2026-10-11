package dev.meldrite.paper.generated.org.bukkit;
import dev.meldrite.craft.WorldDelegate;
import dev.meldrite.paper.facade.FacadeContext;
public final class WorldAdapter extends dev.meldrite.paper.facade.WorldAdapter {
    public WorldAdapter(WorldDelegate delegate,FacadeContext context){super(delegate,context);}
    @Override public String getName(){return super.getName();}
    @Override public java.util.UUID getUID(){return super.getUID();}
    @Override public org.bukkit.NamespacedKey getKey(){return super.getKey();}
    @Override public java.util.List<org.bukkit.entity.Entity> getEntities(){return super.getEntities();}
    @Override public void save(){super.save();}
    @Override public org.bukkit.entity.Entity spawnEntity(org.bukkit.Location location,org.bukkit.entity.EntityType type){return context.spawn(delegate,location,type);}
}
