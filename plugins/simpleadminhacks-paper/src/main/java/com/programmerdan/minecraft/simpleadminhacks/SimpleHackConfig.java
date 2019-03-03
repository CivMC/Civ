package com.programmerdan.minecraft.simpleadminhacks;

import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Simple config shell that assumes a {@link ConfigurationSection} as a base.
 * 
 * @author ProgrammerDan
 */
public abstract class SimpleHackConfig {

	private SimpleAdminHacks plugin;
	private ConfigurationSection base;
	private String name;
	private boolean enabled;

	/**
	 * Constructor that sets the internal config section. That base config is not visible
	 * outside of this class or its subclasses.
	 *
	 * Also extracts the one shared element of all hack configs, the "name" of the hack.
	 *
	 * Calls {@link #wireup(ConfigurationSection)}
	 */
	public SimpleHackConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		this.plugin = plugin;
		this.base = base;
		this.name = base.getString("name", base.getName());
		this.enabled = base.getBoolean("enabled", false);
		plugin.log(Level.INFO, "Config for {0}, enabled set to {1}, instance {2}", this.name, this.enabled, this.toString());
		this.wireup(base);
	}

	/**
	 * Subclasses should override this method.
	 */
	protected abstract void wireup(ConfigurationSection config);

	/**
	 * Might be useful in some situations for subclasses to directly access the 
	 * stored base config.
	 */
	public ConfigurationSection getBase() {
		return base;
	}

	protected SimpleAdminHacks plugin() {
		if (this.plugin == null) {
			return SimpleAdminHacks.instance();
		} else {
			return this.plugin;
		}
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Wrapper for base config's set, for use with the hacks command
	 */
	public void set(String attr, Object value) {
		if (this.base != null && attr != null) {
			base.set(attr, value);
		}
	}

	/**
	 * Wrapper for base config's get, for use with the hacks command
	 */
	public Object get(String attr) {
		if (this.base != null && attr != null) {
			return base.get(attr);
		} else {
			return null;
		}
	}
}
