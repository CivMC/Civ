package vg.civcraft.mc.civmodcore.utilities;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.google.common.base.Strings;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.PluginClassLoader;

public final class CivLogger extends Logger {

	private final String prefix;

	private CivLogger(final Logger logger, final String prefix) {
		super(logger.getName(), logger.getResourceBundleName());
		setParent(logger);
		this.prefix = prefix;
	}

	@Override
	public void log(final LogRecord record) {
		if (!Strings.isNullOrEmpty(this.prefix)) {
			record.setMessage("[" + this.prefix + "] " + record.getMessage());
		}
		super.log(record);
	}

	/**
	 * Creates a logger based on a given class. If the given class was loaded by a plugin, it will piggy back off that
	 * plugin's logger.
	 *
	 * @param clazz The class to base the logger on.
	 * @return Returns a new civ logger.
	 */
	public static CivLogger getLogger(@Nonnull final Class<?> clazz) {
		final ClassLoader classLoader = clazz.getClassLoader();
		if (classLoader instanceof PluginClassLoader) {
			final var plugin = ((PluginClassLoader) classLoader).getPlugin();
			if (plugin != null) {
				return new CivLogger(plugin.getLogger(), clazz.getSimpleName());
			}
			// Plugin has been constructed but not initialised yet
			final var descriptionField = FieldUtils.getDeclaredField(PluginClassLoader.class, "description", true);
			try {
				final var description = (PluginDescriptionFile) descriptionField.get(classLoader);
				final var logger = PaperPluginLogger.getLogger(description);
				return new CivLogger(logger, clazz.getSimpleName());
			}
			catch (final IllegalAccessException ignored) {}
		}
		return new CivLogger(Bukkit.getLogger(), clazz.getSimpleName());
	}

}
