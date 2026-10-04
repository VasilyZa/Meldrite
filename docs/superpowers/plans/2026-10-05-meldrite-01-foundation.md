# Meldrite 合同与基础设施实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 建立可复现 Paper contract、版本边界及能够拒绝虚假兼容声明的测试工具。

**架构：** 中性端口与公共服务脱离 Minecraft 编译；profile保存上游完整合同。一个测试框架服务所有模块，游戏测试以外部JVM扩展。

**技术栈：** Gradle Kotlin DSL、Java17/21、JUnit5、Jackson、ASM、japicmp。

**规格：** [整体架构](../specs/2026-10-05-meldrite-architecture.md)第2–4、13节；[总计划](2026-10-05-meldrite-implementation.md)。

## 全局约束

- 当前实际实现以 Minecraft 1.20.1 为主；从第一天开始预留 Minecraft 1.21.1 支持能力。
- 公共代码不得依赖 Minecraft、Fabric Loader、Mixin、CraftBukkit 或具体 NMS mapping 类型；禁止在公共业务中写 `if version == ...`。
- Paper API 兼容是固定上游构件的 API/ABI contract。API JAR 中存在方法不等于服务端已实现其语义。
- 本计划所有文件以未来独立 Meldrite 仓库为根；本轮不创建源码。

---

## 文件与任务归属

| 路径 | 职责 |
|---|---|
| `settings.gradle.kts`、`build.gradle.kts`、`gradle/libs.versions.toml`、`gradle/verification-metadata.xml` | 子项目、工具链、固定依赖及校验 |
| `build-logic/src/main/kotlin/meldrite.java.gradle.kts` | 通用Java convention与JUnit/fixture任务 |
| `build-logic/src/main/kotlin/meldrite.contract.gradle.kts` | profile lock与合同验证任务 |
| `contracts/locks/` | 精确上游工件和公共依赖锁 |
| `contracts/profiles/` | 符号、行为状态及绑定归属 |
| `testkit/runtime/` | fixture runner与可控时间/假桥支持 |
| `testkit/contract/` | artifact、ABI、依赖与实现绑定检查 |
| `modules/foundation/`、`modules/bridge-spi/`、`modules/plugin-spi/` | 中性值与端口 |

Gradle 项目名与路径绑定：公共模块使用 `:foundation`、`:bridge-spi`、`:plugin-spi`及目录末级名称；testkit使用`:testkit-runtime`、`:testkit-contract`、`:testkit-integration`；API adapter使用`:paper-1201`、`:paper-1211`；versions使用`:v1201-bridge`等前缀；发行物使用`:mc1201`、`:mc1211`。后续所有命令采用这张表。

## 任务 F1：可复现合同与可执行测试框架

**文件：**
- 创建：上述根构建文件、`gradle/wrapper/gradle-wrapper.properties`、`gradlew`、`gradlew.bat`、wrapper JAR。
- 创建：`testkit/runtime/src/main/java/dev/meldrite/testkit/ScenarioDriver.java`、`FixtureSuite.java`。
- 创建：`testkit/contract/src/main/java/dev/meldrite/contract/ArtifactLocker.java`、`LockVerifier.java`。
- 创建：`contracts/lock-input.json`、`contracts/locks/paper-1.20.1.lock.json`、`contracts/locks/tools.lock.json`。
- 测试：`testkit/contract/src/test/java/dev/meldrite/contract/LockContractTest.java`、`LockScenarioDriver.java`。
- 测试资源：`testkit/contract/src/test/resources/cases/f1-lock.json`及 `META-INF/services/dev.meldrite.testkit.ScenarioDriver`。

- [ ] **步骤1：建立最小可运行JUnit入口，写篡改与离线测试。** wrapper起点固定Gradle8.10.2；工具库起点JUnit5.11.4、Jackson2.18.2、ASM9.7.1、mapping-io0.6.1、japicmp0.23.1。它们是实现候选基线，F1必须实际解析、记录SHA和运行，不把本计划当作制品可用性证明。创建runner接口采用总计划第3节；Test调用如下：

