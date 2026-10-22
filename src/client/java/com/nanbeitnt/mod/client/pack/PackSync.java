package com.nanbeitnt.mod.client.pack;

import com.nanbeitnt.mod.NanbeiTnTSMod;
import com.nanbeitnt.mod.client.module.impl.ResourcePackModule;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * 内置材质包与开关的联动。
 *
 * <p>包的本体放在 jar 的 {@code resourcepacks/<id 路径>/}（Fabric 内置包的规定位置），
 * 带 {@link PackActivationType#NORMAL} 注册 —— 默认关闭，玩家在游戏自己的资源包列表里也能手动开关。
 *
 * <p>改资源包必须重载整套资源（期间闪一次"正在加载资源"，装了光影更慢），
 * 所以开关只记状态，等回到主菜单再一次性同步，避免对战中途卡顿。
 */
public final class PackSync {
	private record PackDef(Identifier id, String name) {
	}

	public static final Identifier SHORT_SWORDS = NanbeiTnTSMod.id("short_swords");
	public static final Identifier WOOL_OUTLINE = NanbeiTnTSMod.id("wool_outline");
	public static final Identifier TRANSPARENT_GUI = NanbeiTnTSMod.id("transparent_gui");
	public static final Identifier SKY = NanbeiTnTSMod.id("sky");
	public static final Identifier CROSSHAIR = NanbeiTnTSMod.id("crosshair");

	private static final List<PackDef> DEFS = List.of(
			new PackDef(SHORT_SWORDS, "短剑"),
			new PackDef(WOOL_OUTLINE, "羊毛描边"),
			new PackDef(TRANSPARENT_GUI, "透明 GUI"),
			new PackDef(SKY, "天空覆盖"),
			new PackDef(CROSSHAIR, "准星"));

	private static final List<ResourcePackModule> TRACKED = new ArrayList<>();
	private static boolean pending;

	private PackSync() {
	}

	/** 必须在客户端初始化阶段调用：只登记，真正进包列表要等游戏刷新包列表。 */
	public static void registerBuiltinPacks() {
		ModContainer container = FabricLoader.getInstance().getModContainer(NanbeiTnTSMod.MOD_ID).orElse(null);
		if (container == null) {
			NanbeiTnTSMod.LOGGER.error("找不到自身的 mod 容器，内置材质包不可用");
			return;
		}
		for (PackDef def : DEFS) {
			ResourceLoader.registerBuiltinPack(def.id(), container, Component.literal(def.name()),
					PackActivationType.NORMAL);
		}
	}

	public static void track(ResourcePackModule module) {
		TRACKED.add(module);
	}

	public static void markPending() {
		pending = true;
	}

	public static boolean isPending() {
		return pending;
	}

	/** 天空覆盖是否真的被游戏加载着。开关只代表意图，回到主菜单才落进包列表，所以看实际列表。 */
	public static boolean isSkyPackLoaded() {
		Minecraft mc = Minecraft.getInstance();
		return mc != null && mc.options != null && mc.options.resourcePacks.contains(SKY.toString());
	}

	/** 把各开关的状态同步进游戏的资源包选择；真的变了才重载，没变就不动。 */
	public static void apply(Minecraft mc) {
		pending = false;
		if (mc.options == null) {
			return;
		}

		PackRepository repository = mc.getResourcePackRepository();
		List<String> selected = new ArrayList<>(mc.options.resourcePacks);
		boolean changed = false;
		for (ResourcePackModule module : TRACKED) {
			String id = module.getPackId();
			boolean want = module.isEnabled();
			boolean has = selected.contains(id);
			if (want && !has) {
				if (!repository.isAvailable(id)) {
					// 内置包注册失败（包里没 assets、pack.mcmeta 解析不了）时会走到这里
					NanbeiTnTSMod.LOGGER.warn("内置材质包 {}（{}）不在可用列表里，开关无效", id, module.getName());
					continue;
				}
				selected.add(id);
				changed = true;
			} else if (!want && has) {
				selected.remove(id);
				changed = true;
			}
		}
		if (!changed) {
			return;
		}

		mc.options.resourcePacks = selected;
		repository.setSelected(selected);
		mc.reloadResourcePacks();
	}
}
