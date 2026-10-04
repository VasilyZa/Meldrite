# Meldrite Craft、Remapper 与兼容扩展实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 提供有边界、可选择、可回归的内部访问兼容，保证普通API插件不承担NMS转换成本。

**架构：** Craft精确ABI属于version binding，NMS转换属于纯remap引擎，插件特例属于compat pack。三者分别验收，映射失败不能退回随意暴露服务端内部类。

**技术栈：** Java17、ASM、mapping-io、锁定上游Craft/Paper源码与MC映射、F1/Q1测试工具。

**规格：** [整体架构](../specs/2026-10-05-meldrite-architecture.md)第2、3.2、11–12节；[总计划](2026-10-05-meldrite-implementation.md)。

## 全局约束

- 普通 API 插件不经过 NMS 重映射；同版本 NMS 映射不承诺把其他 Minecraft 版本的插件升级过来。
- 公共 Runtime、ClassLoader 算法、事件派发、Scheduler、Adventure 服务、Registry 映射和 Craft delegate 只保留一套实现。
- 每个兼容声明必须同时指向 contract、功能状态和回归证据；任何生产关键 hook 缺失都阻止该发行版进入 READY。
- Compatibility pack不得通过核心中的插件名条件触发，也不能向普通插件打开整个Knot。

---

## 文件归属

`versions/v1_20_1/craft-bindings/`拥有精确Craft类和typed NMS签名；`versions/v1_20_1/mappings/`拥有映射数据；`modules/remap-engine/`拥有通用转换算法；`modules/compatibility-spi/`和`compatibility-engine/`拥有治理；`compat-packs/`拥有具体例外。输入插件JAR永不原地覆盖。

## 任务 C1：最小Craft ABI闭包与统一wrapper

