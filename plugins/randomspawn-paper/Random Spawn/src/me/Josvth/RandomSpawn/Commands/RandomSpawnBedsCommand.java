package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnBedsCommand extends RandomSpawnCommandExecutor{
	
	
	public RandomSpawnBedsCommand(){
		this.name = "usebeds";
	}
		
	public boolean onCommand(CommandSender sender, List<String> args){
		
		Player player = (Player) sender;
		String worldName = player.getWorld().getName();
		
		if (args.size() == 0){
			if (plugin.getYamlHandler().worlds.getBoolean(worldName + ".usebeds", true)){
				plugin.getYamlHandler().worlds.set(worldName + ".usebeds", false);
				plugin.playerInfo((Player)sender, "Beds are now disabled.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}else{
				plugin.getYamlHandler().worlds.set(worldName + ".usebeds", true);
				plugin.playerInfo((Player)sender, "Beds will now work like normal.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
		}
		
		if (args.size() == 1){
			if (args.get(0).matches("true")){
				plugin.getYamlHandler().worlds.set(worldName + ".usebeds", true);
				plugin.playerInfo(player, "Beds will now work like normal.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
			if (args.get(0).matches("false")){
				plugin.getYamlHandler().worlds.set(worldName + ".usebeds", false);
				plugin.playerInfo(player, "Beds are now disabled.");
				plugin.getYamlHandler().saveWorlds();
				return true;
			}
			
		}
		return false;
	}
}
