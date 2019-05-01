package vg.civcraft.mc.civmodcore.itemHandling;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NiceNames {

	private static Map<Material, String> materialString = new HashMap<>();
	private static Map<Material, Map<String, String>> materialLoreString = new HashMap<>();
	private static Map<Enchantment, String> enchants = new HashMap<>();
	private static Map<Enchantment, String> enchantAcronyms = new HashMap<>();

	private static final Logger log = Bukkit.getLogger();

	public static String getName(ItemStack is) {
		Material material = is.getType();

		// check for material lore name before the switch statement
		if(materialLoreString.containsKey(material) && is.hasItemMeta() && is.getItemMeta().hasLore()) {
			Map<String, String> registeredMat = materialLoreString.get(material);
			String combinedLore = String.join(",",is.getItemMeta().getLore());
			if(registeredMat.containsKey(combinedLore)) {
				return registeredMat.get(combinedLore);
			}
		}

		// use the default nice name for the given material
		switch(material) {
			default:
				return materialString.get(material);
		}
	}

	public static String getName(Enchantment enchant) {
		return enchants.get(enchant);
	}

	public static String getAcronym(Enchantment enchant) {
		return enchantAcronyms.get(enchant);
	}

	public void loadNames() {
		// create material strings
		for(Material material : Material.values()) {
			String name = material.toString().toLowerCase();
			boolean upper = true;
			for(int i = 0; i < name.length(); ++i) {
				if(name.charAt(i) == '_') {
					upper = true;
				} else if(upper) {
					name = name.substring(0,i)+name.substring(i,i+1).toUpperCase()+name.substring(i+1);
					upper = false;
				}
			}
			name = name.replace("_"," ");
			materialString.put(material, name);
		}

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

	public static void registerItem(ItemStack is, String name) {
		if(!is.hasItemMeta() || !is.getItemMeta().hasLore()) {
			return;
		}

		Material material = is.getType();
		ItemMeta meta = is.getItemMeta();
		List<String> lore = meta.getLore();

		if(!materialLoreString.containsKey(material)) {
			materialLoreString.put(material, new HashMap<>());
		}

		Map<String, String> registeredMat = materialLoreString.get(material);
		String combinedLore = String.join(",",lore);

		if(registeredMat.containsKey(combinedLore)) {
			log.info("Registered name override for "+material+"["+combinedLore+"] to "+name);
		}

		registeredMat.put(combinedLore, name);
	}

}
