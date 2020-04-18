package vg.civcraft.mc.civmodcore.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;

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
	 * This is called when the glue's plugin is enabled. Use this as a setup.
	 */
	protected abstract void onGlueEnabled();

	/**
	 * This is called when the glue's plugin is disabled. Use this as a destructor.
	 */
	protected abstract void onGlueDisabled();

	@EventHandler
	public final void onServerLoad(ServerLoadEvent event) {
		this.enabled = Iteration.some(Bukkit.getPluginManager().getPlugins(), (plugin) ->
				TextUtil.stringEqualsIgnoreCase(plugin.getName(), this.pluginName));
		if (this.enabled) {
			onGlueEnabled();
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
