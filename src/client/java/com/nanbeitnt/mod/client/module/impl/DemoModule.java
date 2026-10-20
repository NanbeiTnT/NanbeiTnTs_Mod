package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.gui.Theme;
import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.HudModule;
import com.nanbeitnt.mod.client.module.Module;
import com.nanbeitnt.mod.client.setting.BooleanSetting;
import com.nanbeitnt.mod.client.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** P0 的验收用模块：开启后屏幕左上角常驻一行字，证明「开关 → 生效」这条链路通了。 */
public class DemoModule extends Module implements HudModule {
	private final NumberSetting yOffset = add(new NumberSetting("y_offset", "垂直位置", 8, 0, 300, 1));
	private final BooleanSetting showVersion = add(new BooleanSetting("show_version", "显示版本号", true));

	public DemoModule() {
		super("demo_hud", "HUD 演示", Category.HUD, true);
	}

	@Override
	public void renderHud(GuiGraphics graphics) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.font == null) {
			return;
		}
		int y = (int) (double) yOffset.get();
		graphics.drawString(mc.font, "NanbeiTnT's Mod", 6, y, Theme.ACCENT, true);
		if (showVersion.get()) {
			graphics.drawString(mc.font, "v1.0.0", 6, y + 10, Theme.TEXT_DIM, true);
		}
	}
}
