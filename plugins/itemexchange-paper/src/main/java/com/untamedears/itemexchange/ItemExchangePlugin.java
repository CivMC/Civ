package com.untamedears.itemexchange;

import com.untamedears.itemexchange.commands.CommandRegistrar;
import com.untamedears.itemexchange.rules.BulkExchangeRule;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.ModifierRegistrar;
import com.untamedears.itemexchange.rules.modifiers.BookModifier;
import com.untamedears.itemexchange.rules.modifiers.DamageableModifier;
import com.untamedears.itemexchange.rules.modifiers.DisplayNameModifier;
import com.untamedears.itemexchange.rules.modifiers.EnchantModifier;
import com.untamedears.itemexchange.rules.modifiers.EnchantStorageModifier;
import com.untamedears.itemexchange.rules.modifiers.LoreModifier;
import com.untamedears.itemexchange.rules.modifiers.PotionModifier;
import com.untamedears.itemexchange.rules.modifiers.RepairModifier;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;

/**
 * The main Item Exchange plugin class.
 */
public class ItemExchangePlugin extends ACivMod {

	private static ItemExchangePlugin instance;

	private static ItemExchangeConfig config;

	private static AikarCommandManager commands;

	private static ModifierRegistrar modifiers;

	@Override
	public void onEnable() {
		instance = this;
		useNewCommandHandler = false;
		super.onEnable();
		registerSerializable(ExchangeRule.class);
		registerSerializable(BulkExchangeRule.class);
		saveDefaultConfig();
		config = new ItemExchangeConfig(this);
		config.parse();
		commands = new CommandRegistrar(this);
		commands.register();
		modifiers = new ModifierRegistrar();
		modifiers.registerModifier(new DisplayNameModifier()); // 0
		modifiers.registerModifier(new EnchantModifier()); // 10
		modifiers.registerModifier(new EnchantStorageModifier()); // 20
		modifiers.registerModifier(new LoreModifier()); // 30
		modifiers.registerModifier(new PotionModifier()); // 40
		modifiers.registerModifier(new DamageableModifier()); // 50
		modifiers.registerModifier(new RepairModifier()); // 60
		modifiers.registerModifier(new BookModifier()); // 100
		registerListener(new ItemExchangeListener());
		// Allow debug logging
	}

	@Override
	public void onDisable() {
		if (commands != null) {
			commands.reset();
			commands = null;
		}
		if (config != null) {
			config.reset();
			config = null;
		}
		if (modifiers != null) {
			modifiers.reset();
			modifiers = null;
		}
		super.onDisable();
		instance = null;
	}

	public static ItemExchangeConfig config() {
		return config;
	}

	public static AikarCommandManager commandManager() {
		return commands;
	}

	public static ModifierRegistrar modifierRegistrar() {
		return modifiers;
	}

	public static ItemExchangePlugin getInstance() {
		return instance;
	}

}
