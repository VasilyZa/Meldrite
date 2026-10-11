package dev.meldrite.bridge;
import dev.meldrite.foundation.*;
import java.util.*;
public record ActionSnapshot(String token,String kind,Map<String,String> payload,Optional<BlockBreakInput> blockBreak) {
    public record BlockBreakInput(EntityHandle actor,BlockPosition block,ResourceKey blockType,int experience,boolean dropItems) {
        public BlockBreakInput{Objects.requireNonNull(actor);Objects.requireNonNull(block);Objects.requireNonNull(blockType);if(!actor.world().equals(block.world())||experience<0)throw new IllegalArgumentException("Invalid block-break input");}
    }
    public ActionSnapshot(String token,String kind,Map<String,String> payload){this(token,kind,payload,Optional.empty());}
    public ActionSnapshot {
        Objects.requireNonNull(token);Objects.requireNonNull(kind);payload=Map.copyOf(payload);Objects.requireNonNull(blockBreak);
        if(token.isBlank()||kind.isBlank())throw new IllegalArgumentException("Token and kind must not be blank");
    }
}
