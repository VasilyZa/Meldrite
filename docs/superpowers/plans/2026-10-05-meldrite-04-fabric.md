# Meldrite Fabric 1.20.1 接入实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 用独立1.20.1版本实现把公共系统接入实际Fabric服务器，并验证关键原生效果。

**架构：** Fabric负责游戏与Mod转换；version bootstrap组装端口；Mixin调用version gateway；公共事件/业务返回决策；bridge应用原生效果。先完成一个完整纵向切片，再扩展hook清单。

**技术栈：** Java17、Fabric Loader/API、Loom、Mixin、Yarn开发映射与intermediary发行映射、外部JVM集成fixture。

**规格：** [整体架构](../specs/2026-10-05-meldrite-architecture.md)第4–5、7、10、13节；[总计划](2026-10-05-meldrite-implementation.md)。

## 全局约束

- 当前实际实现以 Minecraft 1.20.1 为主；从第一天开始预留 Minecraft 1.21.1 支持能力。
- Mixin 只承担生命周期接入、状态访问和必要拦截；事件构造、监听器排序、插件策略、命令解析与兼容补丁不得写入 Mixin。
- 每个兼容声明必须同时指向 contract、功能状态和回归证据；任何生产关键 hook 缺失都阻止该发行版进入 READY。
- 本计划不能把named开发启动通过代替intermediary发行包通过。

---

## 文件归属

- `versions/v1_20_1/bootstrap/`：Fabric入口、端口装配与阶段回调。
- `versions/v1_20_1/bridge/`：原生对象/状态/编码、事件与网络接入。
- `versions/v1_20_1/hooks/`：Mixin和access widener；无Bukkit业务依赖。
- `distributions/mc-1.20.1/`：确定的profile/version/toolchain组合。
- `testkit/integration/`：启动与结果采集器，Q1扩展为差分runner。

## 任务 V1：发行组合与最小真实启动

**文件：**
- 创建：`versions/v1_20_1/bootstrap/src/main/java/dev/meldrite/v1201/bootstrap/MeldriteInitializer.java`、`VersionComposition.java`。
- 创建：`versions/v1_20_1/bootstrap/src/main/resources/fabric.mod.json`。
- 创建：`versions/v1_20_1/bridge/src/main/java/dev/meldrite/v1201/bridge/LifecycleBridge.java`、`ExecutionBridge.java`。
- 创建：`modules/bootstrap-common/src/main/java/dev/meldrite/bootstrap/BootstrapCoordinator.java`、`DistributionVerifier.java`。
- 创建：`distributions/mc-1.20.1/distribution.json`、`versions/v1_20_1/toolchain-lock.json`。
- 创建：`testkit/integration/src/main/java/dev/meldrite/integration/ServerProcess.java`、`ScenarioResult.java`。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/BootstrapContractTest.java`、`BootstrapScenarioDriver.java`、`src/test/resources/cases/v1-bootstrap.json`。

- [ ] **步骤1：定义外部进程结果协议并写启动失败测试。** `ServerProcess.start(distribution, scenario, timeout)` 创建临时server目录和端口，采集JSON行结果、stdout/stderr、exit状态；不把出现“Done”一词当作唯一成功条件。runner只在已有EULA接受配置时启动游戏，不替用户写接受声明。

```java
@Test void startsOnlyTheDeclaredVersionComposition() throws Exception {
    FixtureSuite.verify("cases/v1-bootstrap.json");
}
```

```json
{"driver":"fabric-bootstrap","input":{"distribution":"mc-1.20.1","mode":"production","mutations":["none","wrong-profile","duplicate-adventure","wrong-jdk","missing-bridge"]},"expected":{"none":"READY_THEN_STOPPED","wrong-profile":"DISTRIBUTION_MISMATCH","duplicate-adventure":"DUPLICATE_SHARED_API","wrong-jdk":"JAVA_REQUIREMENT","missing-bridge":"MISSING_REQUIRED_CAPABILITY"}}
```

- [ ] **步骤2：执行RED。** `./gradlew :testkit-integration:test --tests '*BootstrapContractTest' -Pmc=1.20.1`；未装配关键端口或用开发类名打包时应失败。下载工具链失败归类为环境失败，不当成功。

- [ ] **步骤3：锁定版本工具并完成组合。**

```text
toolchain lock: Minecraft1.20.1 + JDK17 + 一个实测可构建的Loom/Loader/Fabric API/Yarn组合
候选解析起点：Loom1.7.4、Loader0.16.14、Fabric API0.92.2+1.20.1、Yarn1.20.1+build.10
固定每个artifact SHA，构建后实际运行；任意不兼容时先修改版本组合ADR再重锁
Fabric entrypoint -> VersionComposition -> BridgeBundle -> BootstrapCoordinator
组合根检查所有required ports、唯一API定义权、mapping namespace与发行manifest一致
```

候选版本不是“已验证可用”的声明，实际下载/构建/启动是本任务验收的一部分。API realm和公共服务只组装一次；启动时打印每个阶段与健康结果。构建脚本提供`:mc1201:assemble`，产物路径写入machine-readable manifest，不让runner猜文件名。

- [ ] **步骤4：执行GREEN，开发和发行两次。**

```bash
./gradlew :mc1201:assemble verifyArchitecture
./gradlew :testkit-integration:test --tests '*BootstrapContractTest' -Pmc=1.20.1 -PruntimeNamespace=named
./gradlew :testkit-integration:test --tests '*BootstrapContractTest' -Pmc=1.20.1 -PruntimeNamespace=intermediary
```

测试退出无遗留服务器子进程；预期错误组合从未到READY。记录实际JDK、Fabric和API lock摘要。
- [ ] **步骤5：提交。**

```bash
git add versions/v1_20_1 modules/bootstrap-common distributions/mc-1.20.1 testkit/integration
git commit -m "feat: bootstrap a verified Fabric 1.20.1 distribution"
```

## 任务 V2：block break纵向切片与关键hook门禁

**文件：**
- 创建：`versions/v1_20_1/hooks/src/main/resources/meldrite.mixins.json`、`meldrite.accesswidener`、`hook-manifest.json`。
- 创建：`versions/v1_20_1/hooks/src/main/java/dev/meldrite/v1201/hook/ServerLifecycleMixin.java`、`PlayerBreakMixin.java`。
- 创建：`versions/v1_20_1/bridge/src/main/java/dev/meldrite/v1201/bridge/HookGateway.java`、`BlockBreakBridge.java`、`CommandBridge.java`、`HookHealthVerifier.java`。
- 夹具：`testkit/fixtures/protection/src/main/java/fixture/ProtectionPlugin.java`、`src/main/resources/plugin.yml`。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/BlockBreakContractTest.java`、`BlockBreakScenarioDriver.java`、`src/test/resources/cases/v2-break.json`。

