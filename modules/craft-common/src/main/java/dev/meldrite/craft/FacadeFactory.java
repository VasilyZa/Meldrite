package dev.meldrite.craft;
import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.service.*;
import java.util.*;
import java.util.function.Function;
/** One factory chooses the facade kind; profiles/Craft bindings install creators before use. */
public final class FacadeFactory<E,W> {
    private record BlockKey(WorldService.Reference world,dev.meldrite.foundation.BlockPosition position){}
    private record EntityKey(WorldService.Reference world,UUID uuid,long generation,boolean player){}
    private final EntityService entities;private final WorldService worlds;private final WrapperCache entityCache=new WrapperCache(),worldCache=new WrapperCache(),blockCache=new WrapperCache();
    private final Function<EntityDelegate,E> entityCreator,playerCreator;private final Function<WorldDelegate,W> worldCreator;
    public FacadeFactory(EntityService entities,WorldService worlds,Function<EntityDelegate,E> entityCreator,Function<EntityDelegate,E> playerCreator,Function<WorldDelegate,W> worldCreator){this.entities=Objects.requireNonNull(entities);this.worlds=Objects.requireNonNull(worlds);this.entityCreator=Objects.requireNonNull(entityCreator);this.playerCreator=Objects.requireNonNull(playerCreator);this.worldCreator=Objects.requireNonNull(worldCreator);}
    public E entity(EntityService.Identity identity){EntityHandle handle=identity.handle();EntityKey key=new EntityKey(identity.world(),handle.uuid(),handle.generation(),identity.player());return entityCache.get(key,()->(identity.player()?playerCreator:entityCreator).apply(new EntityDelegate(entities,identity)));}
    public void rebind(EntityDelegate delegate,EntityService.Identity replacement,E facade){
        var previous=delegate.identity();
        if(!previous.handle().uuid().equals(replacement.handle().uuid())||previous.player()!=replacement.player())throw new IllegalArgumentException("ENTITY_REBIND_IDENTITY_MISMATCH");
        entities.requireWritable(replacement);
        entityCache.rebind(new EntityKey(previous.world(),previous.handle().uuid(),previous.handle().generation(),previous.player()),
                new EntityKey(replacement.world(),replacement.handle().uuid(),replacement.handle().generation(),replacement.player()),facade);
        delegate.rebind(replacement);
    }
    public W world(WorldService.Reference reference){return worldCache.get(reference,()->worldCreator.apply(new WorldDelegate(worlds,reference)));}
    public <B>B block(WorldService.Reference reference,dev.meldrite.foundation.BlockPosition position,Function<BlockDelegate,B> creator){return blockCache.get(new BlockKey(reference,position),()->creator.apply(new BlockDelegate(worlds,reference,position)));}
    public void invalidateEntity(EntityHandle handle){entityCache.evict(key->key instanceof EntityKey entity&&entity.world().key().equals(handle.world())&&entity.uuid().equals(handle.uuid())&&entity.generation()==handle.generation());}
    public void unload(WorldService.Reference reference){worlds.unbind(reference);entityCache.evict(key->key instanceof EntityKey entity&&entity.world().equals(reference));worldCache.evict(reference::equals);blockCache.evict(key->key instanceof BlockKey block&&block.world().equals(reference));}
    public int cachedEntities(){return entityCache.entries();}
}
