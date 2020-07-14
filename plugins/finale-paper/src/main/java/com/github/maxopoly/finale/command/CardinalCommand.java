package com.github.maxopoly.finale.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CivCommand(id = "cardinal")
public class CardinalCommand extends StandaloneCommand {

	private static final String[] DIRECTIONS = {"NORTH", "EAST", "SOUTH", "WEST", "UP", "DOWN"};

	@Override
	public boolean execute(CommandSender commandSender, String[] args) {
		Player p = (Player) commandSender;

		if(args.length == 0){
			commandSender.sendMessage(ChatColor.WHITE + "Cardinal direction required. " +
					"You can also use degrees, or 'up' or 'down'. A");
			return true;
		}

		String direction = args[0].toUpperCase();
		Location currLocation = p.getLocation();
		Location l = new Location(p.getWorld(), currLocation.getX(), currLocation.getY(), currLocation.getZ());

		switch(direction){
			case "UP":
				l.setPitch(-90f);
				break;
			case "DOWN":
				l.setPitch(90f);
				break;
			case "SOUTH": case "180":
				l.setYaw(0f);
				break;
			case "EAST": case "90":
				l.setYaw(-90f);
				break;
			case "NORTH": case "0":
				l.setYaw(180f);
				break;
			case "WEST": case "-90": case "270":
				l.setYaw(90f);
				break;
			default:
				p.sendMessage(ChatColor.WHITE + "Cardinal direction required. You can also use degrees, or 'up' or 'down'. B");
				return true;
		}

		p.teleport(l);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender commandSender, String[] strings) {
		return new ArrayList<>(Arrays.asList(DIRECTIONS));
	}
}
