package org.bukkit.craftbukkit.inventory;

import java.util.Map;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.MusicInstrumentMeta;

@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaMusicInstrument extends CraftMetaItem implements MusicInstrumentMeta {
    public CraftMetaMusicInstrument() { }
    private CraftMetaMusicInstrument(CraftMetaMusicInstrument source) { super(source); }
    @Override public MusicInstrument getInstrument() { NamespacedKey key = mutableTag().get("instrument") instanceof String id ? NamespacedKey.fromString(id) : null; return key == null ? null : Registry.INSTRUMENT.get(key); }
    @Override public void setInstrument(MusicInstrument instrument) { value("instrument", instrument == null ? null : instrument.getKey().toString()); }
    @Override public CraftMetaMusicInstrument clone() { return new CraftMetaMusicInstrument(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = serialized("MUSIC_INSTRUMENT"); if (mutableTag().containsKey("instrument")) result.put("instrument", mutableTag().get("instrument")); return result; }
}
