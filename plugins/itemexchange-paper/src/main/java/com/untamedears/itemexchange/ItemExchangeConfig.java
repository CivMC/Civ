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
import vg.civcraft.mc.civmodcore.CoreConfigManager;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.api.RecipeAPI;

public final class ItemExchangeConfig extends CoreConfigManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemExchangeConfig.class.getSimpleName());

	private static final Set<Material> SHOP_COMPATIBLE_BLOCKS = new HashSet<>();

	private static final Set<Material> SUCCESS_BUTTON_BLOCKS = new HashSet<>();

	private static final ItemStack RULE_ITEM = new ItemStack(Material.STONE_BUTTON);

	private static ShapelessRecipe BULK_RULE_RECIPE;

	public ItemExchangeConfig(ItemExchangePlugin plugin) {
		super(plugin);
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		parseShopCompatibleBlocks(config.getStringList("supportedBlocks"));
		parseSuccessButtonBlocks(config.getStringList("disallowedSuccessButtonBlocks"));
		parseRuleItem(config.getString("ruleItem"));
		return true;
	}

	public void reset() {
		SHOP_COMPATIBLE_BLOCKS.clear();
		SUCCESS_BUTTON_BLOCKS.clear();
		RULE_ITEM.setType(Material.STONE_BUTTON);
		if (BULK_RULE_RECIPE != null) {
			RecipeAPI.removeRecipe(BULK_RULE_RECIPE);
			BULK_RULE_RECIPE = null;
		}
	}

	private void parseShopCompatibleBlocks(List<String> config) {
		for (String raw : config) {
			Material material = MaterialAPI.getMaterial(raw);
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

	private void parseSuccessButtonBlocks(List<String> config) {
		Set<Material> disallowedButtonBlocks = new HashSet<>();
		for (String raw : config) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse material for disallowed success button block: " + raw);
				continue;
			}
			if (!material.isBlock()) {
				LOGGER.warn("Supported disallowed success button material not a block: " + raw);
				continue;
			}
			if (disallowedButtonBlocks.contains(material)) {
				LOGGER.warn("Supported disallowed success button material duplicate: " + raw);
				continue;
			}
			LOGGER.info("Supported disallowed success button material parsed: " + material.name());
			disallowedButtonBlocks.add(material);
		}
		SUCCESS_BUTTON_BLOCKS.addAll(SHOP_COMPATIBLE_BLOCKS);
		SUCCESS_BUTTON_BLOCKS.removeAll(disallowedButtonBlocks);
		if (SUCCESS_BUTTON_BLOCKS.isEmpty()) {
			LOGGER.info("There are no supported button triggering shop blocks.");
		}
	}

	private void parseRuleItem(String config) {
		Material material = MaterialAPI.getMaterial(config);
		String defaultWarning = "\tDefaulting to STONE_BUTTON.";
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
		RecipeAPI.registerRecipe(BULK_RULE_RECIPE);
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

	public static ItemStack getRuleItem() {
		return RULE_ITEM.clone();
	}

	public static Material getRuleItemMaterial() {
		return RULE_ITEM.getType();
	}

	public static ShapelessRecipe getBulkItemRecipe() {
		return BULK_RULE_RECIPE;
	}

}
