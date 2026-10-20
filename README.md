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
| Java | 21（toolchain 锁定本机 `C:/Users/1234567/Documents/Nanbei123456/Java/jdk-21`） |

## 常用命令

```bash
./gradlew build                             # 编译 + 打包，产物在 build/libs/
./gradlew runClient                         # 启动带模组的客户端
./gradlew runServer                         # 启动带模组的服务端
./gradlew --refresh-dependencies build      # 依赖异常时强制重新解析
```

国内网络下 Gradle 发行包或依赖下载慢，可在 `~/.gradle/init.gradle` 里换镜像源。

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
- JDK 21 不在 `JAVA_HOME` 里时，靠 `gradle.properties` 的
  `org.gradle.java.installations.paths` 指路；换机器/换路径记得同步改。

## 许可

MIT License，见 [LICENSE](LICENSE)。
