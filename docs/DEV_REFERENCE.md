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
| `client.gui.MiniFont` | 3×5 像素手绘小字（字宽 3、字距 1、字高 5，`width(text)` / `draw(g, text, x, y, color)`）。字符集：`0-9`、`C`、`P`、`S`、`m`、`s`；表里没有的字符跳过不画。**没有阴影和描边**，需要可读性时调用方自己垫底色。游戏自带字体最小 9px 高、单字 6px 宽，塞不进的地方用它，别去缩放位图字（会糊） |
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
| `cps` | CPS 显示 | 显示 | 是 | 无 | 58×15，格式「12 CPS ｜ 8 CPS」，竖杠写死在 x=29；数字用正常字体（左组右对齐、右组左对齐），`CPS` 用 `MiniFont` 小字；每帧用 GLFW 上升沿计数，取最近 1000ms 的次数 |
| `auto_sprint` | 自动疾跑 | 杂项 | 是 | 无 | 按着前进键就强制疾跑；骑乘/旁观/飞行/潜行/饥饿≤6 时不介入 |
| `effect_timer` | 药水效果时间 | 显示 | 是 | 无 | 原版药水图标下方补一行剩余时间，`MiniFont` 小字（5px 高、4 字约 15px 宽），格式 `3m12` / 不足一分钟 `45`（**秒不带单位**：小字里 s 和 5 撞形），去掉小时段、分钟累加。**不是 HudWidget**：位置跟随原版图标由代码算出，不能在布局界面拖动。`HudElementRegistry.attachElementAfter(VanillaHudElements.STATUS_EFFECTS, ...)`，注册在模块构造函数里（`ModuleManager.init()` 在 `onInitializeClient` 内，时机合规） |
| `gamma_override` | 伽马值覆写 | 视觉 | 是 | 无 | 开启后把亮度固定覆写成 10.0（暗处像夜视），关闭还原。**不能用 `OptionInstance.set()`**：超出 0–1 会被打日志并替换成默认值 0.5，只能反射改内部的 `value` 字段（按"非 static、非 final、类型 Object"识别，不写死字段名）。每 tick 检查被改回就重压；`CLIENT_STOPPING` 时还原；启动时若发现值 >1（上次崩溃残留）重置成 1.0 |
| `pack_short_swords` | 短剑 | 视觉 | 是 | 无 | 内置包 `nanbeitnts_mod:short_swords` |
| `pack_wool_outline` | 羊毛描边 | 视觉 | 是 | 无 | 内置包 `nanbeitnts_mod:wool_outline` |
| `pack_transparent_gui` | 透明 GUI | 视觉 | 是 | 无 | 内置包 `nanbeitnts_mod:transparent_gui` |
| `pack_sky` | 天空覆盖 | 视觉 | 是 | 无 | 内置包 `nanbeitnts_mod:sky`。**需要额外装 Skyboxify**（或 Nuit+Interop），原版不认 OptiFine 天空格式。生效期间原版云隐藏（`LevelRendererMixin`） |
| `pack_crosshair` | 准星 | 视觉 | 是 | 无 | 内置包 `nanbeitnts_mod:crosshair` |

## 4. 资源与数据文件

| 路径 | 用途 |
| --- | --- |
| `src/main/resources/fabric.mod.json` | 模组元数据 |
| `src/main/resources/nanbeitnts_mod.mixins.json` | 主端 Mixin 配置（当前列表为空） |
| `src/client/resources/nanbeitnts_mod.client.mixins.json` | 客户端 Mixin 配置（当前含 `LevelRendererMixin`） |
| `src/main/resources/assets/nanbeitnts_mod/icon.png` | 模组图标 128×128 |
| `src/client/resources/resourcepacks/<id 路径>/` | 内置材质包本体（`short_swords` / `wool_outline` / `transparent_gui` / `sky` / `crosshair`）。**路径必须与 Identifier 的 path 一致**，Fabric 按 `resourcepacks/<path>` 找 |
| `config/nanbeitnts_mod/config.json` | 运行时生成的配置（在 `.minecraft/` 下，不在仓库里） |

### 内置材质包（`client.pack.PackSync`）

- 注册：`onInitializeClient` 里 `PackSync.registerBuiltinPacks()` 用 `net.fabricmc.fabric.api.resource.v1.ResourceLoader.registerBuiltinPack(id, container, 显示名, PackActivationType.NORMAL)`，
  `NORMAL` = 默认关闭、玩家也能在游戏自己的资源包列表里手动开关。
- id 就是 `id.toString()`（`nanbeitnts_mod:short_swords`），也就是 `Options.resourcePacks` 里存的那个字符串。
- **开关不立即生效**：拨动只 `markPending()`，等 `Screen instanceof TitleScreen` 的那一 tick 才同步
  （`options.resourcePacks` 增删 id → `resourcePackRepository.setSelected` → `reloadResourcePacks()`），
  避免游戏中途重载资源卡顿；启动时 `CLIENT_STARTED` 对齐一次（不一致才重载）。
- 新增包只需三步：把包解到 `src/client/resources/resourcepacks/<path>/`、在 `PackSync` 加一个 `Identifier` 常量并进 `DEFS`、
  在 `ModuleManager.init()` `register(new ResourcePackModule(...))`。
- 已知风险：与其它同名文件的包（Bare Bones、Faithful 等）同时启用时，谁生效取决于列表顺序（当前实现是**追加到列表末尾**）。
  玩家反馈"开了没效果"时，先看这一点。


