package com.untamedears.itemexchange.commands;

import com.untamedears.itemexchange.ItemExchangePlugin;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

/**
 * Registers all of ItemExchange's commands
 */
public class CommandRegistrar extends CommandManager {

	public CommandRegistrar(final ItemExchangePlugin plugin) {
		super(plugin);
	}

	@Override
	public void registerCommands() {
		registerCommand(new CreateCommand());
		registerCommand(new DebugCommand());
		registerCommand(new InfoCommand());
		registerCommand(new ReloadCommand(getPlugin()));
		registerCommand(new SetCommand());
	}

	/**
	 * Note: Don't try to remove this in favour of a private field as registerCommands() is called from the super
	 * constructor so the field will not yet be assigned.
	 *
	 * @return Returns the plugin attached to this registrar.
	 */
	@Override
	public ItemExchangePlugin getPlugin() {
		return (ItemExchangePlugin) super.getPlugin();
	}

}
