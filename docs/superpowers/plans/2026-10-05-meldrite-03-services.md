# Meldrite 公共服务实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 建立可复用的API行为、Craft delegate、Registry、Adventure、事件、Scheduler与命令系统。

**架构：** 公共模块只操作中性对象与端口，profile承担Bukkit类型和事件构造，version承担原生效果。先以fake bridge证明语义，再用V/Q任务证明实际游戏接入。

**技术栈：** Java17、Adventure、JUnit5、锁定Paper API、F1 fixture testkit。

**规格：** [整体架构](../specs/2026-10-05-meldrite-architecture.md)第3–4、7–10节；[总计划](2026-10-05-meldrite-implementation.md)。

## 全局约束

- 公共 Runtime、ClassLoader 算法、事件派发、Scheduler、Adventure 服务、Registry 映射和 Craft delegate 只保留一套实现。
- Adventure 是基础能力，统一类身份、组件语义、序列化及发送边界。
- 不静默把 Mod 物品、方块或未知持久化内容替换为 AIR、STONE 或其他近似原版内容。
- Mixin 只承担生命周期接入、状态访问和必要拦截；事件构造、监听器排序、插件策略、命令解析与兼容补丁不得写入 Mixin。
- 所有测试fake只替代外部游戏端口，不替代被测业务；实际Fabric行为由V1–V4、Q1复测。

---

## 文件归属

`modules/api-services/`拥有业务规则，`modules/craft-common/`拥有wrapper delegate与缓存，其他公共领域各用同名模块。`adapters/paper-1.20.1/`拥有对外接口实现和类型转换；未来1.21.1只新增薄adapter。任何把这些目录整份复制到versions的变更均拒绝。

## 任务 S1：统一facade工厂与Craft公共delegate

**文件：**
- 创建：`modules/api-services/src/main/java/dev/meldrite/service/EntityService.java`、`WorldService.java`。
- 创建：`modules/craft-common/src/main/java/dev/meldrite/craft/FacadeFactory.java`、`EntityDelegate.java`、`WorldDelegate.java`、`WrapperCache.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/facade/ServerAdapter.java`、`WorldAdapter.java`、`EntityAdapter.java`、`PlayerAdapter.java`、`FacadeContext.java`。
- 测试：`modules/craft-common/src/test/java/dev/meldrite/craft/FacadeContractTest.java`、`FacadeScenarioDriver.java`、`src/test/resources/cases/s1-facade.json`。

- [ ] **步骤1：写稳定身份与失效测试。**

```java
@Test void preservesIdentityWithoutResurrectingStaleEntities() throws Exception {
    FixtureSuite.verify("cases/s1-facade.json");
}
```

```json
{"driver":"facade","input":{"operations":["lookup-twice","remove-entity","reuse-native-id-new-generation","unload-world"]},"expected":{"sameWrapperBeforeRemoval":true,"oldHandleWrite":"STALE_HANDLE","newGenerationHasNewWrapper":true,"unloadedWorldWrite":"STALE_HANDLE","remainingStrongEntityReferences":0}}
```

- [ ] **步骤2：运行RED。** `./gradlew :craft-common:test --tests '*FacadeContractTest'`；仅以原生数字ID作为cache key时必须失败。

- [ ] **步骤3：实现delegate和组合工厂。**

```text
WrapperCache key = 世界identity + 实体UUID + generation + facade kind
EntityDelegate仅保存中性handle与EntityService；活操作每次验证handle
FacadeFactory按profile创建精确Bukkit对象；C1注册Craft subclass factory替代创建入口
同一游戏实体只经过一个factory；不能另造API wrapper与Craft wrapper
world unload -> 删除对应scope cache；已发出的wrapper保留只读identity，拒绝活操作
```

生成API adapter时所有未实现的方法从F3绑定中取得明确unsupported行为；不能在测试为身份通过后把整个Player标记SUPPORTED。Server静态引用在V1启动阶段绑定一次，跨重启测试必须释放。

- [ ] **步骤4：运行GREEN。** `./gradlew :craft-common:test :paper-1201:test verifyArchitecture`。加入玩家退出重入、世界同名不同key、并发读取缓存与主线程写入；wrapper存活不应强留整个世界。
- [ ] **步骤5：提交。**

