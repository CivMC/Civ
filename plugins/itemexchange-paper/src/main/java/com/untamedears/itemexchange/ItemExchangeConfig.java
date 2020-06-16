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

	private static final Set<Material> SHOP_BOUNCE_BLOCKS = new HashSet<>();
	private static int SHOP_BOUNCE_LIMIT;
	private static int SHOP_BOUNCE_MAX_DISTANCE;
	private static int SHOP_BOUNCE_MAX_CONTAINERS;

	private static final Set<Material> SUCCESS_BUTTON_BLOCKS = new HashSet<>();

	private static final ItemStack RULE_ITEM = new ItemStack(Material.STONE_BUTTON);

	private static boolean CREATE_FROM_SHOP = true;

	private static final Set<Material> ITEMS_CAN_ENCHANT = new HashSet<>();

	private static final Set<Material> ITEMS_CAN_DAMAGE = new HashSet<>();

	private static final Set<Material> ITEMS_CAN_REPAIR = new HashSet<>();

	private static ShapelessRecipe BULK_RULE_RECIPE;

	public ItemExchangeConfig(ItemExchangePlugin plugin) {
		super(plugin);
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		parseShopCompatibleBlocks(config.getStringList("supportedBlocks"));
		parseShopBounceBlocks(config.getStringList("shopBounceBlocks"),
				config.getInt("shopBounceLimit", -1),
				config.getInt("shopBounceMaxDistance"),
				config.getInt("shopBounceMaxContainers"));
		parseSuccessButtonBlocks(config.getStringList("disallowedSuccessButtonBlocks"));
		parseRuleItem(config.getString("ruleItem"));
		parseCreateFromShop(config.getBoolean("createShopFromChest", true));
		parseEnchantableItems(config.getStringList("enchantables"));
		parseDamageableItems(config.getStringList("damageables"));
		parseRepairableItems(config.getStringList("repairables"));
		return true;
	}

	public void reset() {
		SHOP_COMPATIBLE_BLOCKS.clear();
		SHOP_BOUNCE_BLOCKS.clear();
		SUCCESS_BUTTON_BLOCKS.clear();
		RULE_ITEM.setType(Material.STONE_BUTTON);
		CREATE_FROM_SHOP = true;
		ITEMS_CAN_ENCHANT.clear();
		ITEMS_CAN_DAMAGE.clear();
		ITEMS_CAN_REPAIR.clear();
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

	private void parseShopBounceBlocks(List<String> config, int bounceLimit, int maxDistance, int maxContainers) {
			for (String raw : config) {
				Material material = MaterialAPI.getMaterial(raw);
				if (material == null) {
					LOGGER.warn("Could not parse material for shop bounce block: " + raw);
					continue;
				}
				if (!material.isBlock()) {
					LOGGER.warn("Shop bounce block material not a block: " + raw);
					continue;
				}
				if (SHOP_COMPATIBLE_BLOCKS.contains(material)) {
					LOGGER.warn("Shop bounce block material duplicate: " + raw);
					continue;
				}
				LOGGER.info("Shop bounce block material parsed: " + material.name());
				SHOP_BOUNCE_BLOCKS.add(material);
			}
			if (SHOP_BOUNCE_BLOCKS.isEmpty()) {
				LOGGER.warn("There are no Shop bounce blocks, try:");
				LOGGER.warn("\tshopBounceBlocks: [ENDER_CHEST]");
			}

			SHOP_BOUNCE_LIMIT = bounceLimit;
			SHOP_BOUNCE_MAX_DISTANCE = maxDistance;
			SHOP_BOUNCE_MAX_CONTAINERS = maxContainers;
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

	private void parseCreateFromShop(boolean config) {
		CREATE_FROM_SHOP = config;
		LOGGER.info("Create Shop From Shop Block: " + (config ? "ENABLED" : "DISABLED"));
	}

	private void parseEnchantableItems(List<String> config) {
		for (String raw : config) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse enchantable material: " + raw);
				continue;
			}
			if (!material.isItem()) {
				LOGGER.warn("Enchantable material is not an item: " + raw);
				continue;
			}
			if (ITEMS_CAN_ENCHANT.contains(material)) {
				LOGGER.warn("Enchantable material duplicate: " + raw);
				continue;
			}
			LOGGER.info("Enchantable material parsed: " + material.name());
			ITEMS_CAN_ENCHANT.add(material);
		}
	}

	private void parseDamageableItems(List<String> config) {
		for (String raw : config) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				LOGGER.warn("Could not parse damageable material: " + raw);
				continue;
			}
			if (!material.isItem()) {
				LOGGER.warn("Damageable material is not an item: " + raw);
				continue;
			}
			if (ITEMS_CAN_DAMAGE.contains(material)) {
				LOGGER.warn("Damageable material duplicate: " + raw);
				continue;
			}
			LOGGER.info("Damageable material parsed: " + material.name());
			ITEMS_CAN_DAMAGE.add(material);
		}
	}

	private void parseRepairableItems(List<String> config) {
		for (String raw : config) {
			Material material = MaterialAPI.getMaterial(raw);
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

	public static Set<Material> getShopCompatibleBlocks() {
		return Collections.unmodifiableSet(SHOP_COMPATIBLE_BLOCKS);
	}

	public static boolean hasCompatibleShopBlock(Material material) {
		return SHOP_COMPATIBLE_BLOCKS.contains(material);
	}

	public static Set<Material> getShopBounceBlocks() {
		return Collections.unmodifiableSet(SHOP_BOUNCE_BLOCKS);
	}

	public static boolean hasShopBounceBlock(Material material) {
		return SHOP_BOUNCE_BLOCKS.contains(material);
	}

	public static int getShopBounceLimit() {
		return SHOP_BOUNCE_LIMIT;
	}

	public static int getShopBounceMaxDistance() {
		return SHOP_BOUNCE_MAX_DISTANCE;
	}

	public static int getShopBounceMaxContainers() {
		return SHOP_BOUNCE_MAX_CONTAINERS;
	}

	public static Set<Material> getSuccessButtonBlocks() {
		return Collections.unmodifiableSet(SUCCESS_BUTTON_BLOCKS);
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

	public static boolean canEnchantItem(Material material) {
		return ITEMS_CAN_ENCHANT.contains(material);
	}

	public static boolean canDamageItem(Material material) {
		return ITEMS_CAN_DAMAGE.contains(material);
	}

	public static boolean canRepairItem(Material material) {
		return ITEMS_CAN_REPAIR.contains(material);
	}

}
