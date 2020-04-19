package com.untamedears.itemexchange;

import com.untamedears.itemexchange.commands.CreateCommand;
import com.untamedears.itemexchange.commands.InfoCommand;
import com.untamedears.itemexchange.commands.ReloadCommand;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.glue.CitadelGlue;
import com.untamedears.itemexchange.glue.NameLayerGlue;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.modifiers.BookModifier;
import com.untamedears.itemexchange.rules.modifiers.DamageableModifier;
import com.untamedears.itemexchange.rules.modifiers.EnchantStorageModifier;
import com.untamedears.itemexchange.rules.modifiers.PotionModifier;
import com.untamedears.itemexchange.rules.modifiers.RepairModifier;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.api.RecipeAPI;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

public class ItemExchangePlugin extends ACivMod {

	public static final Set<Material> SHOP_BLOCKS = new HashSet<>();

	public static final Set<Material> SUCCESS_BUTTON_BLOCKS = new HashSet<>();

	public static final ItemStack RULE_ITEM = new ItemStack(Material.STONE_BUTTON);

	public static final Set<Material> CAN_ENCHANT = new HashSet<>();

	public static final Set<Material> IS_DAMAGEABLE = new HashSet<>();

	public static final Set<Material> IS_REPAIRABLE = new HashSet<>();

	public static ShapelessRecipe BULK_RULE_RECIPE;

	private static ItemExchangePlugin instance;

	private AikarCommandManager commands;

	public static ItemExchangePlugin getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		saveDefaultConfig();
		// Register Events
		registerListener(new ItemExchangeListener(this));
		// Load Commands
		this.commands = new AikarCommandManager(this) {
			@Override
			public void registerCommands() {
				registerCommand(new CreateCommand());
				registerCommand(new InfoCommand());
				registerCommand(new ReloadCommand(ItemExchangePlugin.this));
				registerCommand(new SetCommand());
			}
		};
		// Register Serializables
		registerSerializable(ExchangeRule.class);
		registerSerializable(BulkExchangeRule.class);
		registerSerializable(BookModifier.class);
		registerSerializable(DamageableModifier.class);
		registerSerializable(EnchantStorageModifier.class);
		registerSerializable(PotionModifier.class);
		registerSerializable(RepairModifier.class);
		// Parse which blocks can be used for shops
		SHOP_BLOCKS.clear();
		for (String raw : getConfig().getStringList("supportedBlocks")) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				warning("[Config] Could not parse material for supported block: " + raw);
				continue;
			}
			if (SHOP_BLOCKS.contains(material)) {
				warning("[Config] Supported block material duplicate: " + raw);
				continue;
			}
			info("[Config] Supported block material parsed: " + material.name());
			SHOP_BLOCKS.add(material);
		}
		if (SHOP_BLOCKS.isEmpty()) {
			warning("[Config] There are no supported blocks, try: supportedBlocks: [CHEST, TRAPPED_CHEST]");
		}
		// Parse which shop blocks can trigger the successful transaction button
		SUCCESS_BUTTON_BLOCKS.clear();
		Set<Material> disallowedButtonBlocks = new HashSet<>();
		for (String raw : getConfig().getStringList("disallowedSuccessButtonBlocks")) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				warning("[Config] Could not parse material for disallowed success button block: " + raw);
				continue;
			}
			if (disallowedButtonBlocks.contains(material)) {
				warning("[Config] Supported disallowed success button material duplicate: " + raw);
				continue;
			}
			info("[Config] Supported disallowed success button material parsed: " + material.name());
			disallowedButtonBlocks.add(material);
		}
		SUCCESS_BUTTON_BLOCKS.addAll(SHOP_BLOCKS);
		SUCCESS_BUTTON_BLOCKS.removeAll(disallowedButtonBlocks);
		if (SUCCESS_BUTTON_BLOCKS.isEmpty()) {
			warning("[Config] There are no supported button triggering shop blocks.");
		}
		// Parse which item material will be used for itemized rules
		RULE_ITEM.setType(Material.STONE_BUTTON);
		{
			String raw = getConfig().getString("ruleItem");
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				warning("[Config] Could not parse material for rule item, default to STONE_BUTTON: " + raw);
			}
			else {
				info("[Config] Rule item material parsed: " + material.name());
				RULE_ITEM.setType(material);
			}
		}
		// Parse which item materials can be enchanted
		CAN_ENCHANT.clear();
		for (String raw : getConfig().getStringList("enchantables")) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				warning("[Config] Could not parse enchantable material: " + raw);
				continue;
			}
			if (CAN_ENCHANT.contains(material)) {
				warning("[Config] Enchantable material duplicate: " + raw);
				continue;
			}
			info("[Config] Enchantable material parsed: " + material.name());
			CAN_ENCHANT.add(material);
		}
		// Parse which item materials can be damaged
		IS_DAMAGEABLE.clear();
		for (String raw : getConfig().getStringList("damageables")) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				warning("[Config] Could not parse damageable material: " + raw);
				continue;
			}
			if (IS_DAMAGEABLE.contains(material)) {
				warning("[Config] Damageable material duplicate: " + raw);
				continue;
			}
			info("[Config] Damageable material parsed: " + material.name());
			IS_DAMAGEABLE.add(material);
		}
		// Parse which item materials can be repaired
		IS_REPAIRABLE.clear();
		for (String raw : getConfig().getStringList("repairables")) {
			Material material = MaterialAPI.getMaterial(raw);
			if (material == null) {
				warning("[Config] Could not parse repairable material: " + raw);
				continue;
			}
			if (IS_REPAIRABLE.contains(material)) {
				warning("[Config] Repairable material duplicate: " + raw);
				continue;
			}
			info("[Config] Repairable material parsed: " + material.name());
			IS_REPAIRABLE.add(material);
		}
		// Register the recipe to craft bulk exchange recipes
		if (ItemAPI.isValidItem(RULE_ITEM)) {
			BULK_RULE_RECIPE = new ShapelessRecipe(NamespacedKey.minecraft("bulk_exchange_rule"), RULE_ITEM.clone());
			BULK_RULE_RECIPE.addIngredient(2, RULE_ITEM.getType());
			Bukkit.addRecipe(BULK_RULE_RECIPE);
			info("[Config] Bulk rule item recipe registered.");
		}
		else {
			warning("[Config] Could not create bulk rule item recipe.");
		}
		// Register Glues
		registerListener(NameLayerGlue.INSTANCE);
		registerListener(CitadelGlue.INSTANCE);
	}

	@Override
	public void onDisable() {
		// Unload Configs
		SHOP_BLOCKS.clear();
		SUCCESS_BUTTON_BLOCKS.clear();
		CAN_ENCHANT.clear();
		IS_DAMAGEABLE.clear();
		IS_REPAIRABLE.clear();
		NullCoalescing.exists(BULK_RULE_RECIPE, RecipeAPI::removeRecipe);
		// Unload Commands
		this.commands.reset();
		this.commands = null;
		// Finalise Disable
		super.onDisable();
		instance = null;
	}

}