```java
@Test void rejectsModifiedArtifacts() throws Exception {
    FixtureSuite.verify("cases/f1-lock.json");
}
```

```json
{"driver":"artifact-lock","input":{"source":"local-maven-fixture","operations":["resolve-snapshot","verify","modify-one-byte","verify","restore","verify-offline"]},"expected":{"uniqueVersionRecorded":true,"original":"PASS","modified":"SHA_MISMATCH","offline":"PASS"}}
```

本地Maven fixture由 `LockScenarioDriver` 临时启动HTTP文件服务器，提供两个snapshot timestamp和POM依赖；禁止单元测试依赖外部仓库实时变化。

- [ ] **步骤2：执行RED。**

```bash
./gradlew :testkit-contract:test --tests '*LockContractTest'
```

预期缺少locker行为时失败；有基本框架后故意只按文件名缓存，确认篡改测试断言失败。

- [ ] **步骤3：按如下规则实现locker与runner，再解析真实合同。**

```text
ArtifactLocker.resolve(coordinate, repository, snapshotPolicy) -> ArtifactLock
ArtifactLock = resolvedVersion + sourceUrl + sha256 + pomHash + dependencyLocks
snapshotPolicy = 只解析指定1.20.1坐标的timestamp/build；排除sources/javadoc classifier
LockVerifier.verify(lock, offlineCache) -> verified paths 或带坐标的失败
FixtureSuite.verify(resource) -> 读取input/expected；调用driver；深度比较actual
```

每个依赖递归解析并固化；禁止动态版本绕过lock。首次真实解析使用官方Paper Maven元数据，存储实际唯一版本与日期；若历史制品不可取得，改用固定官方源码commit重建并记录构建来源，不能用别的MC版本替代。

F1注册根任务 `lockContracts`、`verifyContracts`、`verifyArchitecture`，各Java模块注册 `test`与`fixtureTest`（相同测试集只执行一次）。合同与架构任务可先运行基础lock规则，F2/F3扩展规则，不能空任务返回成功。完整构建以JDK21 daemon运行，公共模块Java17 toolchain并启用`--release 17`。

- [ ] **步骤4：执行GREEN与真实锁验证。**

```bash
./gradlew :testkit-contract:test --tests '*LockContractTest'
./gradlew lockContracts -Pprofile=paper-1.20.1
./gradlew verifyContracts --offline
```

预期local fixture全通过、真实lock含唯一版本与所有SHA、离线无网络仍校验通过。不存在缓存时必须明确失败；不静默联网。

- [ ] **步骤5：提交。**

```bash
git add build.gradle.kts settings.gradle.kts gradle gradlew gradlew.bat build-logic contracts testkit
git commit -m "build: lock contracts and establish fixture testkit"
```

## 任务 F2：中性端口与编译依赖防线

**文件：**
- 创建：`modules/foundation/src/main/java/dev/meldrite/foundation/ResourceKey.java`、`WorldKey.java`、`EntityHandle.java`、`RegistryEpoch.java`、`ItemSnapshot.java`、`CapabilitySet.java`、`Diagnostic.java`。
- 创建：`modules/bridge-spi/src/main/java/dev/meldrite/bridge/BridgeBundle.java`、`ExecutionPort.java`、`RegistryPort.java`、`EventIngressPort.java`、`ActionSnapshot.java`、`ActionDecision.java`、`ItemCodecPort.java`、`NetworkAudiencePort.java`、`WorldPort.java`、`EntityPort.java`、`CommandTreePort.java`、`DataFixPort.java`、`ServerLifecyclePort.java`、`InventoryPort.java`。
- 创建：`modules/plugin-spi/src/main/java/dev/meldrite/plugin/spi/PluginIdentity.java`、`PluginScope.java`、`PluginHost.java`。
- 创建：`testkit/contract/src/main/java/dev/meldrite/contract/ArchitectureVerifier.java`、`contracts/architecture-rules.json`。
- 测试：`testkit/contract/src/test/java/dev/meldrite/contract/ArchitectureContractTest.java`、`ArchitectureScenarioDriver.java`。
- 测试资源：`testkit/contract/src/test/resources/cases/f2-boundary.json`。

