# Meldrite 整体实施实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在独立 Meldrite 仓库中落地可验证的 Minecraft 1.20.1 Fabric 混合服务端，并以共用服务扩展到 1.21.1。

**架构：** 公共行为服务、精确 Paper contract adapter、Minecraft version implementation 分离。插件加载与 remapping 分离，Craft 公共 delegate 与 NMS 签名分离，全部支持声明由静态和真服测试证明。

**技术栈：** Java 17/21、Gradle Kotlin DSL、Fabric Loader/API、Loom、Mixin、Adventure、JUnit 5、ASM、mapping-io、Jackson、japicmp 或 Revapi；具体制品由 F1 固定。

**规格：** [整体架构](../specs/2026-10-05-meldrite-architecture.md)；[调研证据](../specs/2026-10-05-meldrite-research.md)。先读规格，再读本索引和当前子计划。

## 全局约束

- 当前实际实现以 Minecraft 1.20.1 为主；从第一天开始预留 Minecraft 1.21.1 支持能力。
- 公共代码不得依赖 Minecraft、Fabric Loader、Mixin、CraftBukkit 或具体 NMS mapping 类型；禁止在公共业务中写 `if version == ...`。
- Paper API 兼容是固定上游构件的 API/ABI contract。API JAR 中存在方法不等于服务端已实现其语义。
- 公共 Runtime、ClassLoader 算法、事件派发、Scheduler、Adventure 服务、Registry 映射和 Craft delegate 只保留一套实现。
- 普通 API 插件不经过 NMS 重映射；同版本 NMS 映射不承诺把其他 Minecraft 版本的插件升级过来。
- Adventure 是基础能力，统一类身份、组件语义、序列化及发送边界。
- 不静默把 Mod 物品、方块或未知持久化内容替换为 AIR、STONE 或其他近似原版内容。
- 每个兼容声明必须同时指向 contract、功能状态和回归证据；任何生产关键 hook 缺失都阻止该发行版进入 READY。
- 本轮只交付文档。以下所有源码、命令与测试是未来执行计划，不表示已经存在或通过。

---

## 1. 执行位置与交付边界

目标是新的 `/home/xiaobai/Projects/Projects/Meldrite` 仓库。所有子计划中的相对路径以该仓库为根，禁止在 Taiyitist 中创建这些模块。若目录已存在，先检查 `git status` 与目录内容；不是空的新项目则停止初始化并核实目标，不覆盖现有文件。

阶段目标：

| 门禁 | 必须交付 | 可以公开的状态 |
|---|---|---|
| G0 合同可审计 | API lock、符号 inventory、模块检查、testkit | 架构骨架，不是可用服务端 |
| G1 首条纵向路径 | packaged Fabric 启动、插件生命周期、消息/命令/任务、一个取消事件 | 开发预览，列出已支持项 |
| G2 1.20.1 兼容基础 | registry 无损边界、Craft 白名单、选择式 remap、Paper plugin 支持、补丁治理 | 限定功能/插件组合的 alpha |
| G3 1.20.1 发布候选 | 全量 API inventory 处置、目标功能集通过、真实插件矩阵、恢复/停机/性能报告 | 有准确范围的兼容发布候选 |
| G4 1.21.1 扩展 | 独立 version/profile、共用公共代码、双版本回归 | 独立1.21.1发行物，不覆盖旧版 |

G3 不把尚未支持功能改为假实现。若完整 Paper contract 的所有行为尚未达标，继续公布受限版本；“Paper 兼容完整实现”必须等其独立门禁满足。1.21.1 可在 G2 后并行移植，但不得阻塞1.20.1基础修复或复制核心代码。

## 2. 子计划与依赖

| 子计划 | 任务 | 交付物 | 前置 |
|---|---|---|---|
| [01 合同与基础](2026-10-05-meldrite-01-foundation.md) | F1–F3 | 可复现合同、端口与完整 inventory | 无 |
| [02 插件运行时](2026-10-05-meldrite-02-runtime.md) | R1–R4 | 元数据、依赖图、ClassLoader、生命周期和 Paper provider | F1–F2；R4需F3 |
| [03 公共服务](2026-10-05-meldrite-03-services.md) | S1–S7 | facade、registry、Adventure、事件、调度、命令和 API 领域覆盖 | F2–F3；owner清理接R3 |
| [04 Fabric 1.20.1](2026-10-05-meldrite-04-fabric.md) | V1–V4 | 真服启动、关键hooks、数据编码和完整纵向场景 | F2；V2起接R/S |
| [05 内部兼容](2026-10-05-meldrite-05-compatibility.md) | C1–C4 | Craft binding、remapper、反射边界与兼容pack | R2、S1、V1；C2可先fake测试 |
| [06 发布与1.21.1](2026-10-05-meldrite-06-validation-port.md) | Q1–Q5 | 差分/插件矩阵、发布检查、第二版本实现 | 贯穿前五计划；Q3在G2后 |

可并行的是模块内单元实现；不可并行修改相同 contract、共享端口和profile factory。冻结端口变更需一项 ADR、两个实现/假桥的编译结果和对应测试。每任务单独提交，失败测试、最小实现、回归、审查形成一个闭环。若任务测试暴露了新的语义切面，将其拆成同任务编号下独立提交，不把数百个API方法混在一次提交里。

## 3. 文件归属与测试契约

文件树以规格第3节为权威；各子计划“文件”清单定义要创建的实际类。包前缀统一 `dev.meldrite`。路径 `src/main/java/dev/meldrite/...` 与 `src/test/java/dev/meldrite/...` 按文件清单写全；`org.bukkit.*` 与 Craft 精确包仅出现在 profile/version适配和fixture中。

