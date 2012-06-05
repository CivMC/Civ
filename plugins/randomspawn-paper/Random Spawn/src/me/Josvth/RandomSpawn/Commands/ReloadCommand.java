package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends AbstractCommand {
	
	public ReloadCommand(RandomSpawn instance){
		super(instance, "reload");
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player) sender;

		if (args.size() == 0) {
			plugin.yamlHandler.loadYamls();
			plugin.playerInfo(player, "Random Spawn configurations reloaded!");
			return true;
		}

		if (args.get(0).matches("config")) {
			plugin.yamlHandler.loadConfig();
			plugin.playerInfo(player, "Random Spawn config file is reloaded!");
			return true;
		}

		if (args.get(0).matches("worlds")) {
			plugin.yamlHandler.loadWorlds();
			plugin.playerInfo(player, "Random Spawn worlds file reloaded!");
			return true;
		}

		if (args.get(0).matches("spawnlocations")) {
			plugin.yamlHandler.loadSpawnLocations();
			plugin.playerInfo(player, "Random Spawn spawnlocations file reloaded!");
			return true;
		}
		
		return false;
	}
}
