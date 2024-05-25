package vg.civcraft.mc.civmodcore.inventory.items.compaction;

import io.papermc.paper.event.player.PlayerLoomPatternSelectEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LoomInventory;
import org.bukkit.inventory.SmithingInventory;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public final class CompactedItemUsageListener implements Listener {
	/**
	 * Prevents players from placing compacted blocks
	 */
	@EventHandler
	private void preventCompactedBlockPlacements(
		final @NotNull BlockPlaceEvent event
	) {
		if (Compaction.isCompacted(event.getItemInHand())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Component.text(
				"You cannot place compacted blocks!",
				NamedTextColor.RED
			));
		}
	}

	/**
	 * Prevents players from crafting with compacted materials
	 */
	@EventHandler
	private void preventCraftingWithCompactedItems(
		final @NotNull CraftItemEvent event
	) {
		for (final ItemStack slot : event.getInventory().getMatrix()) {
			if (Compaction.isCompacted(slot)) {
				event.setCancelled(true);
				if (event.getWhoClicked() instanceof final Player clicker) {
					clicker.sendMessage(Component.text(
						"You cannot craft with compacted items!",
						NamedTextColor.RED
					));
				}
				break;
			}
		}
	}

	/**
	 * Prevents players from enchanting compacted items
	 */
	@EventHandler
	private void preventEnchantmentIfCompacted(
		final @NotNull EnchantItemEvent event
	) {
		if (Compaction.isCompacted(event.getItem())) {
			event.setCancelled(true);
			event.getEnchanter().sendMessage(Component.text(
				"You cannot enchant compacted items!",
				NamedTextColor.RED
			));
		}
	}

	/**
	 * Prevents players from eating compacted items
	 */
	@EventHandler
	private void preventItemConsumptionIfCompacted(
		final @NotNull PlayerItemConsumeEvent event
	) {
		if (Compaction.isCompacted(event.getItem())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Component.text(
				"You cannot eat compacted food!",
				NamedTextColor.RED
			));
		}
	}

	/**
	 * Prevents players from using compacted items in anvils
	 */
	@EventHandler
	private void disableCompactedItemsInAnvils(
		final @NotNull InventoryClickEvent event
	) {
		final ItemStack item = event.getCurrentItem();
		if (ItemUtils.isEmptyItem(item)) {
			return;
		}
		if (!(event.getInventory() instanceof AnvilInventory anvil)) {
			return;
		}
		if (event.getSlotType() != InventoryType.SlotType.RESULT) {
			return;
		}
		if (Compaction.isCompacted(anvil.getFirstItem()) || Compaction.isCompacted(anvil.getSecondItem())) {
			event.setCancelled(true);
			event.getWhoClicked().sendMessage(Component.text(
				"You cannot use compacted items in an anvil!",
				NamedTextColor.RED
			));
		}
	}

	/**
	 * Prevents players from using compacted items in smithing tables
	 */
	@EventHandler
	private void preventSmithingIfCompacted(
		final @NotNull InventoryClickEvent event
	) {
		final ItemStack item = event.getCurrentItem();
		if (ItemUtils.isEmptyItem(item)) {
			return;
		}
		if (!(event.getInventory() instanceof final SmithingInventory smith)) {
			return;
		}
		if (event.getSlotType() != InventoryType.SlotType.RESULT) {
			return;
		}
		if (Compaction.isCompacted(smith.getInputEquipment()) || Compaction.isCompacted(smith.getInputMineral())) {
			event.setCancelled(true);
			event.getWhoClicked().sendMessage(Component.text(
				"You cannot use compacted items in a smithing table!",
				NamedTextColor.RED
			));
		}
	}

	/**
	 * Prevents players from copying compacted maps in cartography tables
	 */
	@EventHandler
	private void preventCartographyIfCompacted(
		final @NotNull InventoryClickEvent event
	) {
		final ItemStack item = event.getCurrentItem();
		if (ItemUtils.isEmptyItem(item)) {
			return;
		}
		if (!(event.getInventory() instanceof final CartographyInventory cartography)) {
			return;
		}
		if (event.getSlotType() != InventoryType.SlotType.RESULT) {
			return;
		}
		if (Compaction.isCompacted(cartography.getItem(0)) || Compaction.isCompacted(cartography.getItem(1))) {
			event.setCancelled(true);
			event.getWhoClicked().sendMessage(Component.text(
				"You cannot copy compacted maps!",
				NamedTextColor.RED
			));
		}
	}

	/**
	 * Prevents players from using compacted items in looms
	 */
	@EventHandler
	private void disableCompactedItemsInLooms(
		final @NotNull PlayerLoomPatternSelectEvent event
	) {
		final LoomInventory loom = event.getLoomInventory();
		if (Compaction.isCompacted(loom.getItem(0)) || Compaction.isCompacted(loom.getItem(1))) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Component.text(
				"You cannot use compacted items in a loom!",
				NamedTextColor.RED
			));
		}
	}
}
