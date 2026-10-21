package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.HudModule;
import com.nanbeitnt.mod.client.module.HudWidget;
import com.nanbeitnt.mod.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/** 左上角常驻帧数。Minecraft.getFps() 本身就是逐秒统计的实时值，不用自己计数。 */
public class FpsModule extends Module implements HudModule {
	private final FpsWidget widget = new FpsWidget();

	public FpsModule() {
		super("fps", "FPS 显示", Category.HUD, true);
	}

	@Override
	public List<HudWidget> widgets() {
		return List.of(widget);
	}

	private static final class FpsWidget extends HudWidget {
		private static final int PADDING = 3;
		private static final int TEXT_COLOR = 0xFFFFFFFF;
		private static final int BG_COLOR = 0x80000000;

		private FpsWidget() {
			super("fps", 4, 4);
		}

		@Override
		public int getBaseWidth(Minecraft mc) {
			return mc.font == null ? 0 : mc.font.width(text()) + PADDING * 2;
		}

		@Override
		public int getBaseHeight(Minecraft mc) {
			return 9 + PADDING * 2;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.font == null) {
				return;
			}

			int boxH = getBaseHeight(mc);
			// 盒子跟着文字宽度走，帧数位数变化时不会露出空隙；文字按行高居中后再 +1，补字形墨迹的偏移
			graphics.fill(x, y, x + getBaseWidth(mc), y + boxH, BG_COLOR);
			graphics.drawString(mc.font, text(), x + PADDING, y + (boxH - 9) / 2 + 1, TEXT_COLOR, true);
		}

		private static String text() {
			return Minecraft.getInstance().getFps() + " FPS";
		}
	}
}
