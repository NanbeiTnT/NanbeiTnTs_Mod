package com.nanbeitnt.mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nanbeitnt.mod.NanbeiTnTSMod;
import com.nanbeitnt.mod.client.gui.ClickGuiScreen;
import com.nanbeitnt.mod.client.module.ModuleManager;
import com.nanbeitnt.mod.client.pack.PackSync;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

public class NanbeiTnTSModClient implements ClientModInitializer {
	private static boolean openKeyWasDown;

	@Override
	public void onInitializeClient() {
		PackSync.registerBuiltinPacks();
		ModuleManager.init();

		HudElementRegistry.addLast(NanbeiTnTSMod.id("hud"),
				(graphics, deltaTracker) -> ModuleManager.renderHud(graphics));

		ClientTickEvents.START_CLIENT_TICK.register(ModuleManager::tick);
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		// 启动时把配置里的开关状态和 options.txt 里的包选择对齐；不一致才重载一次
		ClientLifecycleEvents.CLIENT_STARTED.register(PackSync::apply);
	}

	/**
	 * 打开界面的键用轮询而不是 KeyMapping：KeyMapping 注册后没法改键，
	 * 而这个键要允许玩家在模组界面里自己换。
	 */
	private void onClientTick(Minecraft mc) {
		int key = ModuleManager.core().getOpenKey().get();
		boolean down = InputConstants.isKeyDown(mc.getWindow(), key);

		if (down && !openKeyWasDown) {
			Screen current = mc.screen;
			if (current == null || current instanceof TitleScreen) {
				mc.setScreen(new ClickGuiScreen());
			} else if (current instanceof ClickGuiScreen gui) {
				gui.onClose();
			}
		}

		openKeyWasDown = down;

		// 材质包开关只在主菜单落地，游戏里拨了就得等退回主菜单
		if (PackSync.isPending() && mc.screen instanceof TitleScreen) {
			PackSync.apply(mc);
		}
	}

	/**
	 * 改键保存后立刻调用：把打开键记成"已按下"，压到玩家松手为止。
	 * 不这么做的话，玩家还按着刚录制的键，轮询会把它当成一次新的按下，界面当场被关掉。
	 */
	public static void suppressOpenKeyUntilRelease() {
		openKeyWasDown = true;
	}
}
