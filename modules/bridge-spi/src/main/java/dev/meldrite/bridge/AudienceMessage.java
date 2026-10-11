package dev.meldrite.bridge;
import java.time.Duration;
import java.util.*;
/** Serialized network boundary only; component semantics live in the shared Adventure service. */
public record AudienceMessage(Target target,Channel channel,List<byte[]> components,Optional<TitleTimes> times,Optional<BossBarState> bossBar) {
    public enum Channel { SYSTEM_MESSAGE, UNSIGNED_DECORATION, PLAYER_CHAT, ACTION_BAR, TITLE, TITLE_CLEAR, TITLE_RESET, TAB_HEADER_FOOTER, BOSSBAR_ADD, BOSSBAR_UPDATE, BOSSBAR_REMOVE }
    public record Target(String kind,String identity,long generation){
        public Target{Objects.requireNonNull(kind);Objects.requireNonNull(identity);if(kind.isBlank()||identity.isBlank()||generation<0)throw new IllegalArgumentException("Invalid audience target");}
    }
    public record TitleTimes(Duration fadeIn,Duration stay,Duration fadeOut){
        public TitleTimes{Objects.requireNonNull(fadeIn);Objects.requireNonNull(stay);Objects.requireNonNull(fadeOut);if(fadeIn.isNegative()||stay.isNegative()||fadeOut.isNegative())throw new IllegalArgumentException("Negative title timing");}
    }
    public record BossBarState(UUID id,float progress,String color,String overlay,Set<String> flags){
        public BossBarState{Objects.requireNonNull(id);Objects.requireNonNull(color);Objects.requireNonNull(overlay);flags=Set.copyOf(flags);if(!Float.isFinite(progress)||progress<0||progress>1)throw new IllegalArgumentException("Invalid progress");}
    }
    public AudienceMessage{
        Objects.requireNonNull(target);Objects.requireNonNull(channel);Objects.requireNonNull(times);Objects.requireNonNull(bossBar);
        components=components.stream().map(byte[]::clone).toList();
    }
    @Override public List<byte[]> components(){return components.stream().map(byte[]::clone).toList();}
}