```bash
git add modules/api-services modules/craft-common adapters/paper-1.20.1
git commit -m "feat: centralize facade identity and shared Craft delegates"
```

## 任务 S2：Registry投影、epoch与无损物品快照

**文件：**
- 创建：`modules/registry/src/main/java/dev/meldrite/registry/RegistryCatalog.java`、`ProjectionResult.java`、`RegistrySnapshot.java`、`ReloadCoordinator.java`。
- 创建：`modules/api-services/src/main/java/dev/meldrite/service/InventoryService.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/registry/MaterialProjection.java`、`RegistryAdapter.java`。
- 测试：`modules/registry/src/test/java/dev/meldrite/registry/RegistryContractTest.java`、`RegistryScenarioDriver.java`、`src/test/resources/cases/s2-registry.json`。

- [ ] **步骤1：写原生身份保留与重载失败测试。**

```java
@Test void keepsUnknownContentAndCommitsReloadAtomically() throws Exception {
    FixtureSuite.verify("cases/s2-registry.json");
}
```

```json
{"driver":"registry","input":{"entries":["minecraft:stone","fixture:crystal"],"operations":["project-to-material","save-opaque-item","reload-invalid","reload-valid-tags","resolve-old-handle"]},"expected":{"stoneProjection":"Exact","crystalProjection":"Unsupported","opaqueBytesPreserved":true,"invalidReloadEpochUnchanged":true,"staticItemEpochUnchangedOnTagReload":true,"staleTagHandle":"RERESOLVE"}}
```

- [ ] **步骤2：运行RED。** `./gradlew :registry:test --tests '*RegistryContractTest'`；把未知内容映射STONE或全局epoch一律递增都应被测试识别。

- [ ] **步骤3：实现catalog与部分投影。**

```text
RegistryCatalog.resolve(ResourceKey, scope, epoch) -> resolved entry或Unavailable
ProjectionResult = Exact(value) | Opaque(identity,payload) | Unsupported(reason) | Unavailable(reason)
ReloadCoordinator.prepare(candidate) -> 完整引用/codec校验；commit仅替换受影响scope快照
InventoryService.mutate(snapshot, requestedChanges) -> 先检查可表示/可无损合并，再原子提交
失败时不改原生库存、不写替代内容；typed projection不能返回错误enum值
```

ItemSnapshot读写测试用不透明载荷含未知key、嵌套数据与版本标记。此任务只保证公共层不会破坏它，真正NBT/Data Components编解码由V3/Q4验证。开放registry投影和固定enum投影分开注册。`EntityType.UNKNOWN`仅在该profile确有该常量且行为明确时可作受限类型报告，不代表完整Mod实体兼容。

- [ ] **步骤4：运行GREEN。** `./gradlew :registry:test :api-services:test verifyArchitecture`。增加数字raw ID重排、scope不同同key、移除Mod后的隔离保存、写入回滚、旧epoch查询；确保静态registry冻结后拒绝新增。
- [ ] **步骤5：提交。**

```bash
git add modules/registry modules/api-services adapters/paper-1.20.1
git commit -m "feat: preserve registry identity and reject lossy projections"
```

## 任务 S3：Adventure统一消息服务

**文件：**
- 创建：`modules/adventure/src/main/java/dev/meldrite/adventure/AudienceService.java`、`ComponentCodec.java`、`MessagePolicy.java`、`AudienceHandle.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/adventure/AudienceAdapter.java`。
- 测试：`modules/adventure/src/test/java/dev/meldrite/adventure/AdventureContractTest.java`、`AdventureScenarioDriver.java`、`src/test/resources/cases/s3-adventure.json`。

- [ ] **步骤1：写富文本语义与关闭audience测试。**

```java
@Test void retainsComponentSemanticsAcrossAudienceRoutes() throws Exception {
    FixtureSuite.verify("cases/s3-adventure.json");
}
```

```json
{"driver":"adventure","input":{"component":"translatable+font+click+hover+nested-color","audiences":["player","console","broadcast"],"operations":["json-roundtrip","send-actionbar","send-title","close-player","send-after-close"]},"expected":{"roundtripSemanticEqual":true,"routes":["player","console","broadcast"],"actionbarUsesDedicatedChannel":true,"titleTimingsPreserved":true,"afterClose":"STALE_AUDIENCE","signedPlayerChatFabricated":false}}
```

