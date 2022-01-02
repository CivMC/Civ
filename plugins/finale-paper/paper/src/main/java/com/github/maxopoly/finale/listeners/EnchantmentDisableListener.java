package com.github.maxopoly.finale.listeners;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;

public class EnchantmentDisableListener implements Listener {

	private final Set<Enchantment> disabledEnchants;

	public EnchantmentDisableListener(final Collection<Enchantment> disabledEnchants) {
		this.disabledEnchants = Set.copyOf(disabledEnchants);
	}

	private void removeEnchants(final ItemStack item, final Entity owner) {
		if (item == null || owner == null) {
			return;
		}
		final Map<Enchantment, Integer> enchants = item.getEnchantments();
		for (final Enchantment enchant : enchants.keySet()) {
			if (!this.disabledEnchants.contains(enchant)) {
				continue;
			}
			item.removeEnchantment(enchant);
			owner.sendMessage(ChatColor.RED + "The enchantment "
					+ EnchantUtils.getEnchantNiceName(enchant) + " is disabled and has "
					+ "been removed from your item!");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(final InventoryOpenEvent event) {
		final HumanEntity viewer = event.getPlayer();
		for (final ItemStack item : event.getInventory()) {
			removeEnchants(item, viewer);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryInteraction(final InventoryClickEvent event) {
		removeEnchants(event.getCurrentItem(), event.getWhoClicked());
	}

	@EventHandler(ignoreCancelled = true)
	public void onItemPickup(final EntityPickupItemEvent event) {
		removeEnchants(event.getItem().getItemStack(), event.getEntity());
	}

}
