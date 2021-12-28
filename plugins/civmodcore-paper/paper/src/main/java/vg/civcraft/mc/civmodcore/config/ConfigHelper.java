package vg.civcraft.mc.civmodcore.config;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
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
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.world.model.EllipseArea;
import vg.civcraft.mc.civmodcore.world.model.GlobalYLimitedArea;
import vg.civcraft.mc.civmodcore.world.model.IArea;
import vg.civcraft.mc.civmodcore.world.model.RectangleArea;

@UtilityClass
public final class ConfigHelper {

	private static final Logger LOGGER = Bukkit.getLogger();

	/**
	 * Retrieves the configuration section at the given key on the given configuration section.
	 *
	 * @param config The config to get the keyed section from.
	 * @param key The key of the section to retrieve.
	 * @return Returns the configuration section at the given key, or returns a new, empty section.
	 */
	@Nonnull
	public static ConfigurationSection getSection(@NonNull final ConfigurationSection config,
												  @NonNull final String key) {
		ConfigurationSection found = config.getConfigurationSection(key);
		if (found == null) {
			found = config.createSection(key);
		}
		return found;
	}

	/**
	 * Retrieves a string list from a given config section. If the keyed value is a standalone string instead of a
	 * list, that value will be converted to a list.
	 *
	 * @param config The config section to retrieve the list from.
	 * @param key The key to get the list of.
	 * @return Returns a list of strings, which is never null.
	 */
	@Nonnull
	public static List<String> getStringList(@NonNull final ConfigurationSection config,
											 @NonNull final String key) {
		if (config.isString(key)) {
			final var list = new ArrayList<String>(1);
			list.add(config.getString(key));
			return list;
		}
		return config.getStringList(key);
	}

