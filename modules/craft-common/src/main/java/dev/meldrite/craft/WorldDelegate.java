package dev.meldrite.craft;
import dev.meldrite.service.WorldService;
import java.util.Objects;
public final class WorldDelegate {
    private final WorldService service;private final WorldService.Reference reference;
    public WorldDelegate(WorldService service,WorldService.Reference reference){this.service=Objects.requireNonNull(service);this.reference=Objects.requireNonNull(reference);}
    public WorldService.Reference reference(){return reference;}
    public boolean valid(){return service.valid(reference);}
    public void save(){service.save(reference);}
    public int minHeight(){return service.minHeight(reference);}
    public int maxHeight(){return service.maxHeight(reference);}
    public String environment(){return service.environment(reference);}
}
