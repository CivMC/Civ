package com.untamedears.itemexchange.utility;

import co.aikar.commands.InvalidCommandArgument;
import com.google.common.base.Preconditions;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
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
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.utilities.KeyedUtils;
import vg.civcraft.mc.civmodcore.utilities.NullUtils;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

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
	public static boolean isExchangeRule(final ItemStack item) {
		if (item == null) {
			return false;
		}
		return ExchangeRule.fromItem(item) != null
				|| BulkExchangeRule.fromItem(item) != null;
	}

	/**
	 * Attempts to give a player an exchange rule.
	 *
	 * @param player The player to give the exchange rule to.
	 * @param rule The exchange rule to give the player.
	 */
	public static void givePlayerExchangeRule(final Player player, final ExchangeRule rule) {
		RuntimeException error = new InvalidCommandArgument("Could not create that rule.");
		if (player == null || rule == null) {
			throw error;
		}
		if (!InventoryUtils.safelyAddItemsToInventory(player.getInventory(), new ItemStack[] { rule.toItem() })) {
			throw error;
		}
	}

	/**
	 * Gives items to an inventory or drops them at that inventory's location.
	 *
	 * @param inventory The inventory to give the items to. It must have a location, like a chest inventory.
	 * @param items The items to give to the inventory.
	 */
	public static void giveItemsOrDrop(@Nonnull final Inventory inventory, @Nonnull final ItemStack... items) {
		Preconditions.checkArgument(InventoryUtils.isValidInventory(inventory));
		Preconditions.checkArgument(WorldUtils.isValidLocation(inventory.getLocation()));
		Preconditions.checkArgument(ArrayUtils.isNotEmpty(items));
		World world = inventory.getLocation().getWorld();
		assert world != null;
		for (Map.Entry<Integer, ItemStack> entry : inventory.addItem(items).entrySet()) {
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
	public static boolean conformsRequiresEnchants(final Map<Enchantment, Integer> ruleEnchants,
												   final Map<Enchantment, Integer> metaEnchants,
												   final boolean allowUnlistedEnchants) {
		if (MapUtils.isEmpty(ruleEnchants)) {
			return allowUnlistedEnchants || MapUtils.isEmpty(metaEnchants);
		}
		if (MapUtils.isEmpty(metaEnchants) || metaEnchants.size() < ruleEnchants.size()) {
			return false;
		}
		if (!allowUnlistedEnchants && metaEnchants.size() != ruleEnchants.size()) {
			return false;
		}
		for (final var ruleEnchant : ruleEnchants.entrySet()) {
			if (!metaEnchants.containsKey(ruleEnchant.getKey())) {
				return false;
			}
			if (ruleEnchant.getValue() != ExchangeRule.ANY) {
				if (!NullUtils.equalsNotNull(metaEnchants.get(ruleEnchant.getKey()), ruleEnchant.getValue())) {
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
		Stream.of(shop, WorldUtils.getOtherDoubleChestBlock(shop, true))
				.filter(WorldUtils::isValidBlock)
				.filter((block) -> ItemExchangeConfig.hasSuccessButtonBlock(block.getType()))
				.distinct()
				.forEach((block) -> {
					if (!(block.getBlockData() instanceof Directional directional)) {
						return;
					}
					final BlockFace backFace = directional.getFacing().getOppositeFace();
					final Block behindBlock = block.getRelative(backFace);
					if (!WorldUtils.isValidBlock(behindBlock) || !behindBlock.getType().isOccluding()) {
						return;
					}
					for (BlockFace face : WorldUtils.ALL_SIDES) {
						if (face.getOppositeFace() == backFace) {
							continue;
						}
						final Block buttonBlock = behindBlock.getRelative(face);
						if (!WorldUtils.isValidBlock(buttonBlock) || !Tag.BUTTONS.isTagged(buttonBlock.getType())) {
							continue;
						}
						if (!(buttonBlock.getBlockData() instanceof Switch button)) {
							continue;
						}
						if (WorldUtils.getAttachedFace(button) != face.getOppositeFace()) {
							continue;
						}
						button.setPowered(true);
						buttonBlock.setBlockData(button);
						// Wait to de-power the block
						Bukkit.getScheduler().scheduleSyncDelayedTask(ItemExchangePlugin.getInstance(), () -> {
							final Block newBlock = buttonBlock.getLocation().getBlock(); // Refresh block
							if (!(newBlock.getBlockData() instanceof Switch newButton)
									|| !button.matches(newButton)) {
								return;
							}
							newButton.setPowered(false);
							newBlock.setBlockData(newButton);
						}, 30L);
					}
				});
	}

	// ------------------------------------------------------------
	// Stringifiers
	// ------------------------------------------------------------

	public static String leveledEnchantsToString(final Map<Enchantment, Integer> leveledEnchants) {
		if (MapUtils.isEmpty(leveledEnchants)) {
			return "[]";
		}
		return "[" +
				leveledEnchants.entrySet().stream()
						.map(entry -> KeyedUtils.getString(entry.getKey()) + ":" + entry.getValue())
						.collect(Collectors.joining(",")) +
				"]";
	}

	public static String enchantsToString(final Collection<Enchantment> enchants) {
		if (CollectionUtils.isEmpty(enchants)) {
			return "[]";
		}
		return "[" +
				enchants.stream()
						.map(entry -> KeyedUtils.getString(entry.getKey()))
						.collect(Collectors.joining(",")) +
				"]";
	}

	public static String potionDataToString(final PotionData data) {
		if (data == null) {
			return null;
		}
		return "PotionData{" +
				"type=" + data.getType().name() + "," +
				"extended=" + data.isExtended() + "," +
				"upgraded=" + data.isUpgraded() +
				"}";
	}

	public static String potionEffectsToString(final Collection<PotionEffect> effects) {
		if (CollectionUtils.isEmpty(effects)) {
			return "[]";
		}
		return "[" +
				effects.stream()
						.map(entry -> "PotionEffect{" + entry.serialize() + "}")
						.collect(Collectors.joining(",")) +
				"]";
	}

}
