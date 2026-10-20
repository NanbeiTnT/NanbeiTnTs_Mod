package com.nanbeitnt.mod.client.module;

import net.minecraft.client.gui.GuiGraphics;

/** 需要在屏幕上常驻显示内容的模块实现这个接口。 */
public interface HudModule {
	void renderHud(GuiGraphics graphics);
}
