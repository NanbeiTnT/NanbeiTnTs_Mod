package com.nanbeitnt.mod.client.gui;

import com.nanbeitnt.mod.client.config.ConfigManager;
import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.Module;
import com.nanbeitnt.mod.client.module.ModuleManager;
import com.nanbeitnt.mod.client.setting.BooleanSetting;
import com.nanbeitnt.mod.client.setting.KeySetting;
import com.nanbeitnt.mod.client.setting.NumberSetting;
import com.nanbeitnt.mod.client.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 主界面：左分类、右模块卡片、卡片展开后是设置项。
 * 全部手绘，不用原版按钮 —— 因为原版控件撑不出这种扁平风格。
 */
public class ClickGuiScreen extends Screen {
	private static final int SIDEBAR_W = 128;
	private static final int HEADER_H = 46;
	private static final int CARD_H = 38;
	private static final int ROW_H = 24;

	private final List<Hit> hits = new ArrayList<>();

	private Category selected = Category.HUD;
	private double scroll;
	private double maxScroll;
	private NumberSetting dragSlider;
	private int dragLeft;
	private int dragWidth;
	private KeySetting awaitingKey;

	private int panelX;
	private int panelY;
	private int panelW;
	private int panelH;

	public ClickGuiScreen() {
		super(Component.literal("NanbeiTnT's Mod"));
	}

	@Override
	protected void init() {
		panelW = Math.min(this.width - 40, 640);
		panelH = Math.min(this.height - 40, 430);
		panelX = (this.width - panelW) / 2;
		panelY = (this.height - panelH) / 2;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		Font font = Minecraft.getInstance().font;
		hits.clear();

		graphics.fill(0, 0, this.width, this.height, Theme.OVERLAY);
		graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, Theme.PANEL);
		graphics.renderOutline(panelX, panelY, panelW, panelH, Theme.BORDER);

		graphics.drawString(font, "NanbeiTnT's Mod", panelX + 16, panelY + 15, Theme.ACCENT, false);
		String hint = "右Shift 关闭 · v1.0.0";
		graphics.drawString(font, hint, panelX + panelW - 16 - font.width(hint), panelY + 15, Theme.TEXT_DIM, false);
		graphics.fill(panelX, panelY + HEADER_H - 1, panelX + panelW, panelY + HEADER_H, Theme.BORDER);

		graphics.fill(panelX, panelY + HEADER_H, panelX + SIDEBAR_W, panelY + panelH, Theme.SIDEBAR);
		graphics.fill(panelX + SIDEBAR_W - 1, panelY + HEADER_H, panelX + SIDEBAR_W, panelY + panelH, Theme.BORDER);

		drawSidebar(graphics, font);

