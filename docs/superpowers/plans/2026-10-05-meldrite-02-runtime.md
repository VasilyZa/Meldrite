# Meldrite 插件运行时实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 实现可脱离Minecraft测试、支持legacy与Paper两类插件语义的加载运行时。

**架构：** descriptor adapter提供中性输入，Runtime执行依赖图与生命周期，ClassLoader执行可见性策略。API握手位于profile，不把Minecraft或JavaPlugin类型引入公共运行时。

**技术栈：** Java17、JUnit5、URLClassLoader/JAR APIs、锁定Paper API、可验证Maven解析。

**规格：** [整体架构](../specs/2026-10-05-meldrite-architecture.md)第5–6、11节；[总计划](2026-10-05-meldrite-implementation.md)。

## 全局约束

- 公共 Runtime、ClassLoader 算法、事件派发、Scheduler、Adventure 服务、Registry 映射和 Craft delegate 只保留一套实现。
- 普通 API 插件不经过 NMS 重映射；同版本 NMS 映射不承诺把其他 Minecraft 版本的插件升级过来。
- Fabric Mod 与插件分属不同加载规则；类加载可见性控制不宣称为恶意代码安全沙箱。
- 本轮只交付计划；FixtureSuite由F1定义，测试driver只能观察生产模块的真实结果。

---

## 文件与任务归属

- `modules/plugin-spi/`：描述、图边、宿主与类路由接口。
- `modules/plugin-runtime/`：发现、图排序、状态机、scope回收。
- `modules/plugin-classloading/`：类/资源查找、库realm、生命周期。
- `adapters/paper-1.20.1/`：descriptor语义与JavaPlugin/provider握手。
- `testkit/fixtures/`：真正编译成JAR的插件/库；不从mock ClassLoader推断真实隔离。

## 任务 R1：插件发现与两阶段依赖图

**文件：**
- 创建：`modules/plugin-spi/src/main/java/dev/meldrite/plugin/spi/PluginDescriptor.java`、`DependencyEdge.java`、`LoadPlan.java`。
- 创建：`modules/plugin-runtime/src/main/java/dev/meldrite/plugin/runtime/PluginDiscovery.java`、`DependencyPlanner.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/DescriptorAdapter.java`。
- 测试：`modules/plugin-runtime/src/test/java/dev/meldrite/plugin/runtime/DependencyContractTest.java`、`DependencyScenarioDriver.java`。
- 资源：`modules/plugin-runtime/src/test/resources/cases/r1-dependencies.json`。

- [ ] **步骤1：写依赖/可见性分离测试。**

```java
@Test void separatesOrderFromClasspath() throws Exception {
    FixtureSuite.verify("cases/r1-dependencies.json");
}
```

```json
{"driver":"dependency-plan","input":{"plugins":[{"id":"base","kind":"paper"},{"id":"consumer","kind":"paper","server":[{"id":"base","required":true,"order":"BEFORE_CONSUMER","joinClasspath":false}]}],"negativeCases":["missing-required","optional-absent","cycle","duplicate-id","both-descriptors"]},"expected":{"serverOrder":["base","consumer"],"consumerCanReadBase":false,"missing-required":"REJECT","optional-absent":"LOAD","cycle":"CYCLE_PATH","duplicate-id":"REJECT","both-descriptors":"PROFILE_PRECEDENCE"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :plugin-runtime:test --tests '*DependencyContractTest'`；把顺序边自动当作可见性边的实现必须失败。

- [ ] **步骤3：实现描述器与规划器。**

```text
DependencyEdge(target, phase, required, ordering, joinClasspath, descriptorOrigin)
DependencyPlanner.plan(descriptors, profileRules) -> LoadPlan(bootstrapOrder, serverOrder, visibilityGraph)
先校验identity/descriptor；再按phase检查硬依赖；再计算确定性拓扑序
保留soft/loadbefore和Paper字段原语义；排序稳定性用发现索引作最后tie-breaker
只改变顺序的边不授予类访问；Paper join-classpath显式进入visibilityGraph
```

