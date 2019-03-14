package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "ctb")
public class Bypass extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		AbstractPlayerState currentState = Citadel.getInstance().getStateManager().getState(player);
		if (currentState.isBypassEnabled()) {
			Utility.sendAndLog(player, ChatColor.GREEN, "Bypass mode has been disabled.");
		} else {
			Utility.sendAndLog(player, ChatColor.GREEN,
					"Bypass mode has been enabled. You will be able to break reinforced blocks if you are on the group.");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}
}
