# Meldrite 回归发布与 1.21.1 扩展实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 建立持续兼容回归与诚实发布门禁，并在不复制公共系统的前提下增加Minecraft1.21.1。

**架构：** 同一fixture同时驱动官方Paper与Meldrite，差分报告映射到contract inventory。第二版本只增加profile/version/distribution和必要端口能力，不创建第二套Runtime。

**技术栈：** Java17/21、Gradle、外部JVM测试、固定API/插件制品、JUnit5、ASM、结构化轨迹。

**规格：** [整体架构](../specs/2026-10-05-meldrite-architecture.md)第13–15节；[总计划](2026-10-05-meldrite-implementation.md)。

## 全局约束

- 当前实际实现以 Minecraft 1.20.1 为主；从第一天开始预留 Minecraft 1.21.1 支持能力。
- 公共 Runtime、ClassLoader 算法、事件派发、Scheduler、Adventure 服务、Registry 映射和 Craft delegate 只保留一套实现。
- 公共代码不得依赖 Minecraft、Fabric Loader、Mixin、CraftBukkit 或具体 NMS mapping 类型；禁止在公共业务中写 `if version == ...`。
- 每个兼容声明必须同时指向 contract、功能状态和回归证据；任何生产关键 hook 缺失都阻止该发行版进入 READY。
- 测试计划、未运行的命令和子代理报告均不能代替实际通过证据。

---

## 文件归属

- `testkit/integration/`：进程、场景、轨迹、差分、结果汇总。
- `contracts/regression/`：合法取得且固定hash的插件、Mod、配置和场景锁。
- `.github/workflows/`：静态、无MC行为、真服、双版本与发布门禁。
- `adapters/paper-1.21.1/`、`versions/v1_21_1/`、`distributions/mc-1.21.1/`：只允许真实版本差异。
- `docs/compatibility/`：可公开的范围、矩阵和限制，由验证产物生成。

## 任务 Q1：官方Paper差分与插件回归runner

