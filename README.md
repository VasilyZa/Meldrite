# Meldrite

<p align="center">
  <a href="https://github.com/VasilyZa/Meldrite/actions/workflows/runtime.yml"><img src="https://github.com/VasilyZa/Meldrite/actions/workflows/runtime.yml/badge.svg?branch=1.20.1" alt="CI"></a>
  <img src="https://img.shields.io/badge/Minecraft-1.20.1-62a65a?logo=minecraft&logoColor=white" alt="Minecraft 1.20.1">
  <img src="https://img.shields.io/badge/Java-17%2B-f89820?logo=openjdk&logoColor=white" alt="Java 17+">
  <img src="https://img.shields.io/badge/Loader-Fabric-dbd0b4?logo=fabric&logoColor=black" alt="Fabric Loader">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPL--3.0-blue" alt="GPL-3.0"></a>
</p>

## 这个项目是什么

Meldrite 是一个 Minecraft Java 版服务端，在同一个服务端进程里同时加载两类内容

- Fabric Mod，与原版 Fabric 服务端一样放在 `mods/` 目录
- Bukkit/Paper 插件，放在 `plugins/` 目录，由服务端提供的 API 适配层运行

普通 Fabric 服务端不能加载 Bukkit/Paper 插件，普通 Paper 服务端不能加载 Fabric Mod

Meldrite 的目标是让两者在同一个世界里共存，插件与 Mod 共享同一份世界、实体、玩家和注册表

当前开发与验证目标是 Minecraft 1.20.1

仓库里也有 1.21.1 的目录，但只有入口和适配骨架，不能当作可用的 1.21.1 服务端

### 使用方式

1. 下载发行页的 `meldrite-<版本>.jar`，放进一个空目录
2. 用 Java 17 或更高版本启动，发行清单的 `javaFeature` 为 17，启动器低于该版本时直接报错；插件 JAR 含 Java 21 字节码时需要改用 Java 21

   ```bash
   java -Xms2G -Xmx6G -jar meldrite-1.20.1.jar nogui
   ```

3. 首次启动会创建 `mods/`、`plugins/`、`libraries/meldrite/` 和 `eula.txt`，把 `eula.txt` 中的 `eula=false` 改成 `eula=true` 后再次启动
4. Mod 放进 `mods/`，插件放进 `plugins/`，重启服务端
5. `server.properties`、`bukkit.yml`、`spigot.yml`、`paper.yml` 位于服务端根目录，与对应的服务端软件同名同位置

依赖库在首次启动时下载到 `libraries/meldrite/`，之后按 SHA-256 复用，不需要 Gradle 或额外的安装器

### 插件与 Mod 的支持范围

- 插件通过锁定版本的 Paper API 运行，插件描述文件为 `plugin.yml` 或 `paper-plugin.yml`，`api-version` 支持 `1.19` 与 `1.20`
- 插件与 Mod 的权限检查可接入 Fabric Permissions API，安装 Mod 版 LuckPerms 后可用其命令管理 Bukkit 权限节点，服务端未提供 Vault 服务
- Paper API 的符号按 `contracts/profiles/paper-1.20.1` 下的绑定表逐项标注状态，当前有 271 条 `PARTIAL`、51633 条 `UNSUPPORTED`，未实现的调用会抛出带符号信息的 `UnsupportedOperationException`
- 插件启动成功只说明发现、类加载与启用流程完成，插件业务功能与客户端表现需要各自验证

### 给开发者

仓库使用 Gradle 多项目构建，Java 编写

源码分为通用模块、Paper API 适配、Minecraft 原生实现与发行打包四层，构建、契约审计与验证命令见下文

仓库同时包含 Minecraft 1.20.1 与 1.21.1 的目录，两者实现深度不同，见「版本实现」一节

## 构建目标