- [ ] **步骤1：写游戏操作fixture。** 服务端集成driver通过自有测试Mod在正确游戏线程创建测试世界/玩家上下文，执行与客户端同入口的挖掘流程；网络可见结果另用可控测试客户端验证，不以直接改方块代替挖掘。

```java
@Test void cancellationPreservesAllNativeBreakEffects() throws Exception {
    FixtureSuite.verify("cases/v2-break.json");
}
```

```json
{"driver":"block-break","input":{"block":"minecraft:diamond_ore","pluginCancels":true,"operations":["break","observe-world","observe-drops","observe-xp","observe-client","repeat-with-missing-hook"]},"expected":{"eventCount":1,"blockUnchanged":true,"dropCount":0,"xpSpawned":0,"clientResynced":true,"missingHook":"STARTUP_REJECTED"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-integration:test --tests '*BlockBreakContractTest' -Pmc=1.20.1`；只阻止方块变化但生成掉落/经验时必须失败。

- [ ] **步骤3：定位注入点并记录动作合同。**

```text
从锁定1.20.1源码确定原生pre-commit点、成功出口与客户端恢复路径
hook manifest写真实owner/descriptor/require计数/phase/thread/capability
Mixin只取得参数与短期handle -> HookGateway -> EventIngressPort -> S4
BlockBreakBridge将ActionDecision应用于原生事务；取消时保持方块/背包/掉落/经验一致
HookHealthVerifier在READY前验证关键注入；缺失即终止，不尝试晚注册Mixin
```

把插件load/STARTUP/POSTWORLD/disable和tick入口接到V1/R3/S5，命令树接S6；这几条入口各有manifest记录。不得将`new BlockBreakEvent`或插件名判断写入Mixin。源代码定位得到的目标描述符是版本模块实现内容，不在本架构文档臆造一个无法验证的NMS方法名。

- [ ] **步骤4：运行GREEN并扩充对照。**

```bash
./gradlew :testkit-integration:test --tests '*BlockBreakContractTest' -Pmc=1.20.1
./gradlew verifyArchitecture :mc1201:assemble
```

测试允许破坏、改经验、listener异常、重复hook、嵌套新动作、插件disable、Mod回调先取消；记录Fabric与Bukkit排序契约。故意破坏关键hook目标，确保进程非0且不能READY，再恢复。
- [ ] **步骤5：提交。**

```bash
git add versions/v1_20_1 testkit/fixtures/protection testkit/integration
git commit -m "feat: connect cancellable block actions through thin hooks"
```

## 任务 V3：原生物品、Registry与Adventure往返

