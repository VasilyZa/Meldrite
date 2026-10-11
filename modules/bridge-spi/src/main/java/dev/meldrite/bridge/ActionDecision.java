package dev.meldrite.bridge;
import java.util.*;
public record ActionDecision(boolean cancelled,Map<String,String> modifications,Optional<String> abortReason,Optional<BlockBreakChanges> blockBreak) {
    public record BlockBreakChanges(int experience,boolean dropItems){public BlockBreakChanges{if(experience<0)throw new IllegalArgumentException("Negative block-break experience");}}
    public ActionDecision(boolean cancelled,Map<String,String> modifications){this(cancelled,modifications,Optional.empty(),Optional.empty());}
    public ActionDecision{modifications=Map.copyOf(modifications);Objects.requireNonNull(abortReason);Objects.requireNonNull(blockBreak);if(abortReason.isPresent()&&(!cancelled||abortReason.get().isBlank()))throw new IllegalArgumentException("Invalid abort decision");}
    public static ActionDecision abort(String reason){return new ActionDecision(true,Map.of(),Optional.of(reason),Optional.empty());}
    public static ActionDecision blockBreak(boolean cancelled,int experience,boolean dropItems){return new ActionDecision(cancelled,Map.of(),Optional.empty(),Optional.of(new BlockBreakChanges(experience,dropItems)));}
}
