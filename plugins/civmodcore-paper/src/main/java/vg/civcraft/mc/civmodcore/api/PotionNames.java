package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * Class that loads and store potion names.
 * */
public final class PotionNames {

	private static final Logger LOGGER = LoggerFactory.getLogger(PotionNames.class.getSimpleName());

	private static final Set<SearchResult> POTION_DETAILS = new HashSet<>();

	/**
	 * Resets all item names, custom item names included.
	 * */
	public static void resetPotionNames() {
		POTION_DETAILS.clear();
	}

	/**
	 * Loads item names from configurable files and requests any custom item names programmatically from plugins.
	 * */
	public static void loadPotionNames() {
		resetPotionNames();
		// Load enchantment names from enchantments.csv
		InputStream enchantmentsCSV = CivModCorePlugin.class.getResourceAsStream("/potions.csv");
		if (enchantmentsCSV != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(enchantmentsCSV));
				String line = reader.readLine();
				while (line != null) {
					String[] values = line.split(",");
					// If there's not at least three values (slug, abbreviation, display name) then skip
					if (values.length != 3) {
						LOGGER.warn("[Config] This potion row is corrupted: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If the potion type cannot be found by the given slug, then skip
					PotionType type;
					try {
						type = PotionType.valueOf(values[0]);
					}
					catch (Exception ignored) {
						LOGGER.warn("[Config] Could not find a potion type on this line: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If the line specifies an effect type, then try to find it or skip if not found
					PotionEffectType effectType = null;
					if (!Strings.isNullOrEmpty(values[1])) {
						effectType = PotionEffectType.getByName(values[1]);
						if (effectType == null) {
							LOGGER.warn("[Config] Could not find potion effect type type on this line: " + line);
							// Go to the next line
							line = reader.readLine();
							continue;
						}
					}
					// Get the potion's name
					String name = values[2];
					if (Strings.isNullOrEmpty(name)) {
						LOGGER.warn("[Config] Could not find potion name on this line: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// Put the enchantment and name into the system
					POTION_DETAILS.add(new SearchResult(type, effectType, name));
					LOGGER.info(String.format("[Config] Potion parsed: %s = %s = %s", type.name(),
							effectType == null ? "NULL" : effectType.getName(),
							name));
					line = reader.readLine();
				}
				reader.close();
			}
			catch (IOException error) {
				LOGGER.warn("[Config] Could not load potions from potions.csv");
				error.printStackTrace();
			}
		}
		else {
			LOGGER.warn("[Config] Could not load potions from potions.csv as the file does not exist.");
		}
	}

	/**
	 * Attempts to match a potion type with a set of details.
	 *
	 * @param type The potion type to search with.
	 * @return The potion details, or null.
	 */
	public static SearchResult findByType(PotionType type) {
		if (type == null) {
			return null;
		}
		for (SearchResult details : POTION_DETAILS) {
			if (details.type == type) {
				return details;
			}
		}
		return null;
	}

	/**
	 * Attempts to match a potion effect type with a set of details. This is a less desirable search as some potion
	 * do not have an effect. If you're checking with a static value, best to take a look at {@link PotionType}.
	 *
	 * @param type The potion effect type to search with.
	 * @return The potion details, or null.
	 */
	public static SearchResult findByEffect(PotionEffectType type) {
		if (type == null) {
			return null;
		}
		for (SearchResult details : POTION_DETAILS) {
			if (details.effectType == type) {
				return details;
			}
		}
		return null;
	}

	/**
	 * Attempts to match a potion name with a set of details.
	 *
	 * @param name The potion name to search with.
	 * @return The potion details, or null.
	 */
	public static SearchResult findByName(String name) {
		if (Strings.isNullOrEmpty(name)) {
			return null;
		}
		for (SearchResult details : POTION_DETAILS) {
			if (TextUtil.stringEqualsIgnoreCase(details.name, name)) {
				return details;
			}
		}
		return null;
	}

	/**
	 * This class represents a data set for a particular enchantment.
	 */
	public static final class SearchResult {

		private final PotionType type;

		private final PotionEffectType effectType;

		private final String name;

		private SearchResult(PotionType type, PotionEffectType effectType, String name) {
			this.type = type;
			this.effectType = effectType;
			this.name = name;
		}

		/**
		 * @return Returns the potion type.
		 */
		public PotionType getPotionType() {
			return this.type;
		}

		/**
		 * @return Returns the potion's effect type.
		 */
		public PotionEffectType getEffectType() {
			return this.effectType;
		}

		/**
		 * @return Returns the potion's name.
		 */
		public String getName() {
			return this.name;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.type, this.effectType, this.name);
		}

	}

}
