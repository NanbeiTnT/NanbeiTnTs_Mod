package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.gui.Theme;
import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.HudModule;
import com.nanbeitnt.mod.client.module.HudWidget;
import com.nanbeitnt.mod.client.module.Module;
import com.nanbeitnt.mod.client.setting.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/** P0 的验收用模块：屏幕上常驻一行字，证明「开关 → 生效」这条链路通了。 */
public class DemoModule extends Module implements HudModule {
	private final BooleanSetting showVersion = add(new BooleanSetting("show_version", "显示版本号", true));
	private final DemoNames widget = new DemoNames();

	public DemoModule() {
		super("demo_hud", "HUD 演示", Category.HUD, true);
	}

	@Override
	public List<HudWidget> widgets() {
		return List.of(widget);
	}

	private final class DemoNames extends HudWidget {
		private DemoNames() {
			super("demo_names", 4, 22);
		}

		@Override
		public int getBaseWidth(Minecraft mc) {
			if (mc.font == null) {
				return 0;
			}
			int w = mc.font.width("NanbeiTnT's Mod");
			return showVersion.get() ? Math.max(w, mc.font.width("v1.0.0")) : w;
		}

		@Override
		public int getBaseHeight(Minecraft mc) {
			return showVersion.get() ? 19 : 9;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.font == null) {
				return;
			}
			graphics.drawString(mc.font, "NanbeiTnT's Mod", x, y, Theme.ACCENT, true);
			if (showVersion.get()) {
				graphics.drawString(mc.font, "v1.0.0", x, y + 10, Theme.TEXT_DIM, true);
			}
		}
	}
}
