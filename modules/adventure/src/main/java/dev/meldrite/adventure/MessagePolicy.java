package dev.meldrite.adventure;
import dev.meldrite.bridge.AudienceMessage.*;
import java.util.*;
import java.util.function.Predicate;
public final class MessagePolicy {
    private final Predicate<Target> permission;
    public MessagePolicy(Predicate<Target> permission){this.permission=Objects.requireNonNull(permission);}
    public void check(Target target,Channel channel){
        if(!permission.test(target))throw new SecurityException("AUDIENCE_PERMISSION_DENIED");
        if(channel==Channel.PLAYER_CHAT)throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: authenticated-player-chat");
        if(target.kind().equals("console")&&channel!=Channel.SYSTEM_MESSAGE&&channel!=Channel.UNSIGNED_DECORATION)throw new UnsupportedOperationException("UNSUPPORTED_AUDIENCE_CHANNEL: console/"+channel);
    }
}
