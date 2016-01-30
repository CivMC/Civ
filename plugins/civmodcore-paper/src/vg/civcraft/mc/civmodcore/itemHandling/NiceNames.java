package vg.civcraft.mc.civmodcore.itemHandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.ACivMod;

public class NiceNames {
	private static Map<NameSearchObject, String> items;
	private static Map<Enchantment, String> enchants;
	private static Map<Enchantment, String> enchantAcronyms;
	private static String enchantmentsFile = "/resources/enchantments.csv";
	private static String materialsFile = "/resources/materials.csv";

	private static class NameSearchObject {
		private String data;

		private NameSearchObject(Material m, short dura, List<String> lore) {
			StringBuilder sb = new StringBuilder();
			sb.append(m.toString());
			sb.append("#");
			sb.append(dura);
			for (String s : lore) {
				sb.append(s);
			}
			data = sb.toString();
		}

		private NameSearchObject(ItemStack is) {
			this(is.getType(), is.getDurability(),
					is.getItemMeta().hasLore() ? is.getItemMeta().getLore()
							: new LinkedList<String>());
		}

		public int hashCode() {
			return data.hashCode();
		}

		public boolean equals(Object o) {
			return o instanceof NameSearchObject
					&& ((NameSearchObject) o).getData().equals(data);
		}

		private String getData() {
			return data;
		}
	}

	public static String getName(ItemStack is) {
		return items.get(new NameSearchObject(is));
	}

	public static String getName(Enchantment enchant) {
		return enchants.get(enchant);
	}

	public static String getAcronym(Enchantment enchant) {
		return enchantAcronyms.get(enchant);
	}

	public void loadNames(ACivMod plugin){
		// item aliases
		items = new HashMap<NiceNames.NameSearchObject, String>();
		File materialsDir = new File(plugin.getDataFolder().getParent() + materialsFile);
		try {
			if(materialsDir.exists()){
				BufferedReader reader = new BufferedReader(new FileReader(materialsDir));
				String line = reader.readLine();
				while (line != null) {
					String[] content = line.split(",");
					NameSearchObject nso = new NameSearchObject(
							Material.valueOf(content[1]),
							Short.valueOf(content[3]), new LinkedList<String>());
					items.put(nso, content[0]);
					line = reader.readLine();
				}
				reader.close();
			}
			else{
				plugin.warning("materials.csv could not be loaded because it does not exist!");
				plugin.warning("Attempted to read: " + materialsDir.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// enchantment aliases
		enchants = new HashMap<Enchantment, String>();
		enchantAcronyms = new HashMap<Enchantment, String>();
		File enchantmentsDir = new File(plugin.getDataFolder().getParent() + enchantmentsFile);
		try {
			if(enchantmentsDir.exists()){
				BufferedReader reader = new BufferedReader(new FileReader(enchantmentsDir));
				String line = reader.readLine();
				while (line != null) {
					String[] content = line.split(",");
					Enchantment enchant = Enchantment.getByName(content[1]);
					enchants.put(enchant, content[2]);
					enchantAcronyms.put(enchant, content[0]);
					line = reader.readLine();
				}
				reader.close();
			}
			else{
				plugin.warning("enchantments.csv could not be loaded because it does not exist!");
				plugin.warning("Attempted to read: " + enchantmentsDir.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void registerItem(ItemStack is, String name) {
		items.put(new NameSearchObject(is), name);
	}
}
