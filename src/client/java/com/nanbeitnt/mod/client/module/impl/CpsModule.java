package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.gui.MiniFont;
import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.HudModule;
import com.nanbeitnt.mod.client.module.HudWidget;
import com.nanbeitnt.mod.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** 左右键 CPS。统计最近 1000ms 内的按下次数，刷新跟随帧率。 */
public class CpsModule extends Module implements HudModule {
	private final CpsWidget widget = new CpsWidget();

	public CpsModule() {
		super("cps", "CPS 显示", Category.HUD, true);
	}

	@Override
	public List<HudWidget> widgets() {
		return List.of(widget);
	}

	private static final class CpsWidget extends HudWidget {
		// 宽度与按键显示的空格键一致（58），方便上下对齐
		private static final int WIDTH = 58;
		private static final int HEIGHT = 15;
		private static final int BG = 0x80000000;
		private static final int TEXT = 0xFFFFFFFF;
		private static final int BAR = 0x70FFFFFF;

		private static final long WINDOW_MS = 1000L;
		// 竖杠写死在 x=WIDTH/2。CPS 的 "CPS" 用 3×5 手绘小字（正常字号塞不下两组）
		private static final int BAR_X = WIDTH / 2;
		private static final int GAP = 2;
		private static final int MINI_W = MiniFont.width("CPS");

		private final Deque<Long> leftClicks = new ArrayDeque<>();
		private final Deque<Long> rightClicks = new ArrayDeque<>();
		private boolean leftWasDown;
		private boolean rightWasDown;

		private CpsWidget() {
			super("cps", 4, 44);
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

			// 没有 tick 钩子，用每帧的上升沿计数；按住不放只算一次
			updateClicks(mc);
			String left = Integer.toString(leftClicks.size());
			String right = Integer.toString(rightClicks.size());

			graphics.fill(x, y, x + WIDTH, y + HEIGHT, BG);

			int textY = y + (HEIGHT - 9) / 2 + 1;
			int miniY = y + (HEIGHT - MiniFont.CHAR_H) / 2 + 1;
			graphics.fill(x + BAR_X, y + 4, x + BAR_X + 1, y + 12, BAR);

			// 左组贴竖杠右边对齐、右组贴竖杠左边对齐，数字位数变化时只有外侧在动
			int leftRight = x + BAR_X - GAP;
			MiniFont.draw(graphics, "CPS", leftRight - MINI_W, miniY, TEXT);
			graphics.drawString(mc.font, left, leftRight - MINI_W - 2 - mc.font.width(left), textY, TEXT, true);

			int rightLeft = x + BAR_X + 1 + GAP;
			graphics.drawString(mc.font, right, rightLeft, textY, TEXT, true);
			MiniFont.draw(graphics, "CPS", rightLeft + mc.font.width(right) + 2, miniY, TEXT);
		}

		private void updateClicks(Minecraft mc) {
			long now = System.currentTimeMillis();
			long handle = mc.getWindow().handle();
			boolean leftDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
			boolean rightDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

			if (leftDown && !leftWasDown) {
				leftClicks.addLast(now);
			}
			if (rightDown && !rightWasDown) {
				rightClicks.addLast(now);
			}
			leftWasDown = leftDown;
			rightWasDown = rightDown;

			prune(leftClicks, now);
			prune(rightClicks, now);
		}

		private static void prune(Deque<Long> clicks, long now) {
			while (!clicks.isEmpty() && now - clicks.peekFirst() > WINDOW_MS) {
				clicks.pollFirst();
			}
		}
	}
}
