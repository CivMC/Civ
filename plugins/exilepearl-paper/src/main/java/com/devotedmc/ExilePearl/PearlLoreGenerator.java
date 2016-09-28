package com.devotedmc.ExilePearl;

import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public interface PearlLoreGenerator {

	/**
	 * Generates the lore for the pearl
	 * @return The pearl lore
	 */
	List<String> generateLore(ExilePearl pearl);
	
	/**
	 * Parses a UUID from a pearl item stack
	 * @param is The item stack to parse
	 * @return The player UUID, or null if it can't parse
	 */
	UUID getIDFromItemStack(ItemStack is);
}