**文件：**
- 创建：`versions/v1_20_1/craft-bindings/craft-abi.json`、`src/main/java/dev/meldrite/v1201/craft/CraftFacadeFactory.java`。
- 创建：`versions/v1_20_1/craft-bindings/src/main/java/org/bukkit/craftbukkit/v1_20_R1/CraftServer.java`、`CraftWorld.java`。
- 创建：`versions/v1_20_1/craft-bindings/src/main/java/org/bukkit/craftbukkit/v1_20_R1/entity/CraftEntity.java`、`CraftLivingEntity.java`、`CraftHumanEntity.java`、`CraftPlayer.java`。
- 夹具：`testkit/fixtures/craft-access/src/main/java/fixture/CraftAccessPlugin.java`、`src/main/resources/plugin.yml`。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/CraftAbiContractTest.java`、`CraftAbiScenarioDriver.java`、`src/test/resources/cases/c1-craft.json`。

- [ ] **步骤1：用固定上游开发依赖编译真实Craft插件。** 访问白名单内getHandle与继承关系，将普通Bukkit获取对象强转Craft；fixture自己不能依赖Meldrite实现classfile编译，否则无法发现ABI伪兼容。

```java
@Test void returnsOneFacadeWithExactCraftDescriptors() throws Exception {
    FixtureSuite.verify("cases/c1-craft.json");
}
```

```json
{"driver":"craft-abi","input":{"fixture":"craft-access","profile":"paper-1.20.1","operations":["bukkit-to-craft-cast","check-superclasses","invoke-getHandle","lookup-again","invoke-unlisted-member"]},"expected":{"castsPass":true,"declaredHierarchyMatches":true,"handleDescriptorMatches":true,"sameFacadeIdentity":true,"unlistedMember":"UNSUPPORTED_INTERNAL_SYMBOL"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-integration:test --tests '*CraftAbiContractTest' -Pmc=1.20.1`；只有同名方法但返回Object、CraftPlayer继承层级错误、返回另一套wrapper，都必须失败。

- [ ] **步骤3：生成白名单的继承/描述符闭包并实现bindings。**

```text
从上游固定Craft ABI提取所选类及必要父类/接口/返回类型，写入craft-abi.json
外部可见FQCN/继承/字段/构造/getHandle descriptor必须精确；native target按运行namespace绑定
Craft facade以组合调用S1 delegate；需要继承Craft层级时不直接改成PlayerAdapter父类
字段若允许外部写入，必须定义同步原生状态的语义；无法实现者不进入支持白名单
由唯一FacadeFactory返回Craft对象，Bukkit视角和Craft视角共享identity
```

其他Craft类逐条扩大白名单并附fixture；不能用`Object getHandle()`伪装二进制兼容。无法解析成员在加载扫描可提前拒绝；动态调用未支持成员仍可能抛标准linkage异常，诊断层记录符号，不假装静态扫描能覆盖全部反射。

- [ ] **步骤4：运行GREEN。** `./gradlew :testkit-integration:test --tests '*CraftAbiContractTest' -Pmc=1.20.1`，再运行`:craft-common:test verifyContracts verifyArchitecture`。记录此时支持的具体Craft成员，不把类名存在计作该类所有成员支持。
- [ ] **步骤5：提交。**

```bash
git add versions/v1_20_1/craft-bindings testkit/fixtures/craft-access testkit/integration
git commit -m "feat: provide verified versioned Craft ABI bindings"
```

## 任务 C2：映射图、选择式转换与可验证缓存

**文件：**
- 创建：`modules/remap-engine/src/main/java/dev/meldrite/remap/PluginRequirements.java`、`NamespaceGraph.java`、`MappingBundle.java`、`RemapPlanner.java`、`BytecodeRemapper.java`、`RemapCache.java`。
- 创建：`versions/v1_20_1/mappings/mapping-lock.json`、`namespace-graph.json`、`craft-aliases.json`。
- 修改：R2 `TransformContext`和ClassTransform实现注册，仅在internal-access route启用。
- 测试：`modules/remap-engine/src/test/java/dev/meldrite/remap/RemapContractTest.java`、`RemapScenarioDriver.java`、`src/test/resources/cases/c2-remap.json`。
- 夹具：`testkit/fixtures/nms-access/src/main/java/fixture/NmsPlugin.java`、`src/main/resources/plugin.yml`。

- [ ] **步骤1：写合成classfile golden与实际插件两种测试。** 合成类包含继承重载、数组、泛型签名、method handle、lambda/indy；bytecode verifier和定义后的真实调用结果一起检查。

```java
@Test void remapsOnlyInternalAccessAndInvalidatesChangedMappings() throws Exception {
    FixtureSuite.verify("cases/c2-remap.json");
}
```

```json
{"driver":"remap","input":{"plugins":["api-only","spigot-nms-same-mc","mojang-nms-same-mc","wrong-mc"],"target":"intermediary","cacheMutations":["none","mapping-hash","hierarchy-hash","transformer-version","truncated-cache"]},"expected":{"apiOnlyTransforms":0,"apiOnlyBytesIdentical":true,"spigotResult":"LINKS_AND_RUNS","mojangResult":"LINKS_AND_RUNS","wrongMc":"REJECTED","cacheStatuses":["HIT","MISS","MISS","MISS","REBUILD"]}}
```

- [ ] **步骤2：运行RED。** `./gradlew :remap-engine:test --tests '*RemapContractTest'`；只换类名不解析继承成员时运行fixture必须失败。

- [ ] **步骤3：实现需求判定和纯转换器。**

```text
PluginRequirements.scan(originalJar, descriptor, manifest, explicitPolicy) -> api/internal/unknown需求
RemapPlanner.choose(requirements, targetNamespace, graph) -> IDENTITY 或 source->target plan
NamespaceGraph每条边带固定hash/MC；找不到同MC完整路径即拒绝
BytecodeRemapper转换owner/name/descriptor/继承引用/签名/handles；不加载Minecraft类
RemapCache key含原JAR SHA、profile、MC、全部mapping/继承/transformer/compat版本及classfile策略
写临时文件->校验->原子rename；读取损坏缓存重建；输入永不修改
```

API-only路径不查询mapping bundle；静态分析不完备，unknown反射使用明确策略，不自动对所有插件remap。1.21.1 Mojang manifest在Fabric上仍需到intermediary的转换。单独的legacy Bukkit API migration在调用链上有独立标记和测试，不混淆NMS计数。

- [ ] **步骤4：运行GREEN与生产namespace测试。**

```bash
./gradlew :remap-engine:test --tests '*RemapContractTest'
./gradlew :testkit-integration:test --tests '*CraftAbiContractTest' -Pmc=1.20.1 -PruntimeNamespace=intermediary
./gradlew :plugin-classloading:test verifyArchitecture
```

集成driver同时加载本任务`nms-access` fixture；验证声明为NMS的插件确实调用到游戏效果。还需类重复、未知namespace、残缺mapping与mapping版本不一致负面用例。
- [ ] **步骤5：提交。**

```bash
git add modules/remap-engine modules/plugin-spi modules/plugin-classloading versions/v1_20_1/mappings testkit/fixtures/nms-access testkit/integration
git commit -m "feat: isolate namespace remapping from ordinary plugin loading"
```

## 任务 C3：反射边界、签名制品与Paper internals拒绝

**文件：**
- 创建：`modules/remap-engine/src/main/java/dev/meldrite/remap/ReflectionRewritePolicy.java`、`InternalSymbolVerifier.java`、`JarSignaturePolicy.java`。
- 创建：`contracts/profiles/paper-1.20.1/internal-access-policy.json`。
- 测试：`modules/remap-engine/src/test/java/dev/meldrite/remap/InternalAccessContractTest.java`、`InternalAccessScenarioDriver.java`、`src/test/resources/cases/c3-internals.json`。

- [ ] **步骤1：写名字存在但行为不存在的反例。** fixture引用一个Paper补丁添加的原生成员，目标Fabric classfile中没有该成员；符号从固定来源提取，不凭手写虚构归类。

```java
@Test void neverTreatsNameMappingAsPaperInternalImplementation() throws Exception {
    FixtureSuite.verify("cases/c3-internals.json");
}
```

```json
{"driver":"internal-access","input":{"cases":["constant-class-forName","dynamic-concatenation","paper-added-nms-member","signed-transform-needed","public-paper-api-type"]},"expected":{"constant-class-forName":"SUPPORTED_PATTERN_REWRITTEN","dynamic-concatenation":"UNSUPPORTED_REFLECTION_PATTERN","paper-added-nms-member":"MISSING_TARGET_MEMBER","signed-transform-needed":"SIGNED_ARTIFACT_TRANSFORM_REJECTED","public-paper-api-type":"API_CONTRACT"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :remap-engine:test --tests '*InternalAccessContractTest'`；按`io.papermc.paper`前缀全部拒绝或仅映射名字即判支持都会失败。

- [ ] **步骤3：实现独立反射pass及符号校验。**

```text
InternalSymbolVerifier以API inventory判公共符号，以目标classfile+继承图判原生成员
ReflectionRewritePolicy只接受已声明的调用模式与namespace；动态字符串不猜测
每次rewrite输出原/目标symbol与policy id；未知模式留明确拒绝/边界记录
JarSignaturePolicy在变换前检测签名；需变换的签名JAR默认拒绝，不删签名重包装
```

静态无法识别的动态访问可以运行到标准异常，错误报告保留原始cause；“UNSUPPORTED_REFLECTION_PATTERN”不能宣称扫描已识别所有动态反射。上面的动态fixture是可识别的已知反例。任何Paper internals支持都必须转成C4可追踪扩展而非在通用remapper硬写插件特例。

- [ ] **步骤4：运行GREEN。** `./gradlew :remap-engine:test :plugin-classloading:test verifyContracts`；确认API-only签名JAR无需变换可按正常签名校验加载，且原文件hash不变。
- [ ] **步骤5：提交。**

```bash
git add modules/remap-engine contracts/profiles/paper-1.20.1
git commit -m "feat: enforce explicit reflection and Paper internals boundaries"
```

## 任务 C4：Compatibility pack匹配、冲突与退出机制

**文件：**
- 创建：`modules/compatibility-spi/src/main/java/dev/meldrite/compat/spi/CompatibilityPack.java`、`CompatibilityContext.java`、`CompatibilityStage.java`。
- 创建：`modules/compatibility-engine/src/main/java/dev/meldrite/compat/PackMatcher.java`、`PackPlanner.java`、`PackAudit.java`。
- 创建：`contracts/schemas/compat-pack.schema.json`。
- 创建：`compat-packs/fixture-metadata/pack.json`、`src/main/java/dev/meldrite/compat/fixture/FixtureMetadataPack.java`。
- 测试：`modules/compatibility-engine/src/test/java/dev/meldrite/compat/CompatibilityContractTest.java`、`CompatibilityScenarioDriver.java`、`src/test/resources/cases/c4-packs.json`。

- [ ] **步骤1：以自有缺陷fixture测试pack，不为未知真实插件先加hack。**

```java
@Test void appliesOnlyAnExactSupportedPackAndReportsConflicts() throws Exception {
    FixtureSuite.verify("cases/c4-packs.json");
}
```

```json
{"driver":"compatibility","input":{"pack":"fixture-metadata","cases":["exact-match","wrong-jar-hash","wrong-mc","disabled","conflicting-pack","extension-throws"]},"expected":{"exact-match":"APPLIED","wrong-jar-hash":"NOT_APPLICABLE","wrong-mc":"NOT_APPLICABLE","disabled":"SKIPPED","conflicting-pack":"PLAN_REJECTED","extension-throws":"TARGET_PLUGIN_FAILED","unrelatedPluginChanged":false}}
```

- [ ] **步骤2：运行RED。** `./gradlew :compatibility-engine:test --tests '*CompatibilityContractTest'`；仅按插件名字匹配会误应用，测试必须失败。

- [ ] **步骤3：实现受限扩展点。**

```text
pack manifest: id/issue/owner/plugin id+range+hash/MC/profile/capabilities/stage/conflicts/tests/removalCondition
CompatibilityStage = metadata | library-policy | class-transform | call-adapter | event-policy
PackPlanner在类定义/启动前固定适用集合与顺序；冲突不靠最后加载者胜出
PackAudit记录应用/跳过/失败原因与变换hash；core只看SPI，不import具体pack
```

fixture pack仅修正自有错误元数据，发布默认不包含它。需要全局游戏变换的Mod兼容pack必须在manifest声明`scope=server`并在V1阶段选择，不能在插件加载中临时注册Mixin。规定移除条件：上游插件修复版本/实现本体capability完成后，匹配停止并保留历史测试。

- [ ] **步骤4：运行GREEN与依赖检查。** `./gradlew :compatibility-engine:test :plugin-runtime:test verifyArchitecture`。检查禁用pack时只有其对应fixture失败；其余基础路径仍通过。扫描公共核心禁止插件名条件和对compat-packs的依赖。
- [ ] **步骤5：提交。**

```bash
git add modules/compatibility-spi modules/compatibility-engine compat-packs/fixture-metadata contracts/schemas
git commit -m "feat: isolate compatibility packs with explicit applicability"
```
