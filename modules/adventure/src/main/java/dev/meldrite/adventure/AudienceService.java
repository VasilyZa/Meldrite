package dev.meldrite.adventure;

import dev.meldrite.bridge.*;
import dev.meldrite.bridge.AudienceMessage.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

/** Components remain components until the single serialized native transport boundary. */
public final class AudienceService implements AutoCloseable {
    private final NetworkAudiencePort network;private final ExecutionPort execution;private final ComponentCodec codec;
    private final Predicate<Target> live;private final MessagePolicy policy;private final BiConsumer<Target,Throwable> failures;
    private final Map<Target,AudienceHandle> handles=new ConcurrentHashMap<>();
    private final Map<BossBar,BarBinding> bars=new IdentityHashMap<>();
    private volatile boolean closed;
    private final class BarBinding implements BossBar.Listener {
        final UUID id=UUID.randomUUID();final Set<Target> members=new LinkedHashSet<>();
        private void changed(BossBar bar){List<Target> targets;synchronized(bars){targets=List.copyOf(members);}for(Target target:targets){AudienceHandle handle=handles.get(target);if(handle!=null)try{sendBar(handle,bar,this,Channel.BOSSBAR_UPDATE);}catch(RuntimeException failure){failures.accept(target,failure);}}}
        public void bossBarNameChanged(BossBar bar,Component old,Component current){changed(bar);}
        public void bossBarProgressChanged(BossBar bar,float old,float current){changed(bar);}
        public void bossBarColorChanged(BossBar bar,BossBar.Color old,BossBar.Color current){changed(bar);}
        public void bossBarOverlayChanged(BossBar bar,BossBar.Overlay old,BossBar.Overlay current){changed(bar);}
        public void bossBarFlagsChanged(BossBar bar,Set<BossBar.Flag> added,Set<BossBar.Flag> removed){changed(bar);}
    }
    public AudienceService(NetworkAudiencePort network,ExecutionPort execution,ComponentCodec codec,Predicate<Target> live,MessagePolicy policy,BiConsumer<Target,Throwable> failures){this.network=Objects.requireNonNull(network);this.execution=Objects.requireNonNull(execution);this.codec=Objects.requireNonNull(codec);this.live=Objects.requireNonNull(live);this.policy=Objects.requireNonNull(policy);this.failures=Objects.requireNonNull(failures);}
    public synchronized AudienceHandle open(AudienceHandle.Kind kind,String identity,long generation,Locale locale){
        requireOpen();Target target=new Target(kind.name().toLowerCase(Locale.ROOT),identity,generation);
        if(!live.test(target))throw new IllegalStateException("STALE_AUDIENCE");
        return handles.computeIfAbsent(target,key->new AudienceHandle(key,locale,()->retire(key)));
    }
    private void requireOpen(){if(closed)throw new IllegalStateException("AUDIENCE_SERVICE_CLOSED");}
    private void requireLive(AudienceHandle handle){requireOpen();if(handle.closed()||handles.get(handle.target())!=handle||!live.test(handle.target()))throw new IllegalStateException("STALE_AUDIENCE");}
    private void execute(AudienceHandle handle,Channel channel,Runnable effect){
        requireLive(handle);policy.check(handle.target(),channel);
        if(execution.isPrimaryThread())effect.run();
        else execution.executePrimary(()->{try{requireLive(handle);policy.check(handle.target(),channel);effect.run();}catch(RuntimeException|Error failure){failures.accept(handle.target(),failure);}});
    }
    private void transport(Target target,Channel channel,List<Component> components,Optional<TitleTimes> times,Optional<BossBarState> bar){
        if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_AUDIENCE_EFFECT");
        network.send(new AudienceMessage(target,channel,components.stream().map(component->codec.json(component).getBytes(StandardCharsets.UTF_8)).toList(),times,bar));
    }
    public void send(AudienceHandle handle,Component component,Channel channel){
        Objects.requireNonNull(component);if(channel!=Channel.SYSTEM_MESSAGE&&channel!=Channel.UNSIGNED_DECORATION&&channel!=Channel.PLAYER_CHAT&&channel!=Channel.ACTION_BAR)throw new IllegalArgumentException("Not a message channel: "+channel);
        policy.check(handle.target(),channel);execute(handle,channel,()->{requireLive(handle);policy.check(handle.target(),channel);transport(handle.target(),channel,List.of(component),Optional.empty(),Optional.empty());});
    }
    public void title(AudienceHandle handle,Title title){
        Objects.requireNonNull(title);policy.check(handle.target(),Channel.TITLE);
        var original=title.times();Optional<TitleTimes> times=original==null?Optional.empty():Optional.of(new TitleTimes(original.fadeIn(),original.stay(),original.fadeOut()));
        List<Component> components=List.of(title.title(),title.subtitle());
        execute(handle,Channel.TITLE,()->{policy.check(handle.target(),Channel.TITLE);transport(handle.target(),Channel.TITLE,components,times,Optional.empty());});
    }
    public void clearTitle(AudienceHandle handle,boolean reset){Channel channel=reset?Channel.TITLE_RESET:Channel.TITLE_CLEAR;policy.check(handle.target(),channel);execute(handle,channel,()->transport(handle.target(),channel,List.of(),Optional.empty(),Optional.empty()));}
    public void tab(AudienceHandle handle,Component header,Component footer){policy.check(handle.target(),Channel.TAB_HEADER_FOOTER);List<Component> components=List.of(header,footer);execute(handle,Channel.TAB_HEADER_FOOTER,()->transport(handle.target(),Channel.TAB_HEADER_FOOTER,components,Optional.empty(),Optional.empty()));}
    private BossBarState state(BossBar bar,BarBinding binding){return new BossBarState(binding.id,bar.progress(),bar.color().name(),bar.overlay().name(),bar.flags().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));}
    private void sendBar(AudienceHandle handle,BossBar bar,BarBinding binding,Channel channel){policy.check(handle.target(),channel);execute(handle,channel,()->transport(handle.target(),channel,List.of(bar.name()),Optional.empty(),Optional.of(state(bar,binding))));}
    public void showBossBar(AudienceHandle handle,BossBar bar){
        Objects.requireNonNull(bar);policy.check(handle.target(),Channel.BOSSBAR_ADD);
        execute(handle,Channel.BOSSBAR_ADD,()->{synchronized(bars){
            BarBinding binding=bars.get(bar);if(binding!=null&&binding.members.contains(handle.target()))return;
            boolean added=binding==null;if(added)binding=new BarBinding();
            transport(handle.target(),Channel.BOSSBAR_ADD,List.of(bar.name()),Optional.empty(),Optional.of(state(bar,binding)));
            if(added){bars.put(bar,binding);bar.addListener(binding);}binding.members.add(handle.target());
        }});
    }
    public void hideBossBar(AudienceHandle handle,BossBar bar){
        execute(handle,Channel.BOSSBAR_REMOVE,()->{synchronized(bars){BarBinding binding=bars.get(bar);if(binding==null||!binding.members.contains(handle.target()))return;transport(handle.target(),Channel.BOSSBAR_REMOVE,List.of(),Optional.empty(),Optional.of(state(bar,binding)));removeMembership(handle.target(),bar,binding);}});
    }
    private void removeMembership(Target target,BossBar bar,BarBinding binding){binding.members.remove(target);if(binding.members.isEmpty()){bar.removeListener(binding);bars.remove(bar);}}
    public List<BossBar> activeBossBars(AudienceHandle handle){synchronized(bars){return bars.entrySet().stream().filter(entry->entry.getValue().members.contains(handle.target())).map(Map.Entry::getKey).toList();}}
    private void retire(Target target){
        handles.remove(target);
        Runnable cleanup=()->{synchronized(bars){RuntimeException aggregate=null;for(var entry:List.copyOf(bars.entrySet()))if(entry.getValue().members.contains(target)){
            try{transport(target,Channel.BOSSBAR_REMOVE,List.of(),Optional.empty(),Optional.of(state(entry.getKey(),entry.getValue())));}catch(RuntimeException|Error failure){if(aggregate==null)aggregate=new IllegalStateException("AUDIENCE_CLEANUP_FAILED");aggregate.addSuppressed(failure);}finally{removeMembership(target,entry.getKey(),entry.getValue());}
        }if(aggregate!=null)throw aggregate;}};
        if(execution.isPrimaryThread())cleanup.run();else execution.executePrimary(()->{try{cleanup.run();}catch(RuntimeException|Error failure){failures.accept(target,failure);}});
    }
    @Override public synchronized void close(){
        if(!execution.isPrimaryThread())throw new IllegalStateException("ASYNC_AUDIENCE_CLOSE");if(closed)return;closed=true;
        RuntimeException aggregate=null;for(AudienceHandle handle:List.copyOf(handles.values()))try{handle.close();}catch(RuntimeException|Error failure){if(aggregate==null)aggregate=new IllegalStateException("AUDIENCE_CLEANUP_FAILED");aggregate.addSuppressed(failure);}if(aggregate!=null)throw aggregate;
    }
}
