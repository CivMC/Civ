package com.untamedears.itemexchange.utility;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;
import static vg.civcraft.mc.civmodcore.util.NullCoalescing.equalsNotNull;

import co.aikar.commands.InvalidCommandArgument;
import com.google.common.base.Preconditions;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
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
	public static boolean conformsRequiresEnchants(@Nullable Map<Enchantment, Integer> ruleEnchants,
												   @Nullable Map<Enchantment, Integer> metaEnchants,
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
