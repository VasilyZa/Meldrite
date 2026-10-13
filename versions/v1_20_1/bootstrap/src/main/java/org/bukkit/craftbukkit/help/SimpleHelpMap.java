package org.bukkit.craftbukkit.help;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.MultipleCommandAlias;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.HelpTopicFactory;
import org.bukkit.help.IndexHelpTopic;

/** Thread-safe Bukkit help index backed by the server command map. */
public class SimpleHelpMap implements HelpMap {
    private HelpTopic defaultTopic;
    private final Map<String, HelpTopic> helpTopics = new TreeMap<>(HelpTopicComparator.topicNameComparatorInstance());
    private final Map<Class, HelpTopicFactory<Command>> topicFactoryMap = new HashMap<>();
    private final CraftServer server;
    private HelpYamlReader yaml;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SimpleHelpMap(CraftServer server) {
        this.server = java.util.Objects.requireNonNull(server, "server");
        this.yaml = new HelpYamlReader(server);
        resetDefaultTopic();
        registerHelpTopicFactory(MultipleCommandAlias.class, new MultipleCommandAliasHelpTopicFactory());
    }

    private void resetDefaultTopic() {
        Predicate<HelpTopic> filter = Predicates.not(Predicates.instanceOf(CommandAliasHelpTopic.class));
        if (!yaml.commandTopicsInMasterIndex()) filter = Predicates.and(filter, Predicates.not(new IsCommandTopicPredicate()));
        defaultTopic = new IndexHelpTopic("Index", null, null, Collections2.filter(helpTopics.values(), filter),
                "Use /help [n] to get page n of help.");
    }

    @Override public synchronized HelpTopic getHelpTopic(String topicName) { return topicName.equals("") ? defaultTopic : helpTopics.get(topicName); }
    @Override public Collection<HelpTopic> getHelpTopics() { return helpTopics.values(); }
    @Override public synchronized void addTopic(HelpTopic topic) { helpTopics.putIfAbsent(topic.getName(), topic); }
    @Override public synchronized void clear() { helpTopics.clear(); }
    /** Retire the complete help generation before replacement plugins register topics and factories. */
    public synchronized void resetForReload() {
        helpTopics.clear();
        topicFactoryMap.clear();
        resetDefaultTopic();
        registerHelpTopicFactory(MultipleCommandAlias.class, new MultipleCommandAliasHelpTopicFactory());
    }
    @Override public List<String> getIgnoredPlugins() { return yaml.getIgnoredPlugins(); }

    public synchronized void initializeGeneralTopics() {
        yaml = new HelpYamlReader(server);
        resetDefaultTopic();
        for (HelpTopic topic : yaml.getGeneralTopics()) addTopic(topic);
        for (HelpTopic topic : yaml.getIndexTopics()) {
            if (topic.getName().equals("Default")) defaultTopic = topic;
            else addTopic(topic);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public synchronized void initializeCommands() {
        Set<String> ignored = new HashSet<>(yaml.getIgnoredPlugins());
        if (ignored.contains("All")) return;
        outer: for (Command command : server.getCommandMap().getCommands()) {
            if (ignored(command, ignored)) continue;
            for (Class type : topicFactoryMap.keySet()) {
                if (type.isAssignableFrom(command.getClass()) || command instanceof PluginCommand pluginCommand
                        && pluginCommand.getExecutor() != null && type.isAssignableFrom(pluginCommand.getExecutor().getClass())) {
                    HelpTopic topic = topicFactoryMap.get(type).createTopic(command);
                    if (topic != null) addTopic(topic);
                    continue outer;
                }
            }
            addTopic(new GenericCommandHelpTopic(command));
        }
        for (Command command : server.getCommandMap().getCommands()) {
            if (ignored(command, ignored)) continue;
            for (String alias : command.getAliases()) if (server.getCommandMap().getCommand(alias) == command)
                addTopic(new CommandAliasHelpTopic("/" + alias, "/" + command.getLabel(), this));
        }
        Collection<HelpTopic> aliases = Collections2.filter(helpTopics.values(), Predicates.instanceOf(CommandAliasHelpTopic.class));
        if (!aliases.isEmpty()) addTopic(new IndexHelpTopic("Aliases", "Lists command aliases", null, aliases));
        Map<String, Set<HelpTopic>> indexes = new HashMap<>();
        for (Command command : server.getCommandMap().getCommands()) {
            String owner = owner(command);
            HelpTopic topic = getHelpTopic("/" + command.getLabel());
            if (owner != null && topic != null) indexes.computeIfAbsent(owner, ownerName -> new TreeSet<>(HelpTopicComparator.helpTopicComparatorInstance())).add(topic);
        }
        for (Map.Entry<String, Set<HelpTopic>> entry : indexes.entrySet()) addTopic(new IndexHelpTopic("/" + entry.getKey(),
                "All commands for " + entry.getKey(), null, entry.getValue(), "Below is a list of all " + entry.getKey() + " commands:"));
        for (HelpTopicAmendment amendment : yaml.getTopicAmendments()) {
            HelpTopic topic = helpTopics.get(amendment.getTopicName());
            if (topic != null) {
                topic.amendTopic(amendment.getShortText(), amendment.getFullText());
                if (amendment.getPermission() != null) topic.amendCanSee(amendment.getPermission());
            }
        }
    }

    private boolean ignored(Command command, Set<String> ignored) {
        if (command instanceof BukkitCommand && ignored.contains("Bukkit")) return true;
        return command instanceof PluginIdentifiableCommand identifiable && ignored.contains(identifiable.getPlugin().getName());
    }
    private String owner(Command command) {
        if (command instanceof VanillaCommandWrapper) return "Minecraft";
        if (command instanceof BukkitCommand) return "Bukkit";
        return command instanceof PluginIdentifiableCommand identifiable ? identifiable.getPlugin().getName() : null;
    }
    @Override public void registerHelpTopicFactory(Class commandClass, HelpTopicFactory factory) {
        Preconditions.checkArgument(Command.class.isAssignableFrom(commandClass) || CommandExecutor.class.isAssignableFrom(commandClass),
                "commandClass (%s) must implement either Command or CommandExecutor", commandClass.getName());
        topicFactoryMap.put(commandClass, factory);
    }
    private final class IsCommandTopicPredicate implements Predicate<HelpTopic> {
        @Override public boolean apply(HelpTopic topic) { return !topic.getName().isEmpty() && topic.getName().charAt(0) == '/'; }
    }
}
