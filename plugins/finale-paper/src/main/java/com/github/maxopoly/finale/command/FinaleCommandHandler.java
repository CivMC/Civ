package com.github.maxopoly.finale.command;

import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class FinaleCommandHandler extends CommandHandler {

	@Override
	public void registerCommands() {
		addCommands(new ReloadFinaleCommand());
	}

}
