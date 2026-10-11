package dev.meldrite.paper.facade;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spigotmc.SpigotConfig;
import dev.meldrite.paper.configuration.ServerConfiguration;
public class ServerAdapter implements org.bukkit.Server {
  protected final FacadeContext context;
  private volatile ConsoleCommandSender consoleSender;
  private final Messenger messenger = new StandardMessenger();
  private volatile boolean stopping;
  private final org.bukkit.Server.Spigot spigot = new org.bukkit.Server.Spigot() {
    @Deprecated
    @Override public YamlConfiguration getConfig() { return SpigotConfig.config; }
    @Override public YamlConfiguration getBukkitConfig() { return ServerConfiguration.current().bukkit(); }
    @Override public YamlConfiguration getSpigotConfig() { return SpigotConfig.config; }
    @Override public YamlConfiguration getPaperConfig() { return ServerConfiguration.current().paper(); }
    @Override public void broadcast(net.md_5.bungee.api.chat.BaseComponent component) {
      if (component == null) return;
      for (org.bukkit.entity.Player player : getOnlinePlayers()) player.spigot().sendMessage(component);
    }
    @Override public void broadcast(net.md_5.bungee.api.chat.BaseComponent... components) {
      if (components == null) return;
      for (org.bukkit.entity.Player player : getOnlinePlayers()) player.spigot().sendMessage(components);
    }
    @Override public void restart() {
      throw new UnsupportedOperationException("RESTART_REQUIRES_EXTERNAL_LAUNCHER");
    }
  };
  private final HelpMap helpMap = new HelpMap() {
    private final java.util.Map<String, HelpTopic> topics = new java.util.LinkedHashMap<>();
    @Override public HelpTopic getHelpTopic(String name) { return topics.get(name); }
    @Override public java.util.Collection<HelpTopic> getHelpTopics() { return java.util.List.copyOf(topics.values()); }
    @Override public void addTopic(HelpTopic topic) { if (topic != null) topics.put(topic.getName(), topic); }
    @Override public void clear() { topics.clear(); }
    @Override public void registerHelpTopicFactory(Class<?> commandClass, HelpTopicFactory<?> factory) { }
    @Override public java.util.List<String> getIgnoredPlugins() { return java.util.List.of(); }
  };
  public ServerAdapter(FacadeContext context){this.context=java.util.Objects.requireNonNull(context);}
  /** Exposes the shared facade context to version-owned CraftServer implementations. */
  protected final FacadeContext facadeContext() { return context; }
  @Override public boolean addRecipe(org.bukkit.inventory.Recipe p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#addRecipe(Lorg/bukkit/inventory/Recipe;)Z#METHOD"); }
  @Override public boolean addRecipe(org.bukkit.inventory.Recipe p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#addRecipe(Lorg/bukkit/inventory/Recipe;Z)Z#METHOD"); }
  @Override public java.util.Iterator<org.bukkit.advancement.Advancement> advancementIterator() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#advancementIterator()Ljava/util/Iterator;#METHOD"); }
  @Override public java.lang.Iterable<? extends net.kyori.adventure.audience.Audience> audiences() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#audiences()Ljava/lang/Iterable;#INHERITED_METHOD"); }
  @Override public void banIP(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#banIP(Ljava/lang/String;)V#METHOD"); }
  @Override public void banIP(java.net.InetAddress p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#banIP(Ljava/net/InetAddress;)V#METHOD"); }
  @Override public int broadcast(java.lang.String p0, java.lang.String p1) {
    int delivered = 0;
    for (org.bukkit.entity.Player player : getOnlinePlayers()) {
      if (p1 == null || p1.isBlank() || player.hasPermission(p1)) { player.sendMessage(p0 == null ? "" : p0); delivered++; }
    }
    if (delivered == 0 && p0 != null) context.info().logger().info(p0);
    return delivered;
  }
  @Override public int broadcast(net.kyori.adventure.text.Component p0) { return broadcast(p0 == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(p0), ""); }
  @Override public int broadcast(net.kyori.adventure.text.Component p0, java.lang.String p1) { return broadcast(p0 == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(p0), p1); }
  @Override public int broadcastMessage(java.lang.String p0) { return broadcast(p0, ""); }
  @Override public void clearRecipes() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#clearRecipes()V#METHOD"); }
  @Override public org.bukkit.inventory.ItemStack craftItem(org.bukkit.inventory.ItemStack[] p0, org.bukkit.World p1, org.bukkit.entity.Player p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#craftItem([Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/World;Lorg/bukkit/entity/Player;)Lorg/bukkit/inventory/ItemStack;#METHOD"); }
  @Override public org.bukkit.block.data.BlockData createBlockData(java.lang.String p0) throws java.lang.IllegalArgumentException {
    if (p0 == null) throw new IllegalArgumentException("data cannot be null");
    return createNativeBlockData(null, p0);
  }
  @Override public org.bukkit.block.data.BlockData createBlockData(org.bukkit.Material p0) {
    return createNativeBlockData(java.util.Objects.requireNonNull(p0, "material"), null);
  }
  @Override public org.bukkit.block.data.BlockData createBlockData(org.bukkit.Material p0, java.lang.String p1) throws java.lang.IllegalArgumentException {
    if (p0 == null && p1 == null) throw new IllegalArgumentException("Must provide material or data");
    return createNativeBlockData(p0, p1);
  }
  @Override public org.bukkit.block.data.BlockData createBlockData(org.bukkit.Material p0, java.util.function.Consumer<org.bukkit.block.data.BlockData> p1) {
    var data = createBlockData(p0);
    if (p1 != null) p1.accept(data);
    return data;
  }
  private org.bukkit.block.data.BlockData createNativeBlockData(org.bukkit.Material material, String data) {
    try {
      Class<?> type = Class.forName("dev.meldrite.v1201.compat.NativeBlockData", true, getClass().getClassLoader());
      return (org.bukkit.block.data.BlockData) type.getMethod("parse", org.bukkit.Material.class, String.class).invoke(null, material, data);
    } catch (java.lang.reflect.InvocationTargetException failure) {
      Throwable cause = failure.getCause();
      if (cause instanceof IllegalArgumentException illegal) throw illegal;
      throw new IllegalStateException("NATIVE_BLOCK_DATA_FAILED", cause);
    } catch (ReflectiveOperationException failure) {
      throw new IllegalStateException("NATIVE_BLOCK_DATA_UNAVAILABLE", failure);
    }
  }
  @Override public org.bukkit.boss.BossBar createBossBar(java.lang.String p0, org.bukkit.boss.BarColor p1, org.bukkit.boss.BarStyle p2, org.bukkit.boss.BarFlag... p3) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createBossBar(Ljava/lang/String;Lorg/bukkit/boss/BarColor;Lorg/bukkit/boss/BarStyle;[Lorg/bukkit/boss/BarFlag;)Lorg/bukkit/boss/BossBar;#METHOD"); }
  @Override public org.bukkit.boss.KeyedBossBar createBossBar(org.bukkit.NamespacedKey p0, java.lang.String p1, org.bukkit.boss.BarColor p2, org.bukkit.boss.BarStyle p3, org.bukkit.boss.BarFlag... p4) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createBossBar(Lorg/bukkit/NamespacedKey;Ljava/lang/String;Lorg/bukkit/boss/BarColor;Lorg/bukkit/boss/BarStyle;[Lorg/bukkit/boss/BarFlag;)Lorg/bukkit/boss/KeyedBossBar;#METHOD"); }
  @Override public org.bukkit.generator.ChunkGenerator.ChunkData createChunkData(org.bukkit.World p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createChunkData(Lorg/bukkit/World;)Lorg/bukkit/generator/ChunkGenerator$ChunkData;#METHOD"); }
  @Override public org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer<? super net.kyori.adventure.text.Component> p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createCommandSender(Ljava/util/function/Consumer;)Lorg/bukkit/command/CommandSender;#METHOD"); }
  @Override public org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.StructureType p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createExplorerMap(Lorg/bukkit/World;Lorg/bukkit/Location;Lorg/bukkit/StructureType;)Lorg/bukkit/inventory/ItemStack;#METHOD"); }
  @Override public org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.StructureType p2, int p3, boolean p4) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createExplorerMap(Lorg/bukkit/World;Lorg/bukkit/Location;Lorg/bukkit/StructureType;IZ)Lorg/bukkit/inventory/ItemStack;#METHOD"); }
  @Override public org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.generator.structure.StructureType p2, org.bukkit.map.MapCursor.Type p3, int p4, boolean p5) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createExplorerMap(Lorg/bukkit/World;Lorg/bukkit/Location;Lorg/bukkit/generator/structure/StructureType;Lorg/bukkit/map/MapCursor$Type;IZ)Lorg/bukkit/inventory/ItemStack;#METHOD"); }
  @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, int p1) throws java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createInventory(Lorg/bukkit/inventory/InventoryHolder;I)Lorg/bukkit/inventory/Inventory;#METHOD"); }
  @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, int p1, java.lang.String p2) throws java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createInventory(Lorg/bukkit/inventory/InventoryHolder;ILjava/lang/String;)Lorg/bukkit/inventory/Inventory;#METHOD"); }
  @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, int p1, net.kyori.adventure.text.Component p2) throws java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createInventory(Lorg/bukkit/inventory/InventoryHolder;ILnet/kyori/adventure/text/Component;)Lorg/bukkit/inventory/Inventory;#METHOD"); }
  @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, org.bukkit.event.inventory.InventoryType p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createInventory(Lorg/bukkit/inventory/InventoryHolder;Lorg/bukkit/event/inventory/InventoryType;)Lorg/bukkit/inventory/Inventory;#METHOD"); }
  @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, org.bukkit.event.inventory.InventoryType p1, java.lang.String p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createInventory(Lorg/bukkit/inventory/InventoryHolder;Lorg/bukkit/event/inventory/InventoryType;Ljava/lang/String;)Lorg/bukkit/inventory/Inventory;#METHOD"); }
  @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, org.bukkit.event.inventory.InventoryType p1, net.kyori.adventure.text.Component p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createInventory(Lorg/bukkit/inventory/InventoryHolder;Lorg/bukkit/event/inventory/InventoryType;Lnet/kyori/adventure/text/Component;)Lorg/bukkit/inventory/Inventory;#METHOD"); }
  @Override public org.bukkit.map.MapView createMap(org.bukkit.World p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createMap(Lorg/bukkit/World;)Lorg/bukkit/map/MapView;#METHOD"); }
  @Override public org.bukkit.inventory.Merchant createMerchant(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createMerchant(Ljava/lang/String;)Lorg/bukkit/inventory/Merchant;#METHOD"); }
  @Override public org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createMerchant(Lnet/kyori/adventure/text/Component;)Lorg/bukkit/inventory/Merchant;#METHOD"); }
  @Override public org.bukkit.profile.PlayerProfile createPlayerProfile(java.lang.String p0) { return new PlayerProfileAdapter(null, p0); }
  @Override public org.bukkit.profile.PlayerProfile createPlayerProfile(java.util.UUID p0) { return new PlayerProfileAdapter(p0, null); }
  @Override public org.bukkit.profile.PlayerProfile createPlayerProfile(java.util.UUID p0, java.lang.String p1) { return new PlayerProfileAdapter(p0, p1); }
  @Override public com.destroystokyo.paper.profile.PlayerProfile createProfile(java.lang.String p0) { return createProfile(null, p0); }
  @Override public com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0) { return createProfile(p0, null); }
  @Override public com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0, java.lang.String p1) {
    org.bukkit.entity.Player player = p0 != null ? getPlayer(p0) : (p1 != null ? getPlayerExact(p1) : null);
    return player == null ? new PlayerProfileAdapter(p0, p1) : new PlayerProfileAdapter(PlayerProfileAdapter.from(player.getPlayerProfile()));
  }
  @Override public com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID p0, java.lang.String p1) {
    org.bukkit.entity.Player player = p0 != null ? getPlayer(p0) : (p1 != null ? getPlayerExact(p1) : null);
    com.mojang.authlib.GameProfile profile = new com.mojang.authlib.GameProfile(p0, p1);
    if (player != null) profile.getProperties().putAll(PlayerProfileAdapter.from(player.getPlayerProfile()).getProperties());
    return new PlayerProfileAdapter(profile);
  }
  @Override public org.bukkit.generator.ChunkGenerator.ChunkData createVanillaChunkData(org.bukkit.World p0, int p1, int p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createVanillaChunkData(Lorg/bukkit/World;II)Lorg/bukkit/generator/ChunkGenerator$ChunkData;#METHOD"); }
  @Override public org.bukkit.World createWorld(org.bukkit.WorldCreator p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createWorld(Lorg/bukkit/WorldCreator;)Lorg/bukkit/World;#METHOD"); }
  @Override public org.bukkit.WorldBorder createWorldBorder() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#createWorldBorder()Lorg/bukkit/WorldBorder;#METHOD"); }
  @Override public boolean dispatchCommand(org.bukkit.command.CommandSender p0, java.lang.String p1) throws org.bukkit.command.CommandException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#dispatchCommand(Lorg/bukkit/command/CommandSender;Ljava/lang/String;)Z#METHOD"); }
  @Override public org.bukkit.advancement.Advancement getAdvancement(org.bukkit.NamespacedKey p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAdvancement(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/advancement/Advancement;#METHOD"); }
  @Override public boolean getAllowEnd() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAllowEnd()Z#METHOD"); }
  @Override public boolean getAllowFlight() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAllowFlight()Z#METHOD"); }
  @Override public boolean getAllowNether() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAllowNether()Z#METHOD"); }
  @Override public int getAmbientSpawnLimit() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAmbientSpawnLimit()I#METHOD"); }
  @Override public int getAnimalSpawnLimit() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAnimalSpawnLimit()I#METHOD"); }
  @Override public io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAsyncScheduler()Lio/papermc/paper/threadedregions/scheduler/AsyncScheduler;#METHOD"); }
  @Override public double getAverageTickTime() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getAverageTickTime()D#METHOD"); }
  @Override public <T extends org.bukkit.BanList<?>> T getBanList(org.bukkit.BanList.Type p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getBanList(Lorg/bukkit/BanList$Type;)Lorg/bukkit/BanList;#METHOD"); }
  @Override public java.util.Set<org.bukkit.OfflinePlayer> getBannedPlayers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getBannedPlayers()Ljava/util/Set;#METHOD"); }
  @Override public org.bukkit.boss.KeyedBossBar getBossBar(org.bukkit.NamespacedKey p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getBossBar(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/boss/KeyedBossBar;#METHOD"); }
  @Override public java.util.Iterator<org.bukkit.boss.KeyedBossBar> getBossBars() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getBossBars()Ljava/util/Iterator;#METHOD"); }
  @Override public java.lang.String getBukkitVersion() { return context.info().bukkitVersion(); }
  @Override public java.util.Map<java.lang.String, java.lang.String[]> getCommandAliases() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getCommandAliases()Ljava/util/Map;#METHOD"); }
  @Override public org.bukkit.command.CommandMap getCommandMap() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getCommandMap()Lorg/bukkit/command/CommandMap;#METHOD"); }
  @Override public long getConnectionThrottle() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getConnectionThrottle()J#METHOD"); }
  @Override public org.bukkit.command.ConsoleCommandSender getConsoleSender() {
    var existing = consoleSender;
    if (existing != null) return existing;
    synchronized (this) {
      if (consoleSender == null) consoleSender = createConsoleSender();
      return consoleSender;
    }
  }

  private ConsoleCommandSender createConsoleSender() {
    final ConsoleCommandSender[] holder = new ConsoleCommandSender[1];
    var handler = (java.lang.reflect.InvocationHandler) (proxy, method, args) -> {
      String name = method.getName();
      Object[] actual = args == null ? new Object[0] : args;
      switch (name) {
        case "getServer" -> { return this; }
        case "getName" -> { return "CONSOLE"; }
        case "name" -> { return net.kyori.adventure.text.Component.text("CONSOLE"); }
        case "sendMessage", "sendRawMessage" -> {
          logConsoleMessage(actual);
          return null;
        }
        case "spigot" -> {
          return new CommandSender.Spigot() {
            @Override public void sendMessage(net.md_5.bungee.api.chat.BaseComponent message) {
              if (message != null) context.info().logger().info(message.toLegacyText());
            }
            @Override public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... messages) {
              if (messages != null) for (var message : messages) sendMessage(message);
            }
            @Override public void sendMessage(UUID sender, net.md_5.bungee.api.chat.BaseComponent message) { sendMessage(message); }
            @Override public void sendMessage(UUID sender, net.md_5.bungee.api.chat.BaseComponent... messages) { sendMessage(messages); }
          };
        }
        case "isPermissionSet", "hasPermission" -> { return true; }
        case "addAttachment" -> {
          Plugin plugin = actual.length > 0 && actual[0] instanceof Plugin value ? value : null;
          if (plugin == null) return null;
          return new PermissionAttachment(plugin, holder[0]);
        }
        case "removeAttachment", "recalculatePermissions", "setOp", "acceptConversationInput",
             "abandonConversation" -> { return null; }
        case "getEffectivePermissions" -> { return Set.<PermissionAttachmentInfo>of(); }
        case "isOp" -> { return true; }
        case "isConversing" -> { return false; }
        case "beginConversation" -> { return false; }
        case "getActiveConversation" -> { return null; }
        case "equals" -> { return proxy == actual[0]; }
        case "hashCode" -> { return System.identityHashCode(proxy); }
        case "toString" -> { return "ConsoleCommandSender[CONSOLE]"; }
        default -> { return defaultValue(method.getReturnType()); }
      }
    };
    var proxy = (ConsoleCommandSender) Proxy.newProxyInstance(
        ConsoleCommandSender.class.getClassLoader(), new Class<?>[]{ConsoleCommandSender.class}, handler);
    holder[0] = proxy;
    return proxy;
  }

  private void logConsoleMessage(Object[] args) {
    if (args.length == 0) return;
    Object message = args[args.length - 1];
    if (message instanceof String text) context.info().logger().info(text);
    else if (message instanceof String[] texts) for (var text : texts) context.info().logger().info(text);
    else if (message instanceof net.kyori.adventure.text.Component component) context.info().logger().info(component.toString());
  }

  private static Object defaultValue(Class<?> type) {
    if (!type.isPrimitive()) return null;
    if (type == boolean.class) return false;
    if (type == char.class) return '\0';
    if (type == byte.class || type == short.class || type == int.class || type == long.class) return 0;
    if (type == float.class) return 0F;
    if (type == double.class) return 0D;
    return null;
  }
  @Override public org.bukkit.inventory.Recipe getCraftingRecipe(org.bukkit.inventory.ItemStack[] p0, org.bukkit.World p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getCraftingRecipe([Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/World;)Lorg/bukkit/inventory/Recipe;#METHOD"); }
  @Override public int getCurrentTick() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getCurrentTick()I#METHOD"); }
  @Override public org.bukkit.packs.DataPackManager getDataPackManager() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getDataPackManager()Lorg/bukkit/packs/DataPackManager;#METHOD"); }
  @Override public io.papermc.paper.datapack.DatapackManager getDatapackManager() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getDatapackManager()Lio/papermc/paper/datapack/DatapackManager;#METHOD"); }
  @Override public org.bukkit.GameMode getDefaultGameMode() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getDefaultGameMode()Lorg/bukkit/GameMode;#METHOD"); }
  @Override public org.bukkit.entity.Entity getEntity(java.util.UUID p0) { return context.entityByUuid(p0); }
  @Override public boolean getGenerateStructures() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getGenerateStructures()Z#METHOD"); }
  @Override public io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getGlobalRegionScheduler()Lio/papermc/paper/threadedregions/scheduler/GlobalRegionScheduler;#METHOD"); }
  @Override public org.bukkit.help.HelpMap getHelpMap() { return helpMap; }
  @Override public boolean getHideOnlinePlayers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getHideOnlinePlayers()Z#METHOD"); }
  @Override public java.util.Set<java.lang.String> getIPBans() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getIPBans()Ljava/util/Set;#METHOD"); }
  @Override public int getIdleTimeout() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getIdleTimeout()I#METHOD"); }
  @Override public java.util.List<java.lang.String> getInitialDisabledPacks() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getInitialDisabledPacks()Ljava/util/List;#METHOD"); }
  @Override public java.util.List<java.lang.String> getInitialEnabledPacks() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getInitialEnabledPacks()Ljava/util/List;#METHOD"); }
  @Override public java.lang.String getIp() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getIp()Ljava/lang/String;#METHOD"); }
  @Override public org.bukkit.inventory.ItemFactory getItemFactory() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getItemFactory()Lorg/bukkit/inventory/ItemFactory;#METHOD"); }
  @Override public java.util.Set<java.lang.String> getListeningPluginChannels() {
    var channels = new java.util.HashSet<String>();
    for (var player : getOnlinePlayers()) channels.addAll(player.getListeningPluginChannels());
    return channels;
  }
  @Override public java.util.logging.Logger getLogger() { return context.info().logger(); }
  @Override public org.bukkit.loot.LootTable getLootTable(org.bukkit.NamespacedKey p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getLootTable(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/loot/LootTable;#METHOD"); }
  @Override public org.bukkit.map.MapView getMap(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getMap(I)Lorg/bukkit/map/MapView;#METHOD"); }
  @Override public int getMaxChainedNeighborUpdates() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getMaxChainedNeighborUpdates()I#METHOD"); }
  @Override public int getMaxPlayers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getMaxPlayers()I#METHOD"); }
  @Override public int getMaxWorldSize() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getMaxWorldSize()I#METHOD"); }
  @Override public org.bukkit.plugin.messaging.Messenger getMessenger() { return messenger; }
  @Override public java.lang.String getMinecraftVersion() { return context.info().minecraftVersion(); }
  @Override public com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getMobGoals()Lcom/destroystokyo/paper/entity/ai/MobGoals;#METHOD"); }
  @Override public int getMonsterSpawnLimit() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getMonsterSpawnLimit()I#METHOD"); }
  @Override public java.lang.String getMotd() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getMotd()Ljava/lang/String;#METHOD"); }
  @Override public java.lang.String getName() { return context.info().name(); }
  @Override public org.bukkit.OfflinePlayer getOfflinePlayer(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getOfflinePlayer(Ljava/lang/String;)Lorg/bukkit/OfflinePlayer;#METHOD"); }
  @Override public org.bukkit.OfflinePlayer getOfflinePlayer(java.util.UUID p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getOfflinePlayer(Ljava/util/UUID;)Lorg/bukkit/OfflinePlayer;#METHOD"); }
  @Override public org.bukkit.OfflinePlayer getOfflinePlayerIfCached(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getOfflinePlayerIfCached(Ljava/lang/String;)Lorg/bukkit/OfflinePlayer;#METHOD"); }
  @Override public org.bukkit.OfflinePlayer[] getOfflinePlayers() { return new org.bukkit.OfflinePlayer[0]; }
  @Override public boolean getOnlineMode() { return PlayerProfileAdapter.onlineMode(); }
  @Override public java.util.Collection<? extends org.bukkit.entity.Player> getOnlinePlayers() {
    // Paper plugins query this during onLoad, before the world bridge is ready.
    // Bukkit exposes an empty collection at that point rather than failing the plugin.
    try { return context.players(); }
    catch (IllegalStateException unavailable) {
      if (String.valueOf(unavailable.getMessage()).startsWith("PHASE_UNAVAILABLE:")) return java.util.List.of();
      throw unavailable;
    }
  }
  @Override public java.util.Set<org.bukkit.OfflinePlayer> getOperators() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getOperators()Ljava/util/Set;#METHOD"); }
  @Override public java.lang.String getPermissionMessage() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getPermissionMessage()Ljava/lang/String;#METHOD"); }
  @Override public org.bukkit.entity.Player getPlayer(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getPlayer(Ljava/lang/String;)Lorg/bukkit/entity/Player;#METHOD"); }
  @Override public org.bukkit.entity.Player getPlayer(java.util.UUID p0) { return context.playerByUuid(p0); }
  @Override public org.bukkit.entity.Player getPlayerExact(java.lang.String p0) { return context.playerExact(p0); }
  @Override public java.util.UUID getPlayerUniqueId(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getPlayerUniqueId(Ljava/lang/String;)Ljava/util/UUID;#METHOD"); }
  @Override public org.bukkit.command.PluginCommand getPluginCommand(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getPluginCommand(Ljava/lang/String;)Lorg/bukkit/command/PluginCommand;#METHOD"); }
  @Override public org.bukkit.plugin.PluginManager getPluginManager() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getPluginManager()Lorg/bukkit/plugin/PluginManager;#METHOD"); }
  @Override public java.io.File getPluginsFolder() { return context.info().pluginsDirectory().toFile(); }
  @Override public int getPort() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getPort()I#METHOD"); }
  @Override public org.bukkit.potion.PotionBrewer getPotionBrewer() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getPotionBrewer()Lorg/bukkit/potion/PotionBrewer;#METHOD"); }
  @Override public org.bukkit.inventory.Recipe getRecipe(org.bukkit.NamespacedKey p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getRecipe(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/inventory/Recipe;#METHOD"); }
  @Override public java.util.List<org.bukkit.inventory.Recipe> getRecipesFor(org.bukkit.inventory.ItemStack p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getRecipesFor(Lorg/bukkit/inventory/ItemStack;)Ljava/util/List;#METHOD"); }
  @Override public io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getRegionScheduler()Lio/papermc/paper/threadedregions/scheduler/RegionScheduler;#METHOD"); }
  @Override public <T extends org.bukkit.Keyed> org.bukkit.Registry<T> getRegistry(java.lang.Class<T> p0) {
    return dev.meldrite.paper.registry.RegistryProjection.get(p0);
  }
  @Override public java.lang.String getResourcePack() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getResourcePack()Ljava/lang/String;#METHOD"); }
  @Override public java.lang.String getResourcePackHash() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getResourcePackHash()Ljava/lang/String;#METHOD"); }
  @Override public java.lang.String getResourcePackPrompt() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getResourcePackPrompt()Ljava/lang/String;#METHOD"); }
  @Override public org.bukkit.scheduler.BukkitScheduler getScheduler() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getScheduler()Lorg/bukkit/scheduler/BukkitScheduler;#METHOD"); }
  @Override public org.bukkit.scoreboard.Criteria getScoreboardCriteria(java.lang.String p0) { return dev.meldrite.paper.registry.ScoreboardProjection.criteria(p0); }
  @Override public org.bukkit.scoreboard.ScoreboardManager getScoreboardManager() { return dev.meldrite.paper.registry.ScoreboardProjection.manager(); }
  @Override public org.bukkit.util.CachedServerIcon getServerIcon() {
    java.io.File file = context.info().pluginsDirectory().getParent().resolve("server-icon.png").toFile();
    if (!file.isFile()) return new org.bukkit.craftbukkit.v1_20_R1.util.CraftIconCache(null);
    try { return loadServerIcon(file); }
    catch (Exception failure) { throw new IllegalStateException("SERVER_ICON_LOAD_FAILED", failure); }
  }
  @Override public org.bukkit.plugin.ServicesManager getServicesManager() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getServicesManager()Lorg/bukkit/plugin/ServicesManager;#METHOD"); }
  @Override public java.lang.String getShutdownMessage() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getShutdownMessage()Ljava/lang/String;#METHOD"); }
  @Override public int getSimulationDistance() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getSimulationDistance()I#METHOD"); }
  @Override public int getSpawnLimit(org.bukkit.entity.SpawnCategory p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getSpawnLimit(Lorg/bukkit/entity/SpawnCategory;)I#METHOD"); }
  @Override public int getSpawnRadius() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getSpawnRadius()I#METHOD"); }
  @Override public org.bukkit.structure.StructureManager getStructureManager() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getStructureManager()Lorg/bukkit/structure/StructureManager;#METHOD"); }
  @Override public double[] getTPS() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTPS()[D#METHOD"); }
  @Override public <T extends org.bukkit.Keyed> org.bukkit.Tag<T> getTag(java.lang.String p0, org.bukkit.NamespacedKey p1, java.lang.Class<T> p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTag(Ljava/lang/String;Lorg/bukkit/NamespacedKey;Ljava/lang/Class;)Lorg/bukkit/Tag;#METHOD"); }
  @Override public <T extends org.bukkit.Keyed> java.lang.Iterable<org.bukkit.Tag<T>> getTags(java.lang.String p0, java.lang.Class<T> p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTags(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Iterable;#METHOD"); }
  @Override public long[] getTickTimes() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTickTimes()[J#METHOD"); }
  @Override public int getTicksPerAmbientSpawns() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTicksPerAmbientSpawns()I#METHOD"); }
  @Override public int getTicksPerAnimalSpawns() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTicksPerAnimalSpawns()I#METHOD"); }
  @Override public int getTicksPerMonsterSpawns() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTicksPerMonsterSpawns()I#METHOD"); }
  @Override public int getTicksPerSpawns(org.bukkit.entity.SpawnCategory p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTicksPerSpawns(Lorg/bukkit/entity/SpawnCategory;)I#METHOD"); }
  @Override public int getTicksPerWaterAmbientSpawns() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTicksPerWaterAmbientSpawns()I#METHOD"); }
  @Override public int getTicksPerWaterSpawns() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTicksPerWaterSpawns()I#METHOD"); }
  @Override public int getTicksPerWaterUndergroundCreatureSpawns() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getTicksPerWaterUndergroundCreatureSpawns()I#METHOD"); }
  @Override public org.bukkit.UnsafeValues getUnsafe() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getUnsafe()Lorg/bukkit/UnsafeValues;#METHOD"); }
  @Override public java.lang.String getUpdateFolder() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getUpdateFolder()Ljava/lang/String;#METHOD"); }
  @Override public java.io.File getUpdateFolderFile() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getUpdateFolderFile()Ljava/io/File;#METHOD"); }
  @Override public java.lang.String getVersion() { return context.info().version(); }
  @Override public int getViewDistance() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getViewDistance()I#METHOD"); }
  @Override public org.bukkit.Warning.WarningState getWarningState() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getWarningState()Lorg/bukkit/Warning$WarningState;#METHOD"); }
  @Override public int getWaterAmbientSpawnLimit() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getWaterAmbientSpawnLimit()I#METHOD"); }
  @Override public int getWaterAnimalSpawnLimit() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getWaterAnimalSpawnLimit()I#METHOD"); }
  @Override public int getWaterUndergroundCreatureSpawnLimit() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getWaterUndergroundCreatureSpawnLimit()I#METHOD"); }
  @Override public java.util.Set<org.bukkit.OfflinePlayer> getWhitelistedPlayers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getWhitelistedPlayers()Ljava/util/Set;#METHOD"); }
  @Override public org.bukkit.World getWorld(java.lang.String p0) { return context.worldByName(p0); }
  @Override public org.bukkit.World getWorld(java.util.UUID p0) { return context.worldByUid(p0); }
  @Override public org.bukkit.World getWorld(org.bukkit.NamespacedKey p0) { return context.worldByKey(p0); }
  @Override public java.io.File getWorldContainer() { return context.info().pluginsDirectory().getParent().toFile(); }
  @Override public java.lang.String getWorldType() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#getWorldType()Ljava/lang/String;#METHOD"); }
  @Override public java.util.List<org.bukkit.World> getWorlds() { return context.worlds(); }
  @Override public boolean hasWhitelist() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#hasWhitelist()Z#METHOD"); }
  @Override public boolean isEnforcingSecureProfiles() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isEnforcingSecureProfiles()Z#METHOD"); }
  @Override public boolean isHardcore() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isHardcore()Z#METHOD"); }
  @Override public boolean isOwnedByCurrentRegion(org.bukkit.Location p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isOwnedByCurrentRegion(Lorg/bukkit/Location;)Z#METHOD"); }
  @Override public boolean isOwnedByCurrentRegion(org.bukkit.Location p0, int p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isOwnedByCurrentRegion(Lorg/bukkit/Location;I)Z#METHOD"); }
  @Override public boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isOwnedByCurrentRegion(Lorg/bukkit/World;II)Z#METHOD"); }
  @Override public boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2, int p3) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isOwnedByCurrentRegion(Lorg/bukkit/World;III)Z#METHOD"); }
  @Override public boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isOwnedByCurrentRegion(Lorg/bukkit/World;Lio/papermc/paper/math/Position;)Z#METHOD"); }
  @Override public boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1, int p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isOwnedByCurrentRegion(Lorg/bukkit/World;Lio/papermc/paper/math/Position;I)Z#METHOD"); }
  @Override public boolean isOwnedByCurrentRegion(org.bukkit.entity.Entity p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isOwnedByCurrentRegion(Lorg/bukkit/entity/Entity;)Z#METHOD"); }
  @Override public boolean isPrimaryThread() { return context.primaryThread(); }
  @Override public boolean isResourcePackRequired() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isResourcePackRequired()Z#METHOD"); }
  @Override public boolean isStopping() { return stopping; }
  public final void markStopping() { stopping = true; }
  @Override public boolean isTickingWorlds() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isTickingWorlds()Z#METHOD"); }
  @Override public boolean isWhitelistEnforced() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#isWhitelistEnforced()Z#METHOD"); }
  @Override public org.bukkit.util.CachedServerIcon loadServerIcon(java.awt.image.BufferedImage p0) throws java.lang.IllegalArgumentException, java.lang.Exception { return org.bukkit.craftbukkit.v1_20_R1.util.CraftIconCache.load(p0); }
  @Override public org.bukkit.util.CachedServerIcon loadServerIcon(java.io.File p0) throws java.lang.IllegalArgumentException, java.lang.Exception { return org.bukkit.craftbukkit.v1_20_R1.util.CraftIconCache.load(p0); }
  @Override public java.util.List<org.bukkit.entity.Player> matchPlayer(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#matchPlayer(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public net.kyori.adventure.text.Component motd() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#motd()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public void motd(net.kyori.adventure.text.Component p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#motd(Lnet/kyori/adventure/text/Component;)V#METHOD"); }
  @Override public net.kyori.adventure.text.Component permissionMessage() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#permissionMessage()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public java.util.Iterator<org.bukkit.inventory.Recipe> recipeIterator() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#recipeIterator()Ljava/util/Iterator;#METHOD"); }
  @Override public void reload() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#reload()V#METHOD"); }
  @Override public boolean reloadCommandAliases() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#reloadCommandAliases()Z#METHOD"); }
  @Override public void reloadData() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#reloadData()V#METHOD"); }
  @Override public void reloadPermissions() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#reloadPermissions()V#METHOD"); }
  @Override public void reloadWhitelist() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#reloadWhitelist()V#METHOD"); }
  @Override public boolean removeBossBar(org.bukkit.NamespacedKey p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#removeBossBar(Lorg/bukkit/NamespacedKey;)Z#METHOD"); }
  @Override public boolean removeRecipe(org.bukkit.NamespacedKey p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#removeRecipe(Lorg/bukkit/NamespacedKey;)Z#METHOD"); }
  @Override public boolean removeRecipe(org.bukkit.NamespacedKey p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#removeRecipe(Lorg/bukkit/NamespacedKey;Z)Z#METHOD"); }
  @Override public void resetRecipes() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#resetRecipes()V#METHOD"); }
  @Override public void savePlayers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#savePlayers()V#METHOD"); }
  @Override public java.util.List<org.bukkit.entity.Entity> selectEntities(org.bukkit.command.CommandSender p0, java.lang.String p1) throws java.lang.IllegalArgumentException { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#selectEntities(Lorg/bukkit/command/CommandSender;Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public void sendPluginMessage(org.bukkit.plugin.Plugin plugin, java.lang.String channel, byte[] message) {
    StandardMessenger.validatePluginMessage(getMessenger(), plugin, channel, message);
    for (var player : getOnlinePlayers()) player.sendPluginMessage(plugin, channel, message);
  }
  @Override public void setDefaultGameMode(org.bukkit.GameMode p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#setDefaultGameMode(Lorg/bukkit/GameMode;)V#METHOD"); }
  @Override public void setIdleTimeout(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#setIdleTimeout(I)V#METHOD"); }
  @Override public void setMaxPlayers(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#setMaxPlayers(I)V#METHOD"); }
  @Override public void setMotd(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#setMotd(Ljava/lang/String;)V#METHOD"); }
  @Override public void setSpawnRadius(int p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#setSpawnRadius(I)V#METHOD"); }
  @Override public void setWhitelist(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#setWhitelist(Z)V#METHOD"); }
  @Override public void setWhitelistEnforced(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#setWhitelistEnforced(Z)V#METHOD"); }
  @Override public boolean shouldSendChatPreviews() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#shouldSendChatPreviews()Z#METHOD"); }
  @Override public void shutdown() { stopping = true; }
  @Override public net.kyori.adventure.text.Component shutdownMessage() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#shutdownMessage()Lnet/kyori/adventure/text/Component;#METHOD"); }
  @Override public org.bukkit.Server.Spigot spigot() { return spigot; }
  @Override public boolean suggestPlayerNamesWhenNullTabCompletions() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#suggestPlayerNamesWhenNullTabCompletions()Z#METHOD"); }
  @Override public void unbanIP(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#unbanIP(Ljava/lang/String;)V#METHOD"); }
  @Override public void unbanIP(java.net.InetAddress p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#unbanIP(Ljava/net/InetAddress;)V#METHOD"); }
  @Override public boolean unloadWorld(java.lang.String p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#unloadWorld(Ljava/lang/String;Z)Z#METHOD"); }
  @Override public boolean unloadWorld(org.bukkit.World p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#unloadWorld(Lorg/bukkit/World;Z)Z#METHOD"); }
  @Override public void updateRecipes() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#updateRecipes()V#METHOD"); }
  @Override public void updateResources() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/Server#updateResources()V#METHOD"); }
}
