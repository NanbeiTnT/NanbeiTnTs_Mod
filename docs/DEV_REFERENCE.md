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
| 同上 | `suppressOpenKeyUntilRelease()` | 改键保存后调用，把打开键记成"已按下"，压到玩家松手为止；没有它玩家还按着新键时会被轮询当成一次新按下，界面当场关掉 |
| 同上 | `openKeyWasDown` | `static` 字段，打开键的上一次状态，用于上升沿判定 |

> `build.gradle` 开了 `splitEnvironmentSourceSets()`：`src/main` 不能碰 `net.minecraft.client.*`。

---

## 2. 核心结构

### 模块体系

| 类 | 职责 |
| --- | --- |
| `client.module.Module` | 模块基类：id、显示名、分类、enabled、expanded、设置列表，`add(setting)` 注册设置；`onTick(Minecraft)` 每 tick 调一次 |
| `client.module.Category` | 分类枚举：`COMBAT` 战斗 / `HUD` 显示 / `VISUAL` 视觉 / `MISC` 杂项 |
| `client.module.ModuleManager` | 唯一入口：`init()` 注册全部模块并读档；`all()` / `get(id)` / `byCategory(c)` / `renderHud(g)` / `tick(mc)` |
| `client.module.HudModule` | 需要在屏幕上常驻显示的模块实现它：`widgets()` 返回自己的组件列表，默认 `renderHud` 逐个按自身坐标画 |
| `client.module.HudWidget` | HUD 组件基类：id、可存的位置（x/y）与缩放（scale，0.5–2.0，0.1 一档）、`getBaseWidth/Height`（原始像素）+ `getWidth/Height`（乘过缩放的显示尺寸）、`renderScaled`（按 scale 推矩阵后调 `render`）、`render(g, x, y)`。**子类 render 内部只能用 base 尺寸** |

### 设置类型（`client.setting`）

| 类 | 界面表现 | 存进 JSON 的类型 |
| --- | --- | --- |
| `Setting<T>` | 抽象父类，只有 id / 显示名 / 序列化 | — |
| `BooleanSetting` | 右侧小开关 | 布尔 |
| `NumberSetting` | 右侧滑块 + 数值，带 min/max/step | 数字 |
| `KeySetting` | 右侧键位框，点击后按任意键 | 整数（GLFW 键码） |
| `ButtonSetting` | 右侧按钮，点击执行传入的 `Runnable` | 无（存 `JsonNull`） |

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
| `client.gui.HudEditorScreen` | 拖动摆放 HUD 组件：半透明黑蒙版（不调 `super.render`，否则默认背景会糊掉游戏画面）、组件边框、右下角取消/确定；鼠标悬停在组件上滚轮 = 调整缩放（保持中心，`mouseScrolled`）；取消会把位置和缩放都还原；进入时 `ModuleManager.setHudHidden(true)`，退出时还原 |
| 内部 `Hit` / `HitType` | 绘制时记录可点击区域，点击时反查。新增控件要同时补 `HitType` 和 `mouseClicked` 分支 |

---

## 3. 已注册的模块

