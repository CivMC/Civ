package com.github.igotyou.FactoryMod.commands;

import com.github.igotyou.FactoryMod.commands.commands.Menu;

import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class FactoryModCommandHandler extends CommandHandler{
	public void registerCommands() {
		addCommands(new Menu("fm"));
	}
	

}
