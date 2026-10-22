package com.nanbeitnt.mod.client.module.impl;

import com.nanbeitnt.mod.NanbeiTnTSMod;
import com.nanbeitnt.mod.client.module.Category;
import com.nanbeitnt.mod.client.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 把游戏亮度（gamma）拉到远超原版上限的值，暗处也看得清。
 *
 * <p>原版 {@code OptionInstance.set} 会先校验 0–1，超出范围就记一条 "Illegal option value" 日志
 * 并把值换成默认的 0.5，所以 set() 这条路走不通，只能直接改内部的 value 字段。
 * 字段靠"非 static、非 final、类型 Object"识别，不硬编码字段名 —— 生产环境是混淆名，写死必挂。
 */
public class GammaModule extends Module {
	// 原版亮度滑条上限是 1.0，10.0 基本等于常驻夜视
	private static final double TARGET = 10.0;

	private static final Field VALUE_FIELD = findValueField();

	private Double originalValue;

	public GammaModule() {
		super("gamma_override", "伽马值覆写", Category.VISUAL, true);

		// 构造函数跑在 onInitializeClient 里，此时 Minecraft 还在自身构造中，options 仍为 null，
		// 所以一切碰 mc.options 的动作都不能放这里，只能挂事件延后
		ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
		// 正常退出也要还原，否则玩家下次启动明明没开这个功能却还是亮的
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> restore());
	}

	private void onClientStarted(Minecraft mc) {
		// 崩溃退出时来不及还原，options.txt 里会留着 >1 的值；原版滑条不可能设出这种值，一定是残留
		if (mc.options.gamma().get() > 1.0) {
			write(mc, 1.0);
		}
	}

	@Override
	protected void onEnable() {
		Minecraft mc = Minecraft.getInstance();
		originalValue = mc.options.gamma().get();
		write(mc, TARGET);
	}

	@Override
	protected void onDisable() {
		restore();
	}

	@Override
	public void onTick(Minecraft mc) {
		// 配置是直接套用状态的，从存档读进来的"已开启"不会走 onEnable，这里补记原值
		if (originalValue == null) {
			originalValue = mc.options.gamma().get();
		}
		// 玩家在视频设置里动亮度、或原版重置选项，都会把值改回去
		if (Double.compare(mc.options.gamma().get(), TARGET) != 0) {
			write(mc, TARGET);
		}
	}

	private void restore() {
		if (originalValue != null) {
			write(Minecraft.getInstance(), originalValue);
			originalValue = null;
		}
	}

	private static void write(Minecraft mc, double value) {
		if (VALUE_FIELD == null) {
			return;
		}
		try {
			VALUE_FIELD.set(mc.options.gamma(), value);
		} catch (IllegalAccessException e) {
			NanbeiTnTSMod.LOGGER.error("覆写伽马值失败", e);
		}
	}

	private static Field findValueField() {
		for (Field field : OptionInstance.class.getDeclaredFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)
					&& field.getType() == Object.class) {
				field.setAccessible(true);
				return field;
			}
		}
		NanbeiTnTSMod.LOGGER.error("找不到 OptionInstance 的值字段，伽马值覆写不可用");
		return null;
	}
}
