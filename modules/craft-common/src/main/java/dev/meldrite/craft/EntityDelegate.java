package dev.meldrite.craft;
import dev.meldrite.service.EntityService;
import java.util.Objects;
public final class EntityDelegate {
    private final EntityService service;private volatile EntityService.Identity identity;
    public EntityDelegate(EntityService service,EntityService.Identity identity){this.service=Objects.requireNonNull(service);this.identity=Objects.requireNonNull(identity);}
    public EntityService.Identity identity(){return identity;}
    /** Keeps the published facade when the native lifecycle replaces an entity. */
    public void rebind(EntityService.Identity replacement){
        Objects.requireNonNull(replacement);
        if(!identity.handle().uuid().equals(replacement.handle().uuid())||identity.player()!=replacement.player())throw new IllegalArgumentException("ENTITY_REBIND_IDENTITY_MISMATCH");
        service.requireWritable(replacement);identity=replacement;
    }
    public boolean valid(){return service.valid(identity);}
    public void remove(){service.remove(identity);}
    public void requireWritable(){service.requireWritable(identity);}
    public dev.meldrite.foundation.EntityPosition position(){return service.position(identity);}
    public dev.meldrite.foundation.ResourceKey type(){return service.type(identity);}
    public boolean teleport(dev.meldrite.foundation.EntityPosition position){return service.teleport(identity,position);}
    public void sendMessage(String message){service.sendMessage(identity,message);}
}
