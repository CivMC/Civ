package vg.civcraft.mc.civmodcore.utilities;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

/**
 * Class designed to make creating glue classes easier, particularly with soft dependencies where your plugins may use
 * code from those plugins if they're present.
 *
 * You'll need to register the glues you create with your plugins .registerListener() method.
 */
public abstract class DependencyGlue implements Listener {

	private final String pluginName;

	private Plugin plugin;

	private Logger logger;

	/**
	 * <p>You must provide the name of the plugin you wish to glue.</p>
	 *
	 * <p>Note: The name check is case-insensitive, but it's recommended for the name to be an exact match, if only for
	 * readability and ease of find and replace.</p>
	 *
	 * @param pluginName The name of the plugin you wish to glue.
	 */
	protected DependencyGlue(String pluginName) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(pluginName));
		this.pluginName = pluginName;
		onServerLoad(null);
	}

	/**
	 * Gets the plugin name that this glue will check against.
	 *
	 * @return Returns the glue's plugin name.
	 */
	public String getPluginName() {
		return this.pluginName;
	}

	/**
	 * @return Returns the current plugin instance for this glue.
	 */
	public Plugin plugin() {
		return this.plugin;
	}

	/**
	 * @return Returns the current logger instance for this glue.
	 */
	public Logger logger() {
		return this.logger;
	}

	/**
	 * @return Returns true if this glue's plugin is currently enabled, which is updated live.
	 */
	public boolean isEnabled() {
		if (this.plugin == null) {
			return false;
		}
		if (!this.plugin.isEnabled()) {
			return false;
		}
		return true;
	}

	/**
	 * <p>Determines whether this glue is safe to use.</p>
	 *
	 * <p>This <i>should</i> be kept for internal use because if the glue plugin is enabled but not safe to use, the
	 * APIs should themselves account for that.</p>
	 *
	 * @return Returns true if the glue is deemed safe to use.
	 */
	public boolean isSafeToUse() {
		if (!isEnabled()) {
			return false;
		}
		return true;
	}

	/**
	 * This is called when the glue's plugin is enabled. Use this as a setup.
	 */
	protected void onGlueEnabled() {
		this.logger.info("Gluing " + this.pluginName);
	}

	/**
	 * This is called when the glue's plugin is disabled. Use this as a destructor.
	 */
	protected void onGlueDisabled() {
		this.logger.info("Releasing " + this.pluginName);
	}

	@EventHandler
	public final void onServerLoad(ServerLoadEvent event) {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin == null || !plugin.isEnabled()) {
				continue;
			}
			if (!StringUtils.equalsIgnoreCase(plugin.getName(), this.pluginName)) {
				continue;
			}
			this.plugin = plugin;
			this.logger = plugin.getLogger();
			onGlueEnabled();
			break;
		}
	}

	@EventHandler
	public final void onPluginEnable(PluginEnableEvent event) {
		if (StringUtils.equalsIgnoreCase(event.getPlugin().getName(), this.pluginName)) {
			this.plugin = event.getPlugin();
			this.logger = this.plugin.getLogger();
			onGlueEnabled();
		}
	}

	@EventHandler
	public final void onPluginDisable(PluginDisableEvent event) {
		if (StringUtils.equalsIgnoreCase(event.getPlugin().getName(), this.pluginName)) {
			onGlueDisabled();
			this.plugin = null;
			this.logger = null;
		}
	}

}
