package com.untamedears.JukeAlert.command.commands;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.chat.ChatFiller;

public class InfoCommand extends PlayerCommand {

	public InfoCommand() {
		super("Info");
		setDescription("Displays information from a Snitch");
		setUsage("/jainfo");
		setArgumentRange(0,0);
		setIdentifier("jainfo");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
	           Player player = (Player) sender;
	           World world = player.getWorld();
	           
	           List<Snitch> snitches = plugin.getSnitchManager().getSnitchesByWorld(world);
	           for (Snitch snitch : snitches) {
	        	   //Get only first snitch in cuboid
		           if (snitch.getGroup().isMember(player.getName()) || snitch.getGroup().isFounder(player.getName()) || snitch.getGroup().isModerator(player.getName())) {
		        	   if (snitch.isWithinCuboid(player.getLocation())) {
		        		   sendLog(sender, snitch);
		        		   break;
			   	       }
		           }
	           }

	        } else {
	           sender.sendMessage("You must be a player!");
	           return false;
	        }
	    return false;
		
	}
	
	private void sendLog(CommandSender sender, Snitch snitch) {
		Player player = (Player)sender;
		List<String> info = plugin.getJaLogger().getSnitchInfo(snitch.getLoc(), 0);
		
		player.sendMessage(ChatColor.WHITE + " Snitch Log " + ChatColor.DARK_GRAY + "----------------------------------------");
		player.sendMessage(ChatColor.DARK_GRAY + String.format("  %s %s %s", ChatFiller.fillString("Name", (double) 30), ChatFiller.fillString("Reason", (double) 20), ChatFiller.fillString("Details", (double) 30)));
		for(String dataEntry : info) {
			player.sendMessage(dataEntry);
		}
	}

}