| 项目 | 值 |
| --- | --- |
| Minecraft | 1.20.1 |
| 模组加载器 | Fabric Loader 0.19.5 |
| Fabric API | 0.92.12+1.20.1 |
| Yarn 映射 | 1.20.1+build.10:v2 |
| Loom | 1.17.21 |
| Gradle Wrapper | 9.8.0 |
| 构建 daemon JDK | 21 |
| 公共字节码目标 | Java 17 |
| 发行清单 javaFeature | 17 |
| Paper API 契约 | 1.20.1-R0.1-20230921.165944-178 |
| Paper 上游提交 | 773dd724469bae89d0c2075edc3d1ddc8d5b0b18 |
| 控制台库 | JLine 3.25.1 |
| 许可证 | GPL-3.0 |

`build-logic` 中的 `meldrite.java` 约定统一设置工具链、`--release 17`、依赖锁定与 JUnit 5

`gradle/gradle-daemon-jvm.properties` 指定 daemon 使用 JDK 21，CI 同时安装 17 与 21 两套工具链

## 仓库结构

| 目录 | 内容 |
| --- | --- |
| `build-logic/` | `meldrite.java`、`meldrite.contract` 两个约定插件 |
| `modules/` | 通用模型、SPI、服务、调度器与插件运行时 |
| `adapters/` | Bukkit/Paper API 适配、门面与插件宿主 |
| `versions/` | Minecraft 原生实现、Mixin、映射资源与版本装配 |
| `distributions/` | 发行清单、依赖安装与可执行服务端启动器 |
| `contracts/` | 依赖锁、API 索引、绑定、能力与发布策略 |
| `build-tools/contracts/` | 契约、架构与发行审计工具，Gradle 项目 `:testkit-contract` |
| `testkit/` | 测试运行支持、集成任务与 fixture 构建配置 |
| `tools/` | 静态审计、代码生成与真实服务端验证脚本 |
| `docs/` | ADR、兼容性说明与运维文档 |
| `compat-packs/` | 兼容包元数据与入口 |

`settings.gradle.kts` 定义 28 个 Gradle 子项目

Gradle 项目名与目录名不总相同，例如 `:paper-1201` 对应 `adapters/paper-1.20.1`

## 代码规模

`src/main/java` 下的 Java 文件数，不含 `src/test`、`src/vector`、`src/fabricParity`

| 区域 | 文件数 |
| --- | --- |
| `modules/` | 124 |
| `adapters/` | 143 |
| `versions/` | 1195 |
| `distributions/` | 5 |

`versions/v1_20_1/bootstrap` 的 1179 个文件中，671 个位于 `org/bukkit/craftbukkit` 包，498 个位于 `dev/meldrite/v1201` 包，其余 10 个位于 `io/papermc/paper` 与 `com/destroystokyo/paper` 包

## 通用模块

| Gradle 项目 | 目录 | 主要类型 |
| --- | --- | --- |
| `:foundation` | `modules/foundation` | `ResourceKey`、`CapabilitySet`、`Diagnostic`、`EntityHandle`、`ItemSnapshot` |
| `:bridge-spi` | `modules/bridge-spi` | `BridgeBundle`、`ExecutionPort`、`WorldPort`、`EntityPort`、`RegistryPort` |
| `:plugin-spi` | `modules/plugin-spi` | `PluginDescriptor`、`LoadPlan`、`PluginScope`、`ClassTransform`、`GlobalLibraryAccess` |
| `:plugin-runtime` | `modules/plugin-runtime` | `PluginDiscovery`、`DependencyPlanner`、`PluginController`、`PluginState` |
| `:plugin-classloading` | `modules/plugin-classloading` | `PluginLoaderEngine`、`ApiAuthority`、`PluginRealm`、`LibraryRealm` |
| `:api-services` | `modules/api-services` | `ServiceDirectory` 与 11 个领域 Service |
| `:craft-common` | `modules/craft-common` | `FacadeFactory`、`WrapperCache`、`WorldDelegate`、`EntityDelegate` |
| `:registry` | `modules/registry` | `RegistryCatalog`、`RegistrySnapshot`、`ReloadCoordinator` |
| `:adventure` | `modules/adventure` | `AudienceService`、`ComponentCodec`、`MessagePolicy` |
| `:events` | `modules/events` | `EventCoordinator`、`EventPolicy`、`ListenerRegistry` |
| `:scheduler` | `modules/scheduler` | `TickScheduler`、`AsyncScheduler`、`ComputeService`、`WorkerSettings` |
| `:commands` | `modules/commands` | `CommandRegistry`、`CommandDispatcher`、`PermissionPolicy` |
| `:bootstrap-common` | `modules/bootstrap-common` | `BootstrapCoordinator`、`DistributionVerifier`、`StartupBanner`、`StartupWatchdog` |
| `:extension-api` | `modules/extension-api` | `InteropServices`、`ContentAccess`、`ServerExecutor` |
| `:remap-engine` | `modules/remap-engine` | `RemapPlanner`、`BytecodeRemapper`、`ReflectionRewritePolicy`、`RemapCache` |
| `:compatibility-spi` | `modules/compatibility-spi` | `CompatibilityPack`、`CompatibilityContext`、`CompatibilityStage` |
| `:compatibility-engine` | `modules/compatibility-engine` | `PackMatcher`、`PackPlanner`、`PackAudit` |

