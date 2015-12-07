package com.github.igotyou.FactoryMod.utility;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdvancedItemStack extends ItemStack {

	public AdvancedItemStack(final Material type, final int amount,
			final short damage, final String commonName) {
		super(type, amount, damage);
	}

	public AdvancedItemStack(final ItemStack itemStack, final String commonName) {
		super(itemStack);
	}

	public AdvancedItemStack clone() {
		try {
			AdvancedItemStack namedItemStack = (AdvancedItemStack) super
					.clone();
			return namedItemStack;
		} catch (Error e) {
			throw e;
		}
	}

	public String getName() {
		return getItemMeta().getDisplayName();
	}
	
	public void setName(String name) {
		ItemMeta im = getItemMeta();
		im.setDisplayName(name);
		setItemMeta(im);
	}

	public Map<Enchantment, Integer> getEnchants() {
		return getItemMeta().getEnchants();
	}
	
	public void addEnchant(Enchantment enchant, int level) {
		getItemMeta().addEnchant(enchant, level, true);
	}
	
	public void removeEnchant(Enchantment enchant) {
		getItemMeta().removeEnchant(enchant);
	}
}
