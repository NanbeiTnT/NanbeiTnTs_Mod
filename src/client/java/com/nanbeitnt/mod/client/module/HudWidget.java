package com.nanbeitnt.mod.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;

/**
 * 屏幕上可以被拖动摆放的一个 HUD 组件。
 * 坐标是左上角的 GUI 坐标；编辑器里直接用同一套坐标渲染，保证拖完就是最终效果。
 *
 * 尺寸分两层：getBaseWidth/Height 是原始像素，getWidth/Height 是乘过缩放的显示尺寸。
 * 子类在自己的 render 里必须用 base 尺寸算内部布局，否则会被缩放乘两次。
 */
public abstract class HudWidget {
	public static final float MIN_SCALE = 0.5f;
	public static final float MAX_SCALE = 2.0f;

	private final String id;
	private int x;
	private int y;
	private float scale = 1f;

	protected HudWidget(String id, int defaultX, int defaultY) {
		this.id = id;
		this.x = defaultX;
		this.y = defaultY;
	}

	public String getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Math.round(scale * 10f) / 10f));
	}

	public abstract int getBaseWidth(Minecraft mc);

	public abstract int getBaseHeight(Minecraft mc);

	public int getWidth(Minecraft mc) {
		return Math.max(1, Math.round(getBaseWidth(mc) * scale));
	}

	public int getHeight(Minecraft mc) {
		return Math.max(1, Math.round(getBaseHeight(mc) * scale));
	}

	/** 按缩放画到 (x, y)。缩放以左上角为原点，所以内部一律按 0,0 起画。 */
	public final void renderScaled(GuiGraphics graphics, int x, int y) {
		if (scale == 1f) {
			render(graphics, x, y);
			return;
		}
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(scale, scale);
		render(graphics, 0, 0);
		pose.popMatrix();
	}

	/** 未缩放的绘制，左上角为传入的 x/y。 */
	public abstract void render(GuiGraphics graphics, int x, int y);
}
