package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnSetAreaCommand extends RandomSpawnCommandExecutor{
	
	public RandomSpawnSetAreaCommand(){
		this.name = "setarea";
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player) sender;

		if (args.size() == 1 && args.get(0).matches("[0-9]+")) {
			Location reference = player.getLocation();

			int xmin = (int) (reference.getX() - Integer.parseInt(args.get(0)));
			int xmax = (int) (reference.getX() + Integer.parseInt(args.get(0)));
			int zmin = (int) (reference.getZ() - Integer.parseInt(args.get(0)));
			int zmax = (int) (reference.getZ() + Integer.parseInt(args.get(0)));

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

		if (args.size() == 2 && args.get(1).matches("[0-9]+")) {
			if (args.get(0).matches("square")) {
				Location reference = player.getLocation();

				int xmin = (int) (reference.getX() - Integer.parseInt(args.get(1)));
				int xmax = (int) (reference.getX() + Integer.parseInt(args.get(1)));
				int zmin = (int) (reference.getZ() - Integer.parseInt(args.get(1)));
				int zmax = (int) (reference.getZ() + Integer.parseInt(args.get(1)));

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
			if (args.get(0).matches("circle")) {

			}
			return false;
		}

		if (args.size() == 2) {
			if (args.get(0).matches("-{0,1}[0-9]+") && args.get(1).matches("-{0,1}[0-9]+")){
				Location reference = player.getLocation();

				int xmin = (int) (reference.getX() - Integer.parseInt(args.get(0))/2);
				int xmax = (int) (reference.getX() + Integer.parseInt(args.get(0))/2);
				int zmin = (int) (reference.getZ() - Integer.parseInt(args.get(1))/2);
				int zmax = (int) (reference.getZ() + Integer.parseInt(args.get(1))/2);

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
		}
		
		if (args.size() == 4) {
			if (args.get(0).matches("-{0,1}[0-9]+") && args.get(1).matches("-{0,1}[0-9]+") && args.get(2).matches("-{0,1}[0-9]+") && args.get(3).matches("-{0,1}[0-9]+")){
				Location reference = player.getLocation();

				int xmin = Integer.parseInt(args.get(0));
				int xmax = Integer.parseInt(args.get(1));
				int zmin = Integer.parseInt(args.get(2));
				int zmax = Integer.parseInt(args.get(3));

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
			return false;
		}
		
		return false;

	}
}