依赖边界记录在 `contracts/architecture-rules.json`，`verifyArchitecture` 按该文件检查项目依赖、外部依赖与常量池中的禁用前缀

## 版本实现

### 1.20.1

| Gradle 项目 | 目录 | 内容 |
| --- | --- | --- |
| `:paper-1201` | `adapters/paper-1.20.1` | Bukkit/Paper 门面、事件、命令、调度器、物品工厂、插件宿主与兼容变换 |
| `:v1201-bridge` | `versions/v1_20_1/bridge` | `VersionBridgeBundle`、`HookGateway`、`NativeWorldPort`、`NativeEntityPort`、`ItemNbtCodec`、`AudienceNetworkBridge` |
| `:v1201-bootstrap` | `versions/v1_20_1/bootstrap` | Fabric 入口、装配根、Craft 实现、Mixin、控制台与性能内核 |
| `:mc1201` | `distributions/mc-1.20.1` | 依赖安装、运行类路径、可执行服务端 JAR |

- `versions/v1_20_1/bootstrap/src/main/resources/meldrite.mixins.json` 列出 297 个 server Mixin，配置插件为 `dev.meldrite.v1201.hook.MeldriteMixinPlugin`，访问放宽文件为 `meldrite.accesswidener`
- `versions/v1_20_1/craft-bindings/craft-abi.json` 记录 Craft 类名与原生句柄的对应关系
- `versions/v1_20_1/mappings` 存放 Spigot、Mojang 名称到 Fabric intermediary 的映射资源 `legacy-names.bin.gz`、反射名称表、被拒绝成员清单与生成工具，`provenance.properties` 记录输入来源与哈希

### 1.21.1

1.21.1 目录包含入口与适配骨架，未包含原生实现、Mixin、映射资源和可执行启动器

| 路径 | 内容 |
| --- | --- |
| `versions/v1_21_1/bootstrap` | `MeldriteInitializer`，注册 `SERVER_STARTED`、`SERVER_STOPPING` 并输出运行记录；`fabric.mod.json`、空的 `meldrite.mixins.json`、空的 `meldrite.accesswidener`、`hook-manifest.json`、`craft-abi.json` |
| `versions/v1_21_1/bridge` | `VersionBridge`，返回版本号与原生数据合并结果 |
| `adapters/paper-1.21.1` | `ProfileFactory`、`EventFactory`、`PaperProviderAdapter`、`CommandLifecycleAdapter`、`RegistryAdapter` |
| `distributions/mc-1.21.1` | `DistributionDescriptor` 与清单任务，`distribution.json` 的 artifact 哈希为空 |

`versions/v1_21_1` 没有 `craft-bindings`、`mappings` 与 `hooks` 目录，`hook-manifest.json` 声明的 `block-break.pre-commit` 与 `native-data.component-codec` 没有对应的 Mixin 实现

`versions/v1_21_1` 与 `adapters/paper-1.21.1` 的模块以 Java 21 为目标，`contracts/profiles/paper-1.21.1` 包含该 profile 的 API 索引、delta、生命周期与插件生命周期文件

## 启动流程

