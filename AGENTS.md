# Meldrite Agent 开发规范

本文件适用于整个仓库。沟通和项目说明默认使用中文，代码命名、格式和错误码沿用所在模块的约定。具体任务中用户明确提出的要求优先。

## 开发流程

1. 开始前阅读本文件、[README.md](README.md) 和相关实现；检查 `git status --short --untracked-files=all`、`git diff`、`git diff --cached`，了解整个工作区已有的修改。
2. 目标明确时，简述方案后自主完成实现和验证。需求有歧义、影响公共接口或涉及重要架构取舍时，先提出具体问题确认。
3. 沿调用链了解通用服务、Paper 适配和版本实现之间的关系，再确定修改位置。遵循现有模块职责，避免无关重构和全仓格式化。
4. 根据实际风险选择验证方式。交付时说明改动、验证命令及结果、尚未覆盖的行为和阻塞原因。
5. 新增、移动模块或关键入口时，同步维护本文件索引，使后续 Agent 能按功能定位代码。

## 功能真实性：强制要求

- 禁止用空实现伪装兼容或声称功能已经实现。不得通过无操作方法、固定成功值、`null`、空集合、吞掉异常等方式掩盖缺失行为。
- 只有接口契约本身要求无操作或空返回时，才可采用相应实现，并明确其语义。方法存在、签名匹配、代码编译通过，都不能单独证明功能可用。
- 暂不支持的行为必须通过明确异常、诊断或能力状态体现；报告准确描述支持范围、限制和未完成部分。
- 兼容性声明必须有真实调用链和行为验证。涉及 `SUPPORTED`、`PARTIAL` 等契约状态时，应满足对应审计规则，不得伪造证据、降低断言或禁用检查来制造通过结果。

## 架构与运行约束

- 当前主要开发目标是 Minecraft 1.20.1。1.21.1 有独立适配、版本实现和发行目录，目录存在不等于支持程度与 1.20.1 相同。修改共享模块时检查两套调用方，版本特有行为放在对应版本目录。
- `modules/` 承载通用模型、SPI 和服务；`adapters/` 承载 Bukkit/Paper API 适配；`versions/` 承载 Minecraft/Fabric 原生实现、Mixin 和版本装配；`distributions/` 负责发行包和启动器。
- 新增依赖或跨层引用前，检查 [contracts/architecture-rules.json](contracts/architecture-rules.json) 和相应 Gradle 配置。保持 `foundation`、通用 SPI 和服务的既有依赖边界，避免将版本实现倒灌到通用模块。
- 原生对象、映射名称和 Mixin 修改应在版本层处理；Paper API 的表现应沿现有适配与门面实现。发现架构规则与现有实现不一致时，说明具体差异，确认需要改变的边界，不能仅为通过检查放宽规则。
- 保持同步调度、世界修改、事件回调和 Fabric tick 的线程及顺序约束。插件异步任务与内部纯计算使用各自有界资源；修改并行逻辑时同时检查拒绝、回退、异常和关闭行为。
- 修改插件生命周期、类加载或重映射时，检查加载顺序、API 类身份、可见性、运行命名空间及资源释放，避免以重复打包 API 类或反射兜底掩盖链接问题。
- 保留上游版权和许可证声明；CraftBukkit 相关代码还需注意 [UPSTREAM-CraftBukkit-LICENSE.txt](versions/v1_20_1/bootstrap/UPSTREAM-CraftBukkit-LICENSE.txt)。

## 构建、依赖与验证

### 环境与依赖

- 使用仓库的 Gradle Wrapper。Gradle daemon 使用 Java 21，通用 Java 编译约定以 Java 17 为目标，CI 同时安装 17 和 21 工具链；具体模块以自身配置为准，不能统一改写语言级别。
- 依赖版本、来源和校验信息见 [gradle/](gradle/)、[contracts/locks/](contracts/locks/) 和 [.github/build-dependencies.lock.json](.github/build-dependencies.lock.json)。项目缓存位于 `.gradle/meldrite/artifacts/`，部分依赖还使用用户 Gradle 缓存。
- 新环境通常需要先准备锁定缓存。依赖缺失时先诊断缓存、来源和工具链；只有任务涉及依赖调整时才更新相关锁和校验记录，不用跳过校验或随意升级依赖代替修复。
- `.github/scripts/prepare-build.py` 的 `--trust-loom-outputs` 会修改 `gradle/verification-metadata.xml`，是 CI 使用的选项；本地使用前应明确其作用，提交时按任务审阅该文件。

