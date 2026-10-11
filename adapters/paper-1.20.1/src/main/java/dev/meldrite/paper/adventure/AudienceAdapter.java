package dev.meldrite.paper.adventure;
import dev.meldrite.adventure.*;
import dev.meldrite.bridge.AudienceMessage.Channel;
import java.util.*;
import net.kyori.adventure.audience.*;
import net.kyori.adventure.bossbar.*;
import net.kyori.adventure.chat.*;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.sound.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.*;
/** Locked overloads delegate to implemented terminals; unsupported terminals never inherit no-op bodies. */
@SuppressWarnings("deprecation")
public class AudienceAdapter implements Audience,BossBarViewer {
    private final AudienceService service;private final AudienceHandle handle;
    public AudienceAdapter(AudienceService service,AudienceHandle handle){this.service=Objects.requireNonNull(service);this.handle=Objects.requireNonNull(handle);}
    private UnsupportedOperationException unsupported(String operation){return new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: audience."+operation);}
    @Override public Pointers pointers(){return Pointers.builder().withStatic(Identity.LOCALE,handle.locale()).build();}
    @Override public void sendMessage(Identity sender,Component component,MessageType type){
        if(!Identity.nil().equals(sender))throw unsupported("sender-attribution");
        service.send(handle,component,type==MessageType.SYSTEM?Channel.SYSTEM_MESSAGE:Channel.UNSIGNED_DECORATION);
    }
    @Override public void sendMessage(Component component,ChatType.Bound type){throw unsupported("bound-chat-decoration");}
    @Override public void sendMessage(SignedMessage message,ChatType.Bound type){throw unsupported("authenticated-player-chat");}
    @Override public void deleteMessage(SignedMessage message){throw unsupported("delete-signed-message");}
    @Override public void deleteMessage(SignedMessage.Signature signature){throw unsupported("delete-signed-message");}
    @Override public void sendActionBar(Component component){service.send(handle,component,Channel.ACTION_BAR);}
    @Override public void sendPlayerListHeaderAndFooter(Component header,Component footer){service.tab(handle,header,footer);}
    @Override public void showTitle(Title title){service.title(handle,title);}
    @Override public <T> void sendTitlePart(TitlePart<T> part,T value){throw unsupported("individual-title-part");}
    @Override public void clearTitle(){service.clearTitle(handle,false);}
    @Override public void resetTitle(){service.clearTitle(handle,true);}
    @Override public void showBossBar(BossBar bar){service.showBossBar(handle,bar);}
    @Override public void hideBossBar(BossBar bar){service.hideBossBar(handle,bar);}
    @Override public Iterable<? extends BossBar> activeBossBars(){return service.activeBossBars(handle);}
    @Override public void playSound(Sound sound){throw unsupported("sound");}
    @Override public void playSound(Sound sound,double x,double y,double z){throw unsupported("positioned-sound");}
    @Override public void playSound(Sound sound,Sound.Emitter emitter){throw unsupported("emitter-sound");}
    @Override public void stopSound(Sound sound){throw unsupported("stop-sound");}
    @Override public void stopSound(SoundStop stop){throw unsupported("stop-sound");}
    @Override public void openBook(Book book){throw unsupported("book");}
}
