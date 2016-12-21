package com.untamedears.JukeAlert.command;

import com.untamedears.JukeAlert.command.commands.ClearCommand;
import com.untamedears.JukeAlert.command.commands.ConfigCommand;
import com.untamedears.JukeAlert.command.commands.GroupCommand;
import com.untamedears.JukeAlert.command.commands.HelpCommand;
import com.untamedears.JukeAlert.command.commands.InfoCommand;
import com.untamedears.JukeAlert.command.commands.GUICommand;
import com.untamedears.JukeAlert.command.commands.JaListCommand;
import com.untamedears.JukeAlert.command.commands.JaListLongCommand;
import com.untamedears.JukeAlert.command.commands.JaMuteCommand;
import com.untamedears.JukeAlert.command.commands.JaToggleLeversCommand;
import com.untamedears.JukeAlert.command.commands.LookupCommand;
import com.untamedears.JukeAlert.command.commands.NameCommand;

import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class JukeAlertCommandHandler extends CommandHandler {

	@Override
	public void registerCommands() {
		addCommands(new InfoCommand());
		addCommands(new JaListCommand());
		addCommands(new JaListLongCommand());
		addCommands(new NameCommand());
		addCommands(new ClearCommand());
		addCommands(new HelpCommand());
		addCommands(new GUICommand());
		addCommands(new GroupCommand());
		addCommands(new LookupCommand());
		addCommands(new JaMuteCommand());
		addCommands(new ConfigCommand());
		addCommands(new JaToggleLeversCommand());
	}
}
