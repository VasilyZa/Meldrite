# Meldrite 调研证据、适用范围与决策来源

日期：2026-10-05。调研用于约束全新设计，不作为移植现有混合端架构的依据。三个调研子代理均使用 GPT-5.6-luna。主代理复核了关键Paper历史源码、固定commit和API支撑类握手。

[整体架构](2026-10-05-meldrite-architecture.md)给出Meldrite自己的设计决策；本文件区分上游事实、推论以及本轮未验证的运行效果。本文未构建这些上游项目，也没有运行插件兼容测试。

## 1. 固定来源

| 来源 | 本轮参考版本 | 使用范围 |
|---|---|---|
| Paper-archive 1.20.1 | `773dd724469bae89d0c2075edc3d1ddc8d5b0b18` | 历史API、Java要求、Paper插件与Craft包 |
| Paper-archive 1.21.1 | `d348cb88a9fe8d19e46102c8b9febe18f746d46b` | 第二profile、Mojang runtime与remap |
| Fabric Loader | `0.14.22`历史源码；当前官方文档辅助解释 | 对1.20.1时代Knot/namespace机制的核实；不是最终选型版本 |
| Arclight | `4e94eb7677e6ae86c838c1616151456ad6293ab9`，Trials/1.0.6 | 1.20.1 Forge版的结构性经验，不能当Fabric实现 |
| Cardboard | `854ad26eb4bcc5d64e0ce90ef4e3bbae73ea393e`，ver/1.20 | 历史1.20.1 Fabric混合端参考 |
| Banner | AutoArchives镜像 `611ce6d112de8d13f4f2341aefc06d8de17d4f58` | 补充历史结构证据，非当前官方维护状态证明 |
| Taiyitist | 当前工作区 `2b3e28ebf24aaf31d1133729372edffba62fc695` | 仅环境/可选对照；没有继承其模块或实现方案 |

两个Paper SHA由官方GitHub commit API读取。源码SHA不是API二进制SHA；实现F1仍须解析确切artifact和传递依赖。网页和分支会变化，实施时使用固定commit及本地构建锁。

## 2. Paper合同与加载机制

### E1：Java和API版本必须分开锁定