### 测试源码保持忽略

- 测试源码、fixture 和 probe 输入保持 Git 忽略，这是项目设计的一部分。不得擅自删除相关 `.gitignore` 规则、强制添加这些文件，或将测试移入生产目录来绕过忽略规则。
- 可按需创建和运行本地测试。测试应覆盖真实行为、失败分支和必要的回归场景；修复行为问题时，优先用能够复现问题的测试验证。
- 克隆后的仓库可能只有测试任务配置，没有本地测试源码或 fixture。`NO-SOURCE`、跳过、空测试集、缺少输入或未执行，都不能报告为测试通过。
- 如实记录实际执行的测试数量与结果。需要原生运行环境的行为，应按需在独立测试服验证，不能仅凭模块编译或发行打包声称兼容。

### 常用命令

以下命令均在仓库根目录运行。按改动选择需要的检查，执行前确认锁定缓存、对应工具链和本地测试输入已具备。

| 目的 | 命令或入口 |
| --- | --- |
| 准备锁定的构建依赖 | `python3 .github/scripts/prepare-build.py` |
| 仅检查已有依赖缓存 | `python3 .github/scripts/prepare-build.py --check-cache` |
| 编译单个模块，以 foundation 为例 | `./gradlew :foundation:compileJava --configure-on-demand` |
| 运行单个模块的本地测试，以 foundation 为例 | `./gradlew :foundation:test --configure-on-demand` |
| 构建 1.20.1 可执行服务端 | `./gradlew :mc1201:assemble -Pmc=1.20.1 --configure-on-demand` |
| 校验 1.20.1 契约依赖文件 | `./gradlew verifyContractArtifacts -Pprofile=paper-1.20.1 --configure-on-demand` |
| 架构、契约、发行审计 | 根构建中的 `verifyArchitecture`、`verifyContracts`、`verifyRelease`，先检查所选 profile、distribution 及所需输入 |
| 差异格式检查 | `git diff --check`，提交前另查 `git diff --cached --check` |

- 单次发行构建选择一个 Minecraft 版本，使用对应的模块任务与 `-Pmc`，避免无差别执行全仓发行任务。
- 1.20.1 产物位于 `distributions/mc-1.20.1/build/server/`。`assemble` 负责打包，不能替代真实服务端行为验证。
- 版本由日期、Git 提交数、Minecraft 版本和 channel 组成；通过 `-Pmeldrite.channel` 选择 `dev`、`release` 或 `pre-release`，具体规则见构建约定与 CI。
- 审计任务可能需要配置或编译多个版本，不能假定只传入一个 profile 就具备全部输入。命令受环境或缺失输入阻塞时，报告具体原因，不得通过删任务、删断言或补空实现规避。
- 仅修改文档时，核对路径、描述、命令定义、忽略规则和差异格式即可；交付中准确说明实际验证范围。

## 提交范围：强制要求

以下规则适用于每次执行 Git 提交时；提交、推送的时机按用户任务要求执行。

1. **每次提交必须纳入整个工作区所有应纳入版本管理的生产源码修改，包括本轮对话之外、此前任务或其他来源产生的新增、修改和删除。不得仅提交本轮任务改动的生产源码。**
2. “生产源码”指正式功能的非测试源码，包含模块的生产源集，如 `src/main/java/`、`src/vector/java/`。构建脚本、配置、依赖锁和文档按当前任务纳入，不因上述规则自动全部提交。
3. 提交前同时审阅未暂存、已暂存和未跟踪文件，列出生产源码变更，再按清单暂存。测试源码和临时产物继续保持忽略，不使用强制添加绕过既有规则。
4. 不得为缩小提交范围或清理工作区而撤销、覆盖、隐藏其他来源的生产源码修改。纳入既有修改不代表这些修改由本轮完成，也不代表已验证，应明确其来源和实际验证范围。
5. 提交前检查 `git diff --cached` 和 `git diff --cached --check`，再次对照 `git status --short --untracked-files=all`，确保没有遗漏生产源码或误纳入无关文件。

## 项目文件索引