		graphics.enableScissor(panelX + SIDEBAR_W, panelY + HEADER_H, panelX + panelW, panelY + panelH);
		drawContent(graphics, font, mouseX, mouseY);
		graphics.disableScissor();
	}

	private void drawSidebar(GuiGraphics graphics, Font font) {
		int y = panelY + HEADER_H + 10;
		for (Category category : Category.values()) {
			boolean isSelected = category == selected;
			if (isSelected) {
				graphics.fill(panelX, y, panelX + SIDEBAR_W - 1, y + 26, Theme.SIDEBAR_SELECTED);
				graphics.fill(panelX, y, panelX + 2, y + 26, Theme.ACCENT);
			}
			graphics.drawString(font, category.getLabel(), panelX + 18, y + 9,
					isSelected ? Theme.ACCENT : Theme.TEXT_DIM, false);
			hits.add(new Hit(panelX, y, panelX + SIDEBAR_W - 1, y + 26, HitType.CATEGORY, category));
			y += 28;
		}
	}

	private void drawContent(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		List<Module> modules = ModuleManager.byCategory(selected);

		int x = panelX + SIDEBAR_W + 12;
		int w = panelW - SIDEBAR_W - 24;
		int y = panelY + HEADER_H + 10 - (int) scroll;

		if (modules.isEmpty()) {
			graphics.drawString(font, "这个分类下还没有功能", x + 4, y + 4, Theme.TEXT_DIM, false);
			maxScroll = 0;
			return;
		}

		int total = 0;
		for (Module module : modules) {
			int cardH = CARD_H + (module.isExpanded() ? module.getSettings().size() * ROW_H + 8 : 0);
			boolean hover = isInside(mouseX, mouseY, x, y, x + w, y + CARD_H);

			graphics.fill(x, y, x + w, y + cardH, hover ? Theme.CARD_HOVER : Theme.CARD);
			graphics.renderOutline(x, y, w, cardH, Theme.BORDER);
			graphics.drawString(font, module.getName(), x + 10, y + 15, Theme.TEXT, false);

			int toggleX = x + w - 10 - 30;
			int toggleY = y + (CARD_H - 14) / 2;
			int expandRight;
			if (module.canDisable()) {
				drawToggle(graphics, toggleX, toggleY, module.isEnabled());
				hits.add(new Hit(toggleX - 4, y, toggleX + 34, y + CARD_H, HitType.TOGGLE, module));
				expandRight = toggleX - 6;
			} else {
				graphics.drawString(font, "常开", toggleX + 2, y + 15, Theme.TEXT_DIM, false);
				expandRight = toggleX + 36;
			}

			graphics.drawString(font, module.isExpanded() ? "-" : "+", toggleX - 14, y + 15, Theme.TEXT_DIM, false);
			// 展开区一直延伸到开关左边：加号画在 toggleX-14，留空隙会导致点到加号没反应
			hits.add(new Hit(x, y, expandRight, y + CARD_H, HitType.EXPAND, module));

			if (module.isExpanded()) {
				int rowY = y + CARD_H + 4;
				for (Setting<?> setting : module.getSettings()) {
					drawSetting(graphics, font, setting, x + 10, rowY, w - 20);
					rowY += ROW_H;
				}
			}

			y += cardH + 6;
			total += cardH + 6;
		}

		maxScroll = Math.max(0, total - (panelH - HEADER_H - 20));
	}

	private void drawSetting(GuiGraphics graphics, Font font, Setting<?> setting, int x, int y, int width) {
		graphics.drawString(font, setting.getName(), x, y + 8, Theme.TEXT_DIM, false);

		if (setting instanceof BooleanSetting bool) {
			int toggleX = x + width - 30;
			int toggleY = y + (ROW_H - 14) / 2;
			drawToggle(graphics, toggleX, toggleY, bool.get());
			hits.add(new Hit(toggleX - 4, y, toggleX + 34, y + ROW_H, HitType.BOOL, bool));
			return;
		}

		if (setting instanceof NumberSetting number) {
			int trackW = 90;
			int trackX = x + width - trackW - 36;
			int trackY = y + 11;

			graphics.fill(trackX, trackY, trackX + trackW, trackY + 3, Theme.TRACK);
			int filled = (int) (trackW * number.getProgress());
			graphics.fill(trackX, trackY, trackX + filled, trackY + 3, Theme.ACCENT);
			graphics.fill(trackX + filled - 1, trackY - 2, trackX + filled + 1, trackY + 5, 0xFFFFFFFF);

			String value = String.format(Locale.ROOT, "%.1f", number.get());
			graphics.drawString(font, value, trackX + trackW + 8, y + 8, Theme.TEXT_DIM, false);
			hits.add(new Hit(trackX, y, trackX + trackW, y + ROW_H, HitType.SLIDER, number));
			return;
		}

		if (setting instanceof KeySetting key) {
			String label = awaitingKey == key ? "按任意键…" : key.getDisplayName();
			int boxW = Math.max(48, font.width(label) + 16);
			int boxX = x + width - boxW;

			graphics.fill(boxX, y + 3, boxX + boxW, y + ROW_H - 3, Theme.TRACK);
			graphics.drawString(font, label, boxX + (boxW - font.width(label)) / 2, y + 8,
					awaitingKey == key ? Theme.ACCENT : Theme.TEXT, false);
			hits.add(new Hit(boxX, y, boxX + boxW, y + ROW_H, HitType.KEY, key));
		}
	}

	private void drawToggle(GuiGraphics graphics, int x, int y, boolean on) {
		graphics.fill(x, y, x + 30, y + 14, on ? Theme.ACCENT : Theme.TRACK);
		if (on) {
			graphics.fill(x + 18, y + 2, x + 28, y + 12, 0xFFFFFFFF);
		} else {
			graphics.fill(x + 2, y + 2, x + 12, y + 12, 0xFF7A828F);
		}
	}

	private static boolean isInside(double mouseX, double mouseY, int x1, int y1, int x2, int y2) {
		return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0) {
			return super.mouseClicked(event, doubleClick);
		}

		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit hit = hits.get(i);
			if (!hit.contains(event.x(), event.y())) {
				continue;
			}

			switch (hit.type) {
				case CATEGORY -> {
					selected = (Category) hit.target;
					scroll = 0;
				}
				case TOGGLE -> {
					((Module) hit.target).toggle();
					ConfigManager.save();
				}
				case EXPAND -> {
					Module module = (Module) hit.target;
					module.setExpanded(!module.isExpanded());
				}
				case BOOL -> {
					((BooleanSetting) hit.target).toggle();
					ConfigManager.save();
				}
				case SLIDER -> {
					dragSlider = (NumberSetting) hit.target;
					dragLeft = hit.x1;
					dragWidth = hit.x2 - hit.x1;
					updateSlider(event.x());
				}
				case KEY -> awaitingKey = (KeySetting) hit.target;
			}
			return true;
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragSlider != null) {
			updateSlider(event.x());
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		dragSlider = null;
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scroll -= scrollY * 20;
		scroll = Math.max(0, Math.min(maxScroll, scroll));
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (awaitingKey != null) {
			awaitingKey.set(event.key());
			awaitingKey = null;
			ConfigManager.save();
			return true;
		}
		return super.keyPressed(event);
	}

	private void updateSlider(double mouseX) {
		if (dragSlider == null) {
			return;
		}
		dragSlider.setProgress((mouseX - dragLeft) / (double) dragWidth);
		ConfigManager.save();
	}

	@Override
	public void onClose() {
		ConfigManager.save();
		super.onClose();
	}

	private enum HitType {
		CATEGORY, TOGGLE, EXPAND, BOOL, SLIDER, KEY
	}

	private record Hit(int x1, int y1, int x2, int y2, HitType type, Object target) {
		boolean contains(double mouseX, double mouseY) {
			return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
		}
	}
}
