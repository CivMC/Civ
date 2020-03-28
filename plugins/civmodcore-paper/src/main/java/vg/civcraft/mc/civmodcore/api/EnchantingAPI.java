package vg.civcraft.mc.civmodcore.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Preconditions;

/**
 * Class of static APIs for Enchantments.
 * */
public final class EnchantingAPI {

	private EnchantingAPI() { } // Make the class effectively static

	/**
	 * Determines whether an enchantment is considered safe.
	 *
	 * @param enchantment The enchantment to validate.
	 * @param level The enchantment level to validate.
	 * @return Returns true if the enchantment is not null, and the level is within the acceptable bounds.
	 *
	 * @see Enchantment#getStartLevel() The starting level. A valid level cannot be below this.
	 * @see Enchantment#getMaxLevel() The maximum level. A valid level cannot be above this.
	 * */
	public static boolean isSafeEnchantment(@Nullable Enchantment enchantment, int level) {
		return enchantment != null && level >= enchantment.getStartLevel() && level <= enchantment.getMaxLevel();
	}

	/**
	 * Gets the enchantments from an item.
	 *
	 * @param item The item to retrieve the enchantments from.
	 * @return Returns the item's enchantments, which are never null.
	 *
	 * @throws IllegalArgumentException If the given item stack is not null or if the item meta cannot be retrieved or
	 *                                  generated.
	 * */
	@Nonnull
	public static Map<Enchantment, Integer> getEnchantments(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot retrieve the item's enchantments; the item is null.");
		ItemMeta meta = ItemAPI.getItemMeta(item);
		if (!meta.hasEnchants()) {
			return new HashMap<>();
		}
		return meta.getEnchants();
	}

	/**
	 * Adds a safe enchantment to an item.
	 *
	 * @param item The item to add the enchantment to.
	 * @param enchantment The enchantment to add to the item.
	 * @param level The level of the enchantment to add to the item.
	 * @return Returns true if the enchantment was successfully added.
	 *
	 * @throws IllegalArgumentException If the given item stack is not null or if the item meta cannot be retrieved or
	 *                                  generated, or if the enchantment is null or unsafe.
	 *
	 * @see EnchantingAPI#isSafeEnchantment(Enchantment, int)
	 * */
	public static boolean addEnchantment(@Nonnull ItemStack item, @Nonnull Enchantment enchantment, int level) {
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
	 * @throws IllegalArgumentException If the given item stack is not null or if the item meta cannot be retrieved or
	 *                                  generated, or if the enchantment is null or unsafe if onlyAllowSafeEnchantments
	 *                                  is set to true.
	 *
	 * @see EnchantingAPI#isSafeEnchantment(Enchantment, int)
	 * */
	public static boolean addEnchantment(@Nonnull ItemStack item, @Nonnull Enchantment enchantment, int level,
		 	boolean onlyAllowSafeEnchantments) {
		Preconditions.checkNotNull(item, "Cannot add an enchantment; the item is null.");
		Preconditions.checkNotNull(enchantment, "Cannot add an enchantment; the enchantment is null.");
		ItemMeta meta = ItemAPI.getItemMeta(item);
		if (onlyAllowSafeEnchantments && !isSafeEnchantment(enchantment, level)) {
			throw new IllegalArgumentException(
					String.format("Could not add enchantment [%s] to item as it is unsafe.", enchantment));
		}
		if (!meta.addEnchant(enchantment, level, !onlyAllowSafeEnchantments)) {
			return false;
		}
		item.setItemMeta(meta);
		return true;
	}

	/**
	 * Removes an enchantment from an item.
	 *
	 * @param item The item to remove the enchantment from.
	 * @param enchantment The enchantment to remove from the item.
	 * @return Returns true if the enchantment was successfully removed.
	 *
	 * @throws IllegalArgumentException If the given item stack is not null or if the item meta cannot be retrieved or
	 *                                  generated, or if the enchantment is null.
	 * */
	public static boolean removeEnchantment(@Nonnull ItemStack item, @Nonnull Enchantment enchantment) {
		Preconditions.checkNotNull(item, "Cannot remove that enchantment; the item is null.");
		Preconditions.checkNotNull(enchantment, "Cannot remove that enchantment; the enchantment is null.");
		ItemMeta meta = ItemAPI.getItemMeta(item);
		if (!meta.removeEnchant(enchantment)) {
			return false;
		}
		item.setItemMeta(meta);
		return true;
	}

	/**
	 * Removes all enchantments from an item.
	 *
	 * @param item The item to clear enchantment from.
	 *
	 * @throws IllegalArgumentException If the given item stack is not null or if the item meta cannot be retrieved or
	 *                                  generated.
	 * */
	public static void clearEnchantments(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot clear that item of enchantments; the item is null.");
		for (Enchantment enchantment : getEnchantments(item).keySet()) {
			removeEnchantment(item, enchantment);
		}
	}

}
