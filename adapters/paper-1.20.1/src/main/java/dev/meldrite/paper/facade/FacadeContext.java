package dev.meldrite.paper.facade;
import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.craft.*;
import dev.meldrite.foundation.*;
import dev.meldrite.service.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.entity.*;
/** Typed profile composition; all facade creation passes through one neutral factory. */
public final class FacadeContext implements AutoCloseable {
    public record ServerInfo(String name,String version,String bukkitVersion,String minecraftVersion,Path pluginsDirectory,Logger logger){public ServerInfo{Objects.requireNonNull(name);Objects.requireNonNull(version);Objects.requireNonNull(bukkitVersion);Objects.requireNonNull(minecraftVersion);Objects.requireNonNull(pluginsDirectory);Objects.requireNonNull(logger);}}
    private final EntityService entities;private final WorldService worlds;private final ExecutionPort execution;private final ServerInfo info;
    private final Map<EntityHandle,EntityService.Identity> identities=new ConcurrentHashMap<>();
    // Location weakly references its world, so loaded worlds own their published facades.
    private final Map<WorldService.Reference,World> liveWorlds=new HashMap<>();
    private FacadeFactory<Entity,World> factory;
    private boolean factoryUsed;
    private Function<BlockDelegate,org.bukkit.block.Block> blockCreator;
    private volatile boolean worldsReady,closed;
    private final Server server;
    public FacadeContext(EntityService entities,WorldService worlds,ExecutionPort execution,ServerInfo info){this(entities,worlds,execution,info,ServerAdapter::new);}
    public FacadeContext(EntityService entities,WorldService worlds,ExecutionPort execution,ServerInfo info,Function<FacadeContext,Server> serverCreator){this.entities=Objects.requireNonNull(entities);this.worlds=Objects.requireNonNull(worlds);this.execution=Objects.requireNonNull(execution);this.info=Objects.requireNonNull(info);factory=new FacadeFactory<>(entities,worlds,d->new EntityAdapter(d,this),d->new PlayerAdapter(d,this),d->new WorldAdapter(d,this));server=Objects.requireNonNull(serverCreator.apply(this));var materials=new dev.meldrite.paper.registry.MaterialProjection();blockCreator=d->new BlockAdapter(d,this,materials);}
    public synchronized void installCreators(Function<EntityDelegate,Entity> entity,Function<EntityDelegate,Entity> player,Function<WorldDelegate,World> world){requireOpen();if(factoryUsed)throw new IllegalStateException("FACADE_FACTORY_ALREADY_USED");factory=new FacadeFactory<>(entities,worlds,entity,player,world);}
    public synchronized void installBlockCreator(Function<BlockDelegate,org.bukkit.block.Block> creator){requireOpen();if(factoryUsed)throw new IllegalStateException("FACADE_FACTORY_ALREADY_USED");blockCreator=Objects.requireNonNull(creator);}
    public synchronized org.bukkit.block.Block block(BlockPosition position){available();factoryUsed=true;return factory.block(worlds.reference(position.world()),position,blockCreator);}
    public Optional<Entity> entityForHandle(EntityHandle handle){available();EntityService.Identity identity=identities.get(handle);return identity==null||!entities.valid(identity)?Optional.empty():Optional.of(entity(identity));}
    public ServerInfo info(){return info;}public Server server(){return server;}public boolean primaryThread(){return execution.isPrimaryThread();}
    public boolean entitiesReady(){return worldsReady&&!closed;}
    public synchronized void markWorldsReady(){worlds.requirePrimary();if(closed)throw new IllegalStateException("FACADE_CONTEXT_CLOSED");worldsReady=true;}
    public synchronized Entity entity(EntityService.Identity identity){requireOpen();identities.put(identity.handle(),identity);factoryUsed=true;return factory.entity(identity);}
    public synchronized Entity projectEntity(EntityHandle handle,int nativeId,boolean player,String name){
        available();worlds.requirePrimary();
        var existing=identities.values().stream().filter(identity->identity.handle().world().equals(handle.world())
                &&identity.handle().uuid().equals(handle.uuid())&&identity.nativeId()==nativeId&&identity.player()==player).findFirst();
        if(existing.isPresent())return entity(existing.get());
        var projected=new EntityHandle(handle.world(),handle.uuid(),(long)nativeId+1);
        return entity(new EntityService.Identity(projected,worlds.reference(handle.world()),nativeId,player,name));
    }
    public synchronized void rebindEntity(EntityDelegate delegate,EntityHandle handle,int nativeId,String name,Entity facade){
        available();worlds.requirePrimary();
        var previous=delegate.identity();
        var replacement=new EntityService.Identity(handle,worlds.reference(handle.world()),nativeId,previous.player(),name);
        factory.rebind(delegate,replacement,facade);
        identities.remove(previous.handle(),previous);identities.put(handle,replacement);
    }
    public Entity spawn(WorldDelegate delegate,Location location,EntityType type){
        available();Objects.requireNonNull(location);Objects.requireNonNull(type);var reference=delegate.reference();
        if(location.getWorld()!=world(reference))throw new IllegalArgumentException("FOREIGN_SPAWN_WORLD");
        if(!type.isSpawnable())throw new IllegalArgumentException("UNSPAWNABLE_ENTITY_TYPE: "+type);
        var key=type.getKey();var identity=entities.spawn(reference,new ResourceKey("minecraft","entity_type",key.getNamespace(),key.getKey()),dev.meldrite.paper.entity.EntityProjection.position(reference.key(),location));return entity(identity);
    }
    public synchronized World world(WorldService.Reference reference){requireOpen();if(!worlds.valid(reference))throw new IllegalStateException("STALE_HANDLE: "+reference.key());factoryUsed=true;return liveWorlds.computeIfAbsent(reference,factory::world);}
    public NamespacedKey key(WorldService.Reference reference){ResourceKey key=reference.key().key();return new NamespacedKey(key.entryNamespace(),key.entryPath());}
    private void requireOpen(){if(closed)throw new IllegalStateException("FACADE_CONTEXT_CLOSED");}
    private void available(){requireOpen();if(!worldsReady)throw new IllegalStateException("PHASE_UNAVAILABLE: worlds are not ready");}
    public List<World> worlds(){available();return worlds.worlds().stream().map(this::world).toList();}
    public List<Entity> entities(WorldService.Reference reference){available();if(!worlds.valid(reference))throw new IllegalStateException("STALE_HANDLE: "+reference.key());return identities.values().stream().filter(identity->identity.world().equals(reference)&&entities.valid(identity)).sorted(Comparator.comparing(i->i.handle().uuid())).map(this::entity).filter(Entity::isValid).toList();}
    public World worldByName(String name){available();return uniqueWorld(worlds.worlds().stream().filter(world->world.name().equalsIgnoreCase(name)).toList(),"AMBIGUOUS_WORLD_NAME");}
    public World worldByUid(UUID uid){available();return uniqueWorld(worlds.worlds().stream().filter(world->world.uid().equals(uid)).toList(),"AMBIGUOUS_WORLD_UID");}
    public World worldByKey(NamespacedKey key){available();return uniqueWorld(worlds.worlds().stream().filter(world->key(world).equals(key)).toList(),"AMBIGUOUS_WORLD_KEY");}
    private World uniqueWorld(List<WorldService.Reference> matches,String ambiguity){if(matches.size()>1)throw new IllegalStateException(ambiguity);return matches.isEmpty()?null:world(matches.get(0));}
    private List<EntityService.Identity> matching(UUID uuid,boolean player){available();return identities.values().stream().filter(identity->identity.handle().uuid().equals(uuid)&&(!player||identity.player())&&entities.valid(identity)&&entity(identity).isValid()).toList();}
    public Entity entityByUuid(UUID uuid){var matches=matching(uuid,false);if(matches.size()>1)throw new IllegalStateException("AMBIGUOUS_ENTITY_IDENTITY: "+uuid);return matches.isEmpty()?null:entity(matches.get(0));}
    public Player playerByUuid(UUID uuid){var matches=matching(uuid,true);if(matches.size()>1)throw new IllegalStateException("AMBIGUOUS_PLAYER_IDENTITY: "+uuid);return matches.isEmpty()?null:(Player)entity(matches.get(0));}
    public Player playerExact(String name){available();var matches=identities.values().stream().filter(identity->identity.player()&&identity.name().equalsIgnoreCase(name)&&entities.valid(identity)&&entity(identity).isValid()).toList();if(matches.size()>1)throw new IllegalStateException("AMBIGUOUS_PLAYER_NAME: "+name);return matches.isEmpty()?null:(Player)entity(matches.get(0));}
    public Collection<? extends Player> players(){available();return identities.values().stream().filter(identity->identity.player()&&entities.valid(identity)).sorted(Comparator.comparing(i->i.handle().uuid())).map(identity->(Player)entity(identity)).filter(Entity::isValid).toList();}
    public void remove(EntityDelegate delegate){delegate.remove();identities.remove(delegate.identity().handle());factory.invalidateEntity(delegate.identity().handle());}
    public synchronized void unload(WorldService.Reference reference){worlds.requirePrimary();liveWorlds.remove(reference);identities.entrySet().removeIf(entry->entry.getValue().world().equals(reference));factory.unload(reference);}
    @Override public synchronized void close(){worlds.requirePrimary();if(closed)return;closed=true;try{for(var reference:worlds.worlds())unload(reference);}finally{liveWorlds.clear();identities.clear();}}
}
