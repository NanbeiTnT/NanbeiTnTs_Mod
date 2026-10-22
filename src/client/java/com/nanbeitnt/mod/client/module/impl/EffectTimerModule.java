package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.NanbeiTnTSMod;
import com.nanbeitnt.mod.client.gui.MiniFont;
import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 原版药水图标下面补一行剩余时间。
 * 位置是照原版 renderEffects 的排版重算的，所以不能拖 —— 图标位置一动文字就错位。
 */
public class EffectTimerModule extends Module {
	// 原版布局：图标背景 24×24，横格 25、行距 26
	private static final int ICON_BOX = 24;
	private static final int SLOT_W = 25;
	private static final int SLOT_H = 26;
	private static final int TEXT_COLOR = 0xFFFFFFFF;

	public EffectTimerModule() {
		super("effect_timer", "药水效果时间", Category.HUD, true);
		// 挂在原版效果元素之后：图标由原版画，我们只补文字
		HudElementRegistry.attachElementAfter(VanillaHudElements.STATUS_EFFECTS,
				NanbeiTnTSMod.id("effect_timer"), this::render);
	}

	private void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (!isEnabled()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		// 打开背包这类会展示效果详情的界面时，原版本身就不画图标，文字也不能单独留着
		if (mc.screen != null && mc.screen.showsActiveEffects()) {
			return;
		}

		Collection<MobEffectInstance> active = mc.player.getActiveEffects();
		if (active.isEmpty()) {
			return;
		}

		// 与原版 Ordering.natural().reverse() 同序，保证文字和图标一一对上
		List<MobEffectInstance> effects = new ArrayList<>(active);
		effects.sort(Comparator.reverseOrder());

		int width = graphics.guiWidth();
		int baseY = 1 + (mc.isDemo() ? 15 : 0);
		int beneficial = 0;
		int harmful = 0;

		for (MobEffectInstance effect : effects) {
			if (!effect.showIcon()) {
				continue;
			}
			int x;
			int y = baseY;
			if (effect.getEffect().value().isBeneficial()) {
				beneficial++;
				x = width - SLOT_W * beneficial;
			} else {
				harmful++;
				x = width - SLOT_W * harmful;
				y += SLOT_H * harmful;
			}

			if (effect.isInfiniteDuration()) {
				continue;
			}
			String text = formatDuration(effect.getDuration());
			int textWidth = MiniFont.width(text);
			// 居中对齐图标；往上提 1px，5px 的小字正好不压到下一行图标的顶边
			MiniFont.draw(graphics, text, x + ICON_BOX / 2 - textWidth / 2,
					y + ICON_BOX - 1, TEXT_COLOR);
		}
	}

	/**
	 * tick 转「3m12」：去掉小时段，分钟一直累加（90 分钟就显示 90m），秒一律不写单位。
	 * 秒不带单位是因为原版图标一格只有 25px（5 个字塞不下），而且点阵小字里 s 和 5 太像。
	 */
	private static String formatDuration(int ticks) {
		// 向上取整：还剩半个多秒时显示 1 比显示 0 更贴近直觉
		int total = Math.max(0, (ticks + 19) / 20);
		int minutes = total / 60;
		int seconds = total % 60;
		if (minutes > 0) {
			return minutes + "m" + seconds;
		}
		return Integer.toString(seconds);
	}
}