1. `ServerLauncher.main` 读取 `META-INF/meldrite/bundle.properties`，检查 `javaFeature`，调用 `DependencyInstaller.install` 把依赖写入 `libraries/meldrite/<sha256>/<文件名>`
2. 设置 `fabric.gameJarPath`、`fabric.addMods`、`java.class.path`，通过 `RuntimeClasspath` 把已验证 JAR 加入系统类加载器搜索路径
3. 调用 `net.fabricmc.loader.impl.launch.server.FabricServerLauncher`
4. Fabric 加载 `meldrite` 模组，`MeldriteInitializer.onInitialize` 安装控制台日志与权限桥接，注册命令与生命周期回调
5. `SERVER_STARTING` 阶段由 `VersionComposition.start` 依次执行原生绑定、`DistributionVerifier.verify`、`BootstrapCoordinator` 的阶段推进、`ProfileBootstrap` 的发现、bootstrap、加载与启用
6. `SERVER_STARTED` 阶段执行 `HookHealthVerifier.verify` 与 `worldsReady`，随后进入 `READY`
7. `SERVER_STOPPING` 阶段关闭审计执行器与装配根

`BootstrapCoordinator.Phase` 的取值为 `NEW`、`DISTRIBUTION_VERIFIED`、`TRANSFORMS_REGISTERED`、`SERVICES_BOUND`、`PLUGINS_DISCOVERED`、`PLUGIN_BOOTSTRAP`、`PLUGINS_LOADED`、`STARTUP_ENABLED`、`WORLDS_READY`、`READY`、`STOPPING`、`STOPPED`、`FAILED`

`VersionBridgeBundle.create` 按实际实现装配 `BridgeBundle`：`execution`、`registry`、`eventIngress`、`itemCodec`、`networkAudience`、`world`、`entity`、`dataFix`、`serverLifecycle` 为 `Optional.of`，`commandTree` 与 `inventory` 为 `Optional.empty()`

能力集合包含 `execution.primary`、`server.lifecycle.start-stop`、`events.block-break`、`hook.block-break.pre-commit`、`registry.snapshot`、`item.nbt-opaque`、`datafix.identity`

## 发行包与运行

`./gradlew :mc1201:assemble -Pmc=1.20.1` 生成 `distributions/mc-1.20.1/build/server/meldrite-1.20.1.jar` 与带完整版本号的文件副本

该 JAR 的 `Main-Class` 为 `dev.meldrite.distribution.mc1201.ServerLauncher`，`Launcher-Agent-Class` 为 `dev.meldrite.distribution.mc1201.RuntimeClasspath`

```bash
java -Xms2G -Xmx6G -jar meldrite-1.20.1.jar nogui
```

- 启动器在参数中没有 `nogui` 或 `--nogui` 时自动追加 `nogui`
- 运行目录下的 `mods/` 与 `plugins/` 在启动时创建
- 依赖缓存位于 `libraries/meldrite/`，按 SHA-256 与文件大小校验后复用，缺失或校验失败时从发行清单记录的 URL 下载
- `eula.txt` 由原版服务端生成，发行清单的 `eulaPath` 指向该文件，首次启动后修改为 `eula=true` 并再次启动
- Windows 上 JVM 原生类路径接口对非 ASCII 与超长路径有限制，启动器会把受影响的 JAR 按 SHA-256 复制到 `java.io.tmpdir/meldrite-runtime/` 并复用同一份字节，可用 `-Djava.io.tmpdir=<短 ASCII 路径>` 指定临时目录

## 控制台

`DedicatedConsole` 替换专用服务端的 stdin 读取，命令仍交给原生 dispatcher 执行

- `--nojline` 关闭 JLine，`--noconsole` 关闭 stdin 命令输入，两个参数由 `MainConsoleOptionsMixin` 在解析前移除
- 交互模式提供方向键编辑、历史回溯、Tab 补全与语法着色，历史写入运行目录的 `.console_history`
- Ctrl+C 触发正常停服，Ctrl+D 只关闭输入
- `ConsoleText` 把 `§` 颜色与 RGB 序列转换为 ANSI，`-Dterminal.ansi=false` 关闭转换
- 日志统一为 `[HH:mm:ss] [线程名/级别]: 消息`，JUL 的 INFO、WARNING、SEVERE 映射为 INFO、WARN、ERROR
- `ConsoleLogging` 把 JUL 与标准输出、标准错误接入同一 Log4j 上下文，日志在输入行上方重绘

