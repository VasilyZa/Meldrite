package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.MerchantEntityPaperAccessMixin;
import dev.meldrite.v1201.hook.VillagerEntityPaperAccessMixin;
import dev.meldrite.v1201.hook.VillagerGossipsPaperAccessMixin;
import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.bukkit.Location;
import org.bukkit.entity.Villager;

public class CraftVillager extends CraftAbstractVillager implements Villager {
    public CraftVillager(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public VillagerEntity getHandle() { return (VillagerEntity) super.getHandle(); }
    private VillagerEntityPaperAccessMixin access() { return (VillagerEntityPaperAccessMixin) getHandle(); }
    @Override public void remove() { requireWritable(); access().meldrite$releaseAllTickets(); super.remove(); }
    public static Profession nmsToBukkitProfession(VillagerProfession profession) { return Profession.valueOf(Registries.VILLAGER_PROFESSION.getId(profession).getPath().toUpperCase(Locale.ROOT)); }
    public static VillagerProfession bukkitToNmsProfession(Profession profession) { var key = java.util.Objects.requireNonNull(profession, "profession").getKey(); return Registries.VILLAGER_PROFESSION.get(new Identifier(key.getNamespace(), key.getKey())); }
    @Override public Profession getProfession() { return nmsToBukkitProfession(getHandle().getVillagerData().getProfession()); }
    @Override public void setProfession(Profession profession) { var nativeProfession = bukkitToNmsProfession(profession); requireWritable(); getHandle().setVillagerData(getHandle().getVillagerData().withProfession(nativeProfession)); }
    @Override public Type getVillagerType() { return Type.valueOf(Registries.VILLAGER_TYPE.getId(getHandle().getVillagerData().getType()).getPath().toUpperCase(Locale.ROOT)); }
    @Override public void setVillagerType(Type type) { var key = java.util.Objects.requireNonNull(type, "type").getKey(); requireWritable(); getHandle().setVillagerData(getHandle().getVillagerData().withType(Registries.VILLAGER_TYPE.get(new Identifier(key.getNamespace(), key.getKey())))); }
    @Override public int getVillagerLevel() { return getHandle().getVillagerData().getLevel(); }
    @Override public void setVillagerLevel(int level) { if (level < 1 || level > 5) throw new IllegalArgumentException("Villager level must be between 1 and 5"); requireWritable(); getHandle().setVillagerData(getHandle().getVillagerData().withLevel(level)); }
    @Override public int getVillagerExperience() { return getHandle().getExperience(); }
    @Override public void setVillagerExperience(int experience) { if (experience < 0) throw new IllegalArgumentException("Experience must be nonnegative"); requireWritable(); getHandle().setExperience(experience); }
    @Override public boolean increaseLevel(int amount) {
        if (amount <= 0 || amount > 5 - getVillagerLevel()) throw new IllegalArgumentException("Final villager level must be between 1 and 5");
        requireWritable(); var trades = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(getHandle().getVillagerData().getProfession());
        if (trades == null || trades.isEmpty()) { setVillagerLevel(getVillagerLevel() + amount); return false; }
        for (int i = 0; i < amount; i++) access().meldrite$levelUp();
        return true;
    }
    @Override public boolean addTrades(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Trade count must be positive"); requireWritable();
        var trades = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(getHandle().getVillagerData().getProfession());
        if (trades == null || trades.isEmpty()) return false;
        var pool = trades.get(getVillagerLevel()); if (pool == null) return false;
        ((MerchantEntityPaperAccessMixin) getHandle()).meldrite$fillOffers(getHandle().getOffers(), pool, amount); return true;
    }
    @Override public int getRestocksToday() { return access().meldrite$getRestocksToday(); }
    @Override public void setRestocksToday(int restocks) { requireWritable(); access().meldrite$setRestocksToday(restocks); }
    @Override public boolean sleep(Location location) {
        java.util.Objects.requireNonNull(location, "location"); if (!getWorld().equals(location.getWorld())) throw new IllegalArgumentException("Cannot sleep across worlds");
        requireWritable(); var pos = new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (!(getHandle().getWorld().getBlockState(pos).getBlock() instanceof net.minecraft.block.BedBlock)) return false;
        getHandle().sleep(pos); return true;
    }
    @Override public void wakeup() { if (!isSleeping()) throw new IllegalStateException("Villager is not sleeping"); requireWritable(); getHandle().wakeUp(); }
    @Override public void shakeHead() { requireWritable(); access().meldrite$shakeHead(); }
    @Override public org.bukkit.entity.ZombieVillager zombify() {
        requireWritable(); var original = getHandle(); var world = (net.minecraft.server.world.ServerWorld) original.getWorld(); var silent = original.isSilent();
        var zombie = dev.meldrite.v1201.compat.NativeMobConversionEvents.convert(original, net.minecraft.entity.EntityType.ZOMBIE_VILLAGER, false, org.bukkit.event.entity.EntityTransformEvent.TransformReason.INFECTION, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM); if (zombie == null) return null;
        zombie.initialize(world, world.getLocalDifficulty(zombie.getBlockPos()), net.minecraft.entity.SpawnReason.CONVERSION, new net.minecraft.entity.mob.ZombieEntity.ZombieData(false, true), null);
        zombie.setVillagerData(original.getVillagerData()); zombie.setGossipData(original.getGossip().serialize(net.minecraft.nbt.NbtOps.INSTANCE));
        zombie.setOfferData(original.getOffers().toNbt()); zombie.setXp(original.getExperience());
        if (!silent) world.syncWorldEvent(null, 1026, original.getBlockPos(), 0);
        return (org.bukkit.entity.ZombieVillager) NativeEntityExtensions.bukkit(zombie);
    }
    @Override public Reputation getReputation(UUID uuid) {
        java.util.Objects.requireNonNull(uuid, "uuid"); var values = getHandle().getGossip().getEntityReputationAssociatedGossips().get(uuid);
        var result = new java.util.EnumMap<ReputationType, Integer>(ReputationType.class);
        if (values != null) values.forEach((type, value) -> result.put(ReputationType.valueOf(type.name()), value)); return new Reputation(result);
    }
    @Override public Map<UUID, Reputation> getReputations() { var result = new java.util.HashMap<UUID, Reputation>(); getHandle().getGossip().getEntityReputationAssociatedGossips().keySet().forEach(uuid -> result.put(uuid, getReputation(uuid))); return result; }
    @Override public void setReputation(UUID uuid, Reputation reputation) {
        java.util.Objects.requireNonNull(uuid, "uuid"); java.util.Objects.requireNonNull(reputation, "reputation"); requireWritable(); var gossip = getHandle().getGossip();
        for (var type : net.minecraft.village.VillageGossipType.values()) {
            var apiType = ReputationType.valueOf(type.name()); if (!reputation.hasReputationSet(apiType)) continue;
            int value = reputation.getReputation(apiType);
            var values = gossip.getEntityReputationAssociatedGossips().get(uuid);
            if (values == null && value != 0) { gossip.startGossip(uuid, type, 2); values = gossip.getEntityReputationAssociatedGossips().get(uuid); }
            if (values != null) { if (value == 0) values.removeInt(type); else values.put(type, value); }
        }
    }
    @Override public void setReputations(Map<UUID, Reputation> reputations) { reputations.forEach(this::setReputation); }
    @Override public void clearReputations() { requireWritable(); ((VillagerGossipsPaperAccessMixin) getHandle().getGossip()).meldrite$reputations().clear(); }
}