descriptor双文件优先级、optional循环处理和bootstrap上下文按固定Paper源码及差分fixture确定，结果写入profile lifecycle规则；不能把当前最新版文档默认值直接用于1.20.1。解析YAML限制重复key并给出文件/行错误；扫描不初始化插件类。

- [ ] **步骤4：运行GREEN。**

```bash
./gradlew :plugin-runtime:test --tests '*DependencyContractTest'
./gradlew verifyArchitecture
```

补充`loadbefore`目标不存在、soft dependency版本不符、bootstrap与server图不同三例；要求错误中包含完整循环路径/缺失目标。

- [ ] **步骤5：提交。**

```bash
git add modules/plugin-spi modules/plugin-runtime adapters/paper-1.20.1
git commit -m "feat: plan plugin lifecycle and visibility dependencies"
```

## 任务 R2：类与资源路由、API-only恒等路径

**文件：**
- 创建：`modules/plugin-spi/src/main/java/dev/meldrite/plugin/spi/ClassTransform.java`、`TransformContext.java`、`VisibilityPolicy.java`。
- 创建：`modules/plugin-classloading/src/main/java/dev/meldrite/plugin/classloading/PluginLoaderEngine.java`、`PluginRealm.java`、`LibraryRealm.java`、`ApiAuthority.java`、`LegacyVisibilityPolicy.java`、`PaperVisibilityPolicy.java`。
- 测试：`modules/plugin-classloading/src/test/java/dev/meldrite/plugin/classloading/ClassIdentityContractTest.java`、`ClassloadingScenarioDriver.java`。
- 资源：`modules/plugin-classloading/src/test/resources/cases/r2-classloading.json`。
- 夹具：`testkit/fixtures/classidentity/build.gradle.kts`、`src/pluginA/java/fixture/Probe.java`、`src/pluginB/java/fixture/Probe.java`、`src/libraryA/java/fixture/lib/Version.java`、`src/libraryB/java/fixture/lib/Version.java`。

- [ ] **步骤1：编译实际冲突库JAR并写测试。** 两个`Version`类返回不同字面值；两个插件都引用共享Adventure `Component`和锁定`Plugin`类型。

```java
@Test void keepsPrivateLibrariesAndSharedApiIdentity() throws Exception {
    FixtureSuite.verify("cases/r2-classloading.json");
}
```

```json
{"driver":"classloading","input":{"plugins":["pluginA","pluginB"],"privateLibraryValues":["A","B"],"profile":"paper","dependencyJoin":false},"expected":{"libraryValues":["A","B"],"sameComponentClass":true,"samePluginClass":true,"crossPluginPrivateAccess":"DENIED","nmsTransformCalls":0,"inputClassBytesUnchanged":true}}
```

- [ ] **步骤2：运行RED。** `./gradlew :plugin-classloading:test --tests '*ClassIdentityContractTest'`；先用普通parent-first URLClassLoader，确认父类路径抢占/依赖泄漏反例失败。

- [ ] **步骤3：实现定义权固定的路由。**

```text
ClassTransform.apply(byte[] source, TransformContext context) -> byte[] result
API_ONLY => identity bytes；不调用NMS transformer
loadClass(name): 已加载 -> JDK/API authority -> 本插件/私库 -> 允许的plugin routes -> 明确授权的native route -> 报错
资源搜索使用相同可见性但遵循getResources的多结果规则；不能全量转发给Knot
defineClass前验证包定义权/CodeSource/签名；并发按class name加锁
关闭realm只释放自身JAR和缓存；shared API authority不由插件关闭
```

实际搜索细节通过锁定profile fixture修正；legacy全局搜索由独立策略开启，记录未声明依赖访问；Paper隔离策略禁止该fallback。private库不能重新定义`org.bukkit`/未relocate `net.kyori`。保护列表基于公共contract依赖而不是只写两个包名。

