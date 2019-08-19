package vg.civcraft.mc.civmodcore.itemHandling;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

public class NiceNames {

	private static Map<Enchantment, String> enchants = new HashMap<>();
	private static Map<Enchantment, String> enchantAcronyms = new HashMap<>();

	private static final Logger log = Bukkit.getLogger();

	public static String getName(Enchantment enchant) {
		return enchants.get(enchant);
	}

	public static String getAcronym(Enchantment enchant) {
		return enchantAcronyms.get(enchant);
	}

	public void loadNames() {
		// enchantment aliases
		try {
			InputStream in = getClass().getResourceAsStream("/enchantments.csv");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = reader.readLine();
			while (line != null) {
				String[] content = line.split(",");
				Enchantment enchant = Enchantment.getByName(content[1]);
				enchants.put(enchant, content[2]);
				enchantAcronyms.put(enchant, content[0]);
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			log.log(Level.WARNING, "Failed to load enchantment aliases", e);
		}
	}

}
