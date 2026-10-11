package dev.meldrite.foundation;
import java.util.*;
/** Logical API statistic identity and exact typed registry parameter; never a numeric native ID. */
public record StatisticRequest(ResourceKey statistic,Kind kind,Optional<ResourceKey> parameter) {
    public enum Kind {UNTYPED,ITEM,BLOCK,ENTITY}
    public StatisticRequest{Objects.requireNonNull(statistic);Objects.requireNonNull(kind);parameter=Objects.requireNonNull(parameter);if((kind==Kind.UNTYPED)==parameter.isPresent())throw new IllegalArgumentException("Statistic parameter/type mismatch");if(parameter.isPresent()){String registry=switch(kind){case ITEM->"item";case BLOCK->"block";case ENTITY->"entity_type";default->throw new IllegalArgumentException("Untyped parameter");};if(!parameter.get().registryNamespace().equals("minecraft")||!parameter.get().registryPath().equals(registry))throw new IllegalArgumentException("Statistic parameter registry mismatch");}}
}
