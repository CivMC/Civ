package vg.civcraft.mc.citadel.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class ToggleEasyMode extends PlayerCommand {

	public ToggleEasyMode(String name) {
		super(name);
		setIdentifier("cte");
		setDescription("Toggle easy mode");
		setUsage("/cte [on|off]");
		setArguments(0, 1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Must be a player to perform that command.");
			return true;
		}
		Player p = (Player) sender;
		PlayerState state = PlayerState.get(p);
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				if (!state.getEasyMode()) {
					state.toggleEasyMode();
				}
				Utility.sendAndLog(p, ChatColor.GREEN, "Easy mode has been enabled.");
			} else if (args[0].equalsIgnoreCase("off")) {
				if (state.getEasyMode()) {
					state.toggleEasyMode();
				}
				Utility.sendAndLog(p, ChatColor.GREEN, "Easy mode has been disabled.");
			} else {
				Utility.sendAndLog(p, ChatColor.RED, String.format("Usage: %s", this.getUsage()));
			}
			return true;
		}
		if (state.toggleEasyMode()) {
			Utility.sendAndLog(p, ChatColor.GREEN, "Easy mode has been enabled.");
		} else {
			Utility.sendAndLog(p, ChatColor.GREEN, "Easy mode has been disabled.");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}

}
