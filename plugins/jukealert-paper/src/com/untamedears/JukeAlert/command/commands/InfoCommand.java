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
	           Boolean snitchFound = false;
	           for (Snitch snitch : snitches) {
	        	   //Get only first snitch in cuboid
	        	   if(!snitchFound) {
		               if (snitch.getGroup().isMember(player.getName()) || snitch.getGroup().isFounder(player.getName()) || snitch.getGroup().isModerator(player.getName())) {
			   	           if (snitch.isWithinCuboid(player.getLocation())) {
			   	               snitchFound = true;
			   	               sendLog(sender, snitch);
			   	           }
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
		Map<String, Date> info = plugin.getJaLogger().getSnitchInfo(snitch.getLoc(), 20);
		
		player.sendMessage(ChatColor.WHITE + "Snitch Log " + ChatColor.DARK_GRAY + "----------------------------------------");
		for(Entry<String, Date> dataEntry : info.entrySet()) {
			player.sendMessage(dataEntry.getKey() + " " + dataEntry.getValue());
		}
	}

}