本索引按模块职责和关键入口组织，不逐个列出全部源码。Gradle 项目名与目录名不总相同，以 [settings.gradle.kts](settings.gradle.kts) 为准。

### 工程配置与工具

| 路径 | 用途 |
| --- | --- |
| [README.md](README.md) | 项目介绍、启动方式、运行参数和主要构建命令 |
| [settings.gradle.kts](settings.gradle.kts) | Gradle 项目与实际目录的映射、仓库配置 |
| [build.gradle.kts](build.gradle.kts) | 聚合测试、依赖锁定、契约和架构审计任务 |
| [build-logic/src/main/kotlin/meldrite.java.gradle.kts](build-logic/src/main/kotlin/meldrite.java.gradle.kts) | 公共 Java 编译、测试、依赖锁和版本生成约定 |
| [gradle/](gradle/) | Wrapper、工具链约束、版本目录与依赖验证元数据；各模块另有 `gradle.lockfile` |
| [contracts/architecture-rules.json](contracts/architecture-rules.json) | 模块依赖、外部依赖和符号引用边界 |
| [contracts/locks/](contracts/locks/) | Paper、工具和控制台依赖锁 |
| [contracts/profiles/](contracts/profiles/) | 各 Paper 版本的 API、生命周期和兼容性契约 |
| [build-tools/contracts/](build-tools/contracts/)（`:testkit-contract`） | `ContractAudit`、`ArchitectureVerifier`、`LockVerifier`、`ReleaseVerifier` 等构建审计入口 |
| [.github/workflows/runtime.yml](.github/workflows/runtime.yml) | 1.20.1 构建、产物校验及手动发布流程 |
| [.github/scripts/prepare-build.py](.github/scripts/prepare-build.py) | 恢复并校验构建所需的锁定依赖 |
| [tools/](tools/) | API/Craft 源码审计、代码生成和可执行服务端验证工具；使用前阅读对应脚本的输入要求 |
| [testkit/](testkit/) | 本地测试运行支持、集成任务和 fixture 构建配置；测试输入保持忽略 |

### 通用模块

这些模块的 Gradle 项目名与目录末级名称相同。

| 模块 | 职责与定位线索 |
| --- | --- |
| [modules/foundation/](modules/foundation/) | 通用值对象、句柄、能力集合和诊断，如 `ResourceKey`、`CapabilitySet`、`Diagnostic` |
| [modules/bridge-spi/](modules/bridge-spi/) | 通用服务与原生服务端之间的端口契约，入口 `BridgeBundle`、`ExecutionPort` 等 |
| [modules/plugin-spi/](modules/plugin-spi/) | 插件描述、依赖、宿主、作用域、类变换和可见性契约 |
| [modules/plugin-runtime/](modules/plugin-runtime/) | 插件发现、依赖规划和生命周期，入口 `PluginDiscovery`、`DependencyPlanner`、`PluginController` |
| [modules/plugin-classloading/](modules/plugin-classloading/) | 插件和库的类加载域、API 类身份与可见性，入口 `PluginLoaderEngine`、`ApiAuthority` |
| [modules/api-services/](modules/api-services/) | 世界、实体、物品栏、配置、权限等通用服务，入口 `ServiceDirectory` 和各领域 Service |
| [modules/craft-common/](modules/craft-common/) | 共享门面委托与包装缓存，入口 `FacadeFactory`、`WrapperCache` |
| [modules/registry/](modules/registry/) | 注册表快照、投影与重载，入口 `RegistryCatalog`、`ReloadCoordinator` |
| [modules/adventure/](modules/adventure/) | 消息接收方、组件编解码和消息策略，入口 `AudienceService`、`ComponentCodec` |
| [modules/events/](modules/events/) | 事件动作协调、策略和监听器登记，入口 `EventCoordinator`、`ListenerRegistry` |
| [modules/scheduler/](modules/scheduler/) | tick 调度、插件异步池、纯计算与 worker 配置，入口 `TickScheduler`、`AsyncScheduler`、`ComputeService` |
| [modules/commands/](modules/commands/) | 命令登记、分发和权限策略，入口 `CommandRegistry`、`CommandDispatcher` |
| [modules/bootstrap-common/](modules/bootstrap-common/) | 通用启动阶段、发行校验和启动信息，入口 `BootstrapCoordinator`、`DistributionVerifier` |
| [modules/extension-api/](modules/extension-api/) | Mod 与插件互操作的通用扩展接口，如 `InteropServices` |
| [modules/remap-engine/](modules/remap-engine/) | 字节码重映射、命名空间规划、签名和缓存策略 |
| [modules/compatibility-spi/](modules/compatibility-spi/) | 兼容包上下文、阶段和接口 |
| [modules/compatibility-engine/](modules/compatibility-engine/) | 兼容包匹配、规划和审计 |

