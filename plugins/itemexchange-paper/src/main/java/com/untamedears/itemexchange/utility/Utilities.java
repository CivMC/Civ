package com.untamedears.itemexchange.utility;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.castOrNull;
import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;
import static vg.civcraft.mc.civmodcore.util.NullCoalescing.equalsNotNull;

import co.aikar.commands.InvalidCommandArgument;
import com.google.common.base.Preconditions;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import java.util.Map;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Switch;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.LocationAPI;
import vg.civcraft.mc.civmodcore.util.Iteration;

/**
 * A series of Utilities of ItemExchange
 */
public final class Utilities {

	/**
	 * Tests whether a given item is an exchange rule or bulk exchange rule.
	 *
	 * @param item The item to test.
	 * @return Returns true if the item is an exchange rule or bulk exchange rule.
	 */
	public static boolean isExchangeRule(ItemStack item) {
		if (item == null) {
			return false;
		}
		if (ExchangeRule.fromItem(item) != null) {
			return true;
		}
		if (BulkExchangeRule.fromItem(item) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Attempts to give a player an exchange rule.
	 *
	 * @param player The player to give the exchange rule to.
	 * @param rule The exchange rule to give the player.
	 */
	public static void givePlayerExchangeRule(Player player, ExchangeRule rule) {
		RuntimeException error = new InvalidCommandArgument("Could not create that rule.");
		Inventory inventory = chain(player::getInventory);
		if (inventory == null || rule == null) {
			throw error;
		}
		if (!InventoryAPI.safelyAddItemsToInventory(inventory, new ItemStack[] { rule.toItem() })) {
			throw error;
		}
	}

	/**
	 * Gives items to an inventory or drops them at that inventory's location.
	 *
	 * @param inventory The inventory to give the items to. It must have a location, like a chest inventory.
	 * @param items The items to give to the inventory.
	 */
	public static void giveItemsOrDrop(Inventory inventory, ItemStack... items) {
		Preconditions.checkArgument(InventoryAPI.isValidInventory(inventory));
		Preconditions.checkArgument(LocationAPI.isValidLocation(inventory.getLocation()));
		Preconditions.checkArgument(!Iteration.isNullOrEmpty(items));
		for (Map.Entry<Integer, ItemStack> entry : inventory.addItem(items).entrySet()) {
			World world = inventory.getLocation().getWorld();
			assert world != null;
			world.dropItem(inventory.getLocation(), entry.getValue());
		}
	}

	/**
	 * Checks whether a series of enchantments matches [loosely] another series of enchantments.
	 *
	 * @param ruleEnchants The enchantments that MUST exist.
	 * @param metaEnchants The enchantments that exist.
	 * @param allowUnlistedEnchants Is metaEnchants allowed to include enchantments not included in ruleEnchants?
	 * @return Returns true if the meta enchantments satisfy the rule enchantments.
	 */
	public static boolean conformsRequiresEnchants(Map<Enchantment, Integer> ruleEnchants,
												   Map<Enchantment, Integer> metaEnchants,
												   boolean allowUnlistedEnchants) {
		if (Iteration.isNullOrEmpty(ruleEnchants)) {
			if (allowUnlistedEnchants || Iteration.isNullOrEmpty(metaEnchants)) {
				return true;
			}
			return false;
		}
		if (Iteration.isNullOrEmpty(metaEnchants)) {
			return false;
		}
		assert ruleEnchants != null && metaEnchants != null;
		if (allowUnlistedEnchants && metaEnchants.size() < ruleEnchants.size()) {
			return false;
		}
		else if (metaEnchants.size() != ruleEnchants.size()) {
			return false;
		}
		for (Map.Entry<Enchantment, Integer> entry : ruleEnchants.entrySet()) {
			if (!metaEnchants.containsKey(entry.getKey())) {
				return false;
			}
			if (entry.getValue() != ExchangeRule.ANY) {
				if (!equalsNotNull(metaEnchants.get(entry.getKey()), entry.getValue())) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Trigger the successful transaction buttons.
	 *
	 * @param shop The block representing the shop.
	 */
	public static void successfulTransactionButton(Block shop) {
		Stream.of(shop, BlockAPI.getOtherDoubleChestBlock(shop))
				.filter(BlockAPI::isValidBlock)
				.filter(block -> ItemExchangeConfig.hasSuccessButtonBlock(block.getType()))
				.distinct()
				.forEach(block -> {
					Directional directional = castOrNull(Directional.class, block.getBlockData());
					if (directional == null) {
						return;
					}
					BlockFace backFace = directional.getFacing().getOppositeFace();
					Block behindBlock = block.getRelative(backFace);
					if (!BlockAPI.isValidBlock(behindBlock) || !behindBlock.getType().isOccluding()) {
						return;
					}
					for (BlockFace face : BlockAPI.ALL_SIDES) {
						if (face.getOppositeFace() == backFace) {
							continue;
						}
						Block buttonBlock = behindBlock.getRelative(face);
						if (!BlockAPI.isValidBlock(buttonBlock) || !Tag.BUTTONS.isTagged(buttonBlock.getType())) {
							continue;
						}
						Switch button = castOrNull(Switch.class, buttonBlock.getBlockData());
						if (button == null) {
							continue;
						}
						if (BlockAPI.getAttachedFace(button) != face.getOppositeFace()) {
							continue;
						}
						button.setPowered(true);
						buttonBlock.setBlockData(button);
						// Wait to depower the block
						Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.getInstance(), () -> {
							Block newBlock = buttonBlock.getLocation().getBlock(); // Refresh block
							Switch newButton = castOrNull(Switch.class, newBlock.getBlockData());
							if (!button.matches(newButton)) {
								return;
							}
							newButton.setPowered(false);
							newBlock.setBlockData(newButton);
						}, 30L);
					}
				});
	}

}
