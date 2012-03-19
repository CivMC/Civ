package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnKeepSpawnsCommand extends RandomSpawnCommandExecutor {

	public RandomSpawnKeepSpawnsCommand(){
		this.name = "keepfirstspawns";
	}

	public boolean onCommand(CommandSender sender, List<String> args){

		Player player = (Player) sender;		
		String worldname = player.getWorld().getName();

		if (args.size() == 0){
			if (plugin.getYamlHandler().worlds.getBoolean(worldname + ".keeprandomspawns", false)){
				plugin.getYamlHandler().worlds.set(worldname + ".keeprandomspawns", false);
				plugin.playerInfo(player, "Keep random spawns is now disabled.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}else
			{
				plugin.getYamlHandler().worlds.set(worldname + ".keeprandomspawns", true);
				plugin.playerInfo(player, "Random Spawn will now save the spawn locations.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
		}
		if (args.size() == 1){
			if (args.get(0).matches("true")){
				plugin.getYamlHandler().worlds.set(worldname + ".keeprandomspawns", true);
				plugin.playerInfo(player, "Random Spawn will now save the spawn locations.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
			if (args.get(0).matches("false")){
				plugin.getYamlHandler().worlds.set(worldname + ".keeprandomspawns", false);
				plugin.playerInfo(player, "Keep random spawns is now disabled.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
		}

		return false;
	}
}
