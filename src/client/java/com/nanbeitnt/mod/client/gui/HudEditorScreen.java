package com.nanbeitnt.mod.client.gui;

import com.nanbeitnt.mod.client.config.ConfigManager;
import com.nanbeitnt.mod.client.module.HudWidget;
import com.nanbeitnt.mod.client.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 拖动摆放 HUD 组件。背景不画默认的模糊遮罩，直接盖一层半透明黑，让游戏画面还看得见。 */
public class HudEditorScreen extends Screen {
	private static final int MASK = 0xB0000000;
	private static final int BUTTON_W = 64;
	private static final int BUTTON_H = 20;
	private static final int MARGIN = 16;
	// 滚轮一格变化量；与 HudWidget.setScale 的 0.1 对齐
	private static final float SCALE_STEP = 0.1f;

	private final Screen parent;
	private final List<HudWidget> widgets = new ArrayList<>();
	private final Map<String, float[]> snapshot = new HashMap<>();
	private boolean snapped;
	private HudWidget dragging;
	private int grabX;
	private int grabY;

	public HudEditorScreen(Screen parent) {
		super(Component.literal("组件位置"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ModuleManager.setHudHidden(true);
		if (snapped) {
			return;
		}
		// 取消时要还原到进来之前的位置和大小，所以先拍一份快照
		for (HudWidget widget : ModuleManager.activeWidgets()) {
			widgets.add(widget);
			snapshot.put(widget.getId(), new float[]{widget.getX(), widget.getY(), widget.getScale()});
		}
		snapped = true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// 不调 super.render：那会画一层默认背景，把下面的游戏画面糊掉
		graphics.fill(0, 0, this.width, this.height, MASK);

		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		graphics.drawString(font, "拖动组件调整位置 · 滚轮调整大小 · Esc 取消", MARGIN, MARGIN, Theme.TEXT_DIM, true);

		for (HudWidget widget : widgets) {
			int w = widget.getWidth(mc);
			int h = widget.getHeight(mc);
			boolean active = widget == dragging || isInside(mouseX, mouseY, widget.getX(), widget.getY(), w, h);
			graphics.renderOutline(widget.getX() - 2, widget.getY() - 2, w + 4, h + 4,
					active ? Theme.ACCENT : 0x50FFFFFF);
			widget.renderScaled(graphics, widget.getX(), widget.getY());
		}

		drawButton(graphics, font, cancelX(), "取消", false, mouseX, mouseY);
		drawButton(graphics, font, confirmX(), "确定", true, mouseX, mouseY);
	}

	private void drawButton(GuiGraphics graphics, Font font, int x, String label, boolean primary,
			int mouseX, int mouseY) {
		int y = buttonY();
		boolean hover = isInside(mouseX, mouseY, x, y, BUTTON_W, BUTTON_H);
		int background = primary
				? (hover ? 0xFF6F9BFF : Theme.ACCENT)
				: (hover ? Theme.CARD_HOVER : Theme.CARD);
		graphics.fill(x, y, x + BUTTON_W, y + BUTTON_H, background);
		graphics.renderOutline(x, y, BUTTON_W, BUTTON_H, Theme.BORDER);
		graphics.drawString(font, label, x + (BUTTON_W - font.width(label)) / 2,
				y + (BUTTON_H - 9) / 2 + 1, primary ? 0xFFFFFFFF : Theme.TEXT, false);
	}

	private int confirmX() {
		return this.width - MARGIN - BUTTON_W;
	}

	private int cancelX() {
		return confirmX() - 8 - BUTTON_W;
	}

	private int buttonY() {
		return this.height - MARGIN - BUTTON_H;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0) {
			return super.mouseClicked(event, doubleClick);
		}

		int buttonY = buttonY();
		if (isInside(event.x(), event.y(), cancelX(), buttonY, BUTTON_W, BUTTON_H)) {
			cancel();
			return true;
		}
		if (isInside(event.x(), event.y(), confirmX(), buttonY, BUTTON_W, BUTTON_H)) {
			confirm();
			return true;
		}

		Minecraft mc = Minecraft.getInstance();
		// 后画的在上层，所以从后往前找
		for (int i = widgets.size() - 1; i >= 0; i--) {
			HudWidget widget = widgets.get(i);
			if (isInside(event.x(), event.y(), widget.getX(), widget.getY(),
					widget.getWidth(mc), widget.getHeight(mc))) {
				dragging = widget;
				grabX = (int) event.x() - widget.getX();
				grabY = (int) event.y() - widget.getY();
				return true;
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging == null) {
			return super.mouseDragged(event, dragX, dragY);
		}

		Minecraft mc = Minecraft.getInstance();
		int x = (int) event.x() - grabX;
		int y = (int) event.y() - grabY;
		x = Math.max(0, Math.min(this.width - dragging.getWidth(mc), x));
		y = Math.max(0, Math.min(this.height - dragging.getHeight(mc), y));
		dragging.setPosition(x, y);
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		dragging = null;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY == 0) {
			return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}

		Minecraft mc = Minecraft.getInstance();
		// 后画的在上层，从后往前找最顶上那个
		for (int i = widgets.size() - 1; i >= 0; i--) {
			HudWidget widget = widgets.get(i);
			if (!isInside(mouseX, mouseY, widget.getX(), widget.getY(),
					widget.getWidth(mc), widget.getHeight(mc))) {
				continue;
			}

			// 以中心为基准缩放，组件不会因为变大变小往一边跑
			int centerX = widget.getX() + widget.getWidth(mc) / 2;
			int centerY = widget.getY() + widget.getHeight(mc) / 2;
			widget.setScale(widget.getScale() + (scrollY > 0 ? SCALE_STEP : -SCALE_STEP));

			int w = widget.getWidth(mc);
			int h = widget.getHeight(mc);
			widget.setPosition(clamp(centerX - w / 2, 0, this.width - w),
					clamp(centerY - h / 2, 0, this.height - h));
			return true;
		}

		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			cancel();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		cancel();
	}

	private void cancel() {
		for (HudWidget widget : widgets) {
			float[] state = snapshot.get(widget.getId());
			if (state != null) {
				widget.setPosition((int) state[0], (int) state[1]);
				widget.setScale(state[2]);
			}
		}
		leave();
	}

	private void confirm() {
		ConfigManager.save();
		leave();
	}

	private void leave() {
		ModuleManager.setHudHidden(false);
		Minecraft.getInstance().setScreen(parent);
	}

	private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
	}

	private static int clamp(int value, int min, int max) {
		if (max <= min) {
			return min;
		}
		return Math.max(min, Math.min(max, value));
	}
}
