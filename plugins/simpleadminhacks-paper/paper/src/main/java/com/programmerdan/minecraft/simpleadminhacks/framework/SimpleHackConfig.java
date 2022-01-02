package com.programmerdan.minecraft.simpleadminhacks.framework;

import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Simple config shell that assumes a {@link ConfigurationSection} as a base.
 *
 * @author ProgrammerDan
 */
public abstract class SimpleHackConfig {

	private final SimpleAdminHacks plugin;
	private final ConfigurationSection base;
	private final Map<String, Object> data;
	private boolean shouldEnable;
	private boolean enabled; // Legacy support

	/**
	 * Constructor that sets the internal config section. That base config is not visible
	 * outside of this class or its subclasses.
	 *
	 * Also extracts the one shared element of all hack configs, the "name" of the hack.
	 *
	 * Calls {@link #wireup(ConfigurationSection)}
	 */
	public SimpleHackConfig(final SimpleAdminHacks plugin, final ConfigurationSection base) {
		this(plugin, base, true);
	}

	/**
	 * Constructor that sets the internal config section. That base config is not visible
	 * outside of this class or its subclasses.
	 *
	 * Also extracts the one shared element of all hack configs, the "name" of the hack.
	 *
	 * Calls {@link #wireup(ConfigurationSection)}
	 */
	public SimpleHackConfig(final SimpleAdminHacks plugin, final ConfigurationSection base, final boolean wireup) {
		this.plugin = plugin;
		this.base = base;
		this.data = base.getValues(true);
		this.shouldEnable = this.enabled = base.getBoolean("enabled", false);
		this.plugin.info("Config for \"" + base.getName() + "\"; " +
				"is " + (this.shouldEnable ? "enabled" : "disabled") + "; " +
				"instance [" + this + "]");
		if (wireup) {
			wireup(base);
		}
	}

	/**
	 * Subclasses should override this method.
	 */
	protected abstract void wireup(final ConfigurationSection config);

	/**
	 * Might be useful in some situations for subclasses to directly access the
	 * stored base config().
	 */
	public ConfigurationSection getBase() {
		return base;
	}

	protected SimpleAdminHacks plugin() {
		if (this.plugin == null) {
			return SimpleAdminHacks.instance();
		}
		else {
			return this.plugin;
		}
	}

	/**
	 * @return Returns whether the represented hack should be enabled after being loaded.
	 */
	public boolean shouldEnable() {
		return this.shouldEnable;
	}

	/**
	 * Allows configs to prevent / re-allow the represented hack to be enabled after load.
	 *
	 * @param shouldEnable Whether to load the represented hack after being loaded.
	 */
	protected void setShouldEnable(final boolean shouldEnable) {
		this.shouldEnable = shouldEnable;
	}

	/**
	 * @deprecated Use {@link SimpleHack#isEnabled()} instead.
	 */
	@Deprecated
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @deprecated Use {@link HackManager#enableHack(SimpleHack)} or
	 *             {@link HackManager#disableHack(SimpleHack)} instead.
	 */
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public boolean has(final String key) {
		if (Strings.isNullOrEmpty(key)) {
			return false;
		}
		return this.data.containsKey(key);
	}

	/**
	 * Wrapper for base config's get, for use with the hacks command
	 */
	public Object get(final String key) {
		if (Strings.isNullOrEmpty(key)) {
			return null;
		}
		return this.data.get(key);
	}

	/**
	 * Wrapper for base config's set, for use with the hacks command
	 */
	public void set(final String key, final Object value) {
		if (Strings.isNullOrEmpty(key)) {
			return;
		}
		this.data.put(key, value);
	}

}
