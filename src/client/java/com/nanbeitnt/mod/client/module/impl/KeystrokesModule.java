package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.HudModule;
import com.nanbeitnt.mod.client.module.HudWidget;
import com.nanbeitnt.mod.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** 七个键位作为一个整体绘制、整体拖动，不拆成独立组件。 */
public class KeystrokesModule extends Module implements HudModule {
	private final KeystrokesWidget widget = new KeystrokesWidget();

	public KeystrokesModule() {
		super("keystrokes", "按键显示", Category.HUD, true);
	}

	@Override
	public List<HudWidget> widgets() {
		return List.of(widget);
	}

	private static final class KeystrokesWidget extends HudWidget {
		private static final int KEY_W = 18;
		private static final int KEY_H = 18;
		// 空格是扁条，只有普通键高的一半
		private static final int SPACE_H = 8;
		private static final int GAP = 2;
		// 空格和 LMB/RMB 要跟 A~D 左右对齐，整体宽度只能是三列键位 + 两条缝
		private static final int WIDTH = KEY_W * 3 + GAP * 2;
		private static final int HEIGHT = KEY_H * 3 + SPACE_H + GAP * 3;
		// 对半分（WIDTH - GAP 为偶数，除得尽，不会差 1 像素）
		private static final int HALF = (WIDTH - GAP) / 2;

		private static final int BG = 0x80000000;
		private static final int BG_DOWN = 0xB0FFFFFF;
		private static final int TEXT = 0xFFFFFFFF;
		private static final int TEXT_DOWN = 0xFF101318;

		private KeystrokesWidget() {
			super("keystrokes", 4, 60);
		}

		@Override
		public int getBaseWidth(Minecraft mc) {
			return WIDTH;
		}

		@Override
		public int getBaseHeight(Minecraft mc) {
			return HEIGHT;
		}

		@Override
		public void render(GuiGraphics graphics, int x, int y) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.font == null) {
				return;
			}

			int row2 = y + KEY_H + GAP;
			int row3 = row2 + KEY_H + GAP;
			int row4 = row3 + SPACE_H + GAP;
			// W 居中后左边缘正好落在 S 上，第二行三键自然对齐
			int wX = x + (WIDTH - KEY_W) / 2;

			drawKey(graphics, mc.font, wX, y, KEY_W, KEY_H, "W", mc.options.keyUp.isDown());
			drawKey(graphics, mc.font, x, row2, KEY_W, KEY_H, "A", mc.options.keyLeft.isDown());
			drawKey(graphics, mc.font, wX, row2, KEY_W, KEY_H, "S", mc.options.keyDown.isDown());
			drawKey(graphics, mc.font, x + (KEY_W + GAP) * 2, row2, KEY_W, KEY_H, "D", mc.options.keyRight.isDown());
			drawSpace(graphics, x, row3, WIDTH, SPACE_H, mc.options.keyJump.isDown());
			drawKey(graphics, mc.font, x, row4, HALF, KEY_H, "LMB", isMouseDown(mc, GLFW.GLFW_MOUSE_BUTTON_LEFT));
			drawKey(graphics, mc.font, x + HALF + GAP, row4, HALF, KEY_H, "RMB",
					isMouseDown(mc, GLFW.GLFW_MOUSE_BUTTON_RIGHT));
		}

		// 空格不写字，只画一条居中的横杠
		private static void drawSpace(GuiGraphics graphics, int x, int y, int w, int h, boolean down) {
			graphics.fill(x, y, x + w, y + h, down ? BG_DOWN : BG);
			int barW = w * 2 / 5;
			int barH = 1;
			int bx = x + (w - barW) / 2;
			int by = y + (h - barH) / 2;
			graphics.fill(bx, by, bx + barW, by + barH, down ? TEXT_DOWN : TEXT);
		}

		private static void drawKey(GuiGraphics graphics, Font font, int x, int y, int w, int h,
				String label, boolean down) {
			graphics.fill(x, y, x + w, y + h, down ? BG_DOWN : BG);
			// 按下时是黑字，再叠阴影会糊成一团
			graphics.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 9) / 2 + 1,
					down ? TEXT_DOWN : TEXT, !down);
		}

		private static boolean isMouseDown(Minecraft mc, int button) {
			// 1.21.11 的 Window 没有 getWindow() 了，句柄是 handle()
			return GLFW.glfwGetMouseButton(mc.getWindow().handle(), button) == GLFW.GLFW_PRESS;
		}
	}
}