### 关于 Mixin（重要）

打包产物里**没有 refmap 文件，但这是正常的**：Loom 1.18 在 `remapJar` 阶段直接把注解里的名字改写成 intermediary
（实测 `remapJar` 后 `LevelRendererMixin.class` 常量池里是 `Lnet/minecraft/class_761;` + `method = "method_62204"`，
即 `LevelRenderer` / `addCloudsPass`）。所以 mixin json 里**不要**写 `"refmap"` 字段，写了反而找不到文件。
前提是注入点只用名字/注解表达，**不要在 `@At` 里写需要翻译的方法名字符串**（那种就要 refmap）。

现有客户端 mixin：

| 类 | 目标 | 作用 |
| --- | --- | --- |
| `client.mixin.LevelRendererMixin` | `LevelRenderer.addCloudsPass`（HEAD，cancel） | 天空覆盖包生效时跳过原版云渲染。判定用 `PackSync.isSkyPackLoaded()`（看 `options.resourcePacks` 实际是否含 `nanbeitnts_mod:sky`），不改玩家的云设置 |

新增 mixin：在 `client/mixin/` 下建类、加进 `nanbeitnts_mod.client.mixins.json` 的 `client` 数组，
然后**用生产 jar 在真实实例里实测**（`runClient` 跑的是开发环境，不覆盖生产映射问题）。


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
- **构造函数里绝对不能碰 `Minecraft.getInstance().options` / `font` / `level` 这类成员**：
  `onInitializeClient` 是在 `Minecraft.<init>` 内部被回调的，此时实例只是半成品（`options` 还是 null），
  读了就是启动即崩（`GammaModule` 踩过）。要延后就挂 `ClientLifecycleEvents.CLIENT_STARTED` 或放到 tick/渲染里。

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
| 2026-10-22 | 新增 `effect_timer`（药水效果时间）：挂在 `VanillaHudElements.STATUS_EFFECTS` 之后，用原版 `renderEffects` 的同一套排版（图标 24×24、横格 25、行距 26；良性在 y=1 一行、恶性 y=1+26n）算出每个图标坐标，在底边居中画剩余时间（向上取整到秒）。打开会展示效果详情的界面（`screen.showsActiveEffects()`）时随原版一起不画。无限时长不画文字。**故意不做成 `HudWidget`**：位置由原版图标推导，拖动会错位。 |
| 2026-10-22 | `effect_timer` 时间格式改为 `3m12`：去掉小时段、分钟累加，分钟存在时秒不写单位（`3m12s` 有 5 个字约 28px，塞不进 25px 的格子）；不足一分钟仍写 `12s`，否则单个数字看不出含义。 |
| 2026-10-22 | 新增 `gamma_override`（伽马值覆写，视觉）：亮度覆写到 10.0。**踩坑：`OptionInstance.set()` 会先过 `ValueSet.validateValue`（gamma 是 `UnitDouble`，只允许 0–1），超范围时打 `Illegal option value` 日志并返回 `initialValue`（0.5），等于无效**。改为反射改 `OptionInstance.value` 字段（筛选：非 static、非 final、类型 Object —— 跨 Mojmap/intermediary 都唯一命中）。`LightTexture.tick()` 每 tick 置 `updateLightTexture`，所以改字段下一 tick 就生效。配置加载走 `applyLoadedState` 不触发 `onEnable`，原值改在 `onTick` 里补记。 |
| 2026-10-22 | `effect_timer` 剩余时间彻底不带 `s`（点阵小字里 s 和 5 撞形）：有分钟 `3m12`，不足一分钟只剩 `45`。 |
| 2026-10-22 | 抽出 `client.gui.MiniFont`（3×5 点阵，字符集 `0-9 C P S m s`），`CpsModule` 原来私有的 `MINI_C/P/S` 点阵与 `drawMini` 全部删掉改用共用类（渲染结果不变）。`effect_timer` 的剩余时间也换成小字：5px 高、`3m12` 只占 15px，纵向不再压到下一行图标（原 9px 字会盖住下一行顶部 7px）。 |
| 2026-10-22 | 修启动崩溃：`gamma_override` 在构造函数里读了 `mc.options.gamma()`，但 `onInitializeClient` 跑在 `Minecraft.<init>` 内部、`options` 仍是 null → `NullPointerException` 直接挂在 entrypoint。改为把"启动清理残留亮度"挂到 `ClientLifecycleEvents.CLIENT_STARTED`。 |
| 2026-10-22 | 天空覆盖包生效时不再画原版云：新增客户端首个 mixin `LevelRendererMixin`，注入 `LevelRenderer.addCloudsPass` HEAD 并 cancel。判定看 `options.resourcePacks` 是否实际含 `nanbeitnts_mod:sky`（`PackSync.isSkyPackLoaded()`），因此与天空贴图的生效时机一致（回主菜单），也不改玩家的"云"设置。顺带确认 Loom 1.18 的 remapJar 会直接把注解名改写成 intermediary，不需要 refmap。 |
| 2026-10-22 | 新增 5 个内置材质包开关（视觉）：短剑 / 羊毛描边 / 透明 GUI / 天空覆盖 / 准星。包体在 `src/client/resources/resourcepacks/<id>/`，用 `ResourceLoader.registerBuiltinPack` 以 `NORMAL` 注册，开关状态延迟到回主菜单时才同步（`PackSync`），避免对战中重载资源。天空包依赖 Skyboxify 才有效果。 |
