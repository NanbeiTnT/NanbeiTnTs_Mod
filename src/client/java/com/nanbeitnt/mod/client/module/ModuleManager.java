package com.nanbeitnt.mod.client.module;

import com.nanbeitnt.mod.client.config.ConfigManager;
import com.nanbeitnt.mod.client.module.impl.CoreModule;
import com.nanbeitnt.mod.client.module.impl.CpsModule;
import com.nanbeitnt.mod.client.module.impl.DemoModule;
import com.nanbeitnt.mod.client.module.impl.FpsModule;
import com.nanbeitnt.mod.client.module.impl.HudLayoutModule;
import com.nanbeitnt.mod.client.module.impl.KeystrokesModule;
import com.nanbeitnt.mod.client.module.impl.SprintModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/** 所有模块的登记处：注册、按分类取、遍历渲染、读写配置都从这里走。 */
public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();
	private static CoreModule core;
	private static boolean hudHidden;

	private ModuleManager() {
	}

	public static void init() {
		core = (CoreModule) register(new CoreModule());
		register(new CpsModule());
		register(new DemoModule());
		register(new FpsModule());
		register(new HudLayoutModule());
		register(new KeystrokesModule());
		register(new SprintModule());
		ConfigManager.load();
	}

	public static Module register(Module module) {
		MODULES.add(module);
		return module;
	}

	public static List<Module> all() {
		return List.copyOf(MODULES);
	}

	public static Module get(String id) {
		for (Module module : MODULES) {
			if (module.getId().equals(id)) {
				return module;
			}
		}
		return null;
	}

	public static List<Module> byCategory(Category category) {
		List<Module> result = new ArrayList<>();
		for (Module module : MODULES) {
			if (module.getCategory() == category) {
				result.add(module);
			}
		}
		return result;
	}

	public static CoreModule core() {
		return core;
	}

	public static void renderHud(GuiGraphics graphics) {
		// 拖动布局时原 HUD 先别画，否则编辑器里会看到两份重叠
		if (hudHidden) {
			return;
		}
		for (Module module : MODULES) {
			if (module.isEnabled() && module instanceof HudModule hud) {
				hud.renderHud(graphics);
			}
		}
	}

	public static void setHudHidden(boolean hidden) {
		hudHidden = hidden;
	}

	/** 当前生效的所有 HUD 组件，布局编辑器用它列可拖动的目标。 */
	public static List<HudWidget> activeWidgets() {
		List<HudWidget> result = new ArrayList<>();
		for (Module module : MODULES) {
			if (module.isEnabled() && module instanceof HudModule hud) {
				result.addAll(hud.widgets());
			}
		}
		return result;
	}

	/** 在 tick 开头跑，早于玩家自身的移动计算，改疾跑这类状态才能当帧生效。 */
	public static void tick(Minecraft mc) {
		for (Module module : MODULES) {
			if (module.isEnabled()) {
				module.onTick(mc);
			}
		}
	}
}
