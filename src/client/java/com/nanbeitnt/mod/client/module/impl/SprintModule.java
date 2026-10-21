package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** 按住前进键就强制疾跑，省掉按疾跑键那一步。 */
public class SprintModule extends Module {
	public SprintModule() {
		super("auto_sprint", "自动疾跑", Category.MISC, true);
	}

	@Override
	public void onTick(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || player.isPassenger() || player.isSpectator()) {
			return;
		}
		if (player.getAbilities().flying || player.isShiftKeyDown()) {
			return;
		}
		// 饥饿值降到原版疾跑门槛以下时硬设会被立刻取消，跳过，免得每 tick 反复抖动
		if (player.getFoodData().getFoodLevel() <= 6) {
			return;
		}
		if (mc.options.keyUp.isDown()) {
			player.setSprinting(true);
		}
	}
}
