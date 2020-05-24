package com.untamedears.itemexchange;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;

import com.untamedears.itemexchange.commands.CommandRegistrar;
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

	private final ItemExchangeConfig config = new ItemExchangeConfig(this);

	private final AikarCommandManager commands = new CommandRegistrar(this);

	private final ModifierRegistrar modifiers = new ModifierRegistrar(this);

	@Override
	public void onEnable() {
		instance = this;
		useNewCommandHandler = false;
		super.onEnable();
		saveDefaultConfig();
		this.config.parse();
		this.commands.register();
		this.modifiers.registerModifier(new DisplayNameModifier()); // 0
		this.modifiers.registerModifier(new EnchantModifier()); // 10
		this.modifiers.registerModifier(new EnchantStorageModifier()); // 20
		this.modifiers.registerModifier(new LoreModifier()); // 30
		this.modifiers.registerModifier(new PotionModifier()); // 40
		this.modifiers.registerModifier(new DamageableModifier()); // 50
		this.modifiers.registerModifier(new RepairModifier()); // 60
		this.modifiers.registerModifier(new BookModifier()); // 100
		registerListener(new ItemExchangeListener());
	}

	@Override
	public void onDisable() {
		this.commands.reset();
		this.config.reset();
		this.modifiers.reset();
		super.onDisable();
		instance = null;
	}

	public ItemExchangeConfig config() {
		return this.config;
	}

	public AikarCommandManager commandManager() {
		return this.commands;
	}

	public ModifierRegistrar modifierRegistrar() {
		return this.modifiers;
	}

	public static ItemExchangePlugin getInstance() {
		return instance;
	}

	public static ItemExchangeConfig getConfiguration() {
		return chain(() -> instance.config);
	}

	public static AikarCommandManager getCommandManager() {
		return chain(() -> instance.commands);
	}

	public static ModifierRegistrar getModifierRegistrar() {
		return chain(() -> instance.modifiers);
	}

}
