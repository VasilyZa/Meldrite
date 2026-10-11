package org.bukkit.craftbukkit.help;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.help.HelpTopic;

/** Reads Bukkit's help.yml and converts configured topics into help objects. */
public class HelpYamlReader {
    private static final char ALT_COLOR_CODE = '&';
    private final Server server;
    private YamlConfiguration helpYaml;

    public HelpYamlReader(Server server) {
        this.server = java.util.Objects.requireNonNull(server, "server");
        File file = new File("help.yml");
        try {
            InputStream resource = getClass().getClassLoader().getResourceAsStream("configurations/help.yml");
            YamlConfiguration defaults = resource == null ? new YamlConfiguration()
                    : YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
            helpYaml = YamlConfiguration.loadConfiguration(file);
            helpYaml.options().copyDefaults(true);
            helpYaml.setDefaults(defaults);
            if (!file.exists()) helpYaml.save(file);
        } catch (Exception failure) {
            server.getLogger().log(Level.WARNING, "Failed to load help.yml; using an empty help configuration", failure);
            helpYaml = new YamlConfiguration();
        }
    }

    public List<HelpTopic> getGeneralTopics() {
        List<HelpTopic> topics = new LinkedList<>();
        ConfigurationSection section = helpYaml.getConfigurationSection("general-topics");
        if (section == null) return topics;
        for (String name : section.getKeys(false)) {
            ConfigurationSection topic = section.getConfigurationSection(name);
            if (topic == null) continue;
            String shortText = color(topic.getString("shortText", ""));
            String fullText = color(topic.getString("fullText", ""));
            topics.add(new CustomHelpTopic(name, shortText, fullText, topic.getString("permission", "")));
        }
        return topics;
    }

    public List<HelpTopic> getIndexTopics() {
        List<HelpTopic> topics = new LinkedList<>();
        ConfigurationSection section = helpYaml.getConfigurationSection("index-topics");
        if (section == null) return topics;
        for (String name : section.getKeys(false)) {
            ConfigurationSection topic = section.getConfigurationSection(name);
            if (topic == null) continue;
            topics.add(new CustomIndexHelpTopic(server.getHelpMap(), name, color(topic.getString("shortText", "")),
                    color(topic.getString("permission", "")), topic.getStringList("commands"), color(topic.getString("preamble", ""))));
        }
        return topics;
    }

    public List<HelpTopicAmendment> getTopicAmendments() {
        List<HelpTopicAmendment> amendments = new LinkedList<>();
        ConfigurationSection section = helpYaml.getConfigurationSection("amended-topics");
        if (section == null) return amendments;
        for (String name : section.getKeys(false)) {
            ConfigurationSection topic = section.getConfigurationSection(name);
            if (topic == null) continue;
            amendments.add(new HelpTopicAmendment(name, color(topic.getString("shortText", "")),
                    color(topic.getString("fullText", "")), topic.getString("permission", "")));
        }
        return amendments;
    }

    public List<String> getIgnoredPlugins() { return helpYaml.getStringList("ignore-plugins"); }
    public boolean commandTopicsInMasterIndex() { return helpYaml.getBoolean("command-topics-in-master-index", true); }
    private static String color(String value) { return ChatColor.translateAlternateColorCodes(ALT_COLOR_CODE, value == null ? "" : value); }
}
