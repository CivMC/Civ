package com.github.igotyou.FactoryMod.utility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import com.github.igotyou.FactoryMod.FactoryMod;

public class NiceNames {
	private static Map<Material, Map<Short, String>> items;
	private static Map<Enchantment, String> enchants;

	public static String getName(ItemStack is) {
		Map<Short, String> duraMap = items.get(is.getType());
		if (duraMap == null) {
			return "COULD NOT FIND NAME FOR " + is.getType() + ", dura:"
					+ is.getDurability();
		}
		String res = duraMap.get(is.getDurability());
		if (res == null) {
			return "COULD NOT FIND NAME FOR " + is.getType() + ", dura:"
					+ is.getDurability();
		} else {
			return res;
		}
	}

	public static String getName(Enchantment enchant) {
		return enchants.get(enchant);
	}

	public void loadNames() {
		//item aliases
		items = new HashMap<Material, Map<Short, String>>();
		int counter = 0;
		try {
			InputStream in = getClass().getResourceAsStream("/materials.csv");
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = reader.readLine();
			while (line != null) {
				String[] content = line.split(",");
				Map<Short, String> duraMap = items.get(Material
						.valueOf(content[1]));
				if (duraMap == null) {
					duraMap = new HashMap<Short, String>();
					items.put(Material.valueOf(content[1]), duraMap);
				}
				duraMap.put(Short.valueOf(content[3]), content[0]);
				line = reader.readLine();
				counter++;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FactoryMod.getPlugin().info(
				"Imported " + counter + " item name aliases");
		
		//enchantment aliases
		enchants = new HashMap<Enchantment, String>();
		counter = 0;
		try {
			InputStream in = getClass().getResourceAsStream("/enchantments.csv");
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			String line = reader.readLine();
			while (line != null) {
				String[] content = line.split(",");
				enchants.put(Enchantment.getByName(content[1]), content[0]);
				line = reader.readLine();
				counter++;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FactoryMod.getPlugin().info(
				"Imported " + counter + " enchantment aliases");
	}
}