- [ ] **步骤2：运行RED。** `./gradlew :adventure:test --tests '*AdventureContractTest'`；用plainText serializer中转会丢事件，应导致失败。

- [ ] **步骤3：实现共享组件路径。**

```text
AudienceHandle = kind + target identity + generation
AudienceService.send(handle, component, channel) -> 校验生命周期/权限 -> NetworkAudiencePort
ComponentCodec只在明确序列化边界使用锁定serializer；不把Component转字符串作为内部总线
MessagePolicy区分system message、unsigned decoration和有原始签名的player chat
MiniMessage插值用组件placeholder；玩家输入不作为可执行tag模板解析
```

Common使用profile公共Adventure依赖的交集；profile锁定BOM/serializer版本。若插件或Mod试图定义另一份未relocate `Component`，R2/V1必须拒绝。不存在“Paper侧再加一个BukkitAudiences桥”的第二消息系统。

- [ ] **步骤4：运行GREEN。** `./gradlew :adventure:test :plugin-classloading:test verifyContracts`。加入locale延迟翻译、legacy有损显式标记、bossbar成员清理、播放器退服、点击命令参数注入；V3补网络效果。
- [ ] **步骤5：提交。**

```bash
git add modules/adventure adapters/paper-1.20.1
git commit -m "feat: unify Adventure audiences and component transport"
```

## 任务 S4：事件派发与动作决策

**文件：**
- 修改：F2的 `modules/bridge-spi/src/main/java/dev/meldrite/bridge/ActionSnapshot.java`、`ActionDecision.java`。
- 创建：`modules/events/src/main/java/dev/meldrite/event/EventCoordinator.java`、`ListenerRegistry.java`、`ActionToken.java`、`EventPolicy.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/event/EventFactory.java`、`EventDispatcherAdapter.java`。
- 测试：`modules/events/src/test/java/dev/meldrite/event/EventContractTest.java`、`EventScenarioDriver.java`、`src/test/resources/cases/s4-event.json`。

- [ ] **步骤1：写可取消动作的真实监听器轨迹。**

```java
@Test void honorsCancellationOrderAndNestedActions() throws Exception {
    FixtureSuite.verify("cases/s4-event.json");
}
```

```json
{"driver":"events","input":{"listeners":["LOWEST:cancel","NORMAL:ignoreCancelled","HIGH:throw","MONITOR:observe"],"sameActionIngress":2,"nestedDistinctAction":true},"expected":{"outerOrder":["LOWEST","HIGH","MONITOR"],"outerDispatchCount":1,"nestedDispatchCount":1,"cancelled":true,"exceptionLogged":true,"nativeCommitCount":0}}
```

- [ ] **步骤2：运行RED。** `./gradlew :events:test --tests '*EventContractTest'`；全局重入flag会丢nested事件，忽略取消的实现会误调用NORMAL，二者都必须失败。

- [ ] **步骤3：实现token、排序、profile事件与决策。**

```text
ActionToken = transaction identity；同一原生动作多hook共享，新原生动作新建
EventFactory把ActionSnapshot构造为精确profile Event；EventDispatcherAdapter连接HandlerList
ListenerRegistry按priority与注册顺序快照遍历；隔离异常；每次调用前判ignoreCancelled
EventCoordinator将事件修改转ActionDecision；caller bridge负责native commit，不在common写NMS
异步事件保留线程语义；关键动作输入不可表示时返回明确abort，不绕过保护事件
```

注册/注销过程不修改当前迭代集合；MONITOR契约按锁定API语义处理，不凭优先级名称做未经证实的强制只读改写。自定义插件Event可直接派发，不需要ActionSnapshot；插件disable通过scope解除监听器。

- [ ] **步骤4：运行GREEN。** `./gradlew :events:test :paper-1201:test verifyArchitecture`。增加异步事件内主线程写入拒绝、取消后恢复、监听器新增/删除、异常不等于取消、input不可表示的拒绝；V2验证掉落/经验/客户端同步。
- [ ] **步骤5：提交。**

```bash
git add modules/events modules/bridge-spi adapters/paper-1.20.1
git commit -m "feat: coordinate events and native action decisions"
```