	/**
	 * Attempts to retrieve a list from a config section.
	 *
	 * @param <T> The type to parse the list into.
	 * @param config The config section.
	 * @param key The key of the list.
	 * @param parser The parser to convert the string value into the correct type.
	 * @return Returns a list, or null.
	 */
	@Nonnull
	public static <T> List<T> parseList(@NonNull final ConfigurationSection config,
										@NonNull final String key,
										@NonNull final Function<String, T> parser) {
		if (!config.isList(key)) {
			return new ArrayList<>(0);
		}
		final var entries = getStringList(config, key);
		final var result = new ArrayList<T>(entries.size());
		for (final String entry : entries) {
			final T item = parser.apply(entry);
			if (item != null) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * Attempts to retrieve a list of materials from a config section.
	 *
	 * @param config The config section.
	 * @param key The key of the list.
	 * @return Returns a list of materials, or null.
	 */
	@Nonnull
	public static List<Material> parseMaterialList(@Nonnull final ConfigurationSection config,
												   @Nonnull final String key) {
		return parseList(config, key, MaterialUtils::getMaterial);
	}

	/**
	 * Creates an item map containing all the items listed in the given config
	 * section
	 *
	 * @param config ConfigurationSection to parse the items from
	 * @return The item map created
	 */
	@Nonnull
	public static ItemMap parseItemMap(@Nullable final ConfigurationSection config) {
		final var result = new ItemMap();
		if (config == null) {
			return result;
		}
		for (final String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			ItemMap partMap = parseItemMapDirectly(current);
			result.merge(partMap);
		}
		return result;
	}

	@Nonnull
	public static ItemMap parseItemMapDirectly(@Nullable final ConfigurationSection current) {
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
				LOGGER.severe("Failed to find material " + current.getString("material") + " in section " + current.getCurrentPath());
				return im;
			}
		}
		ItemStack toAdd = new ItemStack(m);
		if (current.isInt("durability")) {
			LOGGER.warning("Item durability as specified at " + current.getCurrentPath() + " is no longer supported");
		}
		ItemMeta meta = toAdd.getItemMeta();
		if (meta == null) {
			LOGGER.severe("No item meta found for" + current.getCurrentPath());
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
						LOGGER.warning("No enchant specified for enchantment entry at " + enchantConfig.getCurrentPath()
								+ ". Entry was ignored");
						continue;
					}
					Enchantment enchant;
					enchant = Enchantment
							.getByKey(NamespacedKey.minecraft((enchantConfig.getString("enchant").toLowerCase())));
					if (enchant == null) {
						LOGGER.severe("Failed to parse enchantment " + enchantConfig.getString("enchant")
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
							Enchantment enchant = EnchantUtils.getEnchantment(currentStoredEnchantSection.getString("enchant"));
							int level = currentStoredEnchantSection.getInt("level", 1);
							if (enchant != null) {
								enchantMeta.addStoredEnchant(enchant, level, true);
							}
							else {
								LOGGER.severe("Failed to parse enchantment at " + currentStoredEnchantSection.getCurrentPath()
										+ ", it was not applied");
							}
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
						LOGGER.warning("Expected potion type at " + potion.getCurrentPath() + ", but "
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

	public static int parseTimeAsTicks(@Nonnull final String arg) {
		return (int) (parseTime(arg, TimeUnit.MILLISECONDS) / 50L);
	}

	public static long parseTime(@Nonnull final String arg,
								 @Nonnull final TimeUnit unit) {
		long millis = parseTime(arg);
		return unit.convert(millis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Parses a time value specified in a config. This allows to specify human
	 * readable time values easily, instead of having to specify every amount in
	 * ticks or seconds. The unit of a number specifed by the letter added after it,
	 * for example 5h means 5 hours or 34s means 34 seconds. Possible modifiers are:
	 * t (ticks), s (seconds), m (minutes), h (hours) and d (days)
	 * <p>
	 * Additionally you can combine those amounts in any way you want, for example
	 * you can specify 3h5m43s as 3 hours, 5 minutes and 43 seconds. This doesn't
	 * have to be sorted and may even list the same unit multiple times for
	 * different values, but the values are not allowed to be separated by anything
	 *
	 * @param input Parsed string containing the time format
	 * @return How many milliseconds the given time value is
	 */
	public static long parseTime(@Nonnull String input) {
		input = input.replace(" ", "").replace(",", "").toLowerCase();
		long result = 0;
		try {
			result += Long.parseLong(input);
			return result;
		} catch (NumberFormatException e) {
		}
		while (!input.equals("")) {
			String typeSuffix = getSuffix(input, Character::isLetter);
			input = input.substring(0, input.length() - typeSuffix.length());
			String numberSuffix = getSuffix(input, Character::isDigit);
			input = input.substring(0, input.length() - numberSuffix.length());
			long duration;
			if (numberSuffix.length() == 0) {
				duration = 1;
			} else {
				duration = Long.parseLong(numberSuffix);
			}
			switch (typeSuffix) {
				case "ms":
				case "milli":
				case "millis":
					result += duration;
					break;
				case "s": // seconds
				case "sec":
				case "second":
				case "seconds":
					result += TimeUnit.SECONDS.toMillis(duration);
					break;
				case "m": // minutes
				case "min":
				case "minute":
				case "minutes":
					result += TimeUnit.MINUTES.toMillis(duration);
					break;
				case "h": // hours
				case "hour":
				case "hours":
					result += TimeUnit.HOURS.toMillis(duration);
					break;
				case "d": // days
				case "day":
				case "days":
					result += TimeUnit.DAYS.toMillis(duration);
					break;
				case "w": // weeks
				case "week":
				case "weeks":
					result += TimeUnit.DAYS.toMillis(duration * 7);
					break;
				case "month": // weeks
				case "months":
					result += TimeUnit.DAYS.toMillis(duration * 30);
					break;
				case "y":
				case "year":
				case "years":
					result += TimeUnit.DAYS.toMillis(duration * 365);
					break;
				case "never":
				case "inf":
				case "infinite":
				case "perm":
				case "perma":
				case "forever":
					// 1000 years counts as perma
					result += TimeUnit.DAYS.toMillis(365 * 1000);
				default:
					// just ignore it
			}
		}
		return result;
	}

	@Nonnull
	private static String getSuffix(@Nonnull final String arg,
									@Nonnull final Predicate<Character> selector) {
		StringBuilder number = new StringBuilder();
		for (int i = arg.length() - 1; i >= 0; i--) {
			if (selector.test(arg.charAt(i))) {
				number.insert(0, arg.substring(i, i + 1));
			} else {
				break;
			}
		}
		return number.toString();
	}

	/**
	 * Parses a potion effect
	 *
	 * @param configurationSection ConfigurationSection to parse the effect from
	 * @return The potion effect parsed
	 */
	@Nonnull
	public static List<PotionEffect> parsePotionEffects(@Nullable final ConfigurationSection configurationSection) {
		List<PotionEffect> potionEffects = Lists.newArrayList();
		if (configurationSection != null) {
			for (String name : configurationSection.getKeys(false)) {
				ConfigurationSection configEffect = configurationSection.getConfigurationSection(name);
				String type = configEffect.getString("type");
				if (type == null) {
					LOGGER.severe("Expected potion type to be specified, but found no \"type\" option at "
							+ configEffect.getCurrentPath());
					continue;
				}
				PotionEffectType effect = PotionEffectType.getByName(type);
				if (effect == null) {
					LOGGER.severe("Expected potion type to be specified at " + configEffect.getCurrentPath()
							+ " but found " + type + " which is no valid type");
				}
				int duration = configEffect.getInt("duration", 200);
				int amplifier = configEffect.getInt("amplifier", 0);
				potionEffects.add(new PotionEffect(effect, duration, amplifier));
			}
		}
		return potionEffects;
	}

	@Nullable
	public static IArea parseArea(@Nullable final ConfigurationSection config) {
		if (config == null) {
			LOGGER.warning("Tried to parse area on null section");
			return null;
		}
		String type = config.getString("type");
		if (type == null) {
			LOGGER.warning("Found no area type at " + config.getCurrentPath());
			return null;
		}
		int lowerYBound = config.getInt("lowerYBound", 0);
		int upperYBound = config.getInt("upperYBound", 255);
		String worldName = config.getString("world");
		if (worldName == null) {
			LOGGER.warning("Found no world specified for area at " + config.getCurrentPath());
			return null;
		}
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			LOGGER.warning("Found no world with name " + worldName + " as specified at " + config.getCurrentPath());
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
					LOGGER.warning("Found no center for area at " + config.getCurrentPath());
					return null;
				}
				if (xSize == -1) {
					LOGGER.warning("Found no xSize for area at " + config.getCurrentPath());
					return null;
				}
				if (zSize == -1) {
					LOGGER.warning("Found no zSize for area at " + config.getCurrentPath());
					return null;
				}
				area = new EllipseArea(lowerYBound, upperYBound, center, xSize, zSize);
				break;
			case "RECTANGLE":
				if (center == null) {
					LOGGER.warning("Found no center for area at " + config.getCurrentPath());
					return null;
				}
				if (xSize == -1) {
					LOGGER.warning("Found no xSize for area at " + config.getCurrentPath());
					return null;
				}
				if (zSize == -1) {
					LOGGER.warning("Found no zSize for area at " + config.getCurrentPath());
					return null;
				}
				area = new RectangleArea(lowerYBound, upperYBound, center, xSize, zSize);
				break;
			default:
				LOGGER.warning("Invalid area type " + type + " at " + config.getCurrentPath());
		}
		return area;
	}

	/**
	 * Parses a section which contains key-value mappings of a type to another type
	 *
	 * @param <K>            Key type
	 * @param <V>            Value type
	 * @param parent         Configuration section containing the section with the values
	 * @param identifier     Config identifier of the section containing the entries
	 * @param logger         The logger to write in progress work to
	 * @param keyConverter   Converts strings to type K
	 * @param valueConverter Converts strings to type V
	 * @param mapToUse       The map to place parsed keys and values.
	 */
	public static <K, V> void parseKeyValueMap(@Nonnull final ConfigurationSection parent,
											   @Nonnull final String identifier,
											   @Nonnull final Logger logger,
											   @Nonnull final Function<String, K> keyConverter,
											   @Nonnull final Function<String, V> valueConverter,
											   @Nonnull final Map<K, V> mapToUse) {
		if (!parent.isConfigurationSection(identifier)) {
			return;
		}
		ConfigurationSection section = parent.getConfigurationSection(identifier);
		for (String keyString : section.getKeys(false)) {
			if (section.isConfigurationSection(keyString)) {
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

	/**
	 * @deprecated Use {@link EnchantUtils#getEnchantment(String)} instead.
	 */
	@Nullable
	@Deprecated(forRemoval = true)
	public static Enchantment parseEnchantment(@Nonnull final ConfigurationSection config,
											   @Nonnull final String key) {
		return EnchantUtils.getEnchantment(config.getString(key));
	}

}
