package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "cto")
public class Off extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Citadel.getInstance().getStateManager().setState((Player) sender, null);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}

}
