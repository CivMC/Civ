package vg.civcraft.mc.civmodcore.inventory.items;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.util.CivLogger;
import vg.civcraft.mc.civmodcore.util.KeyedUtils;

/**
 * Class of static utilities for Enchantments.
 */
public final class EnchantUtils {

	private static final BiMap<Enchantment, String> ENCHANT_NAMES = ImmutableBiMap.<Enchantment, String>builder()
			// Beta 1.9
			.put(Enchantment.DAMAGE_ALL, "Sharpness")
			.put(Enchantment.DAMAGE_ARTHROPODS, "Bane of Arthropods")
			.put(Enchantment.DAMAGE_UNDEAD, "Smite")
			.put(Enchantment.DIG_SPEED, "Efficiency")
			.put(Enchantment.DURABILITY, "Unbreaking")
			.put(Enchantment.FIRE_ASPECT, "Fire Aspect")
			.put(Enchantment.KNOCKBACK, "Knockback")
			.put(Enchantment.LOOT_BONUS_BLOCKS, "Fortune")
			.put(Enchantment.LOOT_BONUS_MOBS, "Looting")
			.put(Enchantment.OXYGEN, "Respiration")
			.put(Enchantment.PROTECTION_ENVIRONMENTAL, "Protection")
			.put(Enchantment.PROTECTION_EXPLOSIONS, "Blast Protection")
			.put(Enchantment.PROTECTION_FALL, "Feather Falling")
			.put(Enchantment.PROTECTION_FIRE, "Fire Protection")
			.put(Enchantment.PROTECTION_PROJECTILE, "Projectile Protection")
			.put(Enchantment.SILK_TOUCH, "Silk Touch")
			.put(Enchantment.WATER_WORKER, "Aqua Affinity")
			// 1.1
			.put(Enchantment.ARROW_DAMAGE, "Power")
			.put(Enchantment.ARROW_FIRE, "Flame")
			.put(Enchantment.ARROW_INFINITE, "Infinity")
			.put(Enchantment.ARROW_KNOCKBACK, "Punch")
			// 1.4.6
			.put(Enchantment.THORNS, "Thorns")
			// 1.7.2
			.put(Enchantment.LUCK, "Luck of the Sea")
			.put(Enchantment.LURE, "Lure")
			// 1.8
			.put(Enchantment.DEPTH_STRIDER, "Depth Strider")
			// 1.9
			.put(Enchantment.FROST_WALKER, "Frost Walker")
			.put(Enchantment.MENDING, "Mending")
			// 1.11
			.put(Enchantment.BINDING_CURSE, "Curse of Binding")
			.put(Enchantment.VANISHING_CURSE, "Curse of Vanishing")
			// 1.11.1
			.put(Enchantment.SWEEPING_EDGE, "Sweeping Edge")
			// 1.13
			.put(Enchantment.CHANNELING, "Channeling")
			.put(Enchantment.IMPALING, "Impaling")
			.put(Enchantment.LOYALTY, "Loyalty")
			.put(Enchantment.RIPTIDE, "Riptide")
			// 1.14
			.put(Enchantment.MULTISHOT, "Multishot")
			.put(Enchantment.PIERCING, "Piercing")
			.put(Enchantment.QUICK_CHARGE, "Quick Charge")
			// 1.16
			.put(Enchantment.SOUL_SPEED, "Soul Speed")
			.build();

	private static final BiMap<Enchantment, String> ENCHANT_ABBR = HashBiMap.create(ENCHANT_NAMES.size());

	static {
		// Determine if there's any enchants missing names
		final Set<Enchantment> missing = new HashSet<>();
		CollectionUtils.addAll(missing, Enchantment.values());
		missing.removeIf(ENCHANT_NAMES::containsKey);
		if (!missing.isEmpty()) {
			//noinspection deprecation
			Bukkit.getLogger().warning("[EnchantUtils] The following enchants are lacking names: " +
					missing.stream().map(Enchantment::getName).collect(Collectors.joining(",")) + ".");
		}
	}

	/**
	 * Loads enchantment names and initials from the config.
	 */
	public static void loadEnchantAbbreviations(final CivModCorePlugin plugin) {
		final var logger = CivLogger.getLogger(EnchantUtils.class);
		ENCHANT_ABBR.clear();
		final File enchantsFile = plugin.getDataFile("enchants.yml");
		final YamlConfiguration enchantsConfig = YamlConfiguration.loadConfiguration(enchantsFile);
		for (final String key : enchantsConfig.getKeys(false)) {
			if (Strings.isNullOrEmpty(key)) {
				logger.warning("Enchantment key was empty.");
				continue;
			}
			final Enchantment enchant = EnchantUtils.getEnchantment(key);
			if (enchant == null) {
				logger.warning("Could not find enchantment: " + key);
				return;
			}
			final String abbreviation = enchantsConfig.getString(key);
			if (Strings.isNullOrEmpty(abbreviation)) {
				logger.warning("Abbreviation for [" + key + "] was empty.");
				continue;
			}
			ENCHANT_ABBR.put(enchant, abbreviation);
		}
		logger.info("Loaded a total of " + ENCHANT_ABBR.size() + " abbreviations from enchants.yml");
		// Determine if there's any enchants missing abbreviations
		final Set<Enchantment> missing = new HashSet<>();
		CollectionUtils.addAll(missing, Enchantment.values());
		missing.removeIf(ENCHANT_ABBR::containsKey);
		if (!missing.isEmpty()) {
			//noinspection deprecation
			logger.warning("The following enchants are missing from enchants.yml: " +
					missing.stream().map(Enchantment::getName).collect(Collectors.joining(",")) + ".");
		}
	}

