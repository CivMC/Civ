package vg.civcraft.mc.civmodcore.util;

import com.google.common.base.Strings;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
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
	public static CivLogger getLogger(final Class<?> clazz) {
		if (clazz == null) {
			return new CivLogger(Logger.getLogger(CivLogger.class.getSimpleName()), null);
		}
		final ClassLoader classLoader = clazz.getClassLoader();
		if (!(classLoader instanceof PluginClassLoader)) {
			return new CivLogger(Logger.getLogger(CivLogger.class.getSimpleName()), clazz.getSimpleName());
		}
		final var plugin = ((PluginClassLoader) classLoader).getPlugin();
		return new CivLogger(plugin.getLogger(), clazz.getSimpleName());
	}

}
