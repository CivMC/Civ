package vg.civcraft.mc.namelayer.config;

import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.config.annotations.NameConfig;
import vg.civcraft.mc.namelayer.config.annotations.NameConfigType;

public class NameConfigNode {

	private final String name;
	private final Object def;
	private final NameConfigType type;
	private Object value;
	private final JavaPlugin plugin;
	private FileConfiguration config;

	public NameConfigNode(JavaPlugin plugin, NameConfig config) {
		name = config.name();
		type = config.type();
		Object obj = new Object();
		def = convert(config.def(), obj);
		if (def == obj) {
			NameLayerPlugin.log(Level.CONFIG,
					"There has been a config mismatch!");
		}
		value = def;
		this.plugin = plugin;
		this.config = plugin.getConfig();
		load();
	}

	public void load() {
		if (!config.isSet(name)) {
			return;
		}
		switch (type) {
		case Bool:
			set(config.getBoolean(name, (Boolean) value));
			break;
		case Int:
			set(config.getInt(name, (Integer) value));
			break;
		case Double:
			set(config.getDouble(name, (Double) value));
			break;
		case String:
			set(config.getString(name, (String) value));
			break;
		default:
			throw new Error("Unknown OptType");
		}
	}

	public Object convert(String value, Object defaultValue) {
		switch (type) {
		case Bool:
			return value.equals("1") || value.equalsIgnoreCase("true");
		case Int:
			if (value.isEmpty()) {
				return -1;
			}
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				return defaultValue;
			}
		case Double:
			if (value.isEmpty()) {
				return -1.00000001;
			}
			try {
				return Double.parseDouble(value);
			} catch (Exception e) {
				return defaultValue;
			}
		case String:
			if (value == null) {
				return (String) defaultValue;
			}
			return value;
		default:
			throw new Error("Unknown NameConfigType");
		}
	}

	public String getName() {
		return name;
	}

	public NameConfigType getType() {
		return type;
	}

	public Boolean getBool() {
		if (type != NameConfigType.Bool) {
			throw new Error(String.format(
					"Config option %s not of type Boolean", name));
		}
		return (Boolean) value;
	}

	public Integer getInt() {
		if (type != NameConfigType.Int) {
			throw new Error(String.format(
					"Config option %s not of type Integer", name));
		}
		return (Integer) value;
	}

	public Double getDouble() {
		if (type != NameConfigType.Double) {
			throw new Error(String.format(
					"Config option %s not of type Double", name));
		}
		return (Double) value;
	}

	public String getString() {
		return value.toString();
	}

	public void set(Object value) {
		if (value instanceof String) {
			setString((String) value);
			return;
		}
		switch (type) {
		case Bool:
			if (!(value instanceof Boolean)) {
				throw new Error(String.format(
						"Value set is not a Boolean for %s: %s", name,
						value.toString()));
			}
			this.value = value;
			break;
		case Int:
			if (!(value instanceof Integer)) {
				throw new Error(String.format(
						"Value set is not a Integer for %s: %s", name,
						value.toString()));
			}
			this.value = value;
			break;
		case Double:
			if (!(value instanceof Double)) {
				throw new Error(String.format(
						"Value set is not a Double for %s: %s", name,
						value.toString()));
			}
			this.value = value;
			break;
		case String:
		default:
			throw new Error("Unknown OptType");
		}
		config.set(name, value);
	}

	public void setString(String value) {
		this.value = convert(value, this.value);
		config.set(name, this.value);
	}
}
