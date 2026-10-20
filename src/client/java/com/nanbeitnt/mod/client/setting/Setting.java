package com.nanbeitnt.mod.client.setting;

import com.google.gson.JsonElement;

/** 设置的公共父类：一个可序列化的命名值，界面按子类型决定怎么画。 */
public abstract class Setting<T> {
	private final String id;
	private final String name;

	protected Setting(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public abstract T get();

	public abstract JsonElement toJson();

	public abstract void fromJson(JsonElement json);
}