### 适配、版本与发行

| 路径 | Gradle 项目 / 职责 |
| --- | --- |
| [adapters/paper-1.20.1/](adapters/paper-1.20.1/) | `:paper-1201`；Paper/Bukkit 适配、插件宿主、API 门面与兼容变换 |
| [versions/v1_20_1/bridge/](versions/v1_20_1/bridge/) | `:v1201-bridge`；`VersionBridgeBundle` 装配原生端口，`HookGateway` 连接原生动作与事件 |
| [versions/v1_20_1/bootstrap/](versions/v1_20_1/bootstrap/) | `:v1201-bootstrap`；Fabric 入口、版本装配、原生兼容、Craft 实现、Mixin 和控制台 |
| [versions/v1_20_1/mappings/](versions/v1_20_1/mappings/) | 旧插件名称到 Fabric 命名空间的映射资源、生成工具和来源说明 |
| [distributions/mc-1.20.1/](distributions/mc-1.20.1/) | `:mc1201`；依赖安装、运行类路径、可执行服务端和发行描述 |
| [adapters/paper-1.21.1/](adapters/paper-1.21.1/) | `:paper-1211`；1.21.1 的 Paper 适配实现 |
| [versions/v1_21_1/bridge/](versions/v1_21_1/bridge/) | `:v1211-bridge`；1.21.1 版本端口实现 |
| [versions/v1_21_1/bootstrap/](versions/v1_21_1/bootstrap/) | `:v1211-bootstrap`；1.21.1 Fabric 入口和版本资源 |
| [distributions/mc-1.21.1/](distributions/mc-1.21.1/) | `:mc1211`；1.21.1 发行打包配置 |

### 按功能查找关键入口

