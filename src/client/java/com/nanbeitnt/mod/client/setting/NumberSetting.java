package com.nanbeitnt.mod.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
	private final double min;
	private final double max;
	private final double step;
	private double value;

	public NumberSetting(String id, String name, double defaultValue, double min, double max, double step) {
		super(id, name);
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = clamp(defaultValue);
	}

	@Override
	public Double get() {
		return value;
	}

	public void set(double v) {
		this.value = clamp(v);
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	/** 0..1，供滑块定位用。 */
	public double getProgress() {
		return max - min == 0 ? 0 : (value - min) / (max - min);
	}

	public void setProgress(double progress) {
		set(min + (max - min) * Math.max(0, Math.min(1, progress)));
	}

	private double clamp(double v) {
		double clamped = Math.max(min, Math.min(max, v));
		if (step <= 0) {
			return clamped;
		}
		return Math.round(clamped / step) * step;
	}

	@Override
	public JsonElement toJson() {
		return new JsonPrimitive(value);
	}

	@Override
	public void fromJson(JsonElement json) {
		if (json != null && json.isJsonPrimitive()) {
			this.value = clamp(json.getAsDouble());
		}
	}
}
