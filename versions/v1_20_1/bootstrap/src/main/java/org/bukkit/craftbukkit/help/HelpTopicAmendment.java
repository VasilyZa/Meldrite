package org.bukkit.craftbukkit.help;

public class HelpTopicAmendment {
    private final String topicName;
    private final String shortText;
    private final String fullText;
    private final String permission;

    public HelpTopicAmendment(String topicName, String shortText, String fullText, String permission) {
        this.topicName = topicName;
        this.shortText = shortText;
        this.fullText = fullText;
        this.permission = permission;
    }

    public String getTopicName() { return topicName; }
    public String getShortText() { return shortText; }
    public String getFullText() { return fullText; }
    public String getPermission() { return permission; }
}