- [1.20.1 README](https://github.com/PaperMC/Paper-archive/blob/773dd724469bae89d0c2075edc3d1ddc8d5b0b18/README.md)使用JDK17。
- [1.21.1 README](https://github.com/PaperMC/Paper-archive/blob/d348cb88a9fe8d19e46102c8b9febe18f746d46b/README.md)使用JDK21。
- [1.20.1 API](https://jd.papermc.io/paper/1.20.1/)与[1.21.1 API](https://jd.papermc.io/paper/1.21.1/)是两套版本化文档；API稳定承诺并不覆盖任意实现类依赖、继承实现或事件构造。

**设计推论：** 公共源码用Java17，两个profile独立ABI；服务端自身必须处理插件通常无需处理的事件构造与接口实现差异。不能把“多数插件可跨版本”推导为“同一套Paper实现类能无修改实现两个profile”。

### E2：两个历史版本均有Paper Plugins，但按各版源码定义语义

- [1.20.1 Paper Plugins API patch](https://github.com/PaperMC/Paper-archive/blob/773dd724469bae89d0c2075edc3d1ddc8d5b0b18/patches/api/0008-Paper-Plugins.patch)
- [1.21.1 Paper Plugins API patch](https://github.com/PaperMC/Paper-archive/blob/d348cb88a9fe8d19e46102c8b9febe18f746d46b/patches/api/0009-Paper-Plugins.patch)
- [1.21.1 Paper Plugins server patch](https://github.com/PaperMC/Paper-archive/blob/d348cb88a9fe8d19e46102c8b9febe18f746d46b/patches/server/0019-Paper-Plugins.patch)
- [当前Paper Plugins说明](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/)，只用于概念解释，不能据此认定历史版拥有当前所有功能。

**已确认：** bootstrap、loader与server阶段不同；两类依赖图和join-classpath需要独立建模；loader阶段的静态状态不能假定和最终插件共享。legacy与Paper插件的跨插件类查找策略也不能合并成同一个无条件全局搜索。

### E3：JavaPlugin握手是profile支撑ABI

主代理直接读取E2中的1.20.1补丁确认：`JavaPlugin`无参构造器检查`ConfiguredPluginClassLoader`并调用`init(this)`；`getPlugin`/`getProvidingPlugin`同样利用该协议。该补丁中旧`org.bukkit.plugin.java.PluginClassLoader`为`public final`，并标注Internal。

**设计推论：** 使用实现该profile接口的Meldrite loader adapter，对接公共PluginLoaderEngine。不能继承final类；不能假定API artifact中的每个public类型都是稳定插件API。API符号库存需同时标注公共API、内部支撑ABI、第三方公共依赖与不承诺的内部符号。保留原始API支撑类与必要替换必须有唯一FQCN和来源记录。

### E4：Paper映射规则不能照搬成Fabric映射规则

- [Paper userdev说明](https://docs.papermc.io/paper/dev/userdev/)
- [1.21.1 Plugin remapping patch](https://github.com/PaperMC/Paper-archive/blob/d348cb88a9fe8d19e46102c8b9febe18f746d46b/patches/server/0020-Plugin-remapping.patch)
- [1.21.1 reflection remapping patch](https://github.com/PaperMC/Paper-archive/blob/d348cb88a9fe8d19e46102c8b9febe18f746d46b/patches/server/0022-Remap-reflection-calls-in-plugins-using-internals.patch)

**已确认：** Paper自1.20.5起使用Mojang-mapped runtime；manifest与descriptor种类影响其输入映射缺省值。1.21.1有反射调用改写，不代表任何动态反射都可重映射。

**设计推论：** Meldrite的目标namespace由Fabric实际运行时决定；同样的Mojang插件在Paper可能无需转换，在intermediary Fabric仍需转换内部引用。仅API插件采用字节恒等路径是Meldrite自己的架构决策，不宣称上游Paper内部一定做相同优化。

### E5：Craft包与getHandle属于单独ABI

- [1.20.1 CraftBukkit relocation构建patch](https://github.com/PaperMC/Paper-archive/blob/773dd724469bae89d0c2075edc3d1ddc8d5b0b18/patches/server/0001-Setup-Gradle-project.patch)
- 1.21.1的无版本Mojang环境与legacy版本包兼容见E4历史patch。

**已确认：** 1.20.1常见Craft路径为`org.bukkit.craftbukkit.v1_20_R1`；1.21.1 Paper Mojang环境使用无版本Craft路径，legacy输入还涉及`v1_21_R1`。具体继承和成员不能只凭包名推定。

**设计推论：** Craft公共delegate复用；实际FQCN、继承关系、getHandle返回描述符和alias在版本bindings。给方法一个Object返回值不满足原NMS返回类型的二进制调用描述符。

### E6：Adventure是Paper API的一部分

- [Adventure原生平台支持](https://docs.papermc.io/adventure/platform/native/)
- [CommandSender 1.20.1](https://jd.papermc.io/paper/1.20.1/org/bukkit/command/CommandSender.html)
- [CommandSender 1.21.1](https://jd.papermc.io/paper/1.21.1/org/bukkit/command/CommandSender.html)

**已确认：** 两目标版本属于Paper原生Adventure平台，CommandSender具有Audience关系。

**设计推论：** 服务器级单一Adventure类身份与统一消息服务是必要边界；它不要求安装某个特定Fabric Adventure平台实现。是否复用其源码/库要在V1/V3验证加载与许可，不能未经验证与已有provider叠加。

## 3. Fabric、Mixin与游戏数据

### E7：Knot与运行namespace

- [Fabric Loader官方文档](https://docs.fabricmc.net/develop/loader/)
- [Loader0.14.22 KnotClassLoader](https://github.com/FabricMC/fabric-loader/blob/0.14.22/src/main/java/net/fabricmc/loader/impl/launch/knot/KnotClassLoader.java)
- [Loader0.14.22 MappingConfiguration](https://github.com/FabricMC/fabric-loader/blob/0.14.22/src/main/java/net/fabricmc/loader/impl/launch/MappingConfiguration.java)
- [Fabric mappings说明](https://docs.fabricmc.net/develop/porting/mappings/)

**已确认：** 对本设计两个历史目标的常用Loom工作流，开发named与发行intermediary需区分；最终运行namespace仍应读取并验证。Knot的委托结构不能推导出“每个Mod独立ClassLoader”。当前映射文档可能谈到更新版本策略，不能向所有未来Minecraft版本泛化。

### E8：Mixin配置必须早于目标类定义

- [Mixin Environment](https://github.com/SpongePowered/Mixin/wiki/Introduction-to-Mixins---The-Mixin-Environment)
- [fabric.mod.json规范](https://docs.fabricmc.net/develop/loader/fabric-mod-json)
- [Access widening文档](https://docs.fabricmc.net/develop/class-tweakers/access-widening)

**设计推论：** 游戏transform归Fabric启动阶段；普通插件加载时不能临时增加Minecraft Mixin。关键注入失败必须停止启动，可选能力需公开禁用；不能把部分转换失败类视作正常类继续运行。本文不依赖“access widener只能用于原版、绝不能触及任何Mod类”这一过强断言。

### E9：Java21与物品Data Components是实质版本差异

- [Minecraft 1.20.5发布说明](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-20-5)
- [Fabric 1.20.5开发变更](https://fabricmc.net/2024/04/19/1205.html)
- [Fabric Data Components文档](https://docs.fabricmc.net/develop/items/custom-data-components)
- [Minecraft 1.21.1发布说明](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-1)

**已确认：** 1.20.5引入Java21要求和Item Stack Components；1.21.1在此基础上演进。因此1.20.1旧NBT表示与1.21.1组件表示不应共享raw native类型。

**设计推论：** 公共层只持有逻辑属性与带codec/version的opaque payload，原生迁移交给版本codec和游戏DataFixer。此处没有证明任意Mod数据都能跨版迁移。

### E10：开放注册表与固定enum存在表达能力差异

- [Fabric动态registry文档](https://docs.fabricmc.net/develop/registries/dynamic-registry)
- [Paper1.20.1 Material](https://jd.papermc.io/paper/1.20.1/org/bukkit/Material.html)
- [Paper1.20.1 EntityType](https://jd.papermc.io/paper/1.20.1/org/bukkit/entity/EntityType.html)
- [Paper1.21.1 Registry](https://jd.papermc.io/paper/1.21.1/org/bukkit/Registry.html)

**已确认：** namespace registry与固定Material等enum不是等价数据结构。动态registry、静态registry、tags/recipes等reloadable data也并非同一种生命周期。

**设计推论：** scope/epoch与部分投影、不可表示数据拒绝写入、无损opaque载荷是Meldrite策略。不能由“存在datapack reload”推断所有registry每次都重建；实际分类由每个version bridge验证。

## 4. 混合端参考：借鉴机制，不继承耦合

### E11：Arclight

- [固定版本settings.gradle](https://github.com/IzzelAliz/Arclight/blob/4e94eb7677e6ae86c838c1616151456ad6293ab9/settings.gradle)
- [Bukkit Mixin清单](https://github.com/IzzelAliz/Arclight/blob/4e94eb7677e6ae86c838c1616151456ad6293ab9/arclight-common/src/main/resources/mixins.arclight.bukkit.json)
- [ClassLoaderRemapper](https://github.com/IzzelAliz/Arclight/blob/4e94eb7677e6ae86c838c1616151456ad6293ab9/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ClassLoaderRemapper.java)
- [Enum扩展机制](https://github.com/IzzelAliz/Arclight/blob/4e94eb7677e6ae86c838c1616151456ad6293ab9/arclight-common/src/main/java/io/izzel/arclight/common/mod/util/remapper/ArclightEnumExtender.java)

**观察：** 有显式Bridge、多模块、包含继承/反射处理的remapper，以及enum扩展技术。研究版本是Forge1.20.1，不能据此宣称已验证Fabric方案。

**采用：** 显式bridge、映射继承图、对真实兼容难题建立fixture。**不采用：** 将插件加载、Craft业务与广泛Mixin补丁合并成Meldrite核心；动态enum不作为默认基线。这是Meldrite约束下的取舍，不是对参考项目所有版本的质量评价。

### E12：Cardboard

- [固定版本fabric.mod.json](https://github.com/CardboardPowered/cardboard/blob/854ad26eb4bcc5d64e0ce90ef4e3bbae73ea393e/src/main/resources/fabric.mod.json)
- [固定版本build.gradle](https://github.com/CardboardPowered/cardboard/blob/854ad26eb4bcc5d64e0ce90ef4e3bbae73ea393e/build.gradle)
- [固定版本README](https://github.com/CardboardPowered/cardboard/blob/854ad26eb4bcc5d64e0ce90ef4e3bbae73ea393e/README.md)

**观察：** 展示Fabric mod内接入Bukkit的实践，历史文档说明NMS remap仍有限制。研究分支当时的支持说明不能当作2026年当前维护承诺。

**采用：** Fabric mod入口路径和对自动NMS兼容局限的认识。**不采用：** 把“存在自动remap”作为普通插件/NMS/Paper internals全面兼容的证明。

### E13：Banner补充档案

- [镜像构建文件](https://github.com/AutoArchives/mohistmc-banner/blob/611ce6d112de8d13f4f2341aefc06d8de17d4f58/build.gradle)
- [BannerMixinPlugin](https://github.com/AutoArchives/mohistmc-banner/blob/611ce6d112de8d13f4f2341aefc06d8de17d4f58/modules/banner-server/src/main/java/com/mohistmc/banner/mixin/BannerMixinPlugin.java)
- [MixinMinecraftServer](https://github.com/AutoArchives/mohistmc-banner/blob/611ce6d112de8d13f4f2341aefc06d8de17d4f58/modules/banner-server/src/main/java/com/mohistmc/banner/mixin/server/MixinMinecraftServer.java)

**范围限制：** 本轮原MohistMC/Banner URL不可取得；镜像仅作源码结构补充，不作为官方当前支持、安全性或维护状态的证明。

**观察与推论：** 多模块launcher、Mixin后处理、按Mod处理冲突等机制提示Meldrite需要把启动时全局变换与插件局部兼容pack分开。不要把整个运行时封入“旧版兼容胶囊”，否则升级时仍会复制Runtime，违背用户目标。

## 5. 许可和验证边界

上游源码/patch复用需要逐文件来源和许可检查；本轮只研究结构，没有把上游业务源码复制进仓库，也没有决定Meldrite最终许可证。F1/Q2将来源清单与构件分发检查作为实际工程工作。具体许可义务由对应文件许可和发布方式决定，不能以一个根LICENSE概括所有依赖和Minecraft工件。

本轮核实的是历史源码/官方文档中的机制。以下均是实施阶段需要实验验证的设计假设：所有所选工具版本能共同构建、全部关键hook在目标Mod集合中成功、普通插件真实行为等价、opaque保存对特定Mod codec无损、1.21.1升级无需新增某种端口。计划已为这些假设安排失败测试和明确验收；没有将其写成既成事实。
