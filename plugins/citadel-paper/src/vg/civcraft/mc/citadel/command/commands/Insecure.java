package vg.civcraft.mc.citadel.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

import java.util.ArrayList;
import java.util.List;

public class Insecure extends PlayerCommand{

	public Insecure(String name) {
		super(name);
		setIdentifier("ctin");
		setDescription("Set a block to an insecure reinforcement.");
		setUsage("/ctin");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Must be a player to perform that command.");
			return true;
		}
		Player p = (Player) sender;
		PlayerState state = PlayerState.get(p);
		if (state.getMode() == ReinforcementMode.INSECURE){
			Utility.sendAndLog(p, ChatColor.GREEN, state.getMode().name() + " has been disabled");
			state.reset();
		}
		else{
			Utility.sendAndLog(p, ChatColor.GREEN, "Reinforcement mode changed to "
					+ ReinforcementMode.INSECURE.name() + ".");
			state.setMode(ReinforcementMode.INSECURE);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}

}