| 命令 | 权限 | 作用 |
| --- | --- | --- |
| `/meldrite version` | 无 | 输出 Meldrite、Minecraft、Fabric Loader、Java 版本 |
| `/meldrite workers` | 等级 2 | 输出异步线程、队列、完成数、拒绝数、计算并行度与向量宽度 |
| `/meldrite audit` | 等级 2 | 在单个后台线程执行原生 ABI 结构审计，报告写入 `reports/meldrite/native-abi.json` |

`CraftCommandMap` 把原生命令根同步到 Bukkit 命令表，`BukkitCommandTree` 把 Bukkit 命令投影回 Brigadier，`CraftServer.syncCommands` 在命令变化后重新发布并推送在线玩家的命令树

## 插件加载

| 阶段 | 实现 |
| --- | --- |
| 发现 | `PluginDiscovery` 读取 `paper-plugin.yml` 与 `plugin.yml`，计算完整 SHA-256，描述文件解析按排序顺序回到调用线程 |
| 依赖规划 | `DependencyPlanner` 生成 `LoadPlan`，`PluginController` 校验宿主与计划一致 |
| 类加载 | `PluginLoaderEngine` 为每个插件建立 `PluginRealm`，`ApiAuthority` 以锁定的 API 类路径作为定义权威 |
| 变换 | `LegacyNameTransform`、`EntityAbiTransform`、`LegacyTextConstructorTransform`、`WorldSpawnAbiTransform` 在 `ClassTransform` 阶段执行 |
| 生命周期 | `PluginController` 的 `PREPARE`、`CONSTRUCT`、`LOAD`、`ENABLE`、`DISABLE`、`CLEANUP` 步骤串行执行 |
| 重载 | `ProfileServer.reload` 分阶段执行配置重载、类加载器退役、插件清理、资源重载、重新发现与启用，日志记录在 `Meldrite.Reload` |

- `PluginState` 取值为 `RESOLVED`、`CONSTRUCTED`、`LOADED`、`ENABLED`、`DISABLING`、`DISABLED`、`FAILED`、`BLOCKED_DEPENDENCY`
- `ProfileBootstrap` 的 `discover`、`bootstrapPlugins`、`loadPlugins`、`enableStartup`、`worldsReady` 均在主线程调用，非主线程调用抛出 `PROFILE_BOOTSTRAP_OFF_PRIMARY`
- 插件异步任务由 `AsyncScheduler` 承载，队列满时拒绝提交
- `MeldriteConfiguredClassLoader` 实现 `ConfiguredPluginClassLoader`，`libraryLoader` 字段指向插件私有依赖域，`InternalAccessDetector` 索引库中的内部访问，`GlobalLibraryRegistry` 处理运行时全局库

## 契约与审计

| 路径 | 内容 |
| --- | --- |
| `contracts/locks/paper-1.20.1.lock.json` | Paper API 根坐标、解析版本、URL、SHA-256、传递依赖 |
| `contracts/locks/paper-1.21.1.lock.json` | 1.21.1 profile 的对应锁 |
| `contracts/locks/tools.lock.json` | 构建工具依赖锁 |
| `contracts/locks/console-3.25.1.lock.json` | JLine 构件与哈希 |
| `contracts/profiles/paper-1.20.1/api-index.json` | 45 个构件的类与符号清单索引，合计 472025 个符号 |
| `contracts/profiles/paper-1.20.1/bindings.json` | 95234 条符号绑定，其中 `PARTIAL` 271 条、`UNSUPPORTED` 51633 条、`NOT_APPLICABLE` 43330 条 |
| `contracts/profiles/paper-1.20.1/capabilities.json` | 14 项能力，8 项 `PARTIAL`、6 项 `UNSUPPORTED` |
| `contracts/profiles/paper-1.20.1/runtime-providers.json` | 服务端提供给插件的运行库版本、来源与 SHA-256 |
| `contracts/profiles/paper-1.20.1/internal-access-policy.json` | 反射改写策略与失败码 |
| `contracts/release-policy.json` | 支持的 Minecraft 版本、profile 与 EULA 策略 |
| `contracts/architecture-rules.json` | 每个模块允许的项目依赖、外部依赖与禁用前缀 |

