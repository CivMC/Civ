package vg.civcraft.mc.civmodcore;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

public abstract class ACivMod extends JavaPlugin {

	private final Set<Class<? extends ConfigurationSerializable>> configClasses = new HashSet<>(0);

	@Override
	public void onEnable() {
		// Self disable when a hard dependency is disabled
		registerListener(new Listener() {
			@EventHandler
			public void onPluginDisable(final PluginDisableEvent event) {
				final String pluginName = event.getPlugin().getName();
				if (getDescription().getDepend().contains(pluginName)) {
					warning("Plugin [" + pluginName + "] has been disabled, disabling this plugin.");
					disable();
				}
			}
		});
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
		Bukkit.getScheduler().cancelTasks(this);
		this.configClasses.forEach(ConfigurationSerialization::unregisterClass);
	}

	/**
	 * Registers a listener class with this plugin.
	 *
	 * @param listener The listener class to register.
	 */
	public void registerListener(@Nonnull final Listener listener) {
		getServer().getPluginManager().registerEvents(
				Objects.requireNonNull(listener, "Cannot register a listener if it's null, you dummy"), this);
	}

	/**
	 * Convenience method you can use to register {@link ConfigurationSerializable} classes, which will be
	 * automatically un-registered when this plugin is disabled.
	 *
	 * @param clazz The serializable class to register and automatically unregister upon disable.
	 */
	public void registerConfigClass(@Nonnull final Class<? extends ConfigurationSerializable> clazz) {
		Objects.requireNonNull(clazz, "Cannot register a config class if it's null, you dummy");
		ConfigurationSerialization.registerClass(clazz);
		this.configClasses.add(clazz);
	}

	/**
	 * Determines whether this plugin is in debug mode, which is determined by a config value.
	 *
	 * @return Returns true if this plugin is in debug mode.
	 */
	public boolean isDebugEnabled() {
		return getConfig().getBoolean("debug", false);
	}

	/**
	 * Generates a file instance based on a file within this plugin's data folder.
	 *
	 * @param path The path of the file relative to the data folder.
	 * @return Returns a file instance of the generated path.
	 */
	public File getDataFile(@Nonnull final String path) {
		return new File(getDataFolder(), Objects.requireNonNull(path));
	}

	/**
	 * Saves a default resource to the plugin's data folder if the file does not already exist.
	 *
	 * @param path The path to the default resource <i>AND</i> the data file.
	 */
	public void saveDefaultResource(@Nonnull final String path) {
		if (!getDataFile(path).exists()) {
			saveResource(path, false);
		}
	}

	/**
	 * Saves a particular default resource to a particular location.
	 *
	 * @param defaultPath The path of the file within the plugin's jar.
	 * @param dataPath The path the file should take within the plugin's data folder.
	 */
//	public void saveDefaultResourceAs(@Nonnull String defaultPath,
//									  @Nonnull String dataPath) {
//		if (getDataFile(defaultPath).exists()) {
//			return;
//		}
//		defaultPath = defaultPath.replace('\\', '/');
//		dataPath = dataPath.replace('\\', '/');
//		final InputStream data = getResource(defaultPath);
//		if (data == null) {
//			throw new IllegalArgumentException("The embedded resource '" + defaultPath +
//					"' cannot be found in " + getFile());
//		}
//		final var outFile = new File(getDataFolder(), dataPath);
//		try {
//			FileUtils.copyInputStreamToFile(data, outFile);
//		}
//		catch (final IOException exception) {
//			severe("Could not save " + outFile.getName() + " to " + outFile);
//			exception.printStackTrace();
//		}
//	}

	/**
	 * Disables this plugin.
	 */
	public void disable() {
		getPluginLoader().disablePlugin(this);
	}

	/**
	 * Simple SEVERE level logging.
	 */
	public void severe(String message) {
		getLogger().log(Level.SEVERE, message);
	}

