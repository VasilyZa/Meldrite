package dev.meldrite.service;
import dev.meldrite.bridge.EntityPort;
import dev.meldrite.foundation.*;
import java.util.Objects;
public final class EntityService {
    public record Identity(EntityHandle handle,WorldService.Reference world,int nativeId,boolean player,String name){public Identity{Objects.requireNonNull(handle);Objects.requireNonNull(world);Objects.requireNonNull(name);if(!handle.world().equals(world.key()))throw new IllegalArgumentException("Entity/world identity mismatch");}}
    private final EntityPort entities;private final WorldService worlds;
    public EntityService(EntityPort entities,WorldService worlds){this.entities=Objects.requireNonNull(entities);this.worlds=Objects.requireNonNull(worlds);}
    public boolean valid(Identity identity){return worlds.valid(identity.world())&&entities.isValid(identity.handle());}
    public void requireWritable(Identity identity){worlds.requirePrimary();if(!valid(identity))throw new IllegalStateException("STALE_HANDLE: "+identity.handle());}
    public void remove(Identity identity){requireWritable(identity);entities.remove(identity.handle());}
    public EntityPosition position(Identity identity){worlds.requirePrimary();if(!valid(identity))throw new IllegalStateException("STALE_HANDLE");var position=Objects.requireNonNull(entities.position(identity.handle()),"Native entity position returned null");if(!position.world().equals(identity.world().key()))throw new IllegalStateException("ENTITY_WORLD_CHANGED");return position;}
    public ResourceKey type(Identity identity){worlds.requirePrimary();if(!valid(identity))throw new IllegalStateException("STALE_HANDLE");return Objects.requireNonNull(entities.type(identity.handle()),"Native entity type returned null");}
    public boolean teleport(Identity identity,EntityPosition target){requireWritable(identity);Objects.requireNonNull(target);if(!target.world().equals(identity.world().key()))throw new UnsupportedOperationException("CROSS_WORLD_TELEPORT_REQUIRES_REBIND");return entities.teleport(identity.handle(),target);}
    public void sendMessage(Identity identity,String message){requireWritable(identity);if(!identity.player())throw new IllegalArgumentException("ENTITY_NOT_A_PLAYER");entities.sendMessage(identity.handle(),Objects.requireNonNull(message,"message"));}
    public Identity spawn(WorldService.Reference world,ResourceKey type,EntityPosition position){worlds.requireWritable(world);Objects.requireNonNull(type);Objects.requireNonNull(position);if(!position.world().equals(world.key()))throw new IllegalArgumentException("Spawn position/world mismatch");var receipt=Objects.requireNonNull(entities.spawn(type,position),"Native spawn returned null");if(!receipt.handle().world().equals(world.key())||!entities.isValid(receipt.handle())||!worlds.valid(world))throw new IllegalStateException("ENTITY_SPAWN_CONTRACT_VIOLATION");return new Identity(receipt.handle(),world,receipt.nativeId(),false,receipt.name());}
}
