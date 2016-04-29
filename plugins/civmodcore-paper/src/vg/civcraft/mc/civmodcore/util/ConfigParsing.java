package vg.civcraft.mc.civmodcore.util;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class ConfigParsing {

	/**
	 * Creates an itemmap containing all the items listed in the given config
	 * section
	 * 
	 * @param config
	 *            ConfigurationSection to parse the items from
	 * @return The item map created
	 */
	public static ItemMap parseItemMap(ConfigurationSection config) {
		ItemMap result = new ItemMap();
		if (config == null) {
			return result;
		}
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			ItemMap partMap = parseItemMapDirectly(current);
			result.merge(partMap);
		}
		return result;
	}

	public static ItemMap parseItemMapDirectly(ConfigurationSection current) {
		ItemMap im = new ItemMap();
		if (current == null) {
			return im;
		}
		Material m = null;
		try {
			m = Material.valueOf(current.getString("material"));
		} catch (IllegalArgumentException iae) {
			m = null;
		} finally {
			if (m == null) {
				Bukkit.getServer()
						.getLogger()
						.log(Level.SEVERE,
								"Failed to find material of section {0}",
								current.getCurrentPath());
				return im;
			}
		}
		ItemStack toAdd = new ItemStack(m);
		int amount = current.getInt("amount", 1);
		toAdd.setAmount(amount);
		int durability = current.getInt("durability", 0);
		toAdd.setDurability((short) durability);
		ItemMeta meta = toAdd.getItemMeta();
		if (meta == null) {
			Bukkit.getServer()
					.getLogger()
					.log(Level.SEVERE, "No item meta found for {0}",
							current.getCurrentPath());
		} else {
			String name = current.getString("name");
			if (name != null) {
				meta.setDisplayName(name);
			}
			List<String> lore = current.getStringList("lore");
			if (lore != null) {
				meta.setLore(lore);
			}
			if (current.contains("enchants")) {
				for (String enchantKey : current.getConfigurationSection(
						"enchants").getKeys(false)) {
					ConfigurationSection enchantConfig = current
							.getConfigurationSection("enchants")
							.getConfigurationSection(enchantKey);
					Enchantment enchant = Enchantment.getByName(enchantConfig
							.getString("enchant"));
					int level = enchantConfig.getInt("level", 1);
					meta.addEnchant(enchant, level, true);
				}
			}
			if (m == Material.LEATHER_BOOTS || m == Material.LEATHER_CHESTPLATE
					|| m == Material.LEATHER_HELMET
					|| m == Material.LEATHER_LEGGINGS) {
				ConfigurationSection color = current
						.getConfigurationSection("color");
				Color leatherColor = null;
				if (color != null) {
					int red = color.getInt("red");
					int blue = color.getInt("blue");
					int green = color.getInt("green");
					leatherColor = Color.fromRGB(red, green, blue);
				} else {
					String hexColorCode = current.getString("color");
					if (hexColorCode != null) {
						Integer hexColor = Integer.parseInt(hexColorCode, 16);
						if (hexColor != null) {
							leatherColor = Color.fromRGB(hexColor);
						}
					}
				}
				if (leatherColor != null) {
					((LeatherArmorMeta) meta).setColor(leatherColor);
				}
			}
			toAdd.setItemMeta(meta);
		}
		im.addItemStack(toAdd);
		return im;
	}

	/**
	 * Parses a time value specified in a config. This allows to specify human
	 * readable time values easily, instead of having to specify every amount in
	 * ticks or seconds. The unit of a number specifed by the letter added after
	 * it, for example 5h means 5 hours or 34s means 34 seconds. Possible
	 * modifiers are: t (ticks), s (seconds), m (minutes), h (hours) and d
	 * (days). If no letter is added the value will be parsed as ticks.
	 * 
	 * Additionally you can combine those amounts in any way you want, for
	 * example you can specify 3h5m43s as 3 hours, 5 minutes and 43 seconds.
	 * This doesn't have to be sorted and may even list the same unit multiple
	 * times for different values, but the values are not allowed to be
	 * separated by anything
	 * 
	 * @param arg
	 *            Parsed string containing the time format
	 * @return How many ticks the given time value is
	 */
	public static long parseTime(String arg) {
		long result = 0;
		boolean set = true;
		try {
			result += Long.parseLong(arg);
		} catch (NumberFormatException e) {
			set = false;
		}
		if (set) {
			return result;
		}
		while (!arg.equals("")) {
			int length = 0;
			switch (arg.charAt(arg.length() - 1)) {
			case 't': // ticks
				long ticks = getLastNumber(arg);
				result += ticks;
				length = String.valueOf(ticks).length() + 1;
				break;
			case 's': // seconds
				long seconds = getLastNumber(arg);
				result += 20 * seconds; // 20 ticks in a second
				length = String.valueOf(seconds).length() + 1;
				break;
			case 'm': // minutes
				long minutes = getLastNumber(arg);
				result += 20 * 60 * minutes;
				length = String.valueOf(minutes).length() + 1;
				break;
			case 'h': // hours
				long hours = getLastNumber(arg);
				result += 20 * 3600 * hours;
				length = String.valueOf(hours).length() + 1;
				break;
			case 'd': // days, mostly here to define a 'never'
				long days = getLastNumber(arg);
				result += 20 * 3600 * 24 * days;
				length = String.valueOf(days).length() + 1;
				break;
			default:
				Bukkit.getLogger()
						.severe("Invalid time value in config:" + arg);
			}
			arg = arg.substring(0, arg.length() - length);
		}
		return result;
	}

	/**
	 * Utility method used for time parsing
	 */
	private static long getLastNumber(String arg) {
		StringBuilder number = new StringBuilder();
		for (int i = arg.length() - 2; i >= 0; i--) {
			if (Character.isDigit(arg.charAt(i))) {
				number.insert(0, arg.substring(i, i + 1));
			} else {
				break;
			}
		}
		long result = Long.parseLong(number.toString());
		return result;
	}

	/**
	 * Parses a potion effect
	 * 
	 * @param configurationSection
	 *            ConfigurationSection to parse the effect from
	 * @return The potion effect parsed
	 */
	public static List<PotionEffect> parsePotionEffects(
			ConfigurationSection configurationSection) {
		List<PotionEffect> potionEffects = Lists.newArrayList();
		if (configurationSection != null) {
			for (String name : configurationSection.getKeys(false)) {
				ConfigurationSection configEffect = configurationSection
						.getConfigurationSection(name);
				String type = configEffect.getString("type");
				PotionEffectType effect = PotionEffectType.getByName(type);
				int duration = configEffect.getInt("duration", 200);
				int amplifier = configEffect.getInt("amplifier", 0);
				potionEffects
						.add(new PotionEffect(effect, duration, amplifier));
			}
		}
		return potionEffects;
	}
}
