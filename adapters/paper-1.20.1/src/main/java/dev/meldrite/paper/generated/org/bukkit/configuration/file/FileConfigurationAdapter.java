package dev.meldrite.paper.generated.org.bukkit.configuration.file;
public final class FileConfigurationAdapter extends org.bukkit.configuration.file.FileConfiguration {
private final dev.meldrite.service.ConfigurationService.Document document;
private final dev.meldrite.service.ConfigurationService service;
private final dev.meldrite.paper.configuration.ConfigurationView view;
private final org.bukkit.configuration.file.FileConfigurationOptions options;
private static final class Options extends org.bukkit.configuration.file.FileConfigurationOptions {Options(org.bukkit.configuration.file.FileConfiguration root){super(root);}}
public FileConfigurationAdapter(dev.meldrite.service.ConfigurationService.Document document,dev.meldrite.service.ConfigurationService service){this.document=java.util.Objects.requireNonNull(document);this.service=java.util.Objects.requireNonNull(service);view=new dev.meldrite.paper.configuration.ConfigurationView(document,this,java.util.List.of(),false);options=new Options(this);}
@Override public String saveToString(){return dev.meldrite.paper.configuration.ConfigurationYaml.save(document);}
 @Override public void loadFromString(String contents)throws org.bukkit.configuration.InvalidConfigurationException{dev.meldrite.paper.configuration.ConfigurationYaml.load(document,contents,options.pathSeparator());}
@Override public void save(String file)throws java.io.IOException {service.save(file,saveToString().getBytes(java.nio.charset.StandardCharsets.UTF_8));}
@Override public void save(java.io.File file)throws java.io.IOException {save(service.relativeFile(file.toPath()));}
@Override public void load(String file)throws java.io.IOException,org.bukkit.configuration.InvalidConfigurationException {loadFromString(java.nio.file.Files.readString(service.resolveFile(file)));}
@Override public void load(java.io.File file)throws java.io.IOException,org.bukkit.configuration.InvalidConfigurationException {load(service.relativeFile(file.toPath()));}
  @Override public void addDefault(java.lang.String p0, java.lang.Object p1) { view.addDefault(p0,p1); }
  @Override public void addDefaults(java.util.Map<java.lang.String, java.lang.Object> p0) { view.addDefaults(p0); }
  @Override public void addDefaults(org.bukkit.configuration.Configuration p0) { view.addDefaults(p0); }
  @Override public boolean contains(java.lang.String p0) { return view.contains(p0); }
  @Override public boolean contains(java.lang.String p0, boolean p1) { return view.contains(p0,p1); }
  @Override public org.bukkit.configuration.ConfigurationSection createSection(java.lang.String p0) { return view.createSection(p0); }
  @Override public org.bukkit.configuration.ConfigurationSection createSection(java.lang.String p0, java.util.Map<?, ?> p1) { return view.createSection(p0,p1); }
  @Override public java.lang.Object get(java.lang.String p0) { return view.get(p0); }
  @Override public java.lang.Object get(java.lang.String p0, java.lang.Object p1) { return view.get(p0,p1); }
  @Override public boolean getBoolean(java.lang.String p0) { return view.getBoolean(p0); }
  @Override public boolean getBoolean(java.lang.String p0, boolean p1) { return view.getBoolean(p0,p1); }
  @Override public java.util.List<java.lang.Boolean> getBooleanList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getBooleanList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.util.List<java.lang.Byte> getByteList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getByteList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.util.List<java.lang.Character> getCharacterList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getCharacterList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public org.bukkit.Color getColor(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getColor(Ljava/lang/String;)Lorg/bukkit/Color;#INHERITED_METHOD"); }
  @Override public org.bukkit.Color getColor(java.lang.String p0, org.bukkit.Color p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getColor(Ljava/lang/String;Lorg/bukkit/Color;)Lorg/bukkit/Color;#INHERITED_METHOD"); }
  @Override public java.util.List<java.lang.String> getComments(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getComments(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public org.bukkit.configuration.ConfigurationSection getConfigurationSection(java.lang.String p0) { return view.getConfigurationSection(p0); }
  @Override public java.lang.String getCurrentPath() { return view.getCurrentPath(); }
  @Override public org.bukkit.configuration.ConfigurationSection getDefaultSection() { return view.getDefaultSection(); }
  @Override public org.bukkit.configuration.Configuration getDefaults() { return view.getDefaults(); }
  @Override public double getDouble(java.lang.String p0) { return view.getDouble(p0); }
  @Override public double getDouble(java.lang.String p0, double p1) { return view.getDouble(p0,p1); }
  @Override public java.util.List<java.lang.Double> getDoubleList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getDoubleList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.util.List<java.lang.Float> getFloatList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getFloatList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.util.List<java.lang.String> getInlineComments(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getInlineComments(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public int getInt(java.lang.String p0) { return view.getInt(p0); }
  @Override public int getInt(java.lang.String p0, int p1) { return view.getInt(p0,p1); }
  @Override public java.util.List<java.lang.Integer> getIntegerList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getIntegerList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public org.bukkit.inventory.ItemStack getItemStack(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getItemStack(Ljava/lang/String;)Lorg/bukkit/inventory/ItemStack;#INHERITED_METHOD"); }
  @Override public org.bukkit.inventory.ItemStack getItemStack(java.lang.String p0, org.bukkit.inventory.ItemStack p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getItemStack(Ljava/lang/String;Lorg/bukkit/inventory/ItemStack;)Lorg/bukkit/inventory/ItemStack;#INHERITED_METHOD"); }
  @Override public java.util.Set<java.lang.String> getKeys(boolean p0) { return view.getKeys(p0); }
  @Override public java.util.List<?> getList(java.lang.String p0) { return view.getList(p0); }
  @Override public java.util.List<?> getList(java.lang.String p0, java.util.List<?> p1) { return view.getList(p0,p1); }
  @Override public org.bukkit.Location getLocation(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getLocation(Ljava/lang/String;)Lorg/bukkit/Location;#INHERITED_METHOD"); }
  @Override public org.bukkit.Location getLocation(java.lang.String p0, org.bukkit.Location p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getLocation(Ljava/lang/String;Lorg/bukkit/Location;)Lorg/bukkit/Location;#INHERITED_METHOD"); }
  @Override public long getLong(java.lang.String p0) { return view.getLong(p0); }
  @Override public long getLong(java.lang.String p0, long p1) { return view.getLong(p0,p1); }
  @Override public java.util.List<java.lang.Long> getLongList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getLongList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.util.List<java.util.Map<?, ?>> getMapList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getMapList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.lang.String getName() { return view.getName(); }
  @Override public <T extends java.lang.Object> T getObject(java.lang.String p0, java.lang.Class<T> p1) { return view.getObject(p0,p1); }
  @Override public <T extends java.lang.Object> T getObject(java.lang.String p0, java.lang.Class<T> p1, T p2) { return view.getObject(p0,p1,p2); }
  @Override public org.bukkit.OfflinePlayer getOfflinePlayer(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getOfflinePlayer(Ljava/lang/String;)Lorg/bukkit/OfflinePlayer;#INHERITED_METHOD"); }
  @Override public org.bukkit.OfflinePlayer getOfflinePlayer(java.lang.String p0, org.bukkit.OfflinePlayer p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getOfflinePlayer(Ljava/lang/String;Lorg/bukkit/OfflinePlayer;)Lorg/bukkit/OfflinePlayer;#INHERITED_METHOD"); }
  @Override public org.bukkit.configuration.ConfigurationSection getParent() { return view.getParent(); }
  @Override public org.bukkit.configuration.Configuration getRoot() { return view.getRoot(); }
  @Override public <T extends org.bukkit.configuration.serialization.ConfigurationSerializable> T getSerializable(java.lang.String p0, java.lang.Class<T> p1) { return view.getSerializable(p0,p1); }
  @Override public <T extends org.bukkit.configuration.serialization.ConfigurationSerializable> T getSerializable(java.lang.String p0, java.lang.Class<T> p1, T p2) { return view.getSerializable(p0,p1,p2); }
  @Override public java.util.List<java.lang.Short> getShortList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getShortList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.lang.String getString(java.lang.String p0) { return view.getString(p0); }
  @Override public java.lang.String getString(java.lang.String p0, java.lang.String p1) { return view.getString(p0,p1); }
  @Override public java.util.List<java.lang.String> getStringList(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getStringList(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public java.util.Map<java.lang.String, java.lang.Object> getValues(boolean p0) { return view.getValues(p0); }
  @Override public org.bukkit.util.Vector getVector(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getVector(Ljava/lang/String;)Lorg/bukkit/util/Vector;#INHERITED_METHOD"); }
  @Override public org.bukkit.util.Vector getVector(java.lang.String p0, org.bukkit.util.Vector p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#getVector(Ljava/lang/String;Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;#INHERITED_METHOD"); }
  @Override public boolean isBoolean(java.lang.String p0) { return view.isBoolean(p0); }
  @Override public boolean isColor(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#isColor(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean isConfigurationSection(java.lang.String p0) { return view.isConfigurationSection(p0); }
  @Override public boolean isDouble(java.lang.String p0) { return view.isDouble(p0); }
  @Override public boolean isInt(java.lang.String p0) { return view.isInt(p0); }
  @Override public boolean isItemStack(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#isItemStack(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean isList(java.lang.String p0) { return view.isList(p0); }
  @Override public boolean isLocation(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#isLocation(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean isLong(java.lang.String p0) { return view.isLong(p0); }
  @Override public boolean isOfflinePlayer(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#isOfflinePlayer(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean isSet(java.lang.String p0) { return view.isSet(p0); }
  @Override public boolean isString(java.lang.String p0) { return view.isString(p0); }
  @Override public boolean isVector(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#isVector(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public org.bukkit.configuration.file.FileConfigurationOptions options() { return options; }
  @Override public void set(java.lang.String p0, java.lang.Object p1) { view.set(p0,p1); }
  @Override public void setComments(java.lang.String p0, java.util.List<java.lang.String> p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#setComments(Ljava/lang/String;Ljava/util/List;)V#INHERITED_METHOD"); }
  @Override public void setDefaults(org.bukkit.configuration.Configuration p0) { view.setDefaults(p0); }
  @Override public void setInlineComments(java.lang.String p0, java.util.List<java.lang.String> p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/configuration/Configuration#setInlineComments(Ljava/lang/String;Ljava/util/List;)V#INHERITED_METHOD"); }
}
