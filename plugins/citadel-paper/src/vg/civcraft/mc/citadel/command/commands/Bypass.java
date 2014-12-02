package vg.civcraft.mc.citadel.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.command.PlayerCommand;
import vg.civcraft.mc.citadel.misc.ReinforcementMode;

public class Bypass extends PlayerCommand{

	public Bypass(String name) {
		super(name);
		setDescription("Used to bypass block reinforcements.");
		setUsage("/ctb");
		setIdentifier("ctbypass");
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
		if (state.getMode() == ReinforcementMode.NORMAL){
			p.sendMessage(ChatColor.GREEN + "Reinforcement mode changed to "
					+ ReinforcementMode.BYPASS.name() + ".");
			state.setMode(ReinforcementMode.BYPASS);
		}
		else{
			p.sendMessage(ChatColor.GREEN + state.getMode().name() + " has been"
					+ " disabled.\nReinforcement mode has been reset.");
			state.reset();
		}
		return true;
	}

}
