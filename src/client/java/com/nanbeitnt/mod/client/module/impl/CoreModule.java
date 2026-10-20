package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.Module;
import com.nanbeitnt.mod.client.setting.KeySetting;
import org.lwjgl.glfw.GLFW;

/** 不可关闭的模块，只放客户端自身的设置（目前是打开界面的键）。 */
public class CoreModule extends Module {
	private final KeySetting openKey = add(new KeySetting("open_key", "打开界面的键", GLFW.GLFW_KEY_RIGHT_SHIFT));

	public CoreModule() {
		super("core", "客户端核心", Category.MISC, false);
	}

	public KeySetting getOpenKey() {
		return openKey;
	}
}
