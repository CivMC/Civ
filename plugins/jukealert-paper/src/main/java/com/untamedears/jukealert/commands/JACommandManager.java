package com.untamedears.jukealert.commands;

import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class JACommandManager extends CommandManager {

	public JACommandManager(Plugin plugin) {
		super(plugin);
		init();
	}

	@Override
	public void registerCommands() {
		registerCommand(new ClearCommand());
		registerCommand(new GUICommand());
		registerCommand(new InfoCommand());
		registerCommand(new ListCommand());
		registerCommand(new MuteCommand());
		registerCommand(new NameCommand());
		registerCommand(new NameAtCommand());
		registerCommand(new ToggleLeverCommand());
	}
}
