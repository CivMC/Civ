package vg.civcraft.mc.civmodcore;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

public abstract class ACivMod extends JavaPlugin {

    private final Set<Class<? extends ConfigurationSerializable>> configClasses = new HashSet<>(0);

    protected ACivMod() {
        super();
    }

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
        this.configClasses.forEach(ConfigurationSerialization::unregisterClass);
    }

    /**
     * Registers a listener class with this plugin.
     *
     * @param listener The listener class to register.
     */
    public void registerListener(@NotNull final Listener listener) {
        getServer().getPluginManager().registerEvents(
            Objects.requireNonNull(listener, "Cannot register a listener if it's null, you dummy"), this);
    }

    /**
     * Convenience method you can use to register {@link ConfigurationSerializable} classes, which will be
     * automatically un-registered when this plugin is disabled.
     *
     * @param clazz The serializable class to register and automatically unregister upon disable.
     */
    public void registerConfigClass(@NotNull final Class<? extends ConfigurationSerializable> clazz) {
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
    public File getDataFile(@NotNull final String path) {
        return new File(getDataFolder(), Objects.requireNonNull(path));
    }

    /**
     * Saves a default resource to the plugin's data folder if the file does not already exist.
     *
     * @param path The path to the default resource <i>AND</i> the data file.
     */
    public void saveDefaultResource(@NotNull final String path) {
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
//	public void saveDefaultResourceAs(@NotNull String defaultPath,
//									  @NotNull String dataPath) {
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
        getServer().getPluginManager().disablePlugin(this);
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
     * <p>
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
     * <p>
     * Skipped if DebugLog is false.
     */
    public void debug(String message, Object... vars) {
        if (isDebugEnabled()) {
            getLogger().log(Level.INFO, message, vars);
        }
    }
}