F1 创建通用 fixture runner：

```java
public interface ScenarioDriver {
    String id();
    JsonNode execute(JsonNode input) throws Exception;
}
public final class FixtureSuite {
    public static void verify(String resourcePath) throws Exception;
}
```

`FixtureSuite` 读取 JSON 的 `driver/input/expected`，通过测试 ServiceLoader 选择 driver，只把 input 交给 driver，再比较其 actual 与 expected。driver 必须调用真实生产模块；游戏场景 driver 启动外部 JVM 并采集结果。预期值不能传给 driver，禁止 driver 直接回显 expected、仅验证 JSON 自洽或用与实现相同的算法计算期望。

每个子计划创建的 `*ScenarioDriver` 必须在其测试资源 `META-INF/services/dev.meldrite.testkit.ScenarioDriver` 中登记完整类名；同一模块多driver时每行一个。未发现driver、重复id或资源无法解析都应让fixture测试失败。测试资源文件也属于相应任务的提交内容。

每个任务提供最小 JSON fixture 与 JUnit 测试。任务内同一个 fixture 还应包含列出的负面用例；数组用例由任务 driver 明确执行，不允许 runner 自动推导业务。最初的 RED 可以是类缺失；创建最小类后至少出现一次行为断言失败，才能算证明测试有效。

所有 Gradle task 名称属于未来 build conventions 的公开接口：F1注册 `test`/`fixtureTest`/`verifyArchitecture`/`verifyContracts`，Q1注册`integrationTest`，Q2注册`verifyRelease`。任务不存在时先实现其定义，不能把“找不到任务”当成业务测试通过。统一输出 `build/reports/meldrite/<task>/`，失败 exit code 非0。

## 4. 步骤与审查要求

每个复选步骤是一个明确动作。小型单元步骤以2–5分钟为目标；下载、真服启动、完整API批次与多模块集成不以这个时间作为完成承诺。复杂任务按 fixture case 逐个重复 RED → 最小实现 → GREEN，不能先写整套业务再补测试。

计划中的 `text` 算法块是实现规范而非已经存在的业务代码；Java块是测试/接口契约。执行者不得将算法块原样变成返回常量的mock实现。每个任务末尾的commit仅在该任务验证通过且只包含其文件时执行。

审查至少检查：依赖方向、真实行为、异常/线程/生命周期、unsupported是否诚实、Fixture是否能杀死错误实现。功能数量多时以F3 inventory生成的领域清单逐批验收，不随意省略难实现的API。

## 5. 需求覆盖表

| 用户要求 | 规格位置 | 实施任务 |
|---|---|---|
| 从零设计、长期维护 | 1、3、13 | F1、F2、Q2 |
| 明确Paper API/ABI contract | 2 | F1、F3、S7、Q1 |
| API implementation与Craft分开 | 3.2 | S1、C1 |
| Minecraft/Fabric/Mixin边界 | 4、5、7 | F2、V1–V4 |
| Runtime/ClassLoader版本解耦 | 6 | R1–R4 |
| Adventure基础能力 | 8 | S3、V3 |
| 事件/命令/Scheduler | 7、9 | S4–S6、V2 |
| Mod/Plugin类加载隔离 | 6.2–6.3 | R2、R4、V4 |
| Registry与自定义内容 | 10 | S2、V3、Q4 |
| 普通插件不NMS remap | 11 | R2、C2、Q1 |
| Craft/NMS/Paper internals边界 | 2、11 | C1–C3 |
| Compatibility hacks隔离 | 12 | C4 |
| 自动检查和插件回归 | 13 | F3、Q1–Q2 |
| 1.21.1新增与复用列表 | 14 | Q3–Q5 |

## 6. 开始执行前的单次动作

### 任务 P0：建立独立计划入口

**文件：** 创建 `docs/architecture/meldrite.md`、`docs/plans/README.md`；原始设计和六个子计划整体复制到新仓库的 `docs/superpowers/`，相对链接保持不变。

- [ ] **步骤1：检查目标目录。** 运行 `test ! -e /home/xiaobai/Projects/Projects/Meldrite`；如果已存在，运行该目录的 `git status --short` 并检查内容，禁止自动覆盖。
- [ ] **步骤2：在确认新目录时初始化。**

```bash
mkdir /home/xiaobai/Projects/Projects/Meldrite
cd /home/xiaobai/Projects/Projects/Meldrite
git init -b main
```

- [ ] **步骤3：复制完整文档并写入口。** 源分支已提交设计文档时，在新仓库根目录运行下列命令；应得到规格、调研记录、总计划和六个子计划共9个文件。随后创建`docs/architecture/meldrite.md`链接规格、`docs/plans/README.md`链接本总计划，记录“G0未完成；当前仅为设计文档”，不要添加已实现徽章。

```bash
git -C /home/xiaobai/Projects/Projects/Taiyitist archive xiaobai/meldrite-architecture-plan docs/superpowers | tar -x -C /home/xiaobai/Projects/Projects/Meldrite
find docs/superpowers -type f -name '*.md' | wc -l
```
- [ ] **步骤4：检查链接。** 用 Python 遍历 `docs/**/*.md` 中相对 `.md` 链接，去掉 fragment 后逐一验证存在；打印不存在的路径并非0退出。预期没有缺失链接。
- [ ] **步骤5：提交设计基线。**

```bash
git add docs
git commit -m "docs: establish Meldrite architecture and delivery plans"
```

执行方式由下一轮用户决定：可用逐任务子代理与两阶段审查，或在当前会话按 executing-plans 内联执行。本文交付不启动上述实现流程。