- [ ] **步骤1：写实际classfile反例。** driver用JavaCompiler在临时目录编译：正常中性值、字段含NMS类型、方法描述符含Craft类型、注解含Fabric类型、常量字符串含NMS；这些是假包测试源，不下载Minecraft。

```java
@Test void rejectsLeakedGameTypes() throws Exception {
    FixtureSuite.verify("cases/f2-boundary.json");
}
```

```json
{"driver":"architecture","input":{"fixtures":["neutral","nms-field","craft-descriptor","fabric-annotation","nms-string"],"module":"scheduler"},"expected":{"neutral":"PASS","nms-field":"FORBIDDEN_REFERENCE","craft-descriptor":"FORBIDDEN_REFERENCE","fabric-annotation":"FORBIDDEN_REFERENCE","nms-string":"FORBIDDEN_REFERENCE"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-contract:test --tests '*ArchitectureContractTest'`；先只检测import时，classfile反例必须失败。

- [ ] **步骤3：定义端口最小合同与扫描规则。**

```text
ResourceKey(registryNamespace, registryPath, entryNamespace, entryPath)
EntityHandle(WorldKey world, UUID uuid, long generation)
RegistryEpoch(String scope, long revision)
ItemSnapshot(ResourceKey type, int count, String codecId, byte[] opaquePayload)
ExecutionPort.isPrimaryThread(); executePrimary(Runnable); currentTick()
RegistryPort.resolve(ResourceKey, RegistryEpoch); snapshot(scope); lifecycle(registry)
EventIngressPort.before(ActionSnapshot) -> ActionDecision
ItemCodecPort.decode(byte[]) -> ItemSnapshot; encode(ItemSnapshot) -> byte[]
PluginScope.register(AutoCloseable); close(); isClosed()
```

`ActionSnapshot/ActionDecision`由S4正式定义，F2仅建立端口文件时一并创建其最小值类型（token、kind、immutable payload；decision包含cancel和修改值），禁止直接使用Object/NMS escape hatch。其余端口用规格第4节的单职责定义一组实际首条用例需要的方法；未用到的游戏操作不先造通用反射调用口。`ItemSnapshot`复制可变byte数组；句柄和Key禁止null，entry key不可用数字ID替代。

ArchitectureVerifier扫描ASM可达类型引用与受限制字符串；将模块图也纳入验证。profile/version/tooling的例外按模块+用途列出，禁止全仓库豁免某个包。

- [ ] **步骤4：运行GREEN并证明双目标编译。**

```bash
./gradlew :testkit-contract:test --tests '*ArchitectureContractTest'
./gradlew :foundation:test :bridge-spi:test verifyArchitecture
```

增加byte-array defensive copy、stale generation、非法key反例；让fake BridgeBundle在Java17与Java21测试JVM中运行同一用例。尚不下载或启动Minecraft。

- [ ] **步骤5：提交。**

```bash
git add modules/foundation modules/bridge-spi modules/plugin-spi testkit/contract contracts/architecture-rules.json
git commit -m "feat: define neutral ports and enforce architecture boundaries"
```

## 任务 F3：完整ABI inventory、绑定与功能状态审计

