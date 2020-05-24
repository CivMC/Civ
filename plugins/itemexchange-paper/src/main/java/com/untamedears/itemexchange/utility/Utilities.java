package com.untamedears.itemexchange.utility;

import co.aikar.commands.InvalidCommandArgument;
import com.google.common.base.Preconditions;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.LocationAPI;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

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
		Inventory inventory = NullCoalescing.chain(player::getInventory);
		if (inventory == null || rule == null) {
			throw error;
		}
		if (!InventoryAPI.safelyAddItemsToInventory(inventory, new ItemStack[] { rule.toItem() })) {
			throw error;
		}
	}

	/**
	 * Ensure a player is holding an exchange rule.
	 *
	 * @param player The player to ensure is holding an exchange rule.
	 * @return Returns the exchange rule the player is holding.
	 */
	public static ExchangeRule ensureHoldingExchangeRule(Player player) {
		RuntimeException error = new InvalidCommandArgument("You must be holding an exchange rule.");
		ItemStack held = NullCoalescing.chain(() -> player.getInventory().getItemInMainHand());
		if (!ItemAPI.isValidItem(held)) {
			throw error;
		}
		ExchangeRule rule = ExchangeRule.fromItem(held);
		if (rule == null) {
			rule = ExchangeRule.fromItem(held);
		}
		if (rule == null) {
			throw error;
		}
		return rule;
	}

	/**
	 * Replaces an exchange rule that the player is holding.
	 *
	 * @param player The player give the exchange rule to.
	 * @param rule The rule to give the player.
	 */
	public static void replaceHoldingExchangeRule(Player player, ExchangeRule rule) {
		RuntimeException error = new InvalidCommandArgument("Could not replace that rule.");
		if (player == null || rule == null) {
			throw error;
		}
		ItemStack item = rule.toItem();
		if (item == null) {
			throw error;
		}
		player.getInventory().setItemInMainHand(item);
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
		if (ruleEnchants == null) {
			ruleEnchants = Collections.emptyMap();
		}
		if (metaEnchants == null) {
			metaEnchants = Collections.emptyMap();
		}
		boolean ruleHasEnchants = !ruleEnchants.isEmpty();
		boolean metaHasEnchants = !metaEnchants.isEmpty();
		// If neither the rule nor the item has enchants, then there's nothing to match, therefore they match
		if (!ruleHasEnchants && !metaHasEnchants) {
			return true;
		}
		// If the rule doesn't list enchants but the item does, match if allowUnlistedEnchants is true
		// (ignore your IDE moaning about redundant conditions, better to keep it for readability)
		if (!ruleHasEnchants && metaHasEnchants && allowUnlistedEnchants) {
			return true;
		}
		// If the rule lists enchants but the item doesn't, the item just doesn't match at all
		if (ruleHasEnchants && !metaHasEnchants) {
			return false;
		}
		// Check sizes as a preliminary condition.
		// - If allowUnlistedEnchants is true, fail only if the item's enchant list is smaller than the rule's
		if (allowUnlistedEnchants) {
			if (metaEnchants.size() < ruleEnchants.size()) {
				return false;
			}
		}
		// - Otherwise the enchant lists must match in size, otherwise you can infer that the enchant lists don't match
		else {
			if (metaEnchants.size() != ruleEnchants.size()) {
				return false;
			}
		}
		for (Map.Entry<Enchantment, Integer> entry : ruleEnchants.entrySet()) {
			if (!metaEnchants.containsKey(entry.getKey())) {
				return false;
			}
			if (entry.getValue() != ExchangeRule.ANY) {
				if (!Objects.equals(metaEnchants.get(entry.getKey()), entry.getValue())) {
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
	// TODO: You can't use block data anymore, at least not in the same way, so we need to simulate a button press in a
	//     different way. This might even be worthy of its own CivModCore API... need to discuss.
	public static void successfulTransactionButton(Block shop) {
		//		BlockFace backFace = NullCoalescing.chain(() ->
		//				((Directional) shop.getBlockData()).getFacing().getOppositeFace());
		//		if (backFace == null) {
		//			return;
		//		}
		//		Block behindShop = shop.getRelative(backFace);
		//		if (!BlockAPI.isValidBlock(behindShop) || !behindShop.getType().isOccluding()) {
		//			return;
		//		}
		//        for (BlockFace face : BlockAPI.ALL_SIDES) {
		//            if (face == backFace.getOppositeFace()) {
		//                continue;
		//            }
		//            Block buttonBlock = behindShop.getRelative(face);
		//            if (!BlockAPI.isValidBlock(buttonBlock)) {
		//                continue;
		//            }
		//			Directional directional = NullCoalescing.chain(() -> (Directional) buttonBlock.getBlockData());
		//            if (directional == null) {
		//            	continue;
		//			}
		//            if (directional.getFacing() != face.getOppositeFace()) {
		//            	continue;
		//			}
		//			Powerable powerable = NullCoalescing.chain(() -> (Powerable) buttonBlock.getBlockData());
		//            if (powerable == null) {
		//            	continue;
		//			}
		//            powerable.setPowered(true);
		//            buttonBlock.setBlockData(powerable);
		//            Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.getInstance(), () -> {
		//            	powerable.setPowered(false);
		//				buttonBlock.setBlockData(powerable);
		//            }, 30L);
		//        }
	}

}
