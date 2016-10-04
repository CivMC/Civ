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
		lore.add(pearl.getPlayerId().toString());
		lore.add(new Integer(pearl.getPearlId()).toString());
		return lore;
	}

	@Override
	public UUID getPlayerIdFromItemStack(ItemStack is) {
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

	@Override
	public int getPearlIdFromItemStack(ItemStack is) {
		if (is == null) {
			return 0;
		}

		if (!is.getType().equals(Material.ENDER_PEARL)) {
			return 0;
		}

		ItemMeta im = is.getItemMeta();
		if (im == null) {
			return 0;
		}

		List<String> lore = im.getLore();
		if (lore == null) {
			return 0;
		}
		
		return new Integer(lore.get(3));
	}

	@Override
	public UUID getPlayerIdFromLegacyPearl(ItemStack is) {
		return null;
	}

	@Override
	public String getKillerNameFromLegacyPearl(ItemStack is) {
		return null;
	}

}
