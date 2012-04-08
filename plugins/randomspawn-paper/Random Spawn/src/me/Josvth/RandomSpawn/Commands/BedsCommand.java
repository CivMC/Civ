package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedsCommand extends RandomSpawnCommandExecutor{
	
	public BedsCommand(){
		name = "usebeds";
	}
		
	public boolean onCommand(CommandSender sender, List<String> args){
		
		Player player = (Player) sender;
		String worldName = player.getWorld().getName();
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
		
		if (args.size() == 0){
			if (randomSpawnFlags.contains("bedrespawn")){
				randomSpawnFlags.remove("bedrespawn");
				plugin.playerInfo((Player)sender, "Beds are now disabled.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}else{
				randomSpawnFlags.add("bedspawn");
				plugin.playerInfo((Player)sender, "Beds will now work like normal.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
		}
		
		if (args.size() == 1){
			if (args.get(0).matches("true")){
				randomSpawnFlags.add("bedspawn");
				plugin.playerInfo((Player)sender, "Beds will now work like normal.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
			if (args.get(0).matches("false")){
				randomSpawnFlags.remove("bedrespawn");
				plugin.playerInfo((Player)sender, "Beds are now disabled.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
			
		}
		return false;
	}
}