## 任务 S5：tick Scheduler与插件取消

**文件：**
- 创建：`modules/scheduler/src/main/java/dev/meldrite/scheduler/TickScheduler.java`、`AsyncScheduler.java`、`TaskHandle.java`、`TaskOwner.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/scheduler/SchedulerAdapter.java`。
- 测试：`modules/scheduler/src/test/java/dev/meldrite/scheduler/SchedulerContractTest.java`、`SchedulerScenarioDriver.java`、`src/test/resources/cases/s5-scheduler.json`。

- [ ] **步骤1：用可控tick而非sleep写测试。**

```java
@Test void cancelsOwnedWorkAndNeverWaitsForItself() throws Exception {
    FixtureSuite.verify("cases/s5-scheduler.json");
}
```

```json
{"driver":"scheduler","input":{"startTick":10,"repeatingDelay":2,"period":3,"disableAtTick":15,"advanceThrough":20,"syncFutureWaitOnPrimary":true},"expected":{"executedTicks":[12],"pendingAfterDisable":0,"newSubmitAfterDisable":"OWNER_DISABLED","blockingWait":"PRIMARY_THREAD_WAIT_REJECTED"}}
```

测试约定disable在tick15调度drain前执行，避免同tick先后不明确。主线程Future处理须由实际profile协议决定是立即完成还是拒绝阻塞；不能默默死锁。

- [ ] **步骤2：运行RED。** `./gradlew :scheduler:test --tests '*SchedulerContractTest'`；仅取消队列但不阻止重复任务重新入队时必须失败。

- [ ] **步骤3：实现同步/异步两条路径。**

```text
TickScheduler.schedule(owner, delayTicks, periodTicks, task) -> TaskHandle
tick drain用稳定顺序；重复任务重排前检查owner/task是否取消
TaskOwner与PluginScope共享存活状态；disable原子阻止提交并撤销pending
AsyncScheduler有界执行器、异常记录、协作取消；已运行任务不强杀线程
callSync按profile创建Future；主线程阻塞等待通过约定的guard处理
```

锁定上游零delay、负参数与同tick调度语义；profile适配其参数校验。公开的其他Paper scheduler API均进入F3 inventory，能由单线程语义实现的分别测试，不宣称Folia并行能力。

- [ ] **步骤4：运行GREEN。** `./gradlew :scheduler:test :plugin-runtime:test verifyContracts`。增加任务自取消、同tick提交、异步异常、停服超时、低TPS墙钟不影响tick顺序及callSync不死锁。
- [ ] **步骤5：提交。**

```bash
git add modules/scheduler adapters/paper-1.20.1
git commit -m "feat: implement tick scheduling and owned task cancellation"
```

## 任务 S6：命令注册、权限和来源

**文件：**
- 创建：`modules/commands/src/main/java/dev/meldrite/command/CommandRegistry.java`、`CommandKey.java`、`CommandSource.java`、`CommandDispatcher.java`、`PermissionPolicy.java`。
- 创建：`adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/command/CommandMapAdapter.java`、`CommandSenderAdapter.java`。
- 测试：`modules/commands/src/test/java/dev/meldrite/command/CommandContractTest.java`、`CommandScenarioDriver.java`、`src/test/resources/cases/s6-command.json`。

- [ ] **步骤1：写冲突、权限与卸载测试。**

```java
@Test void resolvesNamespacedCommandsAndCleansOwnedNodes() throws Exception {
    FixtureSuite.verify("cases/s6-command.json");
}
```

```json
{"driver":"commands","input":{"registrations":["alpha:spawn","beta:spawn"],"invoke":"beta:spawn","permission":false,"disableOwner":"beta","sourceKinds":["player","console","api"]},"expected":{"selectedOwner":"beta","executionCount":0,"denialIsComponent":true,"betaNodesRemaining":0,"alphaNodesRemain":true,"sourcePoliciesIndependent":true}}
```

- [ ] **步骤2：运行RED。** `./gradlew :commands:test --tests '*CommandContractTest'`；仅按裸name删除会删alpha，测试必须失败。

- [ ] **步骤3：实现命令服务与适配器。**