	/**
	 * Simple SEVERE level logging with Throwable record.
	 */
	public void severe(String message, Throwable error) {
		getLogger().log(Level.SEVERE, message, error);
	}

	/**
	 * Simple WARNING level logging.
	 */
	public void warning(String message) {
		getLogger().log(Level.WARNING, message);
	}

	/**
	 * Simple WARNING level logging with Throwable record.
	 */
	public void warning(String message, Throwable error) {
		getLogger().log(Level.WARNING, message, error);
	}

	/**
	 * Simple WARNING level logging with ellipsis notation shortcut for defered
	 * injection argument array.
	 */
	public void warning(String message, Object... vars) {
		getLogger().log(Level.WARNING, message, vars);
	}

	/**
	 * Simple INFO level logging
	 */
	public void info(String message) {
		getLogger().log(Level.INFO, message);
	}

	/**
	 * Simple INFO level logging with ellipsis notation shortcut for defered
	 * injection argument array.
	 */
	public void info(String message, Object... vars) {
		getLogger().log(Level.INFO, message, vars);
	}

	/**
	 * Live activatable debug message (using plugin's config.yml top level debug tag to decide) at
	 * INFO level.
	 *
	 * Skipped if DebugLog is false.
	 */
	public void debug(String message) {
		if (isDebugEnabled()) {
			getLogger().log(Level.INFO, message);
		}
	}

	/**
	 * Live activatable debug message (using plugin's config.yml top level debug tag to decide) at
	 * INFO level with ellipsis notation shorcut for defered injection argument
	 * array.
	 *
	 * Skipped if DebugLog is false.
	 */
	public void debug(String message, Object... vars) {
		if (isDebugEnabled()) {
			getLogger().log(Level.INFO, message, vars);
		}
	}

	/**
	 * <p>Attempts to retrieve a plugin's instance through several known means.</p>
	 *
	 * <ol>
	 *     <li>
	 *         If there's an instance of the class currently enabled. <b>Don't request ACivMod, JavaPlugin, PluginBase,
	 *         or Plugin or you'll just get the the first result.</b>
	 *     </li>
	 *     <li>If there's a public static .getInstance() or .getPlugin() method.</li>
	 *     <li>If there's a static "instance" or "plugin" field.</li>
	 * </ol>
	 *
	 * @param <T> The type of the plugin.
	 * @param clazz The class object of the plugin.
	 * @return Returns the first found instance of the plugin, or null. Nulls don't necessarily mean there isn't an
	 *         instance of the plugin in existence. It could just be that it's located some unexpected place.
	 *         Additionally, just because an instance has been returned does not mean that instance is enabled.
	 */
	@Contract("null -> null")
	@Nullable
	@SuppressWarnings("unchecked")
	public static <T extends JavaPlugin> T getInstance(@Nullable final Class<T> clazz) {
		if (clazz == null) {
			return null;
		}
		try {
			return JavaPlugin.getPlugin(clazz);
		}
		catch (final IllegalArgumentException | IllegalStateException ignored) { }
		for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			if (clazz.equals(plugin.getClass())) {
				return (T) plugin;
			}
		}
		for (final String methodName : Arrays.asList("getInstance", "getPlugin")) {
			try {
				final Method method = clazz.getDeclaredMethod(methodName);
				if (Modifier.isPublic(method.getModifiers())
						&& Modifier.isStatic(method.getModifiers())
						&& method.getParameterCount() == 0
						&& clazz.isAssignableFrom(method.getReturnType())) {
					return (T) method.invoke(null);
				}
			}
			catch (final Throwable ignored) { }
		}
		for (final String fieldName : Arrays.asList("instance", "plugin")) {
			try {
				final Field field = clazz.getField(fieldName);
				if (Modifier.isStatic(field.getModifiers())
						&& clazz.isAssignableFrom(field.getType())) {
					return (T) field.get(null);
				}
			}
			catch (final Throwable ignored) { }
		}
		// Otherwise there's no instance of the plugin, or it's stored in an unusual way
		return null;
	}

}
