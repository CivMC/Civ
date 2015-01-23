package vg.civcraft.mc.citadel.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.command.PlayerCommand;

public class Off extends PlayerCommand{

	public Off(String name) {
		super(name);
		setIdentifier("cto");
		setDescription("Turns all Reinforcement Modes off.");
		setUsage("/cto");
		setArguments(0,0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Must be a player to run this command.");
			return true;
		}
		Player p = (Player) sender;
		PlayerState state = PlayerState.get(p);
		state.reset();
		p.sendMessage(ChatColor.GREEN + "Reinforcement mode has been set to Normal.");
		return true;
	}

}