`build-tools/contracts` 提供 `ContractAudit`、`LockVerifier`、`ArchitectureVerifier`、`ReleaseVerifier`、`ArtifactLocker`、`ApiInventory`、`BindingVerifier`、`CapabilityWorklist`、`ProfileAdapterGenerator` 与 `CompatibilityReport`

`ProfileAdapterGenerator` 从 ABI 清单生成接口实现骨架，可确定的签名生成类型化覆写，无法确定的返回类型与泛型替换直接拒绝，不生成反射代理

## 构建与验证命令

以下命令在仓库根目录执行

| 目的 | 命令 |
| --- | --- |
| 准备锁定依赖缓存 | `python3 .github/scripts/prepare-build.py` |
| 仅检查现有缓存 | `python3 .github/scripts/prepare-build.py --check-cache` |
| 编译单个模块 | `./gradlew :foundation:compileJava --configure-on-demand` |
| 运行单个模块测试 | `./gradlew :foundation:test --configure-on-demand` |
| 构建 1.20.1 可执行服务端 | `./gradlew :mc1201:assemble -Pmc=1.20.1 --configure-on-demand` |
| 运行全部子项目测试 | `./gradlew test --configure-on-demand` |
| 运行集成矩阵 | `./gradlew integrationTest -Pmc=1.20.1 -Pprofile=paper-1.20.1 -Pmodset=fixture -Pscenario=all` |
| 校验契约依赖文件 | `./gradlew verifyContractArtifacts -Pprofile=paper-1.20.1 --configure-on-demand` |
| 校验 API 清单与绑定 | `./gradlew verifyContracts -Pprofile=paper-1.20.1 --configure-on-demand` |
| 校验模块依赖边界 | `./gradlew verifyArchitecture --configure-on-demand` |
| 校验发行策略 | `./gradlew verifyRelease -Pprofile=paper-1.20.1 -Pdistribution=mc-1.20.1 --configure-on-demand` |
| 重新生成清单与初始绑定 | `./gradlew generateContracts -Pprofile=paper-1.20.1 --configure-on-demand` |
| 刷新依赖锁 | `./gradlew lockContracts -Pprofile=paper-1.20.1 --configure-on-demand` |
| 差异格式检查 | `git diff --check` |

- 版本号由日期、Git 提交数、Minecraft 版本与通道组成，形如 `2026.11.14.47-1.20.1-release`
- 日期按 Asia/Shanghai 生成，通道取 `dev`、`release`、`pre-release`，通过 `-Pmeldrite.channel` 选择
- 无 Git 历史时可用 `-Pmeldrite.buildDate` 与 `-Pmeldrite.commitCount` 指定
- 每次发行构建只选择一个 Minecraft 版本，`remapJar` 在目标版本与构建版本不一致时失败

## 运行时参数

| 属性或环境变量 | 默认 | 作用 |
| --- | --- | --- |
| `-Dmeldrite.async.threads` | 自动 | 插件异步线程数，手动范围 1–64 |
| `-Dmeldrite.async.queue-capacity` | 自动 | 异步任务队列上限，手动范围 1–65536 |
| `-Dmeldrite.compute.threads` | 自动 | 纯计算并行度，手动范围 1–64 |
| `-Dmeldrite.compute.minimum-batch` | 4096 | 计算分片的最小输入量 |
| `-Dmeldrite.discovery.threads` | 自动 | 插件 JAR 读取与哈希线程数，手动范围 1–16 |
| `-Dmeldrite.vector.disabled` | false | 关闭 Vector API 内核，回退标量实现 |
| `-Dmeldrite.performance.disabled` | false | 关闭全部版本层性能路径 |
| `-Dmeldrite.performance.<name>.disabled` | false | 单独关闭 `fluid`、`tracking`、`path-index`、`snapshot-copy`、`collision-cache` |
| `-Dmeldrite.abi.requests` | 未设置 | 原生 ABI 审计的外部请求清单路径 |
| `-Dmeldrite.console.enabled` | true | 由 `--noconsole` 设为 false |
| `-Dmeldrite.console.jline` | true | 由 `--nojline` 设为 false |
| `-Dterminal.ansi` | 未设置 | 设为 false 时关闭控制台 ANSI 转换 |
| `MELDRITE_PROFILE` | `paper-1.20.1` | 发行校验读取的 profile 名 |
| `MELDRITE_DISABLE_LIFECYCLE` | false | 移除 `server.lifecycle.start-stop` 能力与生命周期端口 |

