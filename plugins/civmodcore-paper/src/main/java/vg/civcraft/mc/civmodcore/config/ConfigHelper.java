package vg.civcraft.mc.civmodcore.config;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigHelper {

	/**
	 * Retrieves a string list from a given config section. If the keyed value is a standalone string instead of a
	 * list, that value will be converted to a list.
	 *
	 * @param config The config section to retrieve the list from.
	 * @param key The key to get the list of.
	 * @return Returns a list of strings, which is never null.
	 */
	public static List<String> getStringList(@Nonnull final ConfigurationSection config, @Nonnull final String key) {
		Preconditions.checkNotNull(config, "Config cannot be null!");
		Preconditions.checkNotNull(key, "Key cannot be null!");
		if (config.isString(key)) {
			final var list = new ArrayList<String>(1);
			list.add(config.getString(key));
			return list;
		}
		return config.getStringList(key);
	}

}
