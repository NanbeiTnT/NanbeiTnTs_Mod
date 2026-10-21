package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.gui.HudEditorScreen;
import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.Module;
import com.nanbeitnt.mod.client.setting.ButtonSetting;
import net.minecraft.client.Minecraft;

/** 只提供入口，本身不是一个可开关的功能：点按钮进拖动界面。 */
public class HudLayoutModule extends Module {
	public HudLayoutModule() {
		super("hud_layout", "组件位置", Category.HUD, false);
		add(new ButtonSetting("edit", "自定义组件位置", () -> {
			Minecraft mc = Minecraft.getInstance();
			mc.setScreen(new HudEditorScreen(mc.screen));
		}));
	}
}
