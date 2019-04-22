package com.programmerdan.minecraft.simpleadminhacks;

/**
 * Baseline structure for a managed hack.
 *
 * @author ProgrammerDan
 */
public abstract class SimpleHack<T extends SimpleHackConfig> {
	/**
	 * Why store a reference to the plugin? Largely this is for unit test combatibility (to avoid too many static references)
	 * but it also allows us to do either, including static dependency injection on demand.
	 * 
	 * Visibility is set such that subclasses must use the accessor.
	 */
	private SimpleAdminHacks plugin;

	/**
	 * Protected so that subclasses can directly reference; this is the config for the hack as extracted 
	 * by the managing plugin's configuration parsing infrastructure.
	 * 
	 * Using generics so each Hack can more concretely specify the nature of their config.
	 */
	protected T config;

	/**
	 * Captures baseline requirements of the plugin and the config for this hack.
	 */
	public SimpleHack(SimpleAdminHacks plugin, T config) {
		this.plugin = plugin;
		this.config = config;
	}

	/**
	 * Returns either class-local or static instance of this plugin, depending on what is available.
	 */
	public SimpleAdminHacks plugin() {
		if (this.plugin == null) {
			this.plugin = SimpleAdminHacks.instance();
		}
		return this.plugin;
	}

	/**
	 * Enables this hack by calling:
	 * <ul>
	 *   <li> {@link #dataBootstrap}
	 *   <li> {@link #registerCommands}
	 *   <li> {@link #registerListeners}
	 * </ul>
	 */
	public void enable() {
		dataBootstrap();
		registerCommands();
		registerListeners();
	}

	/**
	 * Any listeners that this hack needs should get registered here.
	 */
	public abstract void registerListeners();

	/**
	 * If this hack has its own commands, register them here.
	 */
	public abstract void registerCommands();

	/**
	 * If this hack uses data structures n' such, it is preferred to configure them here instead of the constructor.
	 */
	public abstract void dataBootstrap();

	/**
	 * Disables this hack by calling:
	 * <ul>
	 *   <li> {@link #unregisterListeners}
	 *   <li> {@link #unregisterCommands}
	 *   <li> {@link #dataCleanup}
	 * </ul>
	 * Then discarding the plugin and config references.
	 */
	public void disable() {
		unregisterListeners();
		unregisterCommands();
		dataCleanup();

		this.config = null;
		this.plugin = null;
	}

	/**
	 * Convenience method for "soft" disabling this hack.
	 */
	public void softDisable() {
		this.config.setEnabled(false);
	}

	/**
	 * Convenience method for "soft" enabling this hack.
	 */
	public void softEnable() {
		this.config.setEnabled(true);
	}

	/**
	 * Convenience passthrough wrapper for config.isEnabled();
	 */
	public boolean isEnabled() {
		return this.config == null ? false : this.config.isEnabled();
	}

	/**
	 * Convenience passhthrough wrapper for config.getName();
	 */
	public String getName() {
		return this.config == null ? null : this.config.getName();
	}

	/**
	 * Accessor for config externally.
	 */
	public SimpleHackConfig config() {
		return this.config;
	}

	/**
	 * Typically optional. For full completeness you could define this. All of the plugin's registered listeners are
	 * unregistered during disable, however, at the Spigot level.
	 */
	public abstract void unregisterListeners();

	/**
	 * Also optional; command handlers will likewise be sorted on plugin disable. Fill this out if you intend for 
	 * this hack's custom command handling to be off if only this hack is disabled.
	 */
	public abstract void unregisterCommands();

	/**
	 * Not optional; any data structures, connections, or temporary files should get cleaned up here.
	 */
	public abstract void dataCleanup();

	/**
	 * Not optional; customized status for this SimpleHack for display in CnC
	 */
	public abstract String status();

	/**
	 * Simple name equality. Don't have two hacks with the same name please.
	 */
	@Override
	public boolean equals(Object e) {
		if (e instanceof SimpleHack) {
			SimpleHack<?> f = (SimpleHack<?>) e;
			if (f.config != null && this.config != null && f.config.getName().equals(this.config.getName())) {
				return true;
			}
		}
		return false;
	}
}

