package com.nanbeitnt.mod.client.module;

import com.nanbeitnt.mod.client.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 一个功能就是一个模块。模块只负责自己的状态和逻辑，
 * 不碰文件读写、也不碰界面 —— 那两件事分别由 ConfigManager 和 ClickGuiScreen 统一处理。
 */
public abstract class Module {
	private final String id;
	private final String name;
	private final Category category;
	private final boolean canDisable;
	private final List<Setting<?>> settings = new ArrayList<>();

	private boolean enabled;
	private boolean expanded;

	protected Module(String id, String name, Category category, boolean canDisable) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.canDisable = canDisable;
	}

	protected <S extends Setting<?>> S add(S setting) {
		settings.add(setting);
		return setting;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Category getCategory() {
		return category;
	}

	public boolean canDisable() {
		return canDisable;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		if (!canDisable || this.enabled == value) {
			return;
		}
		this.enabled = value;
		if (value) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public List<Setting<?>> getSettings() {
		return Collections.unmodifiableList(settings);
	}

	/** 仅供配置加载使用：直接套用存档里的状态，不触发 onEnable/onDisable。 */
	public void applyLoadedState(boolean value) {
		this.enabled = value;
	}

	/** 每个客户端 tick 调用一次，只在模块开启时会被调到。 */
	public void onTick(Minecraft mc) {
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}
}
