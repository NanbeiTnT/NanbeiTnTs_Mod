# 开发参考 · Dev Reference

> **这是整个模组代码的索引。** 新增的类、常量、工具方法、注册项、资源文件，都要在这里登记一行。
> 目的有两个：给自己查（避免重复造轮子、避免代码越写越乱），给维护者看（不用读代码也能知道模组有什么）。
>
> 代码里不写教学式的长注释 —— 看懂逻辑是 AI 的事，这里才是给人看的目录。

---

## 0. 基本信息

| 项 | 值 |
| --- | --- |
| 模组名 | NanbeiTnT's Mod |
| mod id | `nanbeitnts_mod` |
| Java 包名 | `com.nanbeitnt.mod` |
| 当前版本 | 1.0.0 |
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.5 |
| Fabric API | 0.141.6+1.21.11 |
| 映射 | Mojang 官方映射（`officialMojangMappings`） |
| 许可证 | MIT |

> mod id 与包名是两套东西：`nanbeitnts_mod` 是资源命名空间（`assets/nanbeitnts_mod/...`、
> 注册表 ID 前缀），`com.nanbeitnt.mod` 是 Java 包路径。两者不一致是正常的，别去"统一"。

---

## 1. 入口与全局常量

| 位置 | 名称 | 说明 |
| --- | --- | --- |
| `com.nanbeitnt.mod.NanbeiTnTSMod` | `onInitialize()` | 主入口，双端都会执行。注册物品/方块/事件在这里发起 |
| 同上 | `MOD_ID` | `"nanbeitnts_mod"`，不要硬编码字符串，一律引用它 |
| 同上 | `LOGGER` | 统一日志器，日志名即 mod id |
| 同上 | `id(String path)` | 生成 `Identifier`（`nanbeitnts_mod:path`）。所有资源 ID 都走这个方法 |

| 位置 | 名称 | 说明 |
| --- | --- | --- |
| `com.nanbeitnt.mod.client.NanbeiTnTSModClient` | `onInitializeClient()` | 客户端入口，只在客户端执行（渲染、输入、界面相关） |

> `build.gradle` 开了 `splitEnvironmentSourceSets()`，所以 `src/main` 里的代码不能碰
> `net.minecraft.client.*`，否则服务端加载时直接崩。客户端代码一律放 `src/client`。

---

## 2. 工具类与公共方法

> 放 `com.nanbeitnt.mod.util` 下的复用工具。写之前先查这张表，别重复造。

| 类 | 方法 / 字段 | 说明 |
| --- | --- | --- |
| — | — | （暂无） |

---

## 3. 注册内容清单

> 每加一个内容就登记一行。注册名一律小写下划线，路径与 `assets`/`data` 下的文件名一致。

### 3.1 物品 Items

| 注册名 | Java 类 | 资源文件 | 备注 |
| --- | --- | --- | --- |
| — | — | — | （暂无） |

### 3.2 方块 Blocks

| 注册名 | Java 类 | 掉落 / 挖掘等级 | 备注 |
| --- | --- | --- | --- |
| — | — | — | （暂无） |

### 3.3 方块实体 Block Entities

| 注册名 | Java 类 | 备注 |
| --- | --- | --- |
| — | — | （暂无） |

### 3.4 实体 Entity Types

| 注册名 | Java 类 | 备注 |
| --- | --- | --- |
| — | — | （暂无） |

### 3.5 状态效果 / 附魔 / 药水

| 类型 | 注册名 | Java 类 | 备注 |
| --- | --- | --- | --- |
| — | — | — | （暂无） |

### 3.6 创造模式标签页 Creative Tabs

| 注册名 | Java 类 | 包含内容 |
| --- | --- | --- |
| — | — | — | （暂无） |

### 3.7 声音 / 粒子

| 类型 | 注册名 | 备注 |
| --- | --- | --- |
| — | — | （暂无） |

---

## 4. 网络与事件

| 类型 | 标识 | 方向 | 说明 |
| --- | --- | --- | --- |
| — | — | — | （暂无） |

---

## 5. 资源与数据文件

| 路径 | 用途 |
| --- | --- |
| `src/main/resources/fabric.mod.json` | 模组元数据（id / 入口 / 依赖 / 许可证） |
| `src/main/resources/nanbeitnts_mod.mixins.json` | 主端 Mixin 配置 |
| `src/client/resources/nanbeitnts_mod.client.mixins.json` | 客户端 Mixin 配置 |
| `src/main/resources/assets/nanbeitnts_mod/icon.png` | 模组图标，128×128 |
| `assets/nanbeitnts_mod/lang/en_us.json` | （待建）英文语言文件 |
| `assets/nanbeitnts_mod/lang/zh_cn.json` | （待建）中文语言文件 |
| `assets/nanbeitnts_mod/textures/` | （待建）贴图 |
| `assets/nanbeitnts_mod/models/` | （待建）模型 JSON |
| `data/nanbeitnts_mod/` | （待建）配方、战利品表、标签 |

---

## 6. 代码约定

- **mod id 常量**：任何 `Identifier` 都用 `NanbeiTnTSMod.id("xxx")`，不手写 `"nanbeitnts_mod:xxx"`。
- **注释**：只写"为什么这么做"的关键一两行，不写逐行解释、不写教程式长注释。
- **包结构**：`mod`（入口）/ `mod.registry`（注册）/ `mod.block` `mod.item` `mod.entity`（内容）/ `mod.util`（工具）/ `mod.mixin`；客户端对应 `mod.client.*`。
- **注册时机**：所有注册在 `onInitialize()` 的静态初始化里完成，注册顺序不影响结果，但同一类内容集中在同一个 `Registry` 类里。
- **数据生成**：需要批量生成模型/配方/标签时再引入 `fabricApi.configureDataGeneration()`，引入后本节更新。

---

## 7. 变更记录

| 日期 | 变更 |
| --- | --- |
| 2026-10-20 | 初版。mod id 由 `nanbeitnts-mod` 改为 `nanbeitnts_mod`；许可证改 MIT；接入 JDK 21 toolchain。 |
