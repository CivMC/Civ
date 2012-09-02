package me.Josvth.RandomSpawn.Commands;

import java.util.ArrayList;
import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetAreaCommand extends AbstractCommand{

	public SetAreaCommand(RandomSpawn instance){
		super(instance,"setarea");
	}

	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player) sender;

		double xmin = 0;
		double xmax = 0;
		double zmin = 0;
		double zmax = 0;

		if (args.size() == 1) {
			Location reference = player.getLocation();

			try{
				xmin = reference.getX() - Double.parseDouble(args.get(0));
				xmax = reference.getX() + Double.parseDouble(args.get(0));
				zmin = reference.getZ() - Double.parseDouble(args.get(0));
				zmax = reference.getZ() + Double.parseDouble(args.get(0));
			} catch(NumberFormatException e){
				sender.sendMessage("Invalid number.");
				return false;
			}

			String worldname = reference.getWorld().getName();

			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-min", xmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-max", xmax);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-min", zmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-max", zmax);

			plugin.yamlHandler.worlds.set(worldname + ".randomspawnenabled", true);

			plugin.yamlHandler.saveWorlds();

			plugin.playerInfo(player,  "Spawn area set!");

			return true;
		}

		if (args.size() == 2 && (args.get(0).equalsIgnoreCase("circle") || args.get(0).equalsIgnoreCase("square"))) {

			Location reference = player.getLocation();

			try{

				xmin = reference.getX() - Double.parseDouble(args.get(1));
				xmax = reference.getX() + Double.parseDouble(args.get(1));
				zmin = reference.getZ() - Double.parseDouble(args.get(1));
				zmax = reference.getZ() + Double.parseDouble(args.get(1));

			} catch(NumberFormatException e){
				sender.sendMessage("Invalid number.");
				return false;
			}

			String worldname = reference.getWorld().getName();

			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-min", xmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-max", xmax);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-min", zmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-max", zmax);

			List<String> rsflags = new ArrayList<String>();
			rsflags.add("respawn");

			plugin.yamlHandler.worlds.set(worldname + ".randomspawnon", rsflags);

			if (args.get(0).matches("circle")) {
				plugin.yamlHandler.worlds.set(worldname + ".spawnarea.type", "circle");
			}else{
				plugin.yamlHandler.worlds.set(worldname + ".spawnarea.type", "square");
			}

			plugin.playerInfo(player, "Spawn area set!");

			plugin.yamlHandler.saveWorlds();

			return true;
		}

		if (args.size() == 2){
			
			Location reference = player.getLocation();

			try{
				xmin = reference.getX() - (Double.parseDouble(args.get(0)) / 2);
				xmax = reference.getX() + (Double.parseDouble(args.get(0)) / 2);
				zmin = reference.getZ() - (Double.parseDouble(args.get(1)) / 2);
				zmax = reference.getZ() + (Double.parseDouble(args.get(1)) / 2);
			}catch(NumberFormatException e){
				sender.sendMessage("Invalid number.");
				return false;
			}


			String worldname = reference.getWorld().getName();

			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-min", xmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-max", xmax);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-min", zmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-max", zmax);

			List<String> rsflags = new ArrayList<String>();
			rsflags.add("respawn");

			plugin.yamlHandler.worlds.set(worldname + ".randomspawnon", rsflags);
			
			plugin.yamlHandler.saveWorlds();

			plugin.playerInfo(player,  "Spawn area set!");

			return true;
		}

		if (args.size() == 4){
			Location reference = player.getLocation();

			try{

				xmin = Double.parseDouble(args.get(0));
				xmax = Double.parseDouble(args.get(1));
				zmin = Double.parseDouble(args.get(2));
				zmax = Double.parseDouble(args.get(3));

			} catch(NumberFormatException e){
				sender.sendMessage("Invalid number.");
				return false;
			}

			String worldname = reference.getWorld().getName();

			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-min", xmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-max", xmax);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-min", zmin);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-max", zmax);

			List<String> rsflags = new ArrayList<String>();
			rsflags.add("respawn");

			plugin.yamlHandler.worlds.set(worldname + ".randomspawnon", rsflags);
			
			plugin.yamlHandler.saveWorlds();

			plugin.playerInfo(player,  "Spawn area set!");

			return true;
		}

		return false;

	}

}