- [ ] **步骤4：运行GREEN并加资源/并发测试。**

```bash
./gradlew :plugin-classloading:test --tests '*ClassIdentityContractTest'
./gradlew :plugin-classloading:fixtureTest verifyArchitecture
```

增加并发同名类、同名ServiceLoader服务、TCCL恢复、资源枚举顺序、重复API、JAR关闭后的新读取；验证库A/B不是同一Class对象，而API类完全同一对象。

- [ ] **步骤5：提交。**

```bash
git add modules/plugin-spi modules/plugin-classloading testkit/fixtures/classidentity
git commit -m "feat: isolate plugin realms and preserve shared API identity"
```

## 任务 R3：生命周期状态机与owner资源清理

**文件：**
- 创建：`modules/plugin-runtime/src/main/java/dev/meldrite/plugin/runtime/PluginController.java`、`PluginState.java`、`ScopeRegistry.java`。
- 修改：F2的 `modules/plugin-spi/src/main/java/dev/meldrite/plugin/spi/PluginScope.java`、`PluginHost.java`。
- 测试：`modules/plugin-runtime/src/test/java/dev/meldrite/plugin/runtime/LifecycleContractTest.java`、`LifecycleScenarioDriver.java`。
- 资源：`modules/plugin-runtime/src/test/resources/cases/r3-lifecycle.json`。

- [ ] **步骤1：记录实际状态轨迹和资源close计数。**

```java
@Test void rollsBackFailedEnableAndDependentScopes() throws Exception {
    FixtureSuite.verify("cases/r3-lifecycle.json");
}
```

```json
{"driver":"plugin-lifecycle","input":{"dependency":"consumer->base","baseFailure":"onEnable","resources":["listener","command","task","service","jar"]},"expected":{"baseState":"FAILED","consumerState":"BLOCKED_DEPENDENCY","resourceCloseCounts":[1,1,1,1,1],"postCloseRegistration":"SCOPE_CLOSED","consumerEnableCalls":0}}
```

- [ ] **步骤2：运行RED。** `./gradlew :plugin-runtime:test --tests '*LifecycleContractTest'`；enable异常后依旧enabled或清理两次都必须失败。

- [ ] **步骤3：实现状态转移与补偿。**

```text
PluginHost = construct/load/enable/disable 的中性回调，profile负责JavaPlugin调用
PluginController.transition(plugin, phase): 校验合法前态 -> 调用host -> 成功提交状态
失败 -> close scope -> 记录原始cause与phase -> required dependents阻断
PluginScope.register(resource): 关闭后拒绝并立即关闭新资源，不能泄漏
PluginScope.close(): 原子置closed；逆序close全部；汇总异常但继续清理
```

`onDisable`是否调用、何时调用取决于已达到的profile状态；不能对从未构造的插件调用。disabled owner的新任务/监听器/服务必须拒绝。第三方插件自建线程记录为诊断，不谎称scope能杀死所有线程。

- [ ] **步骤4：运行GREEN。**

```bash
./gradlew :plugin-runtime:test --tests '*LifecycleContractTest'
./gradlew :plugin-runtime:fixtureTest :plugin-classloading:test
```

补充construct失败、onLoad失败、disable抛错、重复disable、资源close抛错、逆依赖关闭顺序；剩余插件应继续清理。后续S4–S6使用同一ScopeRegistry。

- [ ] **步骤5：提交。**

```bash
git add modules/plugin-runtime modules/plugin-spi
git commit -m "feat: enforce plugin lifecycle and owned resource cleanup"
```

## 任务 R4：Paper provider、JavaPlugin握手与库加载

