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
import vg.civcraft.mc.civmodcore.commands.CommandManager;

/**
 * The main Item Exchange plugin class.
 */
public final class ItemExchangePlugin extends ACivMod implements AutoCloseable {

	private static ItemExchangePlugin instance;
	private static ItemExchangeConfig config;
	private static CommandManager commands;
	private static ModifierRegistrar modifiers;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		saveDefaultConfig();
		config = new ItemExchangeConfig(this);
		config.parse();
		commands = new CommandRegistrar(this);
		commands.init();
		modifiers = new ModifierRegistrar();
		modifiers.registerModifier(DisplayNameModifier.TEMPLATE); // 100
		modifiers.registerModifier(EnchantModifier.TEMPLATE); // 200
		modifiers.registerModifier(EnchantStorageModifier.TEMPLATE); // 201
		modifiers.registerModifier(LoreModifier.TEMPLATE); // 300
		modifiers.registerModifier(PotionModifier.TEMPLATE); // 400
		modifiers.registerModifier(DamageableModifier.TEMPLATE); // 500
		modifiers.registerModifier(RepairModifier.TEMPLATE); // 600
		modifiers.registerModifier(BookModifier.TEMPLATE); // 1000
		registerListener(new ItemExchangeListener());
	}

	@Override
	public void onDisable() {
		if (modifiers != null) {
			modifiers.reset();
			modifiers = null;
		}
		if (config != null) {
			config.reset();
			config = null;
		}
		if (commands != null) {
			commands.reset();
			commands = null;
		}
		super.onDisable();
	}

	@Override
	public void close() throws Exception {
		instance = null;
	}

	public static ItemExchangeConfig config() {
		return config;
	}

	public static CommandManager commandManager() {
		return commands;
	}

	public static ModifierRegistrar modifierRegistrar() {
		return modifiers;
	}

	public static ItemExchangePlugin getInstance() {
		return instance;
	}

}
