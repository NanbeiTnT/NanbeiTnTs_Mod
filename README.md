# NanbeiTnT's Mod

Minecraft Java Edition **1.21.11** 的 Fabric 模组。

## 环境

| 项目 | 版本 |
| --- | --- |
| Minecraft | 1.21.11（混淆时代最后一版，仍需 remap） |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.141.6+1.21.11 |
| Fabric Loom | 1.18.4（插件 `net.fabricmc.fabric-loom-remap`） |
| 映射 | Mojang 官方映射（`officialMojangMappings`） |
| Java | Gradle/Loom 用 **25**，编译用 **21**（见下方说明） |

### 为什么要两个 JDK

这不是冗余，缺一个都构建不了：

- **JDK 25** —— 跑 Gradle 守护进程。Loom 1.18.x 插件本体要求运行时 ≥ 25，用 21 会在配置阶段直接报
  `Dependency requires at least JVM runtime version 25`。
- **JDK 21** —— Minecraft 1.21.11 的编译目标，由 `java.toolchain` 指定，只负责编译和 `runClient`。

本机 JDK 路径写在 `~/.gradle/gradle.properties`（**不进仓库**）：

```properties
org.gradle.java.installations.paths=C:/Users/1234567/Documents/Nanbei123456/Java/jdk-21
```

CI 里由 `.github/workflows/build.yml` 装两个 JDK 并显式传路径，改那一步时注意顺序不能反。

## 常用命令

```bash
./gradlew build                             # 编译 + 打包，产物在 build/libs/
./gradlew runClient                         # 启动带模组的客户端
./gradlew runServer                         # 启动带模组的服务端
./gradlew --refresh-dependencies build      # 依赖异常时强制重新解析
```

国内网络下 Gradle 发行包或依赖下载慢，可在 `~/.gradle/init.gradle` 里换镜像源。

## 装进自己的游戏里试

`./gradlew build` 之后 `build/libs/` 下会有两个文件：

| 文件 | 用途 |
| --- | --- |
| `nanbeitnts_mod-1.0.0.jar` | **这个才是给游戏用的**（已 remap 成游戏内部名） |
| `nanbeitnts_mod-1.0.0-sources.jar` | 源码包，只给人阅读，别丢进 mods |

把前者复制到**对应版本实例**的 `mods` 目录。开着版本隔离时是：

```
.minecraft/versions/<版本名>/mods/nanbeitnts_mod-1.0.0.jar
```

关着隔离才是 `.minecraft/mods/`。放错位置游戏里不会报错，模组就是不出现，很容易误判成"没生效"。

前置条件（缺任一项都会启动失败或模组不加载）：

- **Fabric Loader** 0.19.5（与 `gradle.properties` 一致）
- **Fabric API** 0.141.6+1.21.11
- Java 21（1.21.11 本身要求）

更新版本时**直接覆盖同名 jar**，不要同时留两个 —— 两个同 id 的模组会互相冲突。

卸载就是删掉那个 jar，配置文件 `config/nanbeitnts_mod/config.json` 可以留着。

## 目录结构

```
src/main/java/com/nanbeitnt/mod/           主端代码（服务端也会加载）
src/main/resources/fabric.mod.json         模组元数据
src/main/resources/nanbeitnts_mod.mixins.json
src/client/java/com/nanbeitnt/mod/client/  客户端专属（渲染、按键、界面）
src/client/resources/nanbeitnts_mod.client.mixins.json
docs/DEV_REFERENCE.md                      代码索引：已建的类/方法/注册项
```

**开发时先看 [`docs/DEV_REFERENCE.md`](docs/DEV_REFERENCE.md)** —— 模组里有什么、某个工具方法叫什么、
某个物品的注册名是什么，都在那里面，不用翻代码。

`build.gradle` 启用了 `splitEnvironmentSourceSets()`：引用 `net.minecraft.client.*` 的代码
必须放 `src/client`，否则服务端启动会因为找不到客户端类而崩溃。

## 注意

- `mod id`（`nanbeitnts_mod`）是所有资源命名空间和注册表 ID 的前缀，发布后再改代价极大。
  要改名请在写第一个方块/物品之前。
- JDK 21 不在 `JAVA_HOME` 里时，靠 `~/.gradle/gradle.properties` 的
  `org.gradle.java.installations.paths` 指路；换机器/换路径记得同步改。
- 仓库 remote 走 SSH（`git@github.com:...`，`.ssh/config` 里把 github.com 指到 `ssh.github.com:443`），
  因为本机网络下 github.com 的 443 端口不通。

## 许可

MIT License，见 [LICENSE](LICENSE)。
