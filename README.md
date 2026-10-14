# Meldrite

Meldrite 是基于 Fabric 的 Minecraft 服务端，提供 Bukkit/Paper 插件接口，让 Mod 与插件在同一服务端运行。当前主要支持 Minecraft 1.20.1。

## 功能

- 使用 Fabric Loader 加载 Mod，通过通用接口适配 Bukkit 插件。
- 提供插件命令、事件、调度器及玩家、实体、世界接口。
- 通过 Fabric Permissions API 连接 Mod 与插件权限，支持 Mod 版 LuckPerms 管理插件权限节点。
- 使用 JLine 3 提供命令编辑、补全、历史和终端颜色，统一 Minecraft 日志格式。
- 提供可用 `java -jar` 启动的独立服务端包。

## 启动

使用 Java 21，将服务端 JAR 放到独立目录：

```bash
java -Xms2G -Xmx6G -jar meldrite-1.20.1.jar nogui
```

发行 JAR 仅包含启动器、Meldrite 和必要的本地 API 文件。首次启动会从 Mojang、Fabric 和 Maven Central 下载锁定的依赖，并按 SHA-256 校验，保存到 `libraries/meldrite/`；后续启动复用缓存，依赖齐全时可离线启动。缓存缺失或损坏时会重新下载。首次启动需要联网，下载失败时会显示具体依赖和原因，修复网络后再次执行启动命令即可。

依赖准备完成后在当前 JVM 内启动原版 Fabric，内存参数只作用于一个进程。

地图颜色转换会先在调用线程取得像素快照，再将大图片的独立像素范围分摊到有界计算线程池，返回前等待全部结果。小图片直接在调用线程计算，插件自己的地图颜色缓存保留原有行为。

可通过 `java --add-modules=jdk.incubator.vector -Xms2G -Xmx6G -jar meldrite-1.20.1.jar nogui` 同时启用可选的 Vector API。向量宽度由 JDK 根据硬件选择，保留 Bukkit 的颜色匹配精度；未启用该模块或平台不支持时使用标量算法。`-Dmeldrite.vector.disabled=true` 只关闭 SIMD，`-Dmeldrite.compute.threads=1` 关闭计算分片。

首次启动后阅读并接受 Minecraft EULA，将 `eula.txt` 中的 `eula` 设置为 `true`，然后重新启动。Mod 放入 `mods/`，插件放入 `plugins/`。

控制台支持 `--nojline`（普通输入）和 `--noconsole`（关闭命令输入）。权限管理使用权限 Mod 的原有命令，例如 `lp user 玩家名 permission set 插件权限节点 true`。

初始化时显示 Meldrite ASCII 图案、当前版本、JDK 版本及作者。标签随 JVM 的显示 Locale 切换：中文使用中文标签，其他语言使用英文；可通过 `-Duser.language=zh -Duser.country=CN` 或 `-Duser.language=en -Duser.country=US` 指定语言。

项目仍在开发中，部分 Bukkit/Paper 接口尚未完整实现；部署前请在独立测试服验证所需 Mod 和插件功能。

## 多核调优

插件主动提交的异步任务与内部纯计算使用独立、有界的线程池，避免阻塞式 I/O 占满计算资源。默认依据 JVM 可用 CPU 数选择并行度：32 个逻辑核心时使用最多 8 个插件异步 worker，纯计算最多 4 路协作（调用线程加 3 个 worker）。线程按需创建，空闲 30 秒后释放；同步调度、世界修改、事件和 Fabric tick 回调保留原线程与顺序。

以下参数放在 `-jar` 前，修改后重启生效。CPU 数尊重 JVM 的容器限制和 `-XX:ActiveProcessorCount`；自动选择有上限，为游戏主线程及其他 Mod 的工作池保留余量。

| JVM 属性 | 默认值 | 作用 |
| --- | --- | --- |
| `-Dmeldrite.async.threads=0` | 自动，2–8 | 插件异步 worker 数，手动值 1–64 |
| `-Dmeldrite.async.queue-capacity=256` | `max(128, worker 数 × 32)` | 等待队列容量，手动值 1–65536；表中 256 是 8 个 worker 时的值 |
| `-Dmeldrite.compute.threads=0` | 自动，1–4 | 纯计算总并行度，包含调用线程，手动值 1–64 |
| `-Dmeldrite.compute.minimum-batch=4096` | 4096 | 每片至少处理的像素数，避免小图片调度开销 |

后台或有权限的管理员可用 `meldrite:workers` 查看活跃 worker、队列、完成数、异步拒绝数、计算回退次数及向量宽度。异步队列满时沿用显式拒绝与错误报告，插件回调不会被转移到主线程执行；内部纯计算队列满时由调用线程完成分片，保持同步返回和完整结果。此优化不会自动并行执行第三方 Mod 的世界逻辑。

## 源码

`modules/` 包含公共服务，`adapters/` 包含插件接口适配，`versions/` 包含版本相关实现，`distributions/` 负责服务端打包，`build-tools/` 提供依赖锁校验和构建辅助工具。构建使用 Gradle Wrapper 和 Java 21，依赖版本及校验和记录在 `contracts/locks/` 和 `gradle/`；需先准备锁定的依赖缓存。

```bash
./gradlew :mc1201:assemble -Pmc=1.20.1 --configure-on-demand
```

产物位于 `distributions/mc-1.20.1/build/server/`。版本格式为 `yyyy.MM.dd.commits-minecraftVersion-dev`，可通过 `-Pmeldrite.channel=release` 或 `pre-release` 选择发布类型。

## 许可证

项目采用 [GNU GPL v3](LICENSE)。包含或改编的上游代码保留各自的版权及许可证声明。
