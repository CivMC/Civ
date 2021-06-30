package com.devotedmc.ExilePearl;

import java.util.List;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public interface LoreProvider {

	/**
	 * Generates the lore for the pearl
	 * @param pearl The pearl instance
	 * @return The pearl lore
	 */
	List<String> generateLore(ExilePearl pearl);

	/**
	 * Generates the lore for a pearl with a modified health value
	 * @return The pearl lore
	 */
	List<String> generateLoreWithModifiedHealth(ExilePearl pearl, int healthValue);

	/**
	 * Generates the lore for a pearl with a modified type
	 * @return The pearl lore
	 */
	List<String> generateLoreWithModifiedType(ExilePearl pearl, PearlType type);

	/**
	 * Gets the pearl ID from a pearl item stack
	 * @param is The item stack to parse
	 * @return The pearl ID, or 0 if can't parse
	 */
	int getPearlIdFromItemStack(ItemStack is);

	/**
	 * Gets a player ID from a legacy prison pearl
	 * @return The player ID if it exists
	 */
	UUID getPlayerIdFromLegacyPearl(ItemStack is);

	/**
	 * Generates the info for a pearl
	 * @param pearl The pearl instance
	 * @return The returned lore
	 */
	List<String> generatePearlInfo(ExilePearl pearl);

	/**
	 * Retrieve the exile pearl ID key used for storing the pearl ID on a PersistentDataContainer.
	 */
	NamespacedKey getExilePearlIdKey();
}
