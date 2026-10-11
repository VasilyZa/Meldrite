package org.bukkit.craftbukkit.v1_20_R1.block;

import java.util.Collection;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.bukkit.block.Beacon;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativeBeaconRange;
import dev.meldrite.v1201.hook.BeaconBlockEntityAccessMixin;

/** Bukkit beacon state backed by the native 1.20.1 beacon tile. */
public final class CraftBeacon extends CraftBlockEntityState<BeaconBlockEntity> implements Beacon {
    public CraftBeacon(ServerWorld world, BlockPos position, BlockState state, BeaconBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    private BeaconBlockEntityAccessMixin access() { return (BeaconBlockEntityAccessMixin) (Object) snapshot; }
    private NativeBeaconRange rangeAccess() { return (NativeBeaconRange) (Object) snapshot; }

    @Override public Collection<org.bukkit.entity.LivingEntity> getEntitiesInRange() {
        requirePlaced();
        if (!(world.getBlockEntity(position) instanceof BeaconBlockEntity beacon)) return java.util.List.of();
        double radius = ((NativeBeaconRange) beacon).meldrite$getEffectRange();
        var bounds = new net.minecraft.util.math.Box(position).expand(radius).stretch(0, world.getHeight(), 0);
        return world.getEntitiesByClass(net.minecraft.entity.player.PlayerEntity.class, bounds, player -> !player.isSpectator()).stream()
                .map(player -> (org.bukkit.entity.LivingEntity) NativeEntityExtensions.bukkit(player)).toList();
    }

    @Override public int getTier() { return access().meldrite$level(); }

    @Override public PotionEffect getPrimaryEffect() { return toBukkit(access().meldrite$primary(), access().meldrite$secondary()); }
    @Override public void setPrimaryEffect(PotionEffectType effect) { access().meldrite$primary(toNative(effect)); }
    @Override public PotionEffect getSecondaryEffect() {
        return getTier() >= 4 && access().meldrite$secondary() != access().meldrite$primary()
                ? toBukkit(access().meldrite$secondary(), access().meldrite$primary()) : null;
    }
    @Override public void setSecondaryEffect(PotionEffectType effect) { access().meldrite$secondary(toNative(effect)); }

    private PotionEffect toBukkit(StatusEffect effect, StatusEffect other) {
        if (effect == null) return null;
        PotionEffectType type = PotionEffectType.getByKey(new org.bukkit.NamespacedKey(
                Registries.STATUS_EFFECT.getId(effect).getNamespace(), Registries.STATUS_EFFECT.getId(effect).getPath()));
        if (type == null) throw new UnsupportedOperationException("UNPROJECTED_BEACON_EFFECT: " + Registries.STATUS_EFFECT.getId(effect));
        int amplifier = getTier() >= 4 && effect == other ? 1 : 0;
        return new PotionEffect(type, (9 + getTier() * 2) * 20, amplifier, true, true, true);
    }

    private static StatusEffect toNative(PotionEffectType effect) {
        if (effect == null) return null;
        Identifier id = new Identifier(effect.getKey().getNamespace(), effect.getKey().getKey());
        return Registries.STATUS_EFFECT.getOrEmpty(id).orElseThrow(() -> new IllegalArgumentException("Unknown effect: " + effect.getKey()));
    }

    @Override public double getEffectRange() { return rangeAccess().meldrite$getEffectRange(); }
    @Override public void setEffectRange(double range) { rangeAccess().meldrite$setEffectRange(range); }
    @Override public void resetEffectRange() { rangeAccess().meldrite$resetEffectRange(); }

    @Override public Component customName() {
        Text name = snapshot.getCustomName();
        return name == null ? null : GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(name));
    }
    @Override public void customName(Component name) {
        snapshot.setCustomName(name == null ? null : Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(name)));
    }
    @Override public String getCustomName() {
        Text name = snapshot.getCustomName();
        return name == null ? null : CraftChatMessage.fromComponent(name);
    }
    @Override public void setCustomName(String name) { snapshot.setCustomName(CraftChatMessage.fromStringOrNull(name)); }

    @Override public boolean isLocked() { return !getLock().isEmpty(); }
    @Override public String getLock() {
        var nbt = snapshot.createNbt();
        return nbt.contains("Lock") ? nbt.getString("Lock") : "";
    }
    @Override public void setLock(String key) {
        var nbt = snapshot.createNbt();
        if (key == null || key.isEmpty()) nbt.remove("Lock"); else nbt.putString("Lock", key);
        snapshot.readNbt(nbt);
    }
}
