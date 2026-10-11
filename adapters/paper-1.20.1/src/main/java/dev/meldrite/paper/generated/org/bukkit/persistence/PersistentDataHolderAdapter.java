package dev.meldrite.paper.generated.org.bukkit.persistence;
import java.util.Objects;
import org.bukkit.persistence.*;
public final class PersistentDataHolderAdapter implements PersistentDataHolder {
    private final PersistentDataContainer container;
    public PersistentDataHolderAdapter(PersistentDataContainer container){this.container=Objects.requireNonNull(container);}
    public PersistentDataContainer getPersistentDataContainer(){return container;}
}
