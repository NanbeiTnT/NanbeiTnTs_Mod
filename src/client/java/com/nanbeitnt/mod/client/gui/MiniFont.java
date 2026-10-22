package com.nanbeitnt.mod.client.gui;

import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;

/**
 * 3×5 像素手绘小字。游戏自带的字体最小就是 9px 高、单字 6px 宽，
 * 塞不进的地方（CPS 的单位、药水计时）用这套点阵；把位图字整体缩小会糊。
 * 没有阴影、没有描边，需要可读性时由调用方自己垫底色。
 */
public final class MiniFont {
	/** 字高，调用方排位置时用。 */
	public static final int CHAR_H = 5;
	private static final int CHAR_W = 3;
	// 字宽 3 + 字距 1
	private static final int STEP = CHAR_W + 1;

	private static final Map<Character, String[]> GLYPHS = Map.ofEntries(
			Map.entry('0', rows("111", "101", "101", "101", "111")),
			Map.entry('1', rows("010", "110", "010", "010", "111")),
			Map.entry('2', rows("111", "001", "111", "100", "111")),
			Map.entry('3', rows("111", "001", "111", "001", "111")),
			Map.entry('4', rows("101", "101", "111", "001", "001")),
			Map.entry('5', rows("111", "100", "111", "001", "111")),
			Map.entry('6', rows("111", "100", "111", "101", "111")),
			Map.entry('7', rows("111", "001", "001", "001", "001")),
			Map.entry('8', rows("111", "101", "111", "101", "111")),
			Map.entry('9', rows("111", "101", "111", "001", "111")),
			Map.entry('C', rows("111", "100", "100", "100", "111")),
			Map.entry('P', rows("111", "101", "111", "100", "100")),
			Map.entry('S', rows("111", "100", "111", "001", "111")),
			// 小写 m 在 3px 宽里做不出三笔，用两竖 + 中间连接近似；小写 s 直接借用大写形状
			Map.entry('m', rows("000", "101", "111", "101", "101")),
			Map.entry('s', rows("111", "100", "111", "001", "111")));

	private MiniFont() {
	}

	/** 整串占的像素宽，末字不留字距。 */
	public static int width(String text) {
		return text.isEmpty() ? 0 : text.length() * STEP - 1;
	}

	/** 字表里没有的字符跳过不画，不抛异常。 */
	public static void draw(GuiGraphics graphics, String text, int x, int y, int color) {
		for (int i = 0; i < text.length(); i++) {
			String[] glyph = GLYPHS.get(text.charAt(i));
			if (glyph == null) {
				continue;
			}
			int gx = x + i * STEP;
			for (int row = 0; row < CHAR_H; row++) {
				String line = glyph[row];
				for (int col = 0; col < CHAR_W; col++) {
					if (line.charAt(col) == '1') {
						graphics.fill(gx + col, y + row, gx + col + 1, y + row + 1, color);
					}
				}
			}
		}
	}

	private static String[] rows(String a, String b, String c, String d, String e) {
		return new String[]{a, b, c, d, e};
	}
}