| 要修改的功能 | 先读的文件及后续位置 |
| --- | --- |
| 独立服务端启动、依赖安装 | [ServerLauncher.java](distributions/mc-1.20.1/src/main/java/dev/meldrite/distribution/mc1201/ServerLauncher.java)，同目录的 `DependencyInstaller`、`RuntimeClasspath` |
| Fabric 回调、启动阶段和服务装配 | [MeldriteInitializer.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/bootstrap/MeldriteInitializer.java) → [VersionComposition.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/bootstrap/VersionComposition.java) |
| 服务端关闭状态与兜底清理 | [ServerLifecycleMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/ServerLifecycleMixin.java) 在原生关闭开始时发布 Paper `hasStopped()` 状态，在关闭完成后执行现有网关清理；该状态与原版运行循环结束后的 `isStopped()` 不同 |
| 插件发现、加载和启停 | [ProfileBootstrap.java](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/bootstrap/ProfileBootstrap.java) → [PluginController.java](modules/plugin-runtime/src/main/java/dev/meldrite/plugin/runtime/PluginController.java)，再查 `plugin-spi` 和 `plugin-classloading` |
| Paper 插件类加载器反射与动态依赖 | [MeldriteConfiguredClassLoader.java](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/MeldriteConfiguredClassLoader.java) 的 `URLClassLoader` 字段指向真实私有依赖域；`LegacyNameTransform` 映射 Paper 实现类名，`InternalAccessDetector` 识别对应反射访问；[OwnedRealm.java](modules/plugin-classloading/src/main/java/dev/meldrite/plugin/classloading/OwnedRealm.java) 在动态 URL 用于类或资源查找前执行既有 JAR 校验 |
| Craft 服务端玩家管理器 | [CraftServer.java](versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/CraftServer.java) 的 `playerList` 字段与在线查询共享原生管理器；Fabric 在创建门面后才初始化管理器，`ServerPlayerManagerBindingMixin` 随原生 setter 发布真实引用，启动期尚未安装时在线名单为空 |
| Bukkit/Paper 世界、实体和物品门面 | [paper/facade/](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/facade/)，关联 `api-services`、`craft-common`，以及版本 bootstrap 中的 `compat/` 和 `org/bukkit/craftbukkit/` |
| 事件、命令和调度 | 对应通用模块 → [paper/event/](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/event/)、[paper/command/](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/command/)、[paper/scheduler/](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/scheduler/) → 版本 bridge 和 [hook/](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/) |
| Meldrite 管理与版本查询命令 | 1.20.1 的 [MeldriteCommands.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/bootstrap/MeldriteCommands.java) 注册 `/meldrite workers` 和公开的 `/meldrite version`；由 `MeldriteInitializer` 接入 Fabric 命令注册回调 |
| 玩家方块左右键与插件取消 | 1.20.1 的 [NativePlayerInteractionEvents.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativePlayerInteractionEvents.java) 构造带手持物品、手、点击面和命中坐标的交互事件；[PlayerInteractionMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/PlayerInteractionMixin.java) 在原生破坏或使用前应用取消结果并同步客户端 |
| 插件实体生成回调 ABI | 1.20.1 的 [WorldSpawnAbiTransform.java](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/compat/WorldSpawnAbiTransform.java) 在插件类准备阶段将 Java Consumer 三参数生成调用接入锁定 Paper API 的 Bukkit Consumer；版本装配显式开放桥类的加载路由 |
| 自定义铁砧与 CMI 反射兼容 | 1.20.1 的 [CraftInventoryAnvil.java](versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/inventory/CraftInventoryAnvil.java) 统一原生与插件铁砧包装；[CraftAnvilScreenHandler.java](versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/inventory/CraftAnvilScreenHandler.java) 保留物品和界面身份；[NativeAnvilContext.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativeAnvilContext.java) 在事件期间选择真实容器，原生重命名字段通过 access widener 开放 |
| 日志、控制台输入与补全 | [console/](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/console/)，重点 `ConsoleLogging`、`DedicatedConsole`、`ConsoleInputLoop` |
| 运行时全局依赖 | `plugin-spi` 的 [GlobalLibraryAccess.java](modules/plugin-spi/src/main/java/dev/meldrite/plugin/spi/GlobalLibraryAccess.java) 显式区分源 API 类路径和服务端运行域；[GlobalLibraryRegistry.java](modules/plugin-classloading/src/main/java/dev/meldrite/plugin/classloading/GlobalLibraryRegistry.java) 校验、准备、缓存新增 JAR 并保持共享身份；[JvmClassPath.java](modules/plugin-classloading/src/main/java/dev/meldrite/plugin/classloading/JvmClassPath.java) 只读枚举 JDK 类路径；1.20.1 的 [NativeGlobalLibraryAccess.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/bootstrap/NativeGlobalLibraryAccess.java) 接入 Fabric 运行加载器 |
| Paper 反射映射资源 | [LegacyMappings.java](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/compat/LegacyMappings.java) 将已校验的 Mojang/intermediary 表导出到实际运行命名空间；[NativeReflectionMappings.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/bootstrap/NativeReflectionMappings.java) 安装标准 `META-INF/mappings/reobf.tiny` 资源，生成器和来源见版本 mappings 目录 |
| Craft 世界原生句柄和边界缓存 | [CraftWorld.java](versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/CraftWorld.java) 的 `world` 字段对应该门面的 canonical 原生世界，`worldBorder` 是实际使用的可替换缓存；清空句柄后原生操作明确失效，拒绝跨世界重绑以保持包装身份 |
| 区块持久数据与原生 NBT | `ChunkPersistentDataMixin` 的 `persistentDataContainer` 与 Bukkit 访问器共用真实 Dirty 容器，修改接入原生待保存状态；`ChunkSerializerPersistentDataMixin` 沿用现有 `BukkitValues` 键，并将完整区块读回数据写入临时包装后的真实 WorldChunk；`WorldChunkPersistentDataMixin` 保留生成中区块转正时的拷贝 |
| 原生区块构造与世界句柄 | [CraftChunk.java](versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/CraftChunk.java) 的原生 `WorldChunk` 构造器读取既有 canonical 世界门面和区块坐标，`worldServer` 字段接入实际原生查询；清空后失效，拒绝跨世界重绑 |
| 反射注解元数据 | [ReflectionMetadataRemapper.java](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/compat/ReflectionMetadataRemapper.java) 保留注解字符串供反射库自行解释，同时重映射真实 JVM 类型引用；`LegacyNameTransform` 保留 CraftBukkit 普通字符串，由 `LegacyClassLookup` 在 `Class.forName` 和公开类加载器查询边界解析，避免重复添加版本包名 |
| 原生方块状态缓存兼容 | [BlockStatePaperCacheMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/BlockStatePaperCacheMixin.java) 初始化 Paper 的不透明度和碰撞边界缓存，并将修改接入原生查询；[BlockStateLightCacheMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/BlockStateLightCacheMixin.java) 将条件遮光缓存接入原生光照判断 |
| 区块节碰撞元数据 | [ChunkSectionCollisionCacheMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/ChunkSectionCollisionCacheMixin.java) 随原生 palette 重算与方块替换维护特殊碰撞计数、两位碰撞分类和真实查询；`MeldriteMixinPlugin` 桥接 Paper 的双可写 palette 构造；Fabric 继续沿用原生逐状态碰撞检查 |
| Paper 可写方块材质转换表 | [NativeMaterialProjection.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativeMaterialProjection.java) 独立保存 frozen 注册表身份快照和可写转换表，`CraftMagicNumbers.BLOCK_MATERIAL` 引用稳定门面；表修改直接影响方块和 BlockData 的 Material 查询，未绑定时明确诊断 |
| 原生带原因实体插入 | [NativeEntitySpawner.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativeEntitySpawner.java) 的 `addNative` 执行生成原因、事件取消和单实体原生插入；`ModifiableWorldSpawnReasonMixin` 提供 Paper 接口契约，`ServerWorldSpawnReasonMixin` 与 `ChunkRegionSpawnReasonMixin` 分别接入服务端与世界生成路径 |
| 配方材料精确匹配 | [IngredientExactMatchMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/IngredientExactMatchMixin.java) 提供 CraftBukkit 的 `exact` 字段，将物品及 NBT 比较接入原生材料判断，并通过 Fabric `requiresTesting()` 接入无序配方匹配，保留 Mod 自定义材料实现 |
| 配方与个人配方书同步 | [NativeRecipes.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativeRecipes.java) 同步真实配方列表及每位玩家的 INIT 配方书；`PlayerManagerRecipeReloadMixin` 提供 Paper `reloadRecipeData()`，异步请求交由服务端主线程执行，不阻塞插件 worker |
| 容器当前配方状态 | `InventoryRecipeMixin` 保留无配方状态容器的 Paper 默认契约；`CraftingInventoryRecipeMixin` 在工作台矩阵上保存真实配方；[RecipeManagerMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/RecipeManagerMixin.java) 将普通和缓存 ID 匹配结果写回矩阵，无匹配时清空 |
| 原生实体索引反射入口 | [ServerWorldPaperLookupMixin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/ServerWorldPaperLookupMixin.java) 将 Paper 的 `getEntityLookup()` 接入 `NativeEntityExtensions` 的真实原生索引，保持稳定包装身份和后端主线程约束 |
| 玩家丢物品与取消恢复 | [NativePlayerDrop.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativePlayerDrop.java) 实现 Paper 四参数丢物品、事件和取消后的背包恢复；`PlayerDropItemMixin` 将原生三参数路径接入事件，服务端玩家原有调用方继续负责实体插入与统计 |
| 原生反射构造器和静态 ABI | [MeldriteMixinPlugin.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/hook/MeldriteMixinPlugin.java) 在原生类定义前补齐真实构造或转发入口，包括 Paper 玩家信息包的 Entry 列表构造；[NativeSectionBlockPacketConstructorTransform.java](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/compat/NativeSectionBlockPacketConstructorTransform.java) 提供分区方块更新包的状态数组构造，由 `NativeSectionBlockPacketMixin` 锚点接入；游戏符号由 Fabric MappingResolver 解析 |
| 插件重映射和旧版 ABI 兼容 | `plugin-classloading`、`remap-engine` → [paper/compat/](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/compat/) → [映射说明](versions/v1_20_1/mappings/README.md) 和版本原生实现 |
| 主线程性能与地图颜色计算 | [perf/](versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/perf/)、对应 Mixin，以及 [paper/map/](adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/map/) 和 `scheduler` 的计算服务 |
