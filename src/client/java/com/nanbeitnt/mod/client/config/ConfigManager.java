package com.nanbeitnt.mod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nanbeitnt.mod.NanbeiTnTSMod;
import com.nanbeitnt.mod.client.module.HudModule;
import com.nanbeitnt.mod.client.module.HudWidget;
import com.nanbeitnt.mod.client.module.Module;
import com.nanbeitnt.mod.client.module.ModuleManager;
import com.nanbeitnt.mod.client.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** 把模块开关和设置存成 JSON。写入时机由调用方决定（改开关、关界面、改键）。 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ConfigManager() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir()
				.resolve(NanbeiTnTSMod.MOD_ID)
				.resolve("config.json");
	}

	public static void save() {
		JsonObject root = new JsonObject();
		JsonObject modules = new JsonObject();

		for (Module module : ModuleManager.all()) {
			JsonObject entry = new JsonObject();
			entry.addProperty("enabled", module.isEnabled());

			JsonObject values = new JsonObject();
			for (Setting<?> setting : module.getSettings()) {
				values.add(setting.getId(), setting.toJson());
			}
			entry.add("settings", values);

			if (module instanceof HudModule hud) {
				JsonObject widgets = new JsonObject();
				for (HudWidget widget : hud.widgets()) {
					JsonObject pos = new JsonObject();
					pos.addProperty("x", widget.getX());
					pos.addProperty("y", widget.getY());
					pos.addProperty("scale", widget.getScale());
					widgets.add(widget.getId(), pos);
				}
				entry.add("widgets", widgets);
			}

			modules.add(module.getId(), entry);
		}
		root.add("modules", modules);

		try {
			Path path = file();
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException e) {
			NanbeiTnTSMod.LOGGER.error("写入配置失败: {}", file(), e);
		}
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) {
			return;
		}

		try {
			JsonObject root = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), JsonObject.class);
			if (root == null || !root.has("modules")) {
				return;
			}

			JsonObject modules = root.getAsJsonObject("modules");
			for (Module module : ModuleManager.all()) {
				if (!modules.has(module.getId())) {
					continue;
				}
				JsonObject entry = modules.getAsJsonObject(module.getId());
				if (entry.has("enabled")) {
					module.applyLoadedState(entry.get("enabled").getAsBoolean());
				}
				if (entry.has("settings")) {
					JsonObject values = entry.getAsJsonObject("settings");
					for (Setting<?> setting : module.getSettings()) {
						JsonElement value = values.get(setting.getId());
						if (value != null) {
							setting.fromJson(value);
						}
					}
				}
				if (entry.has("widgets") && module instanceof HudModule hud) {
					JsonObject widgets = entry.getAsJsonObject("widgets");
					for (HudWidget widget : hud.widgets()) {
						if (!widgets.has(widget.getId())) {
							continue;
						}
						JsonObject pos = widgets.getAsJsonObject(widget.getId());
						widget.setPosition(pos.get("x").getAsInt(), pos.get("y").getAsInt());
						if (pos.has("scale")) {
							widget.setScale(pos.get("scale").getAsFloat());
						}
					}
				}
			}
		} catch (Exception e) {
			NanbeiTnTSMod.LOGGER.error("读取配置失败，将使用默认值: {}", path, e);
		}
	}
}
