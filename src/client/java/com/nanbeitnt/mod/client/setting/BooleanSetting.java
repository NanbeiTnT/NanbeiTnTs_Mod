package com.nanbeitnt.mod.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting<Boolean> {
	private boolean value;

	public BooleanSetting(String id, String name, boolean defaultValue) {
		super(id, name);
		this.value = defaultValue;
	}

	@Override
	public Boolean get() {
		return value;
	}

	public void set(boolean v) {
		this.value = v;
	}

	public void toggle() {
		this.value = !this.value;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(value);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json != null && json.isJsonPrimitive()) {
			this.value = json.getAsBoolean();
		}
	}
}
