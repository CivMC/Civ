package com.programmerdan.minecraft.simpleadminhacks;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Simple config shell that assumes a {@link ConfigurationSection} as a base.
 * 
 * @author ProgrammerDan
 */
public abstract class SimpleHackConfig {

	private ConfigurationSection base;
	private String name;

	/**
	 * Constructor that sets the internal config section. That base config is not visible
	 * outside of this class or its subclasses.
	 *
	 * Also extracts the one shared element of all hack configs, the "name" of the hack.
	 *
	 * Calls {@link #wireup(ConfigurationSection)}
	 */
	public SimpleHackConfig(ConfigurationSection base) {
		this.base = base;
		this.name = base.getString("name");
		this.wireup(base);
	}

	/**
	 * Subclasses should override this method.
	 */
	protected void wireup(ConfigurationSection config);

	/**
	 * Might be useful in some situations for subclasses to directly access the 
	 * stored base config.
	 */
	protected ConfigurationSection getBase() {
		return base;
	}

	public String getName() {
		return name;
	}
}

