package com.devotedmc.ExilePearl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlLoreGenerator;

public class MockLoreGenerator implements PearlLoreGenerator {

	@Override
	public List<String> generateLore(ExilePearl pearl) {
		List<String> lore = new ArrayList<String>();
		lore.add(pearl.getItemName());
		lore.add(pearl.getPlayerName());
		lore.add(pearl.getUniqueId().toString());
		return lore;
	}

	@Override
	public UUID getIDFromItemStack(ItemStack is) {
		if (is == null) {
			return null;
		}

		if (!is.getType().equals(Material.ENDER_PEARL)) {
			return null;
		}

		ItemMeta im = is.getItemMeta();
		if (im == null) {
			return null;
		}

		List<String> lore = im.getLore();
		if (lore == null) {
			return null;
		}

		return UUID.fromString(lore.get(2));
	}

}