**文件：**
- 扩展：V1的 `testkit/integration/src/main/java/dev/meldrite/integration/ServerProcess.java`、`ScenarioResult.java`。
- 创建：`testkit/integration/src/main/java/dev/meldrite/integration/TraceNormalizer.java`、`DifferentialRunner.java`、`PluginMatrixRunner.java`。
- 创建：`contracts/regression/servers.lock.json`、`plugins.lock.json`、`mods.lock.json`、`scenarios.json`、`allowed-differences.json`。
- 创建：`testkit/fixtures/api-probe/src/main/java/fixture/ApiProbePlugin.java`、`src/main/resources/plugin.yml`。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/DifferentialContractTest.java`、`DifferentialScenarioDriver.java`、`src/test/resources/cases/q1-differential.json`。

- [ ] **步骤1：构造会被误判的轨迹测试。**

```java
@Test void ignoresOnlyNondeterminismAndDetectsRealSemanticDrift() throws Exception {
    FixtureSuite.verify("cases/q1-differential.json");
}
```

```json
{"driver":"differential","input":{"traceMutations":["timestamp-only","cancelled-drop-created","event-thread-changed","listener-order-changed","unsupported-returned-null","plugin-enabled-but-command-fails"]},"expected":{"timestamp-only":"EQUIVALENT","cancelled-drop-created":"DIFFERENT","event-thread-changed":"DIFFERENT","listener-order-changed":"DIFFERENT","unsupported-returned-null":"DIFFERENT","plugin-enabled-but-command-fails":"FUNCTIONAL_FAILURE"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-integration:test --tests '*DifferentialContractTest'`；只比较启动日志或仅把插件enabled视为通过的实现应失败。

- [ ] **步骤3：实现外部进程差分并注册integrationTest任务。**

```text
每次场景独立临时目录/端口/进程；固定种子、输入、插件hash、配置与目标server hash
trace = scenario + sequence + phase + thread role + event/operation + normalized result
normalizer只处理已列白名单的时间戳/随机identity；保留顺序、取消、线程、异常与实际状态
DifferentialRunner比较官方Paper和Meldrite同profile结果；Mod场景以Fabric+投影规范作oracle
PluginMatrixRunner分别记录DISCOVERY/LOAD/ENABLE/FUNCTIONAL/SHUTDOWN，不合并成单个“能运行”
```

真实插件以小而代表性的集合开始：权限、命令、Adventure、保护事件、配置/数据、Craft访问、NMS/反射。每个制品在lock中有许可证/取得位置、hash和支持MC；没有合法固定制品就使用自有fixture，不自动下载来历不明jar。EULA配置由操作方显式提供，runner不自行写接受条款。

根任务`integrationTest`委托`:testkit-integration:integrationTest`，参数`-Pmc`、`-Pprofile`、`-Pmodset`、`-Pscenario`都被校验；未知参数值失败，不能静默跑默认版本。

- [ ] **步骤4：运行GREEN和首条真实差分。**

```bash
./gradlew :testkit-integration:test --tests '*DifferentialContractTest'
./gradlew integrationTest -Pmc=1.20.1 -Pprofile=paper-1.20.1 -Pmodset=clean -Pscenario=api-probe
./gradlew integrationTest -Pmc=1.20.1 -Pprofile=paper-1.20.1 -Pmodset=content-fixture -Pscenario=native-data
```

测得差异逐条归因为实现错误、profile允许差异或显式unsupported；禁止把整类事件/异常列入ignore。原始日志与规范化轨迹同时保留。
- [ ] **步骤5：提交。**

```bash
git add testkit/integration testkit/fixtures/api-probe contracts/regression build-logic
git commit -m "test: compare plugin semantics against fixed Paper baselines"
```

## 任务 Q2：API、hook、插件矩阵与发布门禁

**文件：**
- 创建：`testkit/contract/src/main/java/dev/meldrite/contract/ReleaseVerifier.java`、`CompatibilityReport.java`。
- 创建：`contracts/release-policy.json`、`distributions/mc-1.20.1/required-capabilities.json`。
- 创建：`.github/workflows/contracts.yml`、`runtime.yml`、`integration.yml`、`release.yml`。
- 创建：`docs/compatibility/1.20.1.md`、`docs/compatibility/limitations.md`、`docs/adr/0001-version-boundaries.md`、`0002-registry-projection.md`、`0003-internal-access.md`。
- 测试：`testkit/contract/src/test/java/dev/meldrite/contract/ReleaseContractTest.java`、`ReleaseScenarioDriver.java`、`src/test/resources/cases/q2-release.json`。

- [ ] **步骤1：用过期/缺失证据测试发布器。**

```java
@Test void rejectsUnsupportedCompatibilityClaims() throws Exception {
    FixtureSuite.verify("cases/q2-release.json");
}
```

```json
{"driver":"release-verifier","input":{"mutations":["missing-api-binding","missing-required-hook","stale-plugin-results","changed-modset","changed-mapping","partial-feature-claimed-complete","missing-source-license"]},"expected":{"allRejected":true,"reportIncludesReasons":true,"unrelatedOptionalUnsupportedDoesNotBecomeSupported":true}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-contract:test --tests '*ReleaseContractTest'`；使用旧commit绿色报告给新artifact放行时必须失败。

- [ ] **步骤3：绑定制品与证据，建立流水线。**

```text
release key = 源码commit + distribution hash + API lock + mappings + plugins/modset + config policy
required-capabilities定义本次发布目标；任何required为PARTIAL/UNSUPPORTED都拒绝
全Paper兼容声明需要完整相关行为contract达标；受限发布必须自动附完整限制列表
hook runtime report/ABI report/functional matrix/stop report均匹配同一release key
收集SBOM、每文件来源许可、patch来源、下载和映射分发要求、可复现构建信息
```

流水线：每PR跑contract与公共单测；版本模块变更跑该版本真服；共享模块变更跑已支持双版本；发布跑完整矩阵。性能基线同机重复测量冷/热加载、remap cache、tick/event/registry操作，失败阈值从已接受基线配置读取；初次基线只记录，不能伪称性能无回归。

故障恢复测试：配置坏、plugin enable失败、异步任务卡住、reload失败、缓存损坏、关闭时异常；均不得损坏原插件JAR或原世界。发布工具只构建/验证制品，真正对外发布由另一次明确动作执行。

- [ ] **步骤4：运行GREEN。**

```bash
./gradlew :testkit-contract:test --tests '*ReleaseContractTest'
./gradlew verifyArchitecture verifyContracts
./gradlew integrationTest -Pmc=1.20.1 -Pprofile=paper-1.20.1 -Pmodset=all-locked -Pscenario=release
./gradlew verifyRelease -Pdistribution=mc-1.20.1
```

预期声明与事实一致；未达标功能保持限制且阻止相应“完整兼容”门禁。输出公开兼容文档必须写MC/profile/插件版本/hash与测试日期。
- [ ] **步骤5：提交。**

```bash
git add testkit/contract contracts/release-policy.json distributions/mc-1.20.1 .github docs/compatibility docs/adr build-logic
git commit -m "ci: gate releases on current compatibility evidence"
```

## 任务 Q3：第二Paper合同与API差异适配

**文件：**
- 创建：`contracts/locks/paper-1.21.1.lock.json`、`contracts/profiles/paper-1.21.1/api-index.json`、`bindings.json`、`capabilities.json`、`lifecycle.json`、`plugin-lifecycle.json`、`api-delta.json`。
- 创建：`adapters/paper-1.21.1/src/main/java/dev/meldrite/paper1211/ProfileFactory.java`、`EventFactory.java`、`PaperProviderAdapter.java`、`CommandLifecycleAdapter.java`、`RegistryAdapter.java`。
- adapter中其他精确接口类由F3 generator列到bindings的adapterFile；公共算法不生成复制。
- 测试：`adapters/paper-1.21.1/src/test/java/dev/meldrite/paper1211/ProfileDeltaContractTest.java`、`ProfileDeltaScenarioDriver.java`、`src/test/resources/cases/q3-profile-delta.json`。

- [ ] **步骤1：锁定真实1.21.1 API并生成差分测试。**

```java
@Test void adaptsTheNewContractWithoutDuplicatingPublicServices() throws Exception {
    FixtureSuite.verify("cases/q3-profile-delta.json");
}
```

```json
{"driver":"profile-delta","input":{"oldProfile":"paper-1.20.1","newProfile":"paper-1.21.1","checks":["event-constructors","plugin-bootstrap","command-lifecycle","registry-api","adventure-public-dependencies"]},"expected":{"unclassifiedDeltas":0,"newRequiredSymbolsUnbound":0,"commonGameReferences":0,"commonServiceImplementationsPerCapability":1,"fakeBridgeScenariosPass":true}}
```

- [ ] **步骤2：运行RED。** `./gradlew :paper-1211:test --tests '*ProfileDeltaContractTest'`；把1.20.1 adapter直接拿来声称1.21.1实现时，应出现真实新增/变更符号失败。

- [ ] **步骤3：逐项处置API delta。**

```text
差分分为新增/删除/签名/继承/默认实现/语义/公共依赖；每项指向adapterFile或公共服务变更
API family专用类型、事件构造和lifecycle registration放paper-1211
可复用规则放api-services/events/commands等原模块，只实现一次
common仍--release17；其Adventure等公共依赖符号必须在两个profile均存在
1.21.1 API adapter按Java21编译；禁止Java17运行时误加载其classfile
```

只把官方1.21.1确有的API纳入合同，不把最新文档中后加的registry/command接口倒灌。fake bridge在没有1.21.1游戏实现时即可运行Runtime、事件、Scheduler、Adventure和registry公共套件。

- [ ] **步骤4：运行GREEN。**

```bash
./gradlew :paper-1211:test --tests '*ProfileDeltaContractTest'
./gradlew verifyContracts -Pprofile=paper-1.21.1
./gradlew :plugin-runtime:test :events:test :scheduler:test :adventure:test :registry:test verifyArchitecture
```

未支持新功能可以诚实标unsupported，但不能留下漏分类/漏绑定；新profile不能把旧profile支持项无理由降级。
- [ ] **步骤5：提交。**

```bash
git add contracts/locks/paper-1.21.1.lock.json contracts/profiles/paper-1.21.1 adapters/paper-1.21.1 modules
git commit -m "feat: adapt the Paper 1.21.1 API contract"
```

`modules`只暂存本任务必要公共能力变更，执行前核对diff；不把无关并行工作一起提交。

## 任务 Q4：1.21.1版本实现、Data Components与Craft/mappings

**文件：**
- 创建：`versions/v1_21_1/bootstrap/src/main/java/dev/meldrite/v1211/bootstrap/MeldriteInitializer.java`、`VersionComposition.java`、`src/main/resources/fabric.mod.json`。
- 创建：`versions/v1_21_1/bridge/src/main/java/dev/meldrite/v1211/bridge/RegistryBridge.java`、`ItemComponentCodec.java`、`AudienceNetworkBridge.java`、`LifecycleBridge.java`、`ExecutionBridge.java`、`HookGateway.java`、`BlockBreakBridge.java`、`CommandBridge.java`、`NativeDataFixBridge.java`。
- 创建：`versions/v1_21_1/hooks/src/main/resources/meldrite.mixins.json`、`hook-manifest.json`、`meldrite.accesswidener`；每个hook的源文件由该manifest明确列出。
- 创建：`versions/v1_21_1/craft-bindings/craft-abi.json`、`src/main/java/dev/meldrite/v1211/craft/CraftFacadeFactory.java`与闭包内无版本 `org/bukkit/craftbukkit/`类。
- 创建：`versions/v1_21_1/mappings/mapping-lock.json`、`namespace-graph.json`、`craft-aliases.json`、`versions/v1_21_1/toolchain-lock.json`。
- 创建：`distributions/mc-1.21.1/distribution.json`、`required-capabilities.json`。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/Version1211ContractTest.java`、`Version1211ScenarioDriver.java`、`src/test/resources/cases/q4-version1211.json`。

这是按V1启动→V2动作→V3数据→C1/C2内部访问拆分的四个审查批次，每批依下列RED/GREEN步骤单独提交；不能一次性从v1_20_1复制目录再改版本号。

- [ ] **步骤1：先用1.21.1运行同一行为fixture。**

```java
@Test void runsSharedScenariosWithNativeComponentsAndNewBindings() throws Exception {
    FixtureSuite.verify("cases/q4-version1211.json");
}
```

```json
{"driver":"version-1211","input":{"mc":"1.21.1","java":21,"scenarios":["bootstrap","block-break","native-data","craft-abi","nms-access"],"itemPayload":"custom-component+unknown-mod-data"},"expected":{"sharedScenarioFailures":0,"unknownComponentsPreserved":true,"canonicalCraftPrefix":"org.bukkit.craftbukkit","legacyAliasUsesSameFacade":true,"commonRuntimeCopies":1,"apiOnlyRemapCalls":0}}
```

- [ ] **步骤2：执行RED。** `./gradlew :testkit-integration:test --tests '*Version1211ContractTest' -Pmc=1.21.1`；把旧NBT直接当新ItemStack格式或使用旧hook描述符时必须失败。

- [ ] **步骤3：实现只属于1.21.1的差异。**

```text
锁定MC1.21.1+Java21+独立Loader/Fabric API/Loom/Yarn组合，并用实际构建启动验证
RegistryBridge声明该版真实registry生命周期；ItemComponentCodec保留组件类型/值与Mod未知数据
新hooks使用该版实际owner/descriptor并保持薄入口，业务调用原模块
canonical Craft bindings用未版本化包；v1_21_R1只作为legacy输入alias映射到同一类身份
Mojang/Spigot source -> 当前Fabric runtime namespace；未声明Paper新增NMS成员仍拒绝
```

可采用候选Fabric API `0.102.1+1.21.1`、Yarn `1.21.1+build.3`开始实际解析，其余工具优先复用V1已验证组合；遇到不兼容以独立toolchain lock解决，不把工具版本判断写进业务。组件处理与原NBT处理是两个原生codec，共享的是ItemSnapshot/无损规则和业务测试。

- [ ] **步骤4：执行GREEN及旧版回归。**

```bash
./gradlew :mc1211:assemble verifyArchitecture
./gradlew :testkit-integration:test --tests '*Version1211ContractTest' -Pmc=1.21.1
./gradlew integrationTest -Pmc=1.20.1 -Pprofile=paper-1.20.1 -Pmodset=all-locked -Pscenario=release
./gradlew integrationTest -Pmc=1.21.1 -Pprofile=paper-1.21.1 -Pmodset=all-locked -Pscenario=release
```

确认目录中没有复制的PluginController/EventCoordinator/TickScheduler/AudienceService；签名转发等机械重复只允许profile生成代码。对未支持的新语义保持PARTIAL并阻止相关发布目标。
- [ ] **步骤5：每个批次提交。**

```bash
git add versions/v1_21_1 distributions/mc-1.21.1 testkit/integration build-logic settings.gradle.kts
git commit -m "feat: add a verified Minecraft 1.21.1 version implementation"
```

## 任务 Q5：双版本发布、存档边界与扩展验收

**文件：**
- 创建：`docs/compatibility/1.21.1.md`、`docs/operations/world-upgrade.md`、`docs/architecture/version-reuse-report.md`。
- 修改：`.github/workflows/integration.yml`、`release.yml`、`contracts/regression/scenarios.json`。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/DualVersionContractTest.java`、`DualVersionScenarioDriver.java`、`src/test/resources/cases/q5-dual-version.json`。

- [ ] **步骤1：写两套独立发行物与世界副本测试。**

```java
@Test void isolatesDistributionsAndNeverMutatesTheOriginalWorldDuringUpgrade() throws Exception {
    FixtureSuite.verify("cases/q5-dual-version.json");
}
```

```json
{"driver":"dual-version","input":{"distributions":["mc-1.20.1","mc-1.21.1"],"operations":["verify-common-artifacts","upgrade-copy-of-vanilla-world","try-missing-mod-codec","try-downgrade","run-both-release-gates"]},"expected":{"sharedCommonCode":true,"onlyOneProfilePerDistribution":true,"originalWorldUnchanged":true,"missingModCodec":"ABORT_WITH_COPY_RETAINED","downgrade":"UNSUPPORTED","reportsSeparatedByVersion":true}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-integration:test --tests '*DualVersionContractTest'`；在原世界就地试升级、混装两版profile或把同一个兼容报告用于两版时必须失败。

- [ ] **步骤3：落实版本复用与发布记录。**

```text
复用报告列每个common artifact的源码commit、字节码target、两发行中的hash或必要依赖适配差异
版本特定文件仅在adapters/versions/contracts-profile/distribution/fixture范围，例外逐条解释
世界升级先停服->备份校验->副本迁移->实际启动/验证；成功前不替换原世界
带Mod世界还必须验证目标Mod/codec齐全；缺失则保留副本与原始文件并拒绝自动迁移
1.20.1/1.21.1各有完整manifest、功能状态、插件矩阵、hook报告和rollback说明
```

不承诺跨MC二进制NMS插件通用；普通插件也需按各profile行为测试。发布文档清楚列出哪些插件JAR可共用、哪些需目标版本重编译、哪些属于D级不支持。维护新版本不能削弱旧版本所需核心套件。

- [ ] **步骤4：运行GREEN并验收“新增版本未复制核心”。**

```bash
./gradlew :testkit-integration:test --tests '*DualVersionContractTest'
./gradlew verifyRelease -Pdistribution=mc-1.20.1
./gradlew verifyRelease -Pdistribution=mc-1.21.1
```

最终审查规格第14节复用表逐行与实际文件对应；公共模块只有一套源码，两个JDK测试通过，两个版本报告独立、有效且与制品hash一致。
- [ ] **步骤5：提交。**

```bash
git add docs/compatibility docs/operations docs/architecture .github/workflows contracts/regression testkit/integration
git commit -m "docs: publish verified dual-version compatibility boundaries"
```

该提交生成可发布结果；本计划不隐含推送、创建release或对外发布。