自动值的计算规则见 `WorkerSettings.resolve`：

- 并发名额上限为 `min(64, 最大堆 / 256 MiB)`
- 异步线程取 `min(名额, min(8, max(1, 处理器数 / 2)))`
- 计算并行度取 `min(名额, min(16, max(1, 处理器数 / 2)))`
- 队列容量取 `max(32, 异步线程数 * 8)`
- `PluginDiscovery.scanThreads` 的自动值为 `min(4, max(1, 处理器数 - 1))`
- `ComputeService.forEach` 在剩余堆低于 `min(256 MiB, 最大堆 / 8)` 时按串行执行

## 测试与验证工具

测试源码、fixture 与 probe 输入由 `.gitignore` 排除，仓库只跟踪测试任务配置与 fixture 构建脚本

克隆后的仓库可能没有测试源码，此时对应任务为 `NO-SOURCE`

| 工具 | 用途 |
| --- | --- |
| `tools/verify-executable-server.py` | 启动重定位后的发行 JAR，验证首次启动、世界保存与同世界重启 |
| `tools/probe-console-pty.py` | 在真实 PTY 中驱动 JLine 控制台，不启动 Minecraft，配合 `:v1201-bootstrap:consoleProbeClasspath` |
| `tools/probe-production-mixins.py` | 用真实 Mod JAR 加载发行包的 Mixin 目标 |
| `tools/audit-native-extensions.sh` | 扫描插件 JAR 的原生与 Paper 内部成员引用，可导出运行时审计请求清单 |
| `tools/audit-plugin-apis.sh` | 统计 Bukkit API 调用点与显式拒绝路径，`--self-test` 运行自检 |
| `tools/audit-craft-sources.mjs` | 对照上游 Craft 源码与本地实现 |
| `tools/fabric-mod-parity.py` | 在 stock Fabric 与 Meldrite 上运行同一 Mod 集合与探针 |
| `tools/fabric-baseline.mjs` | 采集 stock Fabric 基线 |
| `tools/compile-entity-subtypes.mjs` | 编译实体子类型探针 |
| `tools/GenerateEntityApi.java`、`GenerateCraftPlayer.java`、`GenerateCraftBlockData.java`、`GenerateCraftBlockTemplates.java` | 生成门面与方块数据代码 |
| `versions/v1_20_1/mappings/tools/regenerate.sh` | 重新生成 legacy 映射资源 |

`verify-executable-server.py` 的参数为 `--jar`、`--java`、`--eula`、`--mods`、`--plugins`、`--timeout`、`--network-timeout`、`--login-probe`、`--command-probe`、`--interaction-probe`、`--report`，报告写入 `build/reports/meldrite/executable-server/`

审计任务把过程日志写入 `build/reports/meldrite/<task>/process.log`，并以进程退出码判断结果

## CI

`.github/workflows/runtime.yml` 在推送与拉取请求时构建 1.20.1 发行 JAR，校验归档、生成 SHA-256 并上传产物

手动触发时可选 `dev`、`release`、`pre-release` 通道，非 `dev` 通道在构建成功后创建 GitHub Release

## 许可证

项目采用 [GNU GPL v3](LICENSE)

CraftBukkit 相关代码的额外声明见 [UPSTREAM-CraftBukkit-LICENSE.txt](versions/v1_20_1/bootstrap/UPSTREAM-CraftBukkit-LICENSE.txt)