**文件：**
- 创建：`versions/v1_20_1/bridge/src/main/java/dev/meldrite/v1201/bridge/RegistryBridge.java`、`ItemNbtCodec.java`、`NativeDataFixBridge.java`、`AudienceNetworkBridge.java`。
- 夹具：`testkit/fixtures/content-mod/src/main/java/fixture/mod/ContentFixture.java`、`src/main/resources/fabric.mod.json`。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/NativeDataContractTest.java`、`NativeDataScenarioDriver.java`、`src/test/resources/cases/v3-native-data.json`。

- [ ] **步骤1：注册真实Mod内容并写持久化fixture。** 夹具注册`fixture:crystal`物品与带额外数据的实体；保存并重启后检查身份/载荷，不能仅同进程byte数组比较。

```java
@Test void preservesModDataAndComponentMessagesInGame() throws Exception {
    FixtureSuite.verify("cases/v3-native-data.json");
}
```

```json
{"driver":"native-data","input":{"item":"fixture:crystal","payload":{"fixture:secret":731},"operations":["save","restart","read","edit-count","save","restart","reload-tags","send-actionbar"]},"expected":{"itemId":"fixture:crystal","unknownPayloadPreserved":true,"countEditPreservedOtherData":true,"staticRegistryNotRecreated":true,"actionbarClientComponentEqual":true,"unsupportedMaterialWrite":"REJECTED_WITHOUT_MUTATION"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-integration:test --tests '*NativeDataContractTest' -Pmc=1.20.1`；codec丢未知NBT或把Mod item替换为原版时必须失败。

- [ ] **步骤3：实现原生codec与网络端口。**

```text
RegistryBridge按static/world-load/reloadable分类接入，不把普通reload当重建所有registry
ItemNbtCodec将1.20.1完整原生载荷封装成ItemSnapshot，编辑已知字段时保留未知字段
NativeDataFixBridge使用游戏原生迁移与Mod codec；没有可靠codec就禁止破坏性写入
AudienceNetworkBridge将Component转原生text及正确packet channel，保留viewer/locale语义
```

必要的原生text/组件序列化依赖留在version中；common不import packet。移除测试Mod时保留可隔离的原始保存载荷并报告缺失codec，不能自动重写世界为默认物品。

- [ ] **步骤4：运行GREEN。** `./gradlew :testkit-integration:test --tests '*NativeDataContractTest' -Pmc=1.20.1`。增加已知原版NBT、损坏载荷、失败reload回滚、world unload、title/bossbar/hover网络观察。原生游戏遇到不认识Mod的存档格式若无法加载，测试应验证Meldrite拒绝迁移且保留原文件，而非声称能无限容错加载。
- [ ] **步骤5：提交。**

```bash
git add versions/v1_20_1/bridge testkit/fixtures/content-mod testkit/integration
git commit -m "feat: bridge native registries item data and Adventure transport"
```

## 任务 V4：hook能力扩展与Mod互操作边界

**文件：**
- 创建：`modules/extension-api/src/main/java/dev/meldrite/api/ContentAccess.java`、`InteropServices.java`、`ServerExecutor.java`。
- 创建：`versions/v1_20_1/bridge/src/main/java/dev/meldrite/v1201/bridge/InteropProvider.java`。
- 创建：`contracts/profiles/paper-1.20.1/action-contracts.json`。
- 修改：V2的hook manifest及每项新hook独立源文件；文件名在action contract的`hookFile`字段列全，禁止塞进巨大通用Mixin。
- 测试：`testkit/integration/src/test/java/dev/meldrite/integration/InteropContractTest.java`、`InteropScenarioDriver.java`、`src/test/resources/cases/v4-interop.json`。

- [ ] **步骤1：写Mod与插件跨边界fixture。**

```java
@Test void exposesOnlyDeclaredInteropAndProtectsUnknownActions() throws Exception {
    FixtureSuite.verify("cases/v4-interop.json");
}
```

```json
{"driver":"interop","input":{"operations":["mod-register-service","plugin-query-content","plugin-load-private-mod-class","dual-descriptor-jar","protected-break-unrepresentable-item"]},"expected":{"contentId":"fixture:crystal","sharedDtoIdentity":true,"privateModClass":"DENIED","dualDescriptorJar":"REJECTED","protectedAction":"ABORTED_WITH_DATA_INTACT"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :testkit-integration:test --tests '*InteropContractTest' -Pmc=1.20.1`；parent直接暴露Knot时私有Mod类用例必须失败。

- [ ] **步骤3：公开中性interop并按动作清单扩展hook。**

```text
InteropServices.publish(ResourceKey service, public contract, provider, lifecycleScope)
ContentAccess.query(ResourceKey) -> descriptor/snapshot，不能返回NMS object
ServerExecutor.execute(owner, action) -> 主线程任务+scope；拒绝关闭owner
action-contracts记录触发点/线程/可修改字段/取消效果/不可表示策略/hookFile/fixtureId
```

扩展顺序：玩家加入退出→实体伤害/死亡→库存点击→方块放置/交互→聊天→传送/世界→配方/计分板相关动作。每类一个独立真实效果fixture与提交；大类再按一次原生事务拆分。保护性事件严格模式不允许因Mod内容无法表示而静默跳过。协议/packet变更风险通过各version能力处理，不将interop设计成raw packet自由通道。

- [ ] **步骤4：运行GREEN。** `./gradlew :testkit-integration:test --tests '*InteropContractTest' -Pmc=1.20.1`，再运行已完成action-contract对应suite；检查每个SUPPORTED能力有hook/原生callback或明确无需hook的理由。
- [ ] **步骤5：提交。**

```bash
git add modules/extension-api versions/v1_20_1 contracts/profiles/paper-1.20.1 testkit/integration
git commit -m "feat: define explicit Mod interop and verified action hooks"
```