| id | 显示名 | 分类 | 可关闭 | 设置 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `core` | 客户端核心 | 杂项 | 否 | `open_key` 打开界面的键（默认右 Shift） | 客户端自身设置 |
| `demo_hud` | HUD 演示 | 显示 | 是 | `show_version` 显示版本号 | P0 验证用：屏幕左上常驻一行字（位置可拖） |
| `fps` | FPS 显示 | 显示 | 是 | 无 | 左上角常驻「帧数 FPS」，半透明黑底 0x80000000，盒宽随文字变，位置可拖 |
| `hud_layout` | 组件位置 | 显示 | 否 | `edit` 按钮「自定义组件位置」→ 打开 `HudEditorScreen` | 只是入口，不是可开关功能 |
| `keystrokes` | 按键显示 | 显示 | 是 | 无 | 7 键（W/A/S/D/空格/LMB/RMB）合成一个 58×68 的整体组件，只能整体拖；空格是 8px 扁条 + 居中 1px 横杠（不写字）；键位取 `mc.options.keyXxx.isDown()`，鼠标取 `GLFW.glfwGetMouseButton(mc.getWindow().handle(), ...)` |
| `cps` | CPS 显示 | 显示 | 是 | 无 | 58×15，格式「12 CPS ｜ 8 CPS」，竖杠写死在 x=29；数字用正常字体（左组右对齐、右组左对齐），`CPS` 用 3×5 手绘小字（`MINI_C/P/S` 点阵）；每帧用 GLFW 上升沿计数，取最近 1000ms 的次数 |
| `auto_sprint` | 自动疾跑 | 杂项 | 是 | 无 | 按着前进键就强制疾跑；骑乘/旁观/飞行/潜行/饥饿≤6 时不介入 |

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
  3. 要常驻显示就 `implements HudModule` 并实现 `widgets()`，每个组件写一个 `HudWidget` 子类（`getBaseWidth/getBaseHeight/render`），
     坐标和缩放由拖动界面改、由 `ConfigManager` 存进 `"widgets"`；要每 tick 跑就重写 `onTick(Minecraft)`（只在开启时被调到，挂点是 `START_CLIENT_TICK`，早于玩家移动计算）；
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
| 2026-10-21 | 标题栏高度 46 → 34，标题文字改为按行高垂直居中（原来固定 `panelY + 15`，栏高时看着偏上）。 |
| 2026-10-21 | 修改键后界面自动关闭：`ClickGuiScreen.keyPressed` 记录新键后调用 `suppressOpenKeyUntilRelease()`，等玩家松开再恢复监听。 |
| 2026-10-21 | 标题栏右侧提示改为读 `CoreModule.getOpenKey().getDisplayName()`，改键后跟着变；去掉卡片上的"常开"字样，不可关闭卡片的加减号移到开关空位居中。 |
| 2026-10-21 | 新增 `fps`（FPS 显示）：左上角半透明黑底 + 「NN FPS」，取 `Minecraft.getFps()`，逐秒刷新。 |
| 2026-10-21 | 修正纯开关卡片：没有设置项的模块不再画加减号、不留展开区（原来点一下会展开出空白）。`Module` 补 `onTick(Minecraft)`，`ModuleManager.tick()` 挂在 `START_CLIENT_TICK`；新增 `auto_sprint`（自动疾跑）。 |
| 2026-10-21 | FPS 文字在背景里偏上：改为按盒高居中后 +1（字形墨迹比 `drawString` 的 y 低一点）。Gradle wrapper 升到 9.8.0 后官方源下不动，`distributionUrl` 改指腾讯镜像。 |
| 2026-10-21 | HUD 组件位置可拖动：新增 `HudWidget` 基类（位置 + 尺寸 + 自绘）、`HudEditorScreen`（蒙版 + 拖动 + 取消/确定）、`ButtonSetting`（按钮型设置项）、`hud_layout` 模块（入口）。`HudModule` 从 `renderHud` 改为 `widgets()`；`demo_hud` 去掉 `y_offset`（位置改由拖动决定）；配置新增 `"widgets"` 段存组件坐标。 |
| 2026-10-21 | 新增 `keystrokes`（按键显示）：键宽 20、键高 18、缝 2，整体 64×78。空格宽 = 三列总宽，LMB/RMB 等分（各 31）。按下时反白（白底黑字、去阴影）。**1.21.11 的 `Window` 没有 `getWindow()`，句柄是 `handle()`**。 |
| 2026-10-21 | `keystrokes` 收窄：键宽 20 → 18，整体 64×78 → 58×69；空格从 18 高的整键改成 9 高的扁条，不再写「空格」，改成居中一条 2px 横杠（宽 = 空格的 2/5），按下时横杠同样反色。 |
| 2026-10-21 | 空格再调：横杠 2px → 1px，空格高 9 → 8（整体 58×68）。`HudWidget` 新增 `scale`（0.5–2.0）：尺寸拆成 `getBaseWidth/Height` + `getWidth/Height`，渲染走 `renderScaled`（`graphics.pose()` 推 `Matrix3x2fStack`）；`HudEditorScreen` 支持悬停在组件上滚轮缩放（0.1 一档，保持中心，取消可还原），配置 `"widgets"` 增加 `"scale"`。 |
| 2026-10-21 | 新增 `cps`（CPS 显示）：58×15，宽度对齐按键显示的空格键。**正常字号放不下两组「NN CPS」（约需 78px），所以 `CPS` 三个字母改用 3×5 手绘点阵小字**（`drawMini` 逐像素 `fill`），数字仍是正常字体。竖杠 `BAR_X = WIDTH/2` 写死，左组右对齐、右组左对齐，位数变化只影响外侧。计数：每帧读 `glfwGetMouseButton` 做上升沿，`Deque<Long>` 存最近 1000ms 的按下时间戳，`size()` 即 CPS（没有 tick 钩子，靠 HUD 每帧渲染驱动）。 |