**文件：**
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/PluginHostAdapter.java`、`PaperProviderAdapter.java`、`BootstrapContextAdapter.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/MeldriteConfiguredClassLoader.java`，实现锁定profile的`ConfiguredPluginClassLoader`握手并委托公共`PluginLoaderEngine`。
- 创建：`modules/plugin-classloading/src/main/java/dev/meldrite/plugin/classloading/LibraryResolver.java`。
- 创建：`contracts/profiles/paper-1.20.1/plugin-lifecycle.json`。
- 测试：`adapters/paper-1.20.1/src/test/java/dev/meldrite/paper/PaperProviderContractTest.java`、`ProviderScenarioDriver.java`、`src/test/resources/cases/r4-provider.json`。
- 夹具：`testkit/fixtures/paper-provider/src/main/java/fixture/Loader.java`、`Bootstrap.java`、`Plugin.java`、`src/main/resources/paper-plugin.yml`。

- [ ] **步骤1：用官方API编译真实Paper插件fixture。** Loader/Bootstrap/Plugin分别将阶段和自己的classloader identity写到测试轨迹；loader按profile API声明一项离线缓存库，bootstrap与最终Plugin各自尝试加载其marker类。入口按锁定1.20.1接口声明，不引用当前新API。

```java
@Test void preservesPaperProviderPhases() throws Exception {
    FixtureSuite.verify("cases/r4-provider.json");
}
```

```json
{"driver":"paper-provider","input":{"fixture":"paper-provider","operations":["loader","bootstrap","construct","load","enable"],"bootstrapWorldAccess":true,"libraryMode":"offline-cached"},"expected":{"phaseOrder":["loader","bootstrap","construct","load","enable"],"libraryVisibleAtBootstrap":true,"libraryVisibleAtPlugin":true,"worldAccess":"PHASE_UNAVAILABLE","libraryVerified":true,"duplicateJavaPluginInit":false}}
```

测试的`PHASE_UNAVAILABLE`为归一化诊断；公开API实际抛何种异常需遵守锁定profile，不能硬改成所有版本都一样的异常。

- [ ] **步骤2：运行RED。** `./gradlew :paper-1201:test --tests '*PaperProviderContractTest'`；不把loader声明的库加入正确的阶段可见性时必须失败。实际classloader身份和共享范围另与固定Paper baseline对照，不能由本设计预设相同或不同。

- [ ] **步骤3：连接provider与公共引擎。**

```text
DescriptorAdapter -> provider kind -> phase host
ProviderAdapter在独立loader/bootstrap context解析库与bootstrap产物
PluginHostAdapter按profile构造JavaPlugin、完成一次初始化、委托Runtime状态机
MeldriteConfiguredClassLoader实现ConfiguredPluginClassLoader.init/getPlugin/getConfiguration及所需group协议；class search委托PluginLoaderEngine
不继承final的org.bukkit.plugin.java.PluginClassLoader；它是Internal支撑类型，对其具体类依赖按内部访问处理
LibraryResolver使用明确repository list、坐标+SHA缓存与离线失败；不按地区换源
```

bootstrap只暴露该阶段有效的服务；API可用性来自profile规范。加载失败包含插件名、库坐标、下载/校验原因；未知mapping manifest不在此猜测修复。要求Fabric/API realm由V1提供，实现本任务时用同定义权的测试宿主。

- [ ] **步骤4：运行GREEN并对照profile轨迹。**

```bash
./gradlew :paper-1201:test --tests '*PaperProviderContractTest'
./gradlew :plugin-runtime:test :plugin-classloading:test verifyContracts
```

补充`join-classpath=false`、bootstrap缺依赖、libraries hash不符、在线失败/离线缓存、两个描述文件、load阶段触发世界访问。V2/Q1完成后同fixture在官方Paper和Meldrite真服复测。

- [ ] **步骤5：提交。**

```bash
git add adapters/paper-1.20.1 modules/plugin-classloading contracts/profiles/paper-1.20.1 testkit/fixtures/paper-provider
git commit -m "feat: adapt Paper providers to the shared plugin runtime"
```