```text
CommandRegistry.register(owner,key,aliases,permission,executor,completer) -> owned registration
裸命令冲突按profile precedence处理；namespaced key保持确定路由
CommandSource保留origin/身份/权限/locale；不要把API dispatch伪装玩家输入
CommandTreePort发布节点变化并要求更新客户端tree；common不持有Brigadier类型
执行线程和建议线程按profile协议；反馈统一AudienceService
```

别名注册与取消是同一owned transaction；权限结果改变时更新必要命令树可见性。Paper新命令接口属于1.21.1 profile差异，核心registry不再写第二份。

- [ ] **步骤4：运行GREEN。** `./gradlew :commands:test :adventure:test :plugin-runtime:test`。增加tab建议权限、fallback prefix、直接dispatch的事件差异、command抛错、disable后的客户端节点移除；V2连接实际Brigadier。
- [ ] **步骤5：提交。**

```bash
git add modules/commands adapters/paper-1.20.1
git commit -m "feat: unify command ownership and source semantics"
```

## 任务 S7：其余API领域的可审查覆盖批次

**文件：**
- 创建：`modules/api-services/src/main/java/dev/meldrite/service/PermissionService.java`、`ServiceDirectory.java`、`PluginMessageService.java`、`ConfigurationService.java`、`PersistentDataService.java`、`RecipeService.java`、`ScoreboardService.java`、`StatisticsService.java`、`MapService.java`。
- 创建：`modules/api-services/src/test/java/dev/meldrite/service/ApiDomainS7aTest.java`至`ApiDomainS7hTest.java`、`ApiDomainScenarioDriver.java`。
- 创建：`modules/api-services/src/test/resources/cases/s7a-permission.json`、`s7b-services-messaging.json`、`s7c-configuration.json`、`s7d-pdc-itemmeta.json`、`s7e-recipe-inventory.json`、`s7f-scoreboard.json`、`s7g-world-entity.json`、`s7h-statistics-map.json`，以及`contracts/profiles/paper-1.20.1/domain-batches.json`。
- 修改：F3的`bindings.json`、`capabilities.json`；新增adapter文件由F3 `adapterFile`字段给出确切路径并纳入本任务提交清单。

本任务是循环执行的工作包。每个表格行一个独立RED/GREEN/commit/审查批次，所列测试文件和fixture在该批才创建，不把全部领域视为一次实现。F3在执行前输出本次真实symbolId与adapterFile列表；没有这个清单就不开始写该批代码。

| 批次 | 公共owner | 最小正面场景 | 必须失败的边界 |
|---|---|---|---|
| S7a 权限 | PermissionService | 默认值、op、attachment、recalculate | disable遗留attachment |
| S7b 服务与消息 | ServiceDirectory/PluginMessageService | 优先级provider、通道收发 | provider禁用、保留通道、超限payload |
| S7c 配置与序列化 | ConfigurationService | defaults、嵌套配置、注册对象往返 | 无效格式、类无法解析、数据目录逃逸 |
| S7d PDC与ItemMeta | PersistentDataService/InventoryService | 类型化key、clone、未知payload保留 | 错类型、浅拷贝、Mod数据丢失 |
| S7e 配方与库存 | RecipeService/InventoryService | 配方key、原子点击、结果槽 | 取消后的物品复制/消失 |
| S7f 计分板 | ScoreboardService | objective/team/member生命周期 | 删除后幽灵记录、owner泄漏 |
| S7g 世界/实体 | WorldService/EntityService | teleport、spawn、卸载、快照 | 失效句柄、跨世界/线程访问 |
| S7h 统计/地图/其余 | StatisticsService/MapService + F3归属 | 类型校验、地图更新、每项真实效果 | 越界值、非法entity/material、缺失语义 |

- [ ] **步骤1：选择一个批次并写上游API可调用的fixture。** 每批测试调用文件清单中对应的`cases/s7*.json`；下列行按S7a至S7h的顺序分别是对应文件的最小内容，`ApiDomainScenarioDriver`必须通过实际公共服务和profile adapter执行操作并观察副作用。S7h的“其余”符号要根据F3工作清单再拆成同一模式的具名fixture，不能用单个统计用例代表其余API。

