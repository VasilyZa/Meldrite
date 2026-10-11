package dev.meldrite.paper.statistics;
import dev.meldrite.foundation.*;
import java.util.*;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
/** Exact pinned enum/registry parameters; no default entity or Material replacement. */
public final class StatisticProjection {
    private StatisticProjection(){}
    private static ResourceKey key(Statistic statistic){var key=Objects.requireNonNull(statistic).getKey();return new ResourceKey("meldrite","statistic",key.getNamespace(),key.getKey());}
    public static StatisticRequest request(Statistic statistic){if(Objects.requireNonNull(statistic).getType()!=Statistic.Type.UNTYPED)throw new IllegalArgumentException("Statistic requires typed parameter");return new StatisticRequest(key(statistic),StatisticRequest.Kind.UNTYPED,Optional.empty());}
    public static StatisticRequest request(Statistic statistic,Material material){Objects.requireNonNull(statistic);Objects.requireNonNull(material);boolean block=statistic.getType()==Statistic.Type.BLOCK;if(!block&&statistic.getType()!=Statistic.Type.ITEM)throw new IllegalArgumentException("Statistic requires a different parameter type");if(block?!material.isBlock():!material.isItem())throw new IllegalArgumentException("Invalid statistic Material");var key=material.getKey();return new StatisticRequest(key(statistic),block?StatisticRequest.Kind.BLOCK:StatisticRequest.Kind.ITEM,Optional.of(new ResourceKey("minecraft",block?"block":"item",key.getNamespace(),key.getKey())));}
    public static StatisticRequest request(Statistic statistic,EntityType type){Objects.requireNonNull(statistic);Objects.requireNonNull(type);if(statistic.getType()!=Statistic.Type.ENTITY||type==EntityType.UNKNOWN)throw new IllegalArgumentException("Invalid statistic EntityType");var key=type.getKey();return new StatisticRequest(key(statistic),StatisticRequest.Kind.ENTITY,Optional.of(new ResourceKey("minecraft","entity_type",key.getNamespace(),key.getKey())));}
}
