package com.github.igotyou.FactoryMod.listeners;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.events.FactoryActivateEvent;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import io.papermc.paper.event.player.PlayerLoomPatternSelectEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LoomInventory;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Used to handle events related to items with compacted lore
 */
public class CompactItemListener implements Listener {

	/**
	 * Prevents players from placing compacted blocks
	 */
	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent e) {
		if (isCompacted(e.getItemInHand())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "You can not place compacted blocks");
		}

	}

	/**
	 * Prevents players from crafting with compacted materials
	 */
	@EventHandler
	public void craftingEvent(CraftItemEvent e) {
		CraftingInventory ci = e.getInventory();
		for (ItemStack is : ci.getMatrix()) {
			if (isCompacted(is)) {
				e.setCancelled(true);
				HumanEntity h = e.getWhoClicked();
				if (h instanceof Player) {
					h.sendMessage(ChatColor.RED + "You can not craft with compacted items");
				}
				break;
			}
		}
	}

	/**
	 * Prevents players from enchanting compacted items
	 */
	@EventHandler
	public void enchantEvent(EnchantItemEvent e) {
		ItemStack ei = e.getItem();
		if (isCompacted(ei)) {
			e.setCancelled(true);
			e.getEnchanter().sendMessage(ChatColor.RED + "You can not enchant compacted items");
		}
	}

	/**
	 * Prevents players from eating compacted items
	 */
	@EventHandler
	public void itemConsumeEvent(PlayerItemConsumeEvent e) {
		if (isCompacted(e.getItem())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "You can not eat compacted food");
		}
	}

	/**
	 * Prevents players from wordbanking compacted items
	 */
	@EventHandler
	public void wordbankEvent(FactoryActivateEvent e) {
		FurnCraftChestFactory fac = ((FurnCraftChestFactory) e.getFactory());

		if (!fac.getCurrentRecipe().getTypeIdentifier().equals("WORDBANK")) return;

		if (isCompacted(fac.getInputInventory().getItem(0))) {
			e.setCancelled(true);
			e.getActivator().sendMessage(ChatColor.RED + "You can not wordbank compacted items");
		}
	}

	/**
	 * Prevents players from using compacted items in anvils
	 */
	@EventHandler
	public void anvilEvent(InventoryClickEvent e) {
		if (e.getCurrentItem() == null) return;

		if (e.getCurrentItem().getType() == Material.AIR) return;

		if (e.getInventory().getType() == InventoryType.ANVIL) {
			if (e.getSlotType() == InventoryType.SlotType.RESULT) {
				if (isCompacted(e.getInventory().getItem(0)) || isCompacted(e.getInventory().getItem(1))) {
					e.setCancelled(true);
					e.getWhoClicked().sendMessage(ChatColor.RED + "You can not use compacted items in an anvil");
				}
			}
		}
	}

	/**
	 * Prevents players from using compacted items in smithing tables
	 */
	@EventHandler
	public void smithEvent(InventoryClickEvent e) {
		if (e.getCurrentItem() == null) return;

		if (e.getCurrentItem().getType() == Material.AIR) return;

		if (e.getInventory().getType() == InventoryType.SMITHING) {
			if (e.getSlotType() == InventoryType.SlotType.RESULT) {
				if (isCompacted(e.getInventory().getItem(0)) || isCompacted(e.getInventory().getItem(1))) {
					e.setCancelled(true);
					e.getWhoClicked().sendMessage(ChatColor.RED + "You can not use compacted items in a smithing table");
				}
			}
		}
	}

	/**
	 * Prevents players from using compacted items in looms
	 */
	@EventHandler
	public void loomEvent(PlayerLoomPatternSelectEvent e) {
		LoomInventory li = e.getLoomInventory();

		for (int i = 0; i < 2; i++) {
			if (li.getItem(i) == null) continue;
			if (li.getItem(i).getType() == Material.AIR) continue;
			if (isCompacted(li.getItem(i))) {
				e.setCancelled(true);
				e.getPlayer().sendMessage(ChatColor.RED + "You can not use compacted items in a loom");
			}
		}
	}

	/**
	 * Prevents players from copying compacted maps in cartography tables
	 */
	@EventHandler
	public void cartographyCopyEvent(InventoryClickEvent e) {
		if (e.getCurrentItem() == null) return;

		if (e.getCurrentItem().getType() == Material.AIR) return;

		if (e.getInventory().getType() == InventoryType.CARTOGRAPHY) {
			if (e.getSlotType() == InventoryType.SlotType.RESULT) {
				if (isCompacted(e.getInventory().getItem(0)) || isCompacted(e.getInventory().getItem(1))) {
					e.setCancelled(true);
					e.getWhoClicked().sendMessage(ChatColor.RED + "You can not copy compacted maps");
				}
			}
		}
	}

	private boolean isCompacted(ItemStack is) {
		if (is == null) {
			return false;
		}
		if (!is.hasItemMeta()) {
			return false;
		}
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore()) {
			return false;
		}
		for (String lore : im.getLore()) {
			if (FactoryMod.getInstance().getManager().isCompactLore(lore)) {
				return true;
			}
		}
		return false;
	}

}
