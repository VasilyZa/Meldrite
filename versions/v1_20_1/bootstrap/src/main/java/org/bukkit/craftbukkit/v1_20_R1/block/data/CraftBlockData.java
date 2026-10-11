package org.bukkit.craftbukkit.v1_20_R1.block.data;

import dev.meldrite.v1201.compat.NativeBlockData;
import dev.meldrite.v1201.compat.NativeBlockDataBindings;
import dev.meldrite.v1201.compat.NativeMaterialProjection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.SideShapeType;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.EmptyBlockView;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundGroup;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.craftbukkit.v1_20_R1.CraftSoundGroup;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/** Root Craft ABI backed by an immutable native state and exact parsed properties. */
public class CraftBlockData implements BlockData {
    private BlockState state;
    private Map<Property<?>, Comparable<?>> parsedStates;

    public CraftBlockData(BlockState state) { this(state, null); }
    public CraftBlockData(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) {
        this.state = Objects.requireNonNull(state);
        this.parsedStates = parsedStates == null ? null : Collections.unmodifiableMap(new LinkedHashMap<>(parsedStates));
    }
    public static CraftBlockData fromData(BlockState state) { return createData(state, null); }
    public static CraftBlockData createData(BlockState state) { return fromData(state); }
    public static CraftBlockData createData(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) {
        Objects.requireNonNull(state, "state");
        CraftBlockData generated = NativeBlockDataBindings.create(state, parsedStates);
        if (generated != null) return generated;
        if (state.getBlock() instanceof StairsBlock) return new org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftStairs(state, parsedStates);
        if (state.getBlock() instanceof SlabBlock) return new org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftStepAbstract(state, parsedStates);
        if (state.getBlock() instanceof TrappedChestBlock) return new org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftChestTrapped(state, parsedStates);
        if (state.getBlock() instanceof ChestBlock) return new org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftChest(state, parsedStates);
        if (state.getBlock() instanceof DoorBlock) return new org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftDoor(state, parsedStates);
        if (state.getBlock() instanceof FenceBlock) return new org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftFence(state, parsedStates);
        if (state.getBlock() instanceof PillarBlock) return new org.bukkit.craftbukkit.v1_20_R1.block.impl.CraftRotatable(state, parsedStates);
        if (hasProperty(state, "axis", Direction.Axis.class)) return new CraftOrientable(state, parsedStates);
        if (hasProperty(state, "facing", Direction.class)) {
            if (hasProperty(state, "waterlogged", Boolean.class)) return new CraftDirectional.Waterlogged(state, parsedStates);
            return new CraftDirectional(state, parsedStates);
        }
        if (hasProperty(state, "waterlogged", Boolean.class)) return new CraftWaterlogged(state, parsedStates);
        return new CraftBlockData(state, parsedStates);
    }

