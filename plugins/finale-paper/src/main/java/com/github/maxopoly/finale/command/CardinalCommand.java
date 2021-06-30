package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("cardinal")
public class CardinalCommand extends BaseCommand {

	@Syntax("/cardinal")
	@Description("Changes your direction into a cardinal direction")
	public void execute(CommandSender commandSender, @Optional String targetDirection) {
		Player player = (Player) commandSender;
		Location currLocation = player.getLocation();

		if(targetDirection == null){
			double newYaw = Math.rint(currLocation.getYaw() / 45) * 45;
			player.teleport(new Location(player.getWorld(), currLocation.getX(), currLocation.getY(),
					currLocation.getZ(), (float) newYaw, currLocation.getPitch()));
			return;
		}

		String direction = targetDirection.toUpperCase();
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
				return;
		}

		player.teleport(location);
	}
}