```json
{"driver":"api-domains","input":{"batch":"S7a","operations":["grant-default","attach-permission","recalculate","disable-owner","query-after-disable"]},"expected":{"defaultApplied":true,"attachmentApplied":true,"attachmentAfterDisable":false}}
{"driver":"api-domains","input":{"batch":"S7b","operations":["register-low-provider","register-high-provider","resolve","disable-high-provider","resolve","send-oversize-payload"]},"expected":{"firstProvider":"high","afterDisable":"low","oversize":"REJECTED","disabledProviderInvocations":0}}
{"driver":"api-domains","input":{"batch":"S7c","operations":["load-nested-defaults","serialize-registered-object","deserialize","parse-invalid","open-parent-data-path"]},"expected":{"nestedDefaultApplied":true,"objectRoundtrip":true,"invalidFormat":"REJECTED","parentPath":"REJECTED"}}
{"driver":"api-domains","input":{"batch":"S7d","operations":["write-typed-pdc","clone-item-meta","mutate-clone","roundtrip-with-unknown-payload","read-wrong-type"]},"expected":{"originalUnchanged":true,"typedValuePreserved":true,"unknownPayloadPreserved":true,"wrongType":"PROFILE_DEFINED_FAILURE"}}
{"driver":"api-domains","input":{"batch":"S7e","operations":["register-keyed-recipe","craft-preview","cancel-result-click","inspect-inventory"]},"expected":{"recipeResolvable":true,"cancelledClickApplied":false,"inventoryConserved":true}}
{"driver":"api-domains","input":{"batch":"S7f","operations":["create-objective","add-team-member","remove-objective","disable-owner"]},"expected":{"memberBeforeRemoval":true,"objectiveAfterRemoval":false,"ownedRegistrationsAfterDisable":0}}
{"driver":"api-domains","input":{"batch":"S7g","operations":["spawn","teleport","cross-thread-write","unload-world","mutate-stale-wrapper"]},"expected":{"teleportApplied":true,"writeAfterUnload":"STALE_HANDLE","crossThreadWrite":"REJECTED"}}
{"driver":"api-domains","input":{"batch":"S7h","operations":["read-typed-statistic","reject-invalid-entity-type","update-map","reject-out-of-bounds-map-pixel"]},"expected":{"statisticTyped":true,"invalidEntityType":"REJECTED","mapChangeVisible":true,"outOfBounds":"REJECTED"}}
```

上面每行单独放进对应文件；S7g driver还要发起实际跨线程写入来验证`crossThreadWrite`，不能仅查询线程标志。每个fixture至少再加表格中的边界变体，期望值来自上游API语义，而不是当前实现行为。

- [ ] **步骤2：运行本批RED。** 例如S7d运行`./gradlew :api-services:test --tests '*ApiDomainS7dTest'`；PDC浅拷贝或丢未知数据必须失败。其他批次将`S7d`替换为当前批次ID；不可用上一批测试代替。

- [ ] **步骤3：按领域落实符号与行为。**

```text
从domain-batches取本批所有symbolId -> 核对对应上游契约 -> 写可失败的实际fixture
仅实现领域公共规则 -> profile负责参数/异常/返回类型 -> version端口负责实际游戏效果
每个symbol保留状态与evidence；不能用stub标SUPPORTED
无法实现的功能保留明确限制并使完整兼容发布门禁保持失败
```

扩大端口前验证现有端口无法表达该能力；新增语义端口需fake与两个profile编译验证，禁止加`invokeNative(String,Object[])`。完整API覆盖不意味着强行实现上游本就不保证可用的环境操作；必须按来源和场景解释NOT_APPLICABLE。

- [ ] **步骤4：运行GREEN及inventory审计。**

```bash
./gradlew :api-services:test :paper-1201:test verifyContracts verifyArchitecture
```

检查本批符号无遗漏、测试证据匹配当前commit，实际原生效果还未由V/Q验证时标PARTIAL。依次完成S7a–S7h，全部迭代保留独立报告；每批在总回归中重跑已经落地的先前批次。
- [ ] **步骤5：每个批次单独提交。**

```bash
git add modules/api-services adapters/paper-1.20.1 contracts/profiles/paper-1.20.1
git commit -m "feat: implement verified S7d PDC and ItemMeta behavior"
```

上例是S7d提交命令；其他批次将提交主题改为其实际批次ID与领域。提交正文列出实现的capability；不在一个提交里混合多个未独立测试的领域。
