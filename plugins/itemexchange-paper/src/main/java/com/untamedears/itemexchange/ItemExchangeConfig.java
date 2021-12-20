package com.untamedears.itemexchange;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.inventory.RecipeManager;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

public final class ItemExchangeConfig extends ConfigParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemExchangeConfig.class.getSimpleName());

	private static final Set<Material> SHOP_COMPATIBLE_BLOCKS = new HashSet<>();
	private static final Set<Material> SUCCESS_BUTTON_BLOCKS = new HashSet<>();
	private static final ItemStack RULE_ITEM = new ItemStack(Material.STONE_BUTTON);
	private static boolean CREATE_FROM_SHOP = true;
	private static final Set<Material> ITEMS_CAN_REPAIR = new HashSet<>();
	private static final Set<Material> RELAY_COMPATIBLE_BLOCKS = new HashSet<>();
	private static int RELAY_RECURSION_LIMIT;
	private static int RELAY_REACH_DISTANCE;
	private static final Set<Material> RELAY_PERMEABLE_BLOCKS = new HashSet<>();
	private static ShapelessRecipe BULK_RULE_RECIPE;

	public ItemExchangeConfig(final ItemExchangePlugin plugin) {
		super(plugin);
	}

	@Override
	protected boolean parseInternal(final ConfigurationSection config) {
		parseShopCompatibleBlocks(config.getStringList("supportedBlocks"));
		parseSuccessButtonBlocks(config.getStringList("successButtonBlocks"));
		parseRuleItem(config.getString("ruleItem"));
		parseCreateFromShop(config.getBoolean("createShopFromChest", true));
		parseRepairableItems(config.getStringList("repairables"));
		parseShopRelay(config.getConfigurationSection("shopRelay"));
		return true;
	}

	public void reset() {
		SHOP_COMPATIBLE_BLOCKS.clear();
		SUCCESS_BUTTON_BLOCKS.clear();
		RULE_ITEM.setType(Material.STONE_BUTTON);
		CREATE_FROM_SHOP = true;
		ITEMS_CAN_REPAIR.clear();
		RELAY_COMPATIBLE_BLOCKS.clear();
		RELAY_RECURSION_LIMIT = 0;
		RELAY_REACH_DISTANCE = 0;
		RELAY_PERMEABLE_BLOCKS.clear();
		if (BULK_RULE_RECIPE != null) {
			RecipeManager.removeRecipe(BULK_RULE_RECIPE);
			BULK_RULE_RECIPE = null;
		}
	}

	private void parseShopCompatibleBlocks(final List<String> config) {
		for (final String raw : config) {
			final Material material = MaterialUtils.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse material for supported block: " + raw);
				continue;
			}
			if (!material.isBlock()) {
				LOGGER.warn("Supported block material not a block: " + raw);
				continue;
			}
			if (SHOP_COMPATIBLE_BLOCKS.contains(material)) {
				LOGGER.warn("Supported block material duplicate: " + raw);
				continue;
			}
			LOGGER.info("Supported block material parsed: " + material.name());
			SHOP_COMPATIBLE_BLOCKS.add(material);
		}
		if (SHOP_COMPATIBLE_BLOCKS.isEmpty()) {
			LOGGER.warn("There are no supported blocks, try:");
			LOGGER.warn("\tsupportedBlocks: [CHEST, TRAPPED_CHEST]");
		}
	}

	private void parseSuccessButtonBlocks(final List<String> config) {
		for (final String raw : config) {
			final Material material = MaterialUtils.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse material for success button block: " + raw);
				continue;
			}
			if (!material.isBlock()) {
				LOGGER.warn("Supported success button material not a block: " + raw);
				continue;
			}
			if (SUCCESS_BUTTON_BLOCKS.contains(material)) {
				LOGGER.warn("Supported success button material duplicate: " + raw);
				continue;
			}
			LOGGER.info("Supported success button material parsed: " + material.name());
			SUCCESS_BUTTON_BLOCKS.add(material);
		}
		if (SUCCESS_BUTTON_BLOCKS.isEmpty()) {
			LOGGER.info("There are no supported button triggering shop blocks.");
		}
	}

	private void parseRuleItem(final String config) {
		final Material material = MaterialUtils.getMaterial(config);
		final String defaultWarning = "\tDefaulting to STONE_BUTTON.";
		if (material == null) {
			LOGGER.warn("Could not parse material for rule item.");
			LOGGER.warn(defaultWarning);
			return;
		}
		if (!material.isItem()) {
			LOGGER.warn("Rule item material not a valid item.");
			LOGGER.warn(defaultWarning);
			return;
		}
		LOGGER.info("Rule item material parsed: " + material.name());
		RULE_ITEM.setType(material);
		BULK_RULE_RECIPE = new ShapelessRecipe(NamespacedKey.minecraft("bulk_exchange_rule"), RULE_ITEM);
		BULK_RULE_RECIPE.addIngredient(2, material);
		RecipeManager.registerRecipe(BULK_RULE_RECIPE);
	}

	private void parseCreateFromShop(final boolean config) {
		CREATE_FROM_SHOP = config;
		LOGGER.info("Create Shop From Shop Block: " + (config ? "ENABLED" : "DISABLED"));
	}

	private void parseRepairableItems(final List<String> config) {
		for (final String raw : config) {
			final Material material = MaterialUtils.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse repairable material: " + raw);
				continue;
			}
			if (!material.isItem()) {
				LOGGER.warn("Repairable material is not an item: " + raw);
				continue;
			}
			if (ITEMS_CAN_REPAIR.contains(material)) {
				LOGGER.warn("Repairable material duplicate: " + raw);
				continue;
			}
			LOGGER.info("Repairable material parsed: " + material.name());
			ITEMS_CAN_REPAIR.add(material);
		}
	}

	private void parseShopRelay(final ConfigurationSection config) {
		if (config == null) {
			LOGGER.info("Skipping relay parsing: section is missing.");
			return;
		}
		for (final String raw : config.getStringList("relayBlocks")) {
			final Material material = MaterialUtils.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse relay block material: " + raw);
				continue;
			}
			if (!material.isBlock()) {
				LOGGER.warn("Relay block material is not a block: " + raw);
				continue;
			}
			if (RELAY_COMPATIBLE_BLOCKS.contains(material)) {
				LOGGER.warn("Relay block material duplicate: " + raw);
				continue;
			}
			if (SHOP_COMPATIBLE_BLOCKS.contains(material)) {
				LOGGER.warn("Relay/shop block material collision: " + raw);
				continue;
			}
			LOGGER.info("Relay block material parsed: " + material.name());
			RELAY_COMPATIBLE_BLOCKS.add(material);
		}
		if (RELAY_COMPATIBLE_BLOCKS.isEmpty()) {
			LOGGER.info("No relay blocks have been parsed; relays will be effectively disabled.");
		}
		RELAY_RECURSION_LIMIT = Math.max(config.getInt("recursionLimit"), 0);
		if (RELAY_RECURSION_LIMIT > 0) {
			LOGGER.info("Relay recursion limit parsed: " + RELAY_RECURSION_LIMIT);
		}
		else {
			LOGGER.info("Relay recursion disabled.");
		}
		RELAY_REACH_DISTANCE = Math.max(config.getInt("reachDistance"), 0);
		LOGGER.info("Relay reach distance parsed: " + RELAY_REACH_DISTANCE);
		for (final String raw : config.getStringList("permeable")) {
			final Material material = MaterialUtils.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse relay permeable material: " + raw);
				continue;
			}
			if (!material.isBlock()) {
				LOGGER.warn("Relay permeable material is not a block: " + raw);
				continue;
			}
			if (RELAY_PERMEABLE_BLOCKS.contains(material)) {
				LOGGER.warn("Relay permeable material duplicate: " + raw);
				continue;
			}
			if (canBeInteractedWith(material)) {
				LOGGER.warn("Permeable material collision: " + raw);
				continue;
			}
			LOGGER.info("Relay permeable material parsed: " + material.name());
			RELAY_PERMEABLE_BLOCKS.add(material);
		}
	}

	// ------------------------------------------------------------
	// Getters
	// ------------------------------------------------------------

	public static boolean canBeInteractedWith(final Material material) {
		if (SHOP_COMPATIBLE_BLOCKS.contains(material)) {
			return true;
		}
		if (RELAY_COMPATIBLE_BLOCKS.contains(material)) {
			return true;
		}
		return false;
	}

	public static Set<Material> getShopCompatibleBlocks() {
		return Collections.unmodifiableSet(SHOP_COMPATIBLE_BLOCKS);
	}

	public static boolean hasCompatibleShopBlock(Material material) {
		return SHOP_COMPATIBLE_BLOCKS.contains(material);
	}

	public static Set<Material> getSuccessButtonBlocks() {
		return Collections.unmodifiableSet(SUCCESS_BUTTON_BLOCKS);
	}

	public static boolean hasSuccessButtonBlock(Material material) {
		return SUCCESS_BUTTON_BLOCKS.contains(material);
	}

	public static ItemStack getRuleItem() {
		return RULE_ITEM.clone();
	}

	public static boolean canCreateFromShop() {
		return CREATE_FROM_SHOP;
	}

	public static Material getRuleItemMaterial() {
		return RULE_ITEM.getType();
	}

	public static ShapelessRecipe getBulkItemRecipe() {
		return BULK_RULE_RECIPE;
	}

	public static boolean canRepairItem(Material material) {
		return ITEMS_CAN_REPAIR.contains(material);
	}

	public static Set<Material> getRelayCompatibleBlocks() {
		return Collections.unmodifiableSet(RELAY_COMPATIBLE_BLOCKS);
	}

	public static boolean hasRelayCompatibleBlock(Material material) {
		return RELAY_COMPATIBLE_BLOCKS.contains(material);
	}

	public static int getRelayRecursionLimit() {
		return RELAY_RECURSION_LIMIT;
	}

	public static int getRelayReachDistance() {
		return RELAY_REACH_DISTANCE;
	}

	public static Set<Material> getRelayPermeableBlocks() {
		return Collections.unmodifiableSet(RELAY_PERMEABLE_BLOCKS);
	}

	public static boolean hasRelayPermeableBlock(Material material) {
		return RELAY_PERMEABLE_BLOCKS.contains(material);
	}

}
