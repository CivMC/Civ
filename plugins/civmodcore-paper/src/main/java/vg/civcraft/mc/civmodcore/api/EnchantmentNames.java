package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;

/**
 * Class that loads and store enchantments names. Replaces NiceNames.
 * */
public final class EnchantmentNames {

	private static final Logger logger = Bukkit.getLogger();

	private static final Map<Enchantment, String> enchantmentNames = new HashMap<>();

	private EnchantmentNames() { } // Make the class effectively static

	/**
	 * Resets all enchantments names and initials.
	 * */
	public static void resetEnchantmentNames() {
		enchantmentNames.clear();
	}

	/**
	 * Loads enchantment names and initials from the config.
	 * */
	public static void loadEnchantmentNames() {
		resetEnchantmentNames();
		// Load enchantment names from enchantments.csv
		InputStream enchantmentsCSV = CivModCorePlugin.class.getResourceAsStream("/enchantments.csv");
		if (enchantmentsCSV != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(enchantmentsCSV));
				String line = reader.readLine();
				while (line != null) {
					String [] values = line.split(",");
					// If there's not at least three values (slug, initials, name) then skip
					if (values.length < 2) {
						logger.warning("This enchantment row does not have enough data: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If the Enchantment cannot be found by the slug given, then skip
					@SuppressWarnings("deprecation")
					Enchantment enchantment = Enchantment.getByName(values[0]);
					if (enchantment == null) {
						logger.warning("Could not find an enchantment on this line: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If the name is empty, skip
					String name = values[1];
					if (name.isEmpty()) {
						logger.warning("This enchantment has not been given a name: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// Put the enchantment and name into the system
					enchantmentNames.put(enchantment, name);
					logger.info(String.format("Enchantment parsed: %s = %s", enchantment, name));
					line = reader.readLine();
				}
				reader.close();
			}
			catch (IOException error) {
				logger.log(Level.WARNING, "Could not load enchantments from enchantments.csv", error);
			}
		}
		else {
			logger.warning("Could not load enchantments from enchantments.csv as the file does not exist.");
		}
	}

	/**
	 * Gets an enchantment's name, e.g: DIG_SPEED to Efficiency
	 *
	 * @param enchantment The enchantment to get the name of.
	 * @return Returns the enchantment's name, or null is none is set.
	 *
	 * @throws IllegalArgumentException If the given enchantment is null.
	 * */
	@Nullable
	public static String getEnchantmentName(@Nonnull Enchantment enchantment) {
		Preconditions.checkNotNull(enchantment, "Cannot retrieve the enchantments's name; the enchantment is null.");
		return enchantmentNames.get(enchantment);
	}

}