**文件：**
- 创建：`testkit/contract/src/main/java/dev/meldrite/contract/ApiInventory.java`、`BindingVerifier.java`、`ProfileAdapterGenerator.java`、`CapabilityWorklist.java`。
- 创建：`contracts/profiles/paper-1.20.1/api-index.json`、`bindings.json`、`capabilities.json`、`lifecycle.json`。
- 创建：`contracts/schemas/bindings.schema.json`、`capabilities.schema.json`。
- 测试：`testkit/contract/src/test/java/dev/meldrite/contract/ApiInventoryContractTest.java`、`InventoryScenarioDriver.java`。
- 测试资源：`testkit/contract/src/test/resources/cases/f3-inventory.json`。

- [ ] **步骤1：写小型上游API fixture及缺陷实现。** fixture覆盖interface default/static、继承抽象方法、public/protected field、泛型、注解、enum和checked exception元数据；编译好的API文件作为输入，不能用手写JSON假装提取成功。

```java
@Test void distinguishesSurfaceFromImplementation() throws Exception {
    FixtureSuite.verify("cases/f3-inventory.json");
}
```

```json
{"driver":"api-inventory","input":{"mutations":["remove-method","change-descriptor","lose-inherited-method","unsupported-no-reason","supported-no-scenario","empty-body-mark-supported"]},"expected":{"errors":["MISSING_SYMBOL","DESCRIPTOR_CHANGED","MISSING_BINDING","INVALID_STATUS","MISSING_EVIDENCE","MISSING_BEHAVIOR_EVIDENCE"]}}
```

最后一种不是静态分析能判定任意空实现：driver创建一个有实际语义断言的fixture，故意返回默认值使该测试失败，再验证发布审计拒绝其缺少成功证据。

- [ ] **步骤2：运行RED。** `./gradlew :testkit-contract:test --tests '*ApiInventoryContractTest'`；仅比较API JAR时实现缺失反例必须失败。

- [ ] **步骤3：实现提取、绑定与可审查工作清单。**

```text
symbolId = owner + memberName + JVM descriptor + kind
ApiInventory.extract(locked API jar, public dependencies) -> canonical sorted inventory
provenance = API_PUBLIC | API_INTERNAL_SUPPORT | TRANSITIVE_API | SERVER_IMPLEMENTATION
Binding = symbolId + adapterFile + serviceOwner + capability + status + evidenceIds
supported -> 至少一个通过的行为场景；unsupported -> reason + failure contract
ProfileAdapterGenerator -> 精确签名、委托点和UNSUPPORTED显式抛错；不生成伪行为
CapabilityWorklist -> 按领域/相同行为聚类，列实际symbolId、文件、前置和场景ID
```

完整提取真实Paper1.20.1合同，公共API artifact中的所有包都按来源识别，不按`io.papermc`前缀排除。`API_PUBLIC`是插件可调用面；`API_INTERNAL_SUPPORT`是API JAR内标记内部使用但运行握手所需的符号，仍需链接/握手测试，但不据此承诺插件内部兼容；`TRANSITIVE_API`保留上游公共依赖来源及类身份；`SERVER_IMPLEMENTATION`单独从上游server/Craft制品提取，只用于C1/C3精确目标和兼容边界。API自身具体/default实现和第三方公共依赖不重复实现。分类功能状态，默认UNSUPPORTED而非SUPPORTED；未绑定/未分类符号数必须为0，支持率可以低。

生成器只覆盖确定的签名转发；需要具体Craft返回类型的工厂在S1/C1处理。输出所有领域工作清单供S7执行；不是遇到插件错误才发现API存在。

- [ ] **步骤4：运行GREEN和完整合同检查。**

```bash
./gradlew :testkit-contract:test --tests '*ApiInventoryContractTest'
./gradlew verifyContracts -Pprofile=paper-1.20.1
```

预期报告分别显示surface差异、绑定遗漏、支持/部分/未支持数量和证据失效；任何一个支持项缺测试证据则失败。将一项SUPPORTED绑定删掉，确认命令非0，再恢复。

- [ ] **步骤5：提交。**

```bash
git add testkit/contract contracts/profiles contracts/schemas
git commit -m "feat: audit complete API surface and implementation bindings"
```
