package com.github.maxopoly.finale.command;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "cardinal")
public class CardinalCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender commandSender, String[] args) {
		Player player = (Player) commandSender;
		Location currLocation = player.getLocation();

		if(args.length == 0){
			double newYaw = Math.rint(currLocation.getYaw() / 45) * 45;
			player.teleport(new Location(player.getWorld(), currLocation.getX(), currLocation.getY(),
					currLocation.getZ(), (float) newYaw, currLocation.getPitch()));
			return true;
		}

		String direction = args[0].toUpperCase();
		Location location = new Location(player.getWorld(), currLocation.getX(), currLocation.getY(), currLocation.getZ());

		switch(direction){
			case "UP":
				location.setPitch(-90f);
				break;
			case "DOWN":
				location.setPitch(90f);
				break;
			case "SOUTH": case "0":
				location.setYaw(0f);
				break;
			case "EAST": case "-90": case "270":
				location.setYaw(-90f);
				break;
			case "NORTH": case "180":
				location.setYaw(180f);
				break;
			case "WEST": case "90":
				location.setYaw(90f);
				break;
			default:
				player.sendMessage(ChatColor.RED + "Cardinal direction required. You can also use degrees, or 'up' or 'down'. B");
				return true;
		}

		player.teleport(location);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender commandSender, String[] args) {
		return doTabComplete(args[0], Arrays.asList("NORTH","WEST","EAST","SOUTH", "UP","DOWN"), false);
	}
}
