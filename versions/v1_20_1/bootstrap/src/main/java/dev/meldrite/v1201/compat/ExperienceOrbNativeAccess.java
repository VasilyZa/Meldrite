package dev.meldrite.v1201.compat;
public interface ExperienceOrbNativeAccess {
    int meldrite$getAmount();
    void meldrite$setAmount(int value);
    java.util.UUID meldrite$getTriggerEntityId();
    void meldrite$setTriggerEntityId(java.util.UUID value);
    java.util.UUID meldrite$getSourceEntityId();
    void meldrite$setSourceEntityId(java.util.UUID value);
    org.bukkit.entity.ExperienceOrb.SpawnReason meldrite$getSpawnReason();
    void meldrite$setSpawnReason(org.bukkit.entity.ExperienceOrb.SpawnReason value);
}
