# Meldrite

> 基于 Fabric 的 Bukkit/Paper 兼容 Minecraft 服务端

Meldrite 让 Mod 与 Bukkit/Paper 插件运行在同一个服务端进程中。当前主要开发和验证目标是 **Minecraft 1.20.1**；1.21.1 目录是独立适配，支持范围以实际发行说明为准。

## 目录

| 你要做什么 | 直接看这里 |
| --- | --- |
| 启动服务端 | [快速启动](#快速启动) |
| 降低内存压力、调整多核 | [资源调优](#资源调优) |
| 处理插件重载卡顿 | [插件扫描与重载](#插件扫描与重载) |
| 从源码构建 | [开发与构建](#开发与构建) |

## 能力概览

- Fabric Loader 加载 Mod，同时提供 Bukkit/Paper 插件 API 适配。
- 插件命令、事件、调度器，以及玩家、实体、世界和物品栏等常用服务。
- Fabric Permissions API 与 LuckPerms Mod 权限节点互操作。
- JLine 3 命令编辑、补全、历史记录和统一日志格式。
- 可直接使用 `java -jar` 启动的独立发行包。
- 插件异步任务、纯计算和 JAR 扫描使用独立的有界线程池；同步世界修改和 Fabric tick 始终保留原线程顺序。

## 快速启动

### 1. 准备目录

使用 Java 21，把发行 JAR 放入独立目录。首次启动会创建 `mods/`、`plugins/` 和 `libraries/meldrite/`，并下载、校验锁定依赖。

### 2. 启动

```bash
java -Xms2G -Xmx6G -jar meldrite-1.20.1.jar nogui
```

发行 JAR 只包含启动器、Meldrite 和必要的本地 API 文件。依赖下载完成后会在同一个 JVM 中启动 Fabric；后续启动会复用缓存，缓存缺失或校验失败时自动重新下载。

### 3. 完成首次配置

启动一次后打开 `eula.txt`，将 `eula=false` 改为 `eula=true`，再重新启动。Mod 放入 `mods/`，插件放入 `plugins/`。控制台可使用 `--nojline` 或 `--noconsole`。

需要 SIMD 地图颜色计算时，可以在 `-jar` 前加入 Vector API：

```bash
java --add-modules=jdk.incubator.vector -Xms2G -Xmx6G \
  -jar meldrite-1.20.1.jar nogui
```

平台不支持 Vector API 时会自动使用标量算法。`-Dmeldrite.vector.disabled=true` 可单独关闭 SIMD。

## 资源调优

Meldrite 不会替插件或 Mod 强制调用 `System.gc()`。更稳定的做法是限制排队任务和临时线程，让不可执行的任务尽早被拒绝或取消，从而减少闭包、快照和插件类加载器被长时间持有。异步队列取消时会主动移除尚未开始的任务，服务端关闭时线程池也会按超时顺序停止。

### 内存建议

| 场景 | 建议 |
| --- | --- |
| 小型测试服 | `-Xms1G -Xmx3G` |
| 常规插件服 | `-Xms2G -Xmx6G` |
| 大型服 | 先观察 GC 日志和 RSS，再逐步增加 `-Xmx`；为操作系统、原生库和 Mod 保留至少 1–2G |

`-Xmx` 不是越大越好。若容器或主机只有固定内存，给 JVM 留出非堆空间，避免 Java 堆、线程栈和原生分配合计超过上限。Java 21 默认使用 G1；只有在有 GC 日志证据时才调整收集器参数。

### 多核参数

以下属性放在 `-jar` 前，修改后重启生效。自动值尊重 JVM 容器限制和 `-XX:ActiveProcessorCount`，并为游戏主线程及其他 Mod 保留余量。

| JVM 属性 | 默认值 | 范围与作用 |
| --- | --- | --- |
| `-Dmeldrite.async.threads=0` | 自动，1–8 | 插件异步 worker；手动值 1–64 |
| `-Dmeldrite.async.queue-capacity=0` | 自动，`max(32, worker×8)` | 待执行任务上限；手动值 1–65536。队列越小，内存占用越低，满载时越早报告拒绝 |
| `-Dmeldrite.compute.threads=0` | 自动，1–4 | 纯计算总并行度，包含调用线程；手动值 1–64 |
| `-Dmeldrite.compute.minimum-batch=4096` | 4096 | 每个计算分片的最小输入量，避免小任务产生调度开销 |
| `-Dmeldrite.discovery.threads=0` | 自动，1–4 | 启动和插件 reload 时的 JAR 读取与 SHA-256；手动值 1–16 |

例如，内存较紧张的 4 核服可以使用：

```bash
java -Xms2G -Xmx4G \
  -Dmeldrite.async.threads=2 \
  -Dmeldrite.async.queue-capacity=32 \
  -Dmeldrite.compute.threads=1 \
  -jar meldrite-1.20.1.jar nogui
```

队列满时插件异步任务保持显式拒绝，不会偷偷转移到主线程；纯计算分片会退回调用线程，以保证同步返回和完整结果。后台或有权限的管理员可执行 `/meldrite workers` 查看活跃 worker、队列、完成数、拒绝数、计算回退次数和向量宽度。

## 插件扫描与重载

插件 JAR 的描述文件读取和 SHA-256 计算使用临时有界线程池；每次扫描结束后关闭，结果按排序后的插件顺序在调用线程解析。类加载、依赖规划、生命周期回调和资源应用仍按原顺序执行。

1.20.1 的裸 `/reload` 保留原版数据包重载语义。完整 Bukkit 插件重载使用 `/bukkit:reload` 或 `Bukkit.reload()`。重载日志以 `Meldrite.Reload` 记录各阶段；耗时达到 1 秒的阶段会输出警告。扫描并行化不能消除插件同步启停或资源应用造成的主线程阻塞。

## 项目结构

| 目录 | 职责 |
| --- | --- |
| `modules/` | 通用模型、SPI、服务、调度器和插件运行时 |
| `adapters/` | Bukkit/Paper API 适配与门面 |
| `versions/` | Minecraft/Fabric 原生实现、Mixin 和版本装配 |
| `distributions/` | 发行包、依赖安装和启动器 |
| `build-tools/` | 依赖锁、契约、架构和发行审计 |

## 开发与构建

项目使用 Gradle Wrapper。构建前先准备锁定依赖缓存：

```bash
python3 .github/scripts/prepare-build.py
./gradlew :mc1201:assemble -Pmc=1.20.1 --configure-on-demand
```

产物位于 `distributions/mc-1.20.1/build/server/`。常用检查：

```bash
./gradlew :foundation:test --configure-on-demand
./gradlew verifyContractArtifacts -Pprofile=paper-1.20.1 --configure-on-demand
git diff --check
```

测试源码和 fixture 按仓库约定保持 Git 忽略；缺少测试输入时，不将 `NO-SOURCE` 视为测试通过。

## 许可证

项目采用 [GNU GPL v3](LICENSE)。包含或改编的上游代码保留其原有版权和许可证声明。
