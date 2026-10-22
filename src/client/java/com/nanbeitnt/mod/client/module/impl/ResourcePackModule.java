package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.Module;
import com.nanbeitnt.mod.client.pack.PackSync;
import net.minecraft.resources.Identifier;

/**
 * 一个开关对应一个内置材质包：开 = 把包加进游戏的资源包选择，关 = 移出。
 * 真正生效在回到主菜单时（见 {@link PackSync}），游戏中拨动只记状态。
 */
public class ResourcePackModule extends Module {
	private final Identifier pack;

	public ResourcePackModule(String id, String name, Identifier pack) {
		super(id, name, Category.VISUAL, true);
		this.pack = pack;
		PackSync.track(this);
	}

	public String getPackId() {
		return pack.toString();
	}

	@Override
	protected void onEnable() {
		PackSync.markPending();
	}

	@Override
	protected void onDisable() {
		PackSync.markPending();
	}
}
