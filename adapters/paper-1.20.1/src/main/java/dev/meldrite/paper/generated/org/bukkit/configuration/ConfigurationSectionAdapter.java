package dev.meldrite.paper.generated.org.bukkit.configuration;
public final class ConfigurationSectionAdapter implements org.bukkit.configuration.ConfigurationSection {
private final dev.meldrite.paper.configuration.ConfigurationView view;
public ConfigurationSectionAdapter(dev.meldrite.paper.configuration.ConfigurationView view){this.view=java.util.Objects.requireNonNull(view);}
  @Override public void addDefault(java.lang.String p0, java.lang.Object p1) { view.addDefault(p0,p1); }
  @Override public boolean contains(java.lang.String p0) { return view.contains(p0); }
  @Override public boolean contains(java.lang.String p0, boolean p1) { return view.contains(p0,p1); }
  @Override public org.bukkit.configuration.ConfigurationSection createSection(java.lang.String p0) { return view.createSection(p0); }
  @Override public org.bukkit.configuration.ConfigurationSection createSection(java.lang.String p0, java.util.Map<?, ?> p1) { return view.createSection(p0,p1); }
  @Override public java.lang.Object get(java.lang.String p0) { return view.get(p0); }
  @Override public java.lang.Object get(java.lang.String p0, java.lang.Object p1) { return view.get(p0,p1); }
  @Override public boolean getBoolean(java.lang.String p0) { return view.getBoolean(p0); }
  @Override public boolean getBoolean(java.lang.String p0, boolean p1) { return view.getBoolean(p0,p1); }
  @Override public java.util.List<java.lang.Boolean> getBooleanList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getBooleanList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.util.List<java.lang.Byte> getByteList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getByteList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.util.List<java.lang.Character> getCharacterList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getCharacterList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public org.bukkit.Color getColor(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getColor(Ljava/lang/String;)Lorg/bukkit/Color;#METHOD"); }
  @Override public org.bukkit.Color getColor(java.lang.String p0, org.bukkit.Color p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getColor(Ljava/lang/String;Lorg/bukkit/Color;)Lorg/bukkit/Color;#METHOD"); }
  @Override public java.util.List<java.lang.String> getComments(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getComments(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public org.bukkit.configuration.ConfigurationSection getConfigurationSection(java.lang.String p0) { return view.getConfigurationSection(p0); }
  @Override public java.lang.String getCurrentPath() { return view.getCurrentPath(); }
  @Override public org.bukkit.configuration.ConfigurationSection getDefaultSection() { return view.getDefaultSection(); }
  @Override public double getDouble(java.lang.String p0) { return view.getDouble(p0); }
  @Override public double getDouble(java.lang.String p0, double p1) { return view.getDouble(p0,p1); }
  @Override public java.util.List<java.lang.Double> getDoubleList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getDoubleList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.util.List<java.lang.Float> getFloatList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getFloatList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.util.List<java.lang.String> getInlineComments(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getInlineComments(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public int getInt(java.lang.String p0) { return view.getInt(p0); }
  @Override public int getInt(java.lang.String p0, int p1) { return view.getInt(p0,p1); }
  @Override public java.util.List<java.lang.Integer> getIntegerList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getIntegerList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public org.bukkit.inventory.ItemStack getItemStack(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getItemStack(Ljava/lang/String;)Lorg/bukkit/inventory/ItemStack;#METHOD"); }
  @Override public org.bukkit.inventory.ItemStack getItemStack(java.lang.String p0, org.bukkit.inventory.ItemStack p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getItemStack(Ljava/lang/String;Lorg/bukkit/inventory/ItemStack;)Lorg/bukkit/inventory/ItemStack;#METHOD"); }
  @Override public java.util.Set<java.lang.String> getKeys(boolean p0) { return view.getKeys(p0); }
  @Override public java.util.List<?> getList(java.lang.String p0) { return view.getList(p0); }
  @Override public java.util.List<?> getList(java.lang.String p0, java.util.List<?> p1) { return view.getList(p0,p1); }
  @Override public org.bukkit.Location getLocation(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getLocation(Ljava/lang/String;)Lorg/bukkit/Location;#METHOD"); }
  @Override public org.bukkit.Location getLocation(java.lang.String p0, org.bukkit.Location p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getLocation(Ljava/lang/String;Lorg/bukkit/Location;)Lorg/bukkit/Location;#METHOD"); }
  @Override public long getLong(java.lang.String p0) { return view.getLong(p0); }
  @Override public long getLong(java.lang.String p0, long p1) { return view.getLong(p0,p1); }
  @Override public java.util.List<java.lang.Long> getLongList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getLongList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.util.List<java.util.Map<?, ?>> getMapList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getMapList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.lang.String getName() { return view.getName(); }
  @Override public <T extends java.lang.Object> T getObject(java.lang.String p0, java.lang.Class<T> p1) { return view.getObject(p0,p1); }
  @Override public <T extends java.lang.Object> T getObject(java.lang.String p0, java.lang.Class<T> p1, T p2) { return view.getObject(p0,p1,p2); }
  @Override public org.bukkit.OfflinePlayer getOfflinePlayer(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getOfflinePlayer(Ljava/lang/String;)Lorg/bukkit/OfflinePlayer;#METHOD"); }
  @Override public org.bukkit.OfflinePlayer getOfflinePlayer(java.lang.String p0, org.bukkit.OfflinePlayer p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getOfflinePlayer(Ljava/lang/String;Lorg/bukkit/OfflinePlayer;)Lorg/bukkit/OfflinePlayer;#METHOD"); }
  @Override public org.bukkit.configuration.ConfigurationSection getParent() { return view.getParent(); }
  @Override public org.bukkit.configuration.Configuration getRoot() { return view.getRoot(); }
  @Override public <T extends org.bukkit.configuration.serialization.ConfigurationSerializable> T getSerializable(java.lang.String p0, java.lang.Class<T> p1) { return view.getSerializable(p0,p1); }
  @Override public <T extends org.bukkit.configuration.serialization.ConfigurationSerializable> T getSerializable(java.lang.String p0, java.lang.Class<T> p1, T p2) { return view.getSerializable(p0,p1,p2); }
  @Override public java.util.List<java.lang.Short> getShortList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getShortList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.lang.String getString(java.lang.String p0) { return view.getString(p0); }
  @Override public java.lang.String getString(java.lang.String p0, java.lang.String p1) { return view.getString(p0,p1); }
  @Override public java.util.List<java.lang.String> getStringList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getStringList(Ljava/lang/String;)Ljava/util/List;#METHOD"); }
  @Override public java.util.Map<java.lang.String, java.lang.Object> getValues(boolean p0) { return view.getValues(p0); }
  @Override public org.bukkit.util.Vector getVector(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getVector(Ljava/lang/String;)Lorg/bukkit/util/Vector;#METHOD"); }
  @Override public org.bukkit.util.Vector getVector(java.lang.String p0, org.bukkit.util.Vector p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#getVector(Ljava/lang/String;Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;#METHOD"); }
  @Override public boolean isBoolean(java.lang.String p0) { return view.isBoolean(p0); }
  @Override public boolean isColor(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#isColor(Ljava/lang/String;)Z#METHOD"); }
  @Override public boolean isConfigurationSection(java.lang.String p0) { return view.isConfigurationSection(p0); }
  @Override public boolean isDouble(java.lang.String p0) { return view.isDouble(p0); }
  @Override public boolean isInt(java.lang.String p0) { return view.isInt(p0); }
  @Override public boolean isItemStack(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#isItemStack(Ljava/lang/String;)Z#METHOD"); }
  @Override public boolean isList(java.lang.String p0) { return view.isList(p0); }
  @Override public boolean isLocation(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#isLocation(Ljava/lang/String;)Z#METHOD"); }
  @Override public boolean isLong(java.lang.String p0) { return view.isLong(p0); }
  @Override public boolean isOfflinePlayer(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#isOfflinePlayer(Ljava/lang/String;)Z#METHOD"); }
  @Override public boolean isSet(java.lang.String p0) { return view.isSet(p0); }
  @Override public boolean isString(java.lang.String p0) { return view.isString(p0); }
  @Override public boolean isVector(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#isVector(Ljava/lang/String;)Z#METHOD"); }
  @Override public void set(java.lang.String p0, java.lang.Object p1) { view.set(p0,p1); }
  @Override public void setComments(java.lang.String p0, java.util.List<java.lang.String> p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#setComments(Ljava/lang/String;Ljava/util/List;)V#METHOD"); }
  @Override public void setInlineComments(java.lang.String p0, java.util.List<java.lang.String> p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/ConfigurationSection#setInlineComments(Ljava/lang/String;Ljava/util/List;)V#METHOD"); }
}
