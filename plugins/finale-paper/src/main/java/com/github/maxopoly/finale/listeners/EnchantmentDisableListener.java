package com.github.maxopoly.finale.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentDisableListener implements Listener {
	
	private Set<Enchantment> disabledEnchants;
	
	public EnchantmentDisableListener(Collection<Enchantment> disabledEnchants) {
		this.disabledEnchants = new HashSet<Enchantment>(disabledEnchants);
	}
	
	public boolean isDisabledEnchantment(Enchantment enchant) {
		return disabledEnchants.contains(enchant);
	}
	@EventHandler
	public void itemClick(InventoryClickEvent e) {
		ItemStack is = e.getCurrentItem();
		if (is == null || is.getEnchantments().size() == 0) {
			return;
		}
		ItemMeta im = is.getItemMeta();
		for(Enchantment ench : im.getEnchants().keySet()) {
			if (isDisabledEnchantment(ench)) {
				is.removeEnchantment(ench);
			}
		}
	}

	@EventHandler
	public void pickUp(PlayerPickupItemEvent e) {
		if (e.getItem() == null) {
			return;
		}
		ItemStack is = e.getItem().getItemStack();
		if (is == null || is.getEnchantments().size() == 0) {
			return;
		}
		ItemMeta im = is.getItemMeta();
		for(Enchantment ench : im.getEnchants().keySet()) {
			if (isDisabledEnchantment(ench)) {
				is.removeEnchantment(ench);
			}
		}
	}

}
