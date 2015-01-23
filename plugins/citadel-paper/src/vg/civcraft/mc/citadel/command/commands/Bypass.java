package vg.civcraft.mc.citadel.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.command.PlayerCommand;

public class Bypass extends PlayerCommand{

	public Bypass(String name) {
		super(name);
		setIdentifier("ctb");
		setDescription("Used to bypass block reinforcements.");
		setUsage("/ctb");
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
		if (state.toggleBypassMode()){
			p.sendMessage(ChatColor.GREEN + "Bypass mode has been enabled.");
		}
		else 
			p.sendMessage(ChatColor.GREEN + "Bypass mode has been disabled.");
		return true;
	}

}
