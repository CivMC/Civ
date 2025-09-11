package vg.civcraft.mc.civmodcore.inventory.items;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;
import vg.civcraft.mc.civmodcore.utilities.KeyedUtils;

/**
 * Class of static utilities for Enchantments.
 */
public final class EnchantUtils {
    private static final Logger LOGGER = CivLogger.getLogger(EnchantUtils.class);
    private static final BiMap<Enchantment, String> ENCHANT_ABBR = HashBiMap.create();

    /**
     * Loads enchantment names and initials from the config.
     */
    public static void loadEnchantAbbreviations() {
        ENCHANT_ABBR.clear();

        // Beta 1.9
        ENCHANT_ABBR.put(Enchantment.SHARPNESS, "S");
        ENCHANT_ABBR.put(Enchantment.BANE_OF_ARTHROPODS, "BoA");
        ENCHANT_ABBR.put(Enchantment.SMITE, "Sm");
        ENCHANT_ABBR.put(Enchantment.EFFICIENCY, "E");
        ENCHANT_ABBR.put(Enchantment.UNBREAKING, "U");
        ENCHANT_ABBR.put(Enchantment.FIRE_ASPECT, "FA");
        ENCHANT_ABBR.put(Enchantment.KNOCKBACK, "K");
        ENCHANT_ABBR.put(Enchantment.FORTUNE, "F");
        ENCHANT_ABBR.put(Enchantment.LOOTING, "L");
        ENCHANT_ABBR.put(Enchantment.RESPIRATION, "R");
        ENCHANT_ABBR.put(Enchantment.PROTECTION, "P");
        ENCHANT_ABBR.put(Enchantment.BLAST_PROTECTION, "BP");
        ENCHANT_ABBR.put(Enchantment.FEATHER_FALLING, "FF");
        ENCHANT_ABBR.put(Enchantment.FIRE_PROTECTION, "FP");
        ENCHANT_ABBR.put(Enchantment.PROJECTILE_PROTECTION, "PP");
        ENCHANT_ABBR.put(Enchantment.SILK_TOUCH, "ST");
        ENCHANT_ABBR.put(Enchantment.AQUA_AFFINITY, "AA");
        // 1.1
        ENCHANT_ABBR.put(Enchantment.POWER, "Po");
        ENCHANT_ABBR.put(Enchantment.FLAME, "Fl");
        ENCHANT_ABBR.put(Enchantment.INFINITY, "I");
        ENCHANT_ABBR.put(Enchantment.PUNCH, "Pu");
        // 1.4.6
        ENCHANT_ABBR.put(Enchantment.THORNS, "T");
        // 1.7.2
        ENCHANT_ABBR.put(Enchantment.LUCK_OF_THE_SEA, "LS");
        ENCHANT_ABBR.put(Enchantment.LURE, "Lu");
        // 1.8
        ENCHANT_ABBR.put(Enchantment.DEPTH_STRIDER, "DS");
        // 1.9
        ENCHANT_ABBR.put(Enchantment.FROST_WALKER, "FW");
        ENCHANT_ABBR.put(Enchantment.MENDING, "M");
        // 1.11
        ENCHANT_ABBR.put(Enchantment.BINDING_CURSE, "CoB");
        ENCHANT_ABBR.put(Enchantment.VANISHING_CURSE, "CoV");
        // 1.11.1
        ENCHANT_ABBR.put(Enchantment.SWEEPING_EDGE, "SE");
        // 1.13
        ENCHANT_ABBR.put(Enchantment.CHANNELING, "Ch");
        ENCHANT_ABBR.put(Enchantment.IMPALING, "Im");
        ENCHANT_ABBR.put(Enchantment.LOYALTY, "Lo");
        ENCHANT_ABBR.put(Enchantment.RIPTIDE, "RP");
        // 1.14
        ENCHANT_ABBR.put(Enchantment.MULTISHOT, "MS");
        ENCHANT_ABBR.put(Enchantment.PIERCING, "Pr");
        ENCHANT_ABBR.put(Enchantment.QUICK_CHARGE, "QC");
        // 1.16
        ENCHANT_ABBR.put(Enchantment.SOUL_SPEED, "SS");
        // 1.19
        ENCHANT_ABBR.put(Enchantment.SWIFT_SNEAK, "SwS");
        // 1.21
        ENCHANT_ABBR.put(Enchantment.BREACH, "Br");
        ENCHANT_ABBR.put(Enchantment.DENSITY, "D");
        ENCHANT_ABBR.put(Enchantment.WIND_BURST, "WB");

        // Determine if there's any enchants missing abbreviations
        final Set<Enchantment> missing = Registry.ENCHANTMENT.stream()
            .filter(Predicate.not(ENCHANT_ABBR::containsKey))
            .collect(Collectors.toUnmodifiableSet());
        if (!missing.isEmpty()) {
            //noinspection deprecation
            LOGGER.warning("The following enchants are missing abbreviations: %s.".formatted(
                missing.stream().map(Enchantment::getKey).map(NamespacedKey::asString).collect(Collectors.joining(","))
            ));
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
     * @param enchant The enchantment to get the name of.
     * @return Returns the name of the enchantment, or null.
     * @deprecated Use {@link Component#translatable(Translatable)} instead.
     */
    @Nullable
    @Deprecated
    public static String getEnchantNiceName(@Nullable final Enchantment enchant) {
        return enchant == null ? null : ChatUtils.stringify(Component.translatable(enchant));
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
     * @param level       The enchantment level to validate.
     * @return Returns true if the enchantment is not null, and the level is within the acceptable bounds.
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
    @NotNull
    public static Map<Enchantment, Integer> getEnchantments(@Nullable final ItemStack item) {
        return item == null ? ImmutableMap.of() : item.getEnchantments();
    }

    /**
     * Adds a safe enchantment to an item.
     *
     * @param item        The item to add the enchantment to.
     * @param enchantment The enchantment to add to the item.
     * @param level       The level of the enchantment to add to the item.
     * @return Returns true if the enchantment was successfully added.
     * @see EnchantUtils#isSafeEnchantment(Enchantment, int)
     */
    public static boolean addEnchantment(@NotNull final ItemStack item,
                                         @NotNull final Enchantment enchantment,
                                         final int level) {
        return addEnchantment(item, enchantment, level, true);
    }

    /**
     * Adds an enchantment to an item.
     *
     * @param item                      The item to add the enchantment to.
     * @param enchantment               The enchantment to add to the item.
     * @param level                     The level of the enchantment to add to the item.
     * @param onlyAllowSafeEnchantments Requires enchantments to be safe if set to true.
     * @return Returns true if the enchantment was successfully added.
     * @see EnchantUtils#isSafeEnchantment(Enchantment, int)
     */
    public static boolean addEnchantment(@NotNull final ItemStack item,
                                         @NotNull final Enchantment enchantment,
                                         final int level,
                                         final boolean onlyAllowSafeEnchantments) {
        return ItemUtils.handleItemMeta(Objects.requireNonNull(item), (ItemMeta meta) ->
            meta.addEnchant(enchantment, level, !onlyAllowSafeEnchantments));
    }

    /**
     * Removes an enchantment from an item.
     *
     * @param item    The item to remove the enchantment from.
     * @param enchant The enchantment to remove from the item.
     * @return Returns true if the enchantment was successfully removed.
     */
    public static boolean removeEnchantment(@NotNull final ItemStack item,
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
    public static void clearEnchantments(@NotNull final ItemStack item) {
        ItemUtils.handleItemMeta(Objects.requireNonNull(item), (ItemMeta meta) -> {
            meta.getEnchants().forEach((key, value) -> meta.removeEnchant(key));
            return true;
        });
    }

}
