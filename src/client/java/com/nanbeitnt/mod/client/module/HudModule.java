package com.nanbeitnt.mod.client.module;

import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/** 需要在屏幕上常驻显示内容的模块实现这个接口。 */
public interface HudModule {
	List<HudWidget> widgets();

	default void renderHud(GuiGraphics graphics) {
		for (HudWidget widget : widgets()) {
			widget.renderScaled(graphics, widget.getX(), widget.getY());
		}
	}
}
