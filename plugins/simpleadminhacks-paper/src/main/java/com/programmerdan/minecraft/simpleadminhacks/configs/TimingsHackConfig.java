package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.ConfigurationSection;

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

	public void setTimingsMap(int mapId) {
		timingsMap = mapId;
		getBase().set("timingMap", mapId);
	}

	public Integer getBindMap(String bind) {
		if (bind == null) {
			return null;
		}
		return bindMaps.get(bind);
	}

	public void setBindMap(String bind, int mapId) {
		Integer prior = bindMaps.replace(bind, mapId);
		if (prior != null) {
			reverseBindMaps.remove(prior);
		}
		reverseBindMaps.replace(mapId, bind);
		getBase().set("bindings." + bind, mapId);
	}

	public String getBindFromId(int mapId) {
		return reverseBindMaps.get(mapId);
	}
}
