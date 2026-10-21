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
| JDK | Gradle/Loom 运行时 25，编译 toolchain 21（**Loom 1.18.x 要求运行时 ≥ 25，CI 不能改回 21**） |

> mod id 与包名是两套东西：`nanbeitnts_mod` 是资源命名空间（`assets/nanbeitnts_mod/...`、
> 注册表 ID 前缀），`com.nanbeitnt.mod` 是 Java 包路径。两者不一致是正常的，别去"统一"。

---

## 1. 入口与全局常量

| 位置 | 名称 | 说明 |
| --- | --- | --- |
| `com.nanbeitnt.mod.NanbeiTnTSMod` | `onInitialize()` | 主入口（双端）。本模组逻辑全在客户端，这里只留 `MOD_ID`、`LOGGER`、`id()` |
| 同上 | `MOD_ID` | `"nanbeitnts_mod"`，不要硬编码字符串 |
| 同上 | `LOGGER` | 统一日志器 |
| 同上 | `id(String path)` | 生成 `Identifier`，所有资源 ID 都走它 |
| `com.nanbeitnt.mod.client.NanbeiTnTSModClient` | `onInitializeClient()` | 客户端入口：初始化模块、注册 HUD、注册 tick |
| 同上 | `onClientTick(Minecraft)` | 轮询打开键（默认右 Shift），边沿触发开/关界面 |

> `build.gradle` 开了 `splitEnvironmentSourceSets()`：`src/main` 不能碰 `net.minecraft.client.*`。

---

## 2. 核心结构

### 模块体系

| 类 | 职责 |
| --- | --- |
| `client.module.Module` | 模块基类：id、显示名、分类、enabled、expanded、设置列表，`add(setting)` 注册设置 |
| `client.module.Category` | 分类枚举：`COMBAT` 战斗 / `HUD` 显示 / `VISUAL` 视觉 / `MISC` 杂项 |
| `client.module.ModuleManager` | 唯一入口：`init()` 注册全部模块并读档；`all()` / `get(id)` / `byCategory(c)` / `renderHud(g)` |
| `client.module.HudModule` | 需要在屏幕上常驻显示的模块实现它：`renderHud(GuiGraphics)` |

### 设置类型（`client.setting`）

| 类 | 界面表现 | 存进 JSON 的类型 |
| --- | --- | --- |
| `Setting<T>` | 抽象父类，只有 id / 显示名 / 序列化 | — |
| `BooleanSetting` | 右侧小开关 | 布尔 |
| `NumberSetting` | 右侧滑块 + 数值，带 min/max/step | 数字 |
| `KeySetting` | 右侧键位框，点击后按任意键 | 整数（GLFW 键码） |

### 配置

| 位置 | 说明 |
| --- | --- |
| `client.config.ConfigManager` | `save()` / `load()`，路径 `config/nanbeitnts_mod/config.json`，Gson 序列化 |
| 触发时机 | 切开关、改设置、拖动滑块、改键、关闭界面时各存一次（够用，且不怕游戏崩） |

### 界面

| 类 | 说明 |
| --- | --- |
| `client.gui.Theme` | 深色扁平配色的全部色值，换主题只改这里 |
| `client.gui.ClickGuiScreen` | 主界面：左分类栏 + 右卡片列，卡片可展开显示设置；全部手绘 |
| 内部 `Hit` / `HitType` | 绘制时记录可点击区域，点击时反查。新增控件要同时补 `HitType` 和 `mouseClicked` 分支 |

---

## 3. 已注册的模块

| id | 显示名 | 分类 | 可关闭 | 设置 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `core` | 客户端核心 | 杂项 | 否 | `open_key` 打开界面的键（默认右 Shift） | 客户端自身设置 |
| `demo_hud` | HUD 演示 | 显示 | 是 | `y_offset` 垂直位置 0–300；`show_version` 显示版本号 | P0 验证用：开启后左上角常驻一行字 |

## 4. 资源与数据文件

| 路径 | 用途 |
| --- | --- |
| `src/main/resources/fabric.mod.json` | 模组元数据 |
| `src/main/resources/nanbeitnts_mod.mixins.json` | 主端 Mixin 配置（当前列表为空） |
| `src/client/resources/nanbeitnts_mod.client.mixins.json` | 客户端 Mixin 配置（当前列表为空） |
| `src/main/resources/assets/nanbeitnts_mod/icon.png` | 模组图标 128×128 |
| `config/nanbeitnts_mod/config.json` | 运行时生成的配置（在 `.minecraft/` 下，不在仓库里） |

### 关于 Mixin（重要）

两个 mixin 配置目前都是**空列表**，模板自带的 `ExampleMixin` / `ExampleClientMixin` 已删除。
原因：生产环境的 mixin 需要 refmap 把 Mojmap 方法名翻译成 intermediary 名，而打包产物里没有生成 refmap，
`required: true` 会让游戏启动时直接崩。骨架阶段用不到 mixin，先清空。

以后真要写 mixin 时：在 `client/mixin/` 下建类并加进 `nanbeitnts_mod.client.mixins.json` 的 `client` 数组，
然后**必须用生产 jar 实测一次**（`./gradlew runClient` 跑的是开发环境，掩盖 refmap 问题）。
若加载报 "no refmap" 之类，需要在 mixin json 里补 `"refmap": "nanbeitnts_mod-refmap.json"` 并让 Loom 生成。


---

## 5. 代码约定

- **mod id 常量**：任何 `Identifier` 都用 `NanbeiTnTSMod.id("xxx")`。
- **注释**：只写"为什么"的关键一两行，不写逐行解释。
- **新增一个功能 = 四步**（照这个来，界面和配置都不用改）：
  1. 在 `client.module.impl` 下新建类继承 `Module`，构造函数里 `super(id, 显示名, Category.X, true)`；
  2. 需要设置就 `add(new XxxSetting("id", "显示名", 默认值...))`；
  3. 要常驻显示就 `implements HudModule` 并实现 `renderHud`，要每 tick 跑就加重写的 tick 方法（后续补）；
  4. 在 `ModuleManager.init()` 里 `register(new XxxModule())`，最后到本文档第 3 节加一行。
- **不碰界面**：模块里不读文件、不画界面、不处理按键。

---

## 6. 变更记录

| 日期 | 变更 |
| --- | --- |
| 2026-10-20 | 初版。mod id 由 `nanbeitnts-mod` 改为 `nanbeitnts_mod`；许可证改 MIT；接入 JDK 21 toolchain。 |
| 2026-10-20 | P0 骨架：模块体系、设置类型、配置读写、深色扁平主界面、HUD 注册、可改的打开键；新增 `core`、`demo_hud` 两个模块。 |
| 2026-10-21 | 修 `ClickGuiScreen` 点击死区：卡片展开区原本止于 `toggleX-20`，而 `+/-` 画在 `toggleX-14`，落在展开区与开关区之间的空隙里，点加号没反应。现在展开区右端贴到开关左侧（`toggleX-6`），"常开" 卡片则延伸到 `toggleX+36`。 |
| 2026-10-21 | 删除模板示例 mixin（无 refmap 会在生产环境崩），两个 mixin 配置清空；产出可直接安装的发布 jar 并在真实 1.21.11 实例中验证通过。 |
