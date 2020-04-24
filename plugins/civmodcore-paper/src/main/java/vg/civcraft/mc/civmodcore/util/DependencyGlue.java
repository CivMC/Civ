package vg.civcraft.mc.civmodcore.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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

	private boolean enabled;

	/**
	 * You must provide the name of the plugin you wish to glue.
	 *
	 * @param pluginName The name of the plugin you wish to glue.
	 *
	 * @apiNote The name check is case-insensitive, but it's recommended for the name to be an exact match, if only for
	 *     readability and ease of find and replace.
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
	 * @return Returns true if this glue's plugin is currently enabled, which is updated live.
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Determines whether this glue is safe to use.
	 *
	 * @return Returns true if the glue is deemed safe to use.
	 *
	 * @apiNote This <i>should</i> be kept for internal use because if the glue plugin is enabled but not safe to use,
	 *     the APIs should themselves account for that.
	 */
	public boolean isSafeToUse() {
		return isEnabled();
	}

	/**
	 * This is called when the glue's plugin is enabled. Use this as a setup.
	 */
	protected void onGlueEnabled() { }

	/**
	 * This is called when the glue's plugin is disabled. Use this as a destructor.
	 */
	protected void onGlueDisabled() { }

	@EventHandler
	public final void onServerLoad(ServerLoadEvent event) {
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (plugin == null || !plugin.isEnabled()) {
				continue;
			}
			if (!TextUtil.stringEqualsIgnoreCase(plugin.getName(), this.pluginName)) {
				continue;
			}
			this.enabled = true;
			onGlueEnabled();
			break;
		}
	}

	@EventHandler
	public final void onPluginEnable(PluginEnableEvent event) {
		if (TextUtil.stringEqualsIgnoreCase(event.getPlugin().getName(), this.pluginName)) {
			this.enabled = true;
			onGlueEnabled();
		}
	}

	@EventHandler
	public final void onPluginDisable(PluginDisableEvent event) {
		if (TextUtil.stringEqualsIgnoreCase(event.getPlugin().getName(), this.pluginName)) {
			this.enabled = false;
			onGlueDisabled();
		}
	}

}
