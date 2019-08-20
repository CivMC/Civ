package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class TimingsHackConfig extends SimpleHackConfig {

	private Integer timingsMap;
	private Map<String, Integer> bindMaps;
	private Map<Integer, String> reverseBindMaps;

	public TimingsHackConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		timingsMap = config.contains("timingMap") ? config.getInt("timingMap") : null;
		bindMaps = new ConcurrentHashMap<>();
		reverseBindMaps = new ConcurrentHashMap<>();
		if (config.contains("bindings")) {
			ConfigurationSection bindings = config.getConfigurationSection("bindings");
			Set<String> keys = bindings.getKeys(false);
			for (String key : keys) {
				bindMaps.put(key, bindings.getInt(key));
				reverseBindMaps.put(bindings.getInt(key), key);
			}
		}
	}

	public Integer getTimingsMap() {
		return timingsMap;
	}

	public void setTimingsMap(int i) {
		timingsMap = i;
		getBase().set("timingMap", i);
	}

	public Integer getBindMap(String bind) {
		if (bind == null) {
			return null;
		}
		return bindMaps.get(bind);
	}

	public void setBindMap(String bind, int i) {
		Integer prior = bindMaps.replace(bind, i);
		if (prior != null) {
			reverseBindMaps.remove(prior);
		}
		reverseBindMaps.replace(i, bind);
		getBase().set("bindings." + bind, i);
	}

	public String getBindFromId(int i) {
		return reverseBindMaps.get(i);
	}
}
