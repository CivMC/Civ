package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class TimingsHackConfig extends SimpleHackConfig {

	private Short timingsMap;
	private Map<String, Short> bindMaps;
	private Map<Short, String> reverseBindMaps;

	public TimingsHackConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		timingsMap = config.contains("timingMap") ? (short) config.getInt("timingMap") : null;
		bindMaps = new ConcurrentHashMap<String, Short>();
		reverseBindMaps = new ConcurrentHashMap<Short, String>();
		if (config.contains("bindings")) {
			ConfigurationSection bindings = config.getConfigurationSection("bindings");
			Set<String> keys = bindings.getKeys(false);
			for (String key : keys) {
				bindMaps.put(key, (short) bindings.getInt(key));
				reverseBindMaps.put((short) bindings.getInt(key), key);
			}
		}
	}

	public Short getTimingsMap() {
		return timingsMap;
	}

	public void setTimingsMap(short mapId) {
		timingsMap = mapId;
		getBase().set("timingMap", mapId);
	}

	public Short getBindMap(String bind) {
		if (bind == null) {
			return null;
		}
		return bindMaps.get(bind);
	}

	public void setBindMap(String bind, short mapId) {
		Short prior = bindMaps.replace(bind, mapId);
		if (prior != null) {
			reverseBindMaps.remove(prior);
		}
		reverseBindMaps.replace(mapId, bind);
		getBase().set("bindings." + bind, mapId);
	}

	public String getBindFromId(short mapId) {
		return reverseBindMaps.get(mapId);
	}
}
