package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;


public class RandomSpawnFirstJoinCommand extends RandomSpawnCommandExecutor{
	
	public RandomSpawnFirstJoinCommand(){
		this.name = "firstjoin";
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		
		Player player = (Player) sender;
		String worldname = player.getWorld().getName();
			
		if (args.size() == 0){
			if (plugin.getYamlHandler().worlds.getBoolean(worldname + ".randomspawnonfirstjoin", true)){
				plugin.getYamlHandler().worlds.set(worldname + ".randomspawnonfirstjoin", false);
				plugin.playerInfo(player, "Random Spawn will not spawn new players.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}else{
				plugin.getYamlHandler().worlds.set(worldname + ".randomspawnonfirstjoin", true);
				plugin.playerInfo(player, "Random Spawn will random spawn new players.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
		}
		if (args.size() == 1){
			if (args.get(0).matches("true")){
				plugin.getYamlHandler().worlds.set(worldname + ".randomspawnonfirstjoin", true);
				plugin.playerInfo(player, "Random Spawn will random spawn new players.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
			if (args.get(0).matches("false")){
				plugin.getYamlHandler().worlds.set(worldname + ".randomspawnonfirstjoin", false);
				plugin.playerInfo(player, "Random Spawn will not spawn new players.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
		}
		return false;
	}
}
