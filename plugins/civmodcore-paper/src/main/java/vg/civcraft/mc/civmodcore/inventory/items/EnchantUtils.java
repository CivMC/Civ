package vg.civcraft.mc.civmodcore.inventory.items;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;
import vg.civcraft.mc.civmodcore.utilities.KeyedUtils;

/**
 * Class of static utilities for Enchantments.
 */
@UtilityClass
public final class EnchantUtils {

	private static final BiMap<Enchantment, String> ENCHANT_ABBR = HashBiMap.create(Enchantment.values().length);

	/**
	 * Loads enchantment names and initials from the config.
	 */
	public static void loadEnchantAbbreviations(@Nonnull final CivModCorePlugin plugin) {
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
	@Nullable
	@SuppressWarnings("deprecation")
	public static Enchantment getEnchantment(@Nullable final String value) {
		if (Strings.isNullOrEmpty(value)) {
			return null;
		}
		Enchantment enchantment;
		// From NamespacedKey
		final var enchantmentKey = KeyedUtils.fromString(value);
		if (enchantmentKey != null) {
			enchantment = Enchantment.getByKey(enchantmentKey);
			if (enchantment != null) {
				return enchantment;
			}
		}
		// From Name
		enchantment = Enchantment.getByName(value.toUpperCase()); // deprecated
		if (enchantment != null) {
			return enchantment;
		}
		// From Abbreviation
		enchantment = ENCHANT_ABBR.inverse().get(value);
		if (enchantment != null) {
			return enchantment;
		}
		return null;
	}

	/**
	 * @param enchant The enchantment to get a translatable component for.
	 * @return Returns a translatable component for the given enchantment.
	 */
	@Nonnull
	public static TranslatableComponent asTranslatable(@Nonnull final Enchantment enchant) {
		return Component.translatable(enchant.translationKey());
	}

	/**
	 * @param enchant The enchantment to get the name of.
	 * @return Returns the name of the enchantment, or null.
	 *
	 * @deprecated Use {@link #asTranslatable(Enchantment)} instead.
	 */
	@Nullable
	@Deprecated
	public static String getEnchantNiceName(@Nullable final Enchantment enchant) {
		return enchant == null ? null : ChatUtils.stringify(asTranslatable(enchant));
	}

	/**
	 * @param enchant The enchantment to get the abbreviation of.
	 * @return Returns the abbreviation of the enchantment, or null.
	 */
	@Nullable
	public static String getEnchantAbbreviation(@Nullable final Enchantment enchant) {
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
	public static boolean isSafeEnchantment(@Nullable final Enchantment enchantment, final int level) {
		return enchantment != null
				&& level >= enchantment.getStartLevel()
				&& level <= enchantment.getMaxLevel();
	}

	/**
	 * Gets the enchantments from an item.
	 *
	 * @param item The item to retrieve the enchantments from.
	 * @return Returns the item's enchantments, which are never null.
	 */
	@Nonnull
	public static Map<Enchantment, Integer> getEnchantments(@Nullable final ItemStack item) {
		return item == null ? ImmutableMap.of() : item.getEnchantments();
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
	public static boolean addEnchantment(@Nonnull final ItemStack item,
										 @Nonnull final Enchantment enchantment,
										 final int level) {
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
	public static boolean addEnchantment(@Nonnull final ItemStack item,
										 @Nonnull final Enchantment enchantment,
										 final int level,
										 final boolean onlyAllowSafeEnchantments) {
		return ItemUtils.handleItemMeta(Objects.requireNonNull(item), (ItemMeta meta) ->
				meta.addEnchant(enchantment, level, !onlyAllowSafeEnchantments));
	}

	/**
	 * Removes an enchantment from an item.
	 *
	 * @param item The item to remove the enchantment from.
	 * @param enchant The enchantment to remove from the item.
	 * @return Returns true if the enchantment was successfully removed.
	 */
	public static boolean removeEnchantment(@Nonnull final ItemStack item,
											@Nullable final Enchantment enchant) {
		return enchant == null
				|| ItemUtils.handleItemMeta(Objects.requireNonNull(item),
						(ItemMeta meta) -> meta.removeEnchant(enchant));
	}

	/**
	 * Removes all enchantments from an item.
	 *
	 * @param item The item to clear enchantment from.
	 */
	public static void clearEnchantments(@Nonnull final ItemStack item) {
		ItemUtils.handleItemMeta(Objects.requireNonNull(item), (ItemMeta meta) -> {
			meta.getEnchants().forEach((key, value) -> meta.removeEnchant(key));
			return true;
		});
	}

}
