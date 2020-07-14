package com.github.maxopoly.finale.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

import java.util.LinkedList;
import java.util.List;

@CivCommand(id = "cardinal")
public class CardinalCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender commandSender, String[] args) {
		Player p = (Player) commandSender;
		Location currLocation = p.getLocation();

		if(args.length == 0){

			double newYaw = Math.rint(currLocation.getYaw() / 45) * 45;

			p.teleport(new Location(p.getWorld(), currLocation.getX(), currLocation.getY(),
					currLocation.getZ(), (float) newYaw, currLocation.getPitch()));
			return true;
		}

		String direction = args[0].toUpperCase();
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
		return new LinkedList<>();
	}
}
