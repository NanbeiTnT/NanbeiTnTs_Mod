package com.nanbeitnt.mod.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

/** 只有动作、没有值的设置项：界面上画成一个按钮，点了就执行。 */
public class ButtonSetting extends Setting<Void> {
	private final Runnable action;

	public ButtonSetting(String id, String name, Runnable action) {
		super(id, name);
		this.action = action;
	}

	public void run() {
		action.run();
	}

	@Override
	public Void get() {
		return null;
	}

	@Override
	public JsonElement toJson() {
		return JsonNull.INSTANCE;
	}

	@Override
	public void fromJson(JsonElement json) {
	}
}
