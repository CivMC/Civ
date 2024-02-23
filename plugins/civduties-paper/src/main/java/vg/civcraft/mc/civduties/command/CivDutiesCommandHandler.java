package vg.civcraft.mc.civduties.command;

import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civduties.command.commands.Duty;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class CivDutiesCommandHandler extends CommandManager {

	public CivDutiesCommandHandler(Plugin plugin) {
		super(plugin);
		init();
	}

	@Override
	public void registerCommands() {
		registerCommand(new Duty());
	}
	
}