    protected static net.minecraft.state.property.BooleanProperty getBoolean(Class<? extends net.minecraft.block.Block> block, String name) {
        return (net.minecraft.state.property.BooleanProperty) getState(block, name, false);
    }
    protected static net.minecraft.state.property.BooleanProperty getBoolean(Class<? extends net.minecraft.block.Block> block, String name, boolean optional) {
        return (net.minecraft.state.property.BooleanProperty) getState(block, name, optional);
    }
    protected static net.minecraft.state.property.EnumProperty<?> getEnum(Class<? extends net.minecraft.block.Block> block, String name) {
        return (net.minecraft.state.property.EnumProperty<?>) getState(block, name, false);
    }
    protected static net.minecraft.state.property.EnumProperty<?> getEnum(Class<? extends net.minecraft.block.Block> block, String name, boolean optional) {
        return (net.minecraft.state.property.EnumProperty<?>) getState(block, name, optional);
    }
    protected static net.minecraft.state.property.IntProperty getInteger(Class<? extends net.minecraft.block.Block> block, String name) {
        return (net.minecraft.state.property.IntProperty) getState(block, name, false);
    }
    private static Property<?> getState(Class<? extends net.minecraft.block.Block> block, String name, boolean optional) {
        Property<?> found = null;
        for (net.minecraft.block.Block instance : net.minecraft.registry.Registries.BLOCK) {
            if (instance.getClass() != block) continue;
            Property<?> candidate = instance.getStateManager().getProperty(name);
            if (candidate != null && found != null && candidate != found) throw new IllegalStateException("State mismatch " + block + "," + name);
            if (candidate != null) found = candidate;
        }
        if (found == null && !optional) throw new IllegalStateException("Null state for " + block + "," + name);
        return found;
    }
    protected static int getMin(net.minecraft.state.property.IntProperty property) { return property.getValues().stream().mapToInt(Integer::intValue).min().orElseThrow(); }
    protected static int getMax(net.minecraft.state.property.IntProperty property) { return property.getValues().stream().mapToInt(Integer::intValue).max().orElseThrow(); }
    public static CraftBlockData newData(Material material, String data) { return NativeBlockData.parse(material, data); }
    public BlockState getState() { return state; }
    protected static BlockState requireBlock(BlockState state, Class<? extends net.minecraft.block.Block> type) {
        Objects.requireNonNull(state, "state");
        if (!type.isInstance(state.getBlock())) throw new IllegalArgumentException("Native block is not " + type.getSimpleName() + ": " + state);
        return state;
    }
    protected final Property<?> property(String name) {
        return state.getProperties().stream().filter(property -> property.getName().equals(name)).findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Native block has no property '" + name + "': " + state));
    }
    protected final net.minecraft.state.property.BooleanProperty templateBoolean(String name) { return templateBoolean(name, false); }
    protected final net.minecraft.state.property.BooleanProperty templateBoolean(String name, boolean optional) {
        Property<?> property = state.getBlock().getStateManager().getProperty(name);
        if (property == null && optional) return null;
        if (!(property instanceof net.minecraft.state.property.BooleanProperty result)) throw new IllegalArgumentException("Missing boolean property: " + name + " in " + state);
        return result;
    }
    protected final net.minecraft.state.property.IntProperty templateInteger(String name) { return (net.minecraft.state.property.IntProperty) property(name); }
    protected final net.minecraft.state.property.EnumProperty<?> templateEnum(String name) { return (net.minecraft.state.property.EnumProperty<?>) property(name); }
    @SuppressWarnings("unchecked")
    protected final <T extends Comparable<T>> T propertyValue(String name) { return (T) state.get((Property<T>) property(name)); }
    protected final void propertyValue(String name, Comparable<?> value) { state = with(state, property(name), value); parsedStates = null; }
    private static boolean hasProperty(BlockState state, String name, Class<?> type) {
        return state.getProperties().stream().anyMatch(property -> property.getName().equals(name) && property.getType() == type);
    }
    @Override public Material getMaterial() {
        Material material = NativeMaterialProjection.material(state.getBlock());
        if (material == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_BLOCK_DATA: " + state);
        return material;
    }

    public <T extends Comparable<T>> T get(Property<T> property) { return state.get(property); }
    public <T extends Comparable<T>, V extends T> void set(Property<T> property, V value) {
        state = state.with(property, value);
        parsedStates = null;
    }

    protected <B extends Enum<B>> B get(net.minecraft.state.property.EnumProperty<?> property, Class<B> type) {
        return toBukkit(state.get(property), type);
    }
    protected <B extends Enum<B>> java.util.Set<B> getValues(net.minecraft.state.property.EnumProperty<?> property, Class<B> type) {
        java.util.Set<B> values = new java.util.LinkedHashSet<>();
        for (Enum<?> value : property.getValues()) values.add(toBukkit(value, type));
        return java.util.Collections.unmodifiableSet(values);
    }
    protected void set(net.minecraft.state.property.EnumProperty<?> property, Enum<?> value) {
        Objects.requireNonNull(value, "value");
        Enum<?> mapped = toNMS(value, property.getType());
        state = with(state, property, mapped);
        parsedStates = null;
    }
    private static <B extends Enum<B>> B toBukkit(Enum<?> value, Class<B> type) {
        if (value instanceof Direction direction && type == BlockFace.class) return type.cast(BlockFace.valueOf(direction.name()));
        B[] values = type.getEnumConstants();
        if (value.ordinal() >= values.length) throw new IllegalArgumentException("Unmapped native enum " + value + " for " + type);
        return values[value.ordinal()];
    }
    public static <N extends Enum<N>> N toNMS(Enum<?> value, Class<N> type) {
        Objects.requireNonNull(value, "value");
        if (value instanceof BlockFace && type == Direction.class) return Enum.valueOf(type, value.name());
        N[] values = type.getEnumConstants();
        if (value.ordinal() >= values.length) throw new IllegalArgumentException("Unmapped Bukkit enum " + value + " for " + type);
        return values[value.ordinal()];
    }

    @Override public CraftBlockData merge(BlockData data) {
        if (!(data instanceof CraftBlockData other)) throw new IllegalArgumentException("BlockData has no native state");
        if (other.parsedStates == null) throw new IllegalArgumentException("Data not created via string parsing or has been modified");
        if (state.getBlock() != other.state.getBlock()) throw new IllegalArgumentException("States have different native block types");
        CraftBlockData merged = clone();
        merged.parsedStates = null;
        for (var property : other.parsedStates.keySet()) merged.state = with(merged.state, property, other.state.getEntries().get(property));
        return merged;
    }
    @Override public boolean matches(BlockData data) {
        if (!(data instanceof CraftBlockData other) || state.getBlock() != other.state.getBlock()) return false;
        return equals(other) || other.parsedStates != null && merge(other).equals(this);
    }
    @Override public CraftBlockData clone() {
        try { return (CraftBlockData) super.clone(); }
        catch (CloneNotSupportedException failure) { throw new AssertionError(failure); }
    }
    @Override public String getAsString() { return BlockArgumentParser.stringifyBlockState(state); }
    @Override public String getAsString(boolean hideUnspecified) {
        return hideUnspecified && parsedStates != null ? toString(parsedStates) : getAsString();
    }
    public String toString(Map<Property<?>, Comparable<?>> properties) {
        String id = net.minecraft.registry.Registries.BLOCK.getId(state.getBlock()).toString();
        if (properties.isEmpty()) return id;
        StringJoiner values = new StringJoiner(",", id + "[", "]");
        properties.forEach((property, value) -> values.add(property.getName() + "=" + name(property, value)));
        return values.toString();
    }
    public NbtCompound toStates() {
        NbtCompound tag = new NbtCompound();
        state.getEntries().forEach((property, value) -> tag.putString(property.getName(), name(property, value)));
        return tag;
    }
    @Override public String toString() { return "CraftBlockData{" + getAsString() + "}"; }
    @Override public boolean equals(Object other) { return other instanceof CraftBlockData data && state.equals(data.state); }
    @Override public int hashCode() { return state.hashCode(); }
    @Override public SoundGroup getSoundGroup() { return CraftSoundGroup.getSoundGroup(state.getSoundGroup()); }
    @Override public int getLightEmission() { return state.getLuminance(); }
    @Override public boolean isOccluding() { return state.isOpaque(); }
    @Override public boolean requiresCorrectToolForDrops() { return state.isToolRequired(); }
    @Override public boolean isPreferredTool(ItemStack tool) { return isPreferredTool(state, CraftItemStack.asNMSCopy(Objects.requireNonNull(tool))); }
    public static boolean isPreferredTool(BlockState state, net.minecraft.item.ItemStack tool) { return !state.isToolRequired() || tool.isSuitableFor(state); }
    @Override public PistonMoveReaction getPistonMoveReaction() {
        return switch (state.getPistonBehavior()) {
            case NORMAL -> PistonMoveReaction.MOVE;
            case DESTROY -> PistonMoveReaction.BREAK;
            case BLOCK -> PistonMoveReaction.BLOCK;
            case IGNORE -> PistonMoveReaction.IGNORE;
            case PUSH_ONLY -> PistonMoveReaction.PUSH_ONLY;
        };
    }
    @Override public boolean isSupported(org.bukkit.block.Block block) { return isSupported(Objects.requireNonNull(block).getLocation()); }
    @Override public boolean isSupported(Location location) {
        Objects.requireNonNull(location, "location").checkFinite();
        if (!(location.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("Location has no native world");
        return state.canPlaceAt(world.getHandle(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
    @Override public boolean isFaceSturdy(BlockFace face, BlockSupport support) {
        Objects.requireNonNull(face, "face"); Objects.requireNonNull(support, "support");
        Direction direction;
        try { direction = Direction.valueOf(face.name()); }
        catch (IllegalArgumentException failure) { throw new IllegalArgumentException("Face is not an orthogonal native direction: " + face, failure); }
        return state.isSideSolid(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, direction, SideShapeType.valueOf(support.name()));
    }
    @Override public Material getPlacementMaterial() {
        Material material = NativeMaterialProjection.material(state.getBlock().asItem());
        if (material == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_PLACEMENT_ITEM: " + state);
        return material;
    }
    @Override public void rotate(StructureRotation rotation) {
        state = state.rotate(BlockRotation.valueOf(Objects.requireNonNull(rotation).name()));
        parsedStates = null;
    }
    @Override public void mirror(Mirror mirror) {
        state = state.mirror(BlockMirror.valueOf(Objects.requireNonNull(mirror).name()));
        parsedStates = null;
    }
    @Override public org.bukkit.block.BlockState createBlockState() { return new org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState(state); }
    @Override public float getDestroySpeed(ItemStack itemStack, boolean considerEnchants) {
        var item = CraftItemStack.unwrap(Objects.requireNonNull(itemStack));
        float speed = item.getMiningSpeedMultiplier(state);
        if (speed > 1 && considerEnchants) {
            int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, item);
            if (efficiency > 0) speed += efficiency * efficiency + 1;
        }
        return speed;
    }
    @Override public boolean isRandomlyTicked() { return state.hasRandomTicks(); }
    public float getHardness() { return state.getHardness(EmptyBlockView.INSTANCE, BlockPos.ORIGIN); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState with(BlockState state, Property property, Comparable value) { return state.with(property, value); }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String name(Property property, Comparable value) { return property.name(value); }
}
