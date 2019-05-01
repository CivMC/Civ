package vg.civcraft.mc.civmodcore.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.google.common.collect.Lists;

import vg.civcraft.mc.civmodcore.areas.EllipseArea;
import vg.civcraft.mc.civmodcore.areas.GlobalYLimitedArea;
import vg.civcraft.mc.civmodcore.areas.IArea;
import vg.civcraft.mc.civmodcore.areas.RectangleArea;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class ConfigParsing {

	private static final Logger log = Bukkit.getLogger();

	/**
	 * Creates an itemmap containing all the items listed in the given config
	 * section
	 *
	 * @param config ConfigurationSection to parse the items from
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
				log.severe("Failed to find material" + m + " in section " + current.getCurrentPath());
				return im;
			}
		}
		ItemStack toAdd = new ItemStack(m);
		if (current.isInt("durability")) {
			log.warning("Item durability as specified at " + current.getCurrentPath() + " is no longer supported");
		}
		ItemMeta meta = toAdd.getItemMeta();
		if (meta == null) {
			log.severe("No item meta found for" + current.getCurrentPath());
		} else {
			String name = current.getString("name");
			if (name != null) {
				meta.setDisplayName(name);
			}
			List<String> lore = current.getStringList("lore");
			if (lore != null) {
				meta.setLore(lore);
			}
			if (current.isBoolean("unbreakable")) {
				meta.setUnbreakable(current.getBoolean("unbreakable"));
			}
			if (current.isBoolean("hideFlags") && current.getBoolean("hideFlags")) {
				for (ItemFlag flag : ItemFlag.values()) {
					meta.addItemFlags(flag);
				}
			}
			if (current.contains("enchants")) {
				for (String enchantKey : current.getConfigurationSection("enchants").getKeys(false)) {
					ConfigurationSection enchantConfig = current.getConfigurationSection("enchants")
							.getConfigurationSection(enchantKey);
					if (!enchantConfig.isString("enchant")) {
						log.warning("No enchant specified for enchantment entry at " + enchantConfig.getCurrentPath()
								+ ". Entry was ignored");
						continue;
					}
					Enchantment enchant;
					enchant = Enchantment
							.getByKey(NamespacedKey.minecraft((enchantConfig.getString("enchant").toLowerCase())));
					if (enchant == null) {
						log.severe("Failed to parse enchantment " + enchantConfig.getString("enchant")
								+ ", the entry was ignored");
						continue;
					}
					int level = enchantConfig.getInt("level", 1);
					meta.addEnchant(enchant, level, true);
				}
			}
			if (m == Material.LEATHER_BOOTS || m == Material.LEATHER_CHESTPLATE || m == Material.LEATHER_HELMET
					|| m == Material.LEATHER_LEGGINGS) {
				ConfigurationSection color = current.getConfigurationSection("color");
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
			if (m == Material.ENCHANTED_BOOK) {
				ConfigurationSection storedEnchantSection = current.getConfigurationSection("stored_enchants");
				if (storedEnchantSection != null) {
					EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) meta;
					for (String sEKey : storedEnchantSection.getKeys(false)) {
						ConfigurationSection currentStoredEnchantSection = storedEnchantSection
								.getConfigurationSection(sEKey);
						if (currentStoredEnchantSection != null) {
							Enchantment enchant = Enchantment
									.getByName(currentStoredEnchantSection.getString("enchant"));
							int level = currentStoredEnchantSection.getInt("level", 1);
							enchantMeta.addStoredEnchant(enchant, level, true);
						}
					}
				}
			}
			if (m == Material.POTION || m == Material.SPLASH_POTION || m == Material.LINGERING_POTION
					|| m == Material.TIPPED_ARROW) {
				ConfigurationSection potion = current.getConfigurationSection("potion_effects");
				if (potion != null) {
					PotionType potType;
					try {
						potType = PotionType.valueOf(potion.getString("type", "AWKWARD"));
					} catch (IllegalArgumentException e) {
						log.warning("Expected potion type at " + potion.getCurrentPath() + ", but "
								+ potion.getString("type") + " is not a valid potion type");
						potType = PotionType.AWKWARD;
					}
					boolean upgraded = potion.getBoolean("upgraded", false);
					boolean extended = potion.getBoolean("extended", false);
					PotionMeta potMeta = (PotionMeta) meta;
					potMeta.setBasePotionData(new PotionData(potType, extended, upgraded));
					ConfigurationSection customEffects = potion.getConfigurationSection("custom_effects");
					if (customEffects != null) {
						List<PotionEffect> pots = parsePotionEffects(potion);
						for (PotionEffect pe : pots) {
							potMeta.addCustomEffect(pe, true);
						}
					}
				}

			}
			toAdd.setItemMeta(meta);
			if (current.contains("nbt")) {
				toAdd = ItemMap.enrichWithNBT(toAdd, 1, current.getConfigurationSection("nbt").getValues(true));
			}
		}
		// Setting amount must be last just in cast enrichWithNBT is called,
		// which
		// resets the amount to 1.
		int amount = current.getInt("amount", 1);
		toAdd.setAmount(amount);
		im.addItemStack(toAdd);
		return im;
	}

	public static long parseTime(String arg, TimeUnit unit) {
		long inTicks = parseTime(arg);
		long millis = inTicks * 50;
		return unit.convert(millis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Parses a time value specified in a config. This allows to specify human
	 * readable time values easily, instead of having to specify every amount in
	 * ticks or seconds. The unit of a number specifed by the letter added after it,
	 * for example 5h means 5 hours or 34s means 34 seconds. Possible modifiers are:
	 * t (ticks), s (seconds), m (minutes), h (hours) and d (days). If no letter is
	 * added the value will be parsed as ticks.
	 * <p>
	 * Additionally you can combine those amounts in any way you want, for example
	 * you can specify 3h5m43s as 3 hours, 5 minutes and 43 seconds. This doesn't
	 * have to be sorted and may even list the same unit multiple times for
	 * different values, but the values are not allowed to be separated by anything
	 *
	 * @param arg Parsed string containing the time format
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
				log.severe("Invalid time value in config:" + arg);
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
	 * @param configurationSection ConfigurationSection to parse the effect from
	 * @return The potion effect parsed
	 */
	public static List<PotionEffect> parsePotionEffects(ConfigurationSection configurationSection) {
		List<PotionEffect> potionEffects = Lists.newArrayList();
		if (configurationSection != null) {
			for (String name : configurationSection.getKeys(false)) {
				ConfigurationSection configEffect = configurationSection.getConfigurationSection(name);
				String type = configEffect.getString("type");
				if (type == null) {
					log.severe("Expected potion type to be specified, but found no \"type\" option at "
							+ configEffect.getCurrentPath());
					continue;
				}
				PotionEffectType effect = PotionEffectType.getByName(type);
				if (effect == null) {
					log.severe("Expected potion type to be specified at " + configEffect.getCurrentPath()
							+ " but found " + type + " which is no valid type");
				}
				int duration = configEffect.getInt("duration", 200);
				int amplifier = configEffect.getInt("amplifier", 0);
				potionEffects.add(new PotionEffect(effect, duration, amplifier));
			}
		}
		return potionEffects;
	}

	public static IArea parseArea(ConfigurationSection config) {
		if (config == null) {
			log.warning("Tried to parse area on null section");
			return null;
		}
		String type = config.getString("type");
		if (type == null) {
			log.warning("Found no area type at " + config.getCurrentPath());
			return null;
		}
		int lowerYBound = config.getInt("lowerYBound", 0);
		int upperYBound = config.getInt("upperYBound", 255);
		String worldName = config.getString("world");
		if (worldName == null) {
			log.warning("Found no world specified for area at " + config.getCurrentPath());
			return null;
		}
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			log.warning("Found no world with name " + worldName + " as specified at " + config.getCurrentPath());
			return null;
		}
		Location center = null;
		if (config.isConfigurationSection("center")) {
			ConfigurationSection centerSection = config.getConfigurationSection("center");
			int x = centerSection.getInt("x", 0);
			int y = centerSection.getInt("y", 0);
			int z = centerSection.getInt("z", 0);
			if (world != null) {
				center = new Location(world, x, y, z);
			}
		}
		int xSize = config.getInt("xSize", -1);
		int zSize = config.getInt("zSize", -1);
		IArea area = null;
		switch (type) {
		case "GLOBAL":
			area = new GlobalYLimitedArea(lowerYBound, upperYBound, world);
			break;
		case "ELLIPSE":
			if (center == null) {
				log.warning("Found no center for area at " + config.getCurrentPath());
				return null;
			}
			if (xSize == -1) {
				log.warning("Found no xSize for area at " + config.getCurrentPath());
				return null;
			}
			if (zSize == -1) {
				log.warning("Found no zSize for area at " + config.getCurrentPath());
				return null;
			}
			area = new EllipseArea(lowerYBound, upperYBound, center, xSize, zSize);
			break;
		case "RECTANGLE":
			if (center == null) {
				log.warning("Found no center for area at " + config.getCurrentPath());
				return null;
			}
			if (xSize == -1) {
				log.warning("Found no xSize for area at " + config.getCurrentPath());
				return null;
			}
			if (zSize == -1) {
				log.warning("Found no zSize for area at " + config.getCurrentPath());
				return null;
			}
			area = new RectangleArea(lowerYBound, upperYBound, center, xSize, zSize);
			break;
		default:
			log.warning("Invalid area type " + type + " at " + config.getCurrentPath());
		}
		return area;
	}

	/**
	 * Parses a section which contains key-value mappings of a type to another type
	 * 
	 * @param <E>        Key type
	 * @param <V>        Value type
	 * @param parent     Configuration section containing the section with the
	 *                   values
	 * @param identifier Config identifier of the section containing the entries
	 */
	public static <K, V> void parseKeyValueMap(ConfigurationSection parent, String identifier, Logger logger,
			Function<String, K> keyConverter, Function<String, V> valueConverter, Map<K, V> mapToUse) {
		if (!parent.isConfigurationSection(identifier)) {
			return;
		}
		ConfigurationSection section = parent.getConfigurationSection(identifier);
		for (String keyString : section.getKeys(false)) {
			if (!section.isString(keyString)) {
				logger.warning(
						"Ignoring invalid " + identifier + " entry " + keyString + " at " + section.getCurrentPath());
				continue;
			}
			K keyinstance;
			try {
				keyinstance = keyConverter.apply(keyString);
			} catch (IllegalArgumentException e) {
				logger.warning("Failed to parse " + identifier + " " + keyString + " at " + section.getCurrentPath()
						+ ": " + e.toString());
				continue;
			}
			V value = valueConverter.apply(section.getString(keyString));
			mapToUse.put(keyinstance, value);
		}
	}

}