	/**
	 * Attempts to retrieve an enchantment by its slug, display name, and abbreviation.
	 *
	 * @param value The value to search for a matching enchantment by.
	 * @return Returns a matched enchantment or null.
	 */
	@SuppressWarnings("deprecation")
	public static Enchantment getEnchantment(final String value) {
		if (Strings.isNullOrEmpty(value)) {
			return null;
		}
		Enchantment enchantment;
		final var enchantmentKey = KeyedUtils.fromString(value);
		if (enchantmentKey != null) {
			enchantment = Enchantment.getByKey(enchantmentKey);
			if (enchantment != null) {
				return enchantment;
			}
		}
		enchantment = Enchantment.getByName(value.toUpperCase()); // deprecated
		if (enchantment != null) {
			return enchantment;
		}
		enchantment = ENCHANT_NAMES.inverse().get(value);
		if (enchantment != null) {
			return enchantment;
		}
		enchantment = ENCHANT_ABBR.inverse().get(value);
		if (enchantment != null) {
			return enchantment;
		}
		return null;
	}

	public static String getEnchantNiceName(final Enchantment enchant) {
		return ENCHANT_NAMES.get(enchant);
	}

	public static String getEnchantAbbreviation(final Enchantment enchant) {
		return ENCHANT_ABBR.get(enchant);
	}

	/**
	 * Determines whether an enchantment is considered safe.
	 *
	 * @param enchantment The enchantment to validate.
	 * @param level The enchantment level to validate.
	 * @return Returns true if the enchantment is not null, and the level is within the acceptable bounds.
	 *
	 * @see Enchantment#getStartLevel() The starting level. A valid level cannot be below this.
	 * @see Enchantment#getMaxLevel() The maximum level. A valid level cannot be above this.
	 */
	public static boolean isSafeEnchantment(final Enchantment enchantment, final int level) {
		return enchantment != null && level >= enchantment.getStartLevel() && level <= enchantment.getMaxLevel();
	}

	/**
	 * Gets the enchantments from an item.
	 *
	 * @param item The item to retrieve the enchantments from.
	 * @return Returns the item's enchantments, which are never null.
	 */
	public static Map<Enchantment, Integer> getEnchantments(final ItemStack item) {
		if (item == null) {
			return ImmutableMap.of();
		}
		return item.getEnchantments();
	}

	/**
	 * Adds a safe enchantment to an item.
	 *
	 * @param item The item to add the enchantment to.
	 * @param enchantment The enchantment to add to the item.
	 * @param level The level of the enchantment to add to the item.
	 * @return Returns true if the enchantment was successfully added.
	 *
	 * @see EnchantUtils#isSafeEnchantment(Enchantment, int)
	 */
	public static boolean addEnchantment(final ItemStack item, final Enchantment enchantment, final int level) {
		return addEnchantment(item, enchantment, level, true);
	}

	/**
	 * Adds an enchantment to an item.
	 *
	 * @param item The item to add the enchantment to.
	 * @param enchantment The enchantment to add to the item.
	 * @param level The level of the enchantment to add to the item.
	 * @param onlyAllowSafeEnchantments Requires enchantments to be safe if set to true.
	 * @return Returns true if the enchantment was successfully added.
	 *
	 * @see EnchantUtils#isSafeEnchantment(Enchantment, int)
	 */
	public static boolean addEnchantment(final ItemStack item,
										 final Enchantment enchantment,
										 final int level,
										 final boolean onlyAllowSafeEnchantments) {
		Preconditions.checkArgument(ItemUtils.isValidItem(item));
		return ItemUtils.handleItemMeta(item, (ItemMeta meta) ->
				meta.addEnchant(enchantment, level, !onlyAllowSafeEnchantments));
	}

	/**
	 * Removes an enchantment from an item.
	 *
	 * @param item The item to remove the enchantment from.
	 * @param enchant The enchantment to remove from the item.
	 * @return Returns true if the enchantment was successfully removed.
	 */
	public static boolean removeEnchantment(final ItemStack item, final Enchantment enchant) {
		Preconditions.checkArgument(ItemUtils.isValidItem(item));
		if (enchant == null) {
			return true;
		}
		return ItemUtils.handleItemMeta(item, (ItemMeta meta) -> meta.removeEnchant(enchant));
	}

	/**
	 * Removes all enchantments from an item.
	 *
	 * @param item The item to clear enchantment from.
	 */
	public static void clearEnchantments(final ItemStack item) {
		Preconditions.checkArgument(ItemUtils.isValidItem(item));
		ItemUtils.handleItemMeta(item, (ItemMeta meta) -> {
			meta.getEnchants().forEach((key, value) -> meta.removeEnchant(key));
			return true;
		});
	}

}
