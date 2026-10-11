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

首次启动后阅读并接受 Minecraft EULA，将 `eula.txt` 中的 `eula` 设置为 `true`，然后重新启动。Mod 放入 `mods/`，插件放入 `plugins/`。

控制台支持 `--nojline`（普通输入）和 `--noconsole`（关闭命令输入）。权限管理使用权限 Mod 的原有命令，例如 `lp user 玩家名 permission set 插件权限节点 true`。

项目仍在开发中，部分 Bukkit/Paper 接口尚未完整实现；部署前请在独立测试服验证所需 Mod 和插件功能。

## 源码

`modules/` 包含公共服务，`adapters/` 包含插件接口适配，`versions/` 包含版本相关实现，`distributions/` 负责服务端打包，`build-tools/` 提供依赖锁校验和构建辅助工具。构建使用 Gradle Wrapper 和 Java 21，依赖版本及校验和记录在 `contracts/locks/` 和 `gradle/`；需先准备锁定的依赖缓存。

```bash
./gradlew :mc1201:assemble -Pmc=1.20.1 --configure-on-demand
```

产物位于 `distributions/mc-1.20.1/build/server/`。版本格式为 `yyyy.MM.dd.commits-minecraftVersion-dev`，可通过 `-Pmeldrite.channel=release` 或 `pre-release` 选择发布类型。

## 许可证

项目采用 [GNU GPL v3](LICENSE)。包含或改编的上游代码保留各自的版权及许可证声明。
