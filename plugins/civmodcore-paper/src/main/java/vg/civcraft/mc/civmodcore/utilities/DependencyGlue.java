package vg.civcraft.mc.civmodcore.utilities;

import java.util.Objects;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Class designed to make creating glue classes easier, particularly with soft dependencies where your plugins may use
 * code from those plugins if they're present.
 *
 * You'll need to register the glues you create with {@link #registerGlue()}.
 */
public abstract class DependencyGlue implements Listener {

	private final PluginManager pluginManager;
	protected final Plugin plugin;
	protected final Logger logger;
	protected final String dependencyName;
	private Plugin dependencyInstance;

	/**
	 * You must provide the name of the plugin you wish to glue.
	 *
	 * @param plugin The host plugin that requires the glued dependency. Must be enabled.
	 * @param dependencyName The name of the plugin you wish to glue.
	 */
	protected DependencyGlue(@Nonnull final Plugin plugin, @Nonnull final String dependencyName) {
		this.pluginManager = Bukkit.getPluginManager();
		this.plugin = Objects.requireNonNull(plugin);
		this.logger = CivLogger.getLogger(plugin.getClass(), getClass());
		this.dependencyName = Objects.requireNonNull(dependencyName);
	}

	/**
	 * Gets the dependency's name that this glue will check against.
	 *
	 * @return Returns the glue's plugin name.
	 */
	@Nonnull
	public String getDependencyName() {
		return this.dependencyName;
	}

	/**
	 * @return Returns the current dependency instance.
	 */
	@Nullable
	public Plugin getDependencyInstance() {
		return this.dependencyInstance;
	}

	/**
	 * @return Returns true if the dependency is currently enabled.
	 */
	public boolean isDependencyEnabled() {
		return this.pluginManager.isPluginEnabled(this.dependencyInstance);
	}

	/**
	 * Will register this glue with Bukkit.
	 */
	public void registerGlue() {
		this.pluginManager.registerEvents(this, this.plugin);
	}

	/**
	 * Will unregister this glue with Bukkit.
	 */
	public void resetGlue() {
		HandlerList.unregisterAll(this);
	}

	/**
	 * This is called when the dependency is enabled. Use this as a setup.
	 */
	protected abstract void onDependencyEnabled();

	/**
	 * This is called when the dependency is disabled. Use this as a destructor.
	 */
	protected abstract void onDependencyDisabled();

	@EventHandler
	public final void INTERNAL_onServerLoad(final ServerLoadEvent event) {
		for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin.isEnabled() && StringUtils.equalsIgnoreCase(plugin.getName(), this.dependencyName)) {
				this.dependencyInstance = plugin;
				INTERNAL_logDependencyEnabled();
				onDependencyEnabled();
				break;
			}
		}
	}

	@EventHandler
	public final void INTERNAL_onPluginEnable(final PluginEnableEvent event) {
		if (StringUtils.equalsIgnoreCase(event.getPlugin().getName(), this.dependencyName)) {
			this.dependencyInstance = event.getPlugin();
			INTERNAL_logDependencyEnabled();
			onDependencyEnabled();
		}
	}

	@EventHandler
	public final void INTERNAL_onPluginDisable(final PluginDisableEvent event) {
		if (StringUtils.equalsIgnoreCase(event.getPlugin().getName(), this.dependencyName)) {
			INTERNAL_logDependencyDisabled();
			onDependencyDisabled();
			this.dependencyInstance = null;
		}
	}

	private void INTERNAL_logDependencyEnabled() {
		this.logger.info("Gluing " + this.dependencyName);
	}

	private void INTERNAL_logDependencyDisabled() {
		this.logger.info("Releasing " + this.dependencyName);
	}

}
