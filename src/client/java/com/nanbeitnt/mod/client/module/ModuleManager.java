package com.nanbeitnt.mod.client.module;

import com.nanbeitnt.mod.client.config.ConfigManager;
import com.nanbeitnt.mod.client.module.impl.CoreModule;
import com.nanbeitnt.mod.client.module.impl.DemoModule;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/** 所有模块的登记处：注册、按分类取、遍历渲染、读写配置都从这里走。 */
public final class ModuleManager {
	private static final List<Module> MODULES = new ArrayList<>();
	private static CoreModule core;

	private ModuleManager() {
	}

	public static void init() {
		core = (CoreModule) register(new CoreModule());
		register(new DemoModule());
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
		for (Module module : MODULES) {
			if (module.isEnabled() && module instanceof HudModule hud) {
				hud.renderHud(graphics);
			}
		}
	}
}
