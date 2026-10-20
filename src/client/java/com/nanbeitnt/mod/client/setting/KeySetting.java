package com.nanbeitnt.mod.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.InputConstants;

public class KeySetting extends Setting<Integer> {
	private int key;

	public KeySetting(String id, String name, int defaultKey) {
		super(id, name);
		this.key = defaultKey;
	}

	@Override
	public Integer get() {
		return key;
	}

	public void set(int key) {
		this.key = key;
	}

	public String getDisplayName() {
		return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(key);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json != null && json.isJsonPrimitive()) {
			this.key = json.getAsInt();
		}
	}
}
