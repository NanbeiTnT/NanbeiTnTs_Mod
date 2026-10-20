package com.nanbeitnt.mod.client.module;

/** 界面左侧的分类。顺序即显示顺序。 */
public enum Category {
	COMBAT("战斗"),
	HUD("显示"),
	VISUAL("视觉"),
	MISC("杂项");

	private final String label;

	Category(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
