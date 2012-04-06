package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnUnsetFirstSpawnCommand extends RandomSpawnCommandExecutor{
	
	public RandomSpawnUnsetFirstSpawnCommand(){
		this.name = "unsetfirstspawn";
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player)sender;
		String worldname = player.getWorld().getName();
		if (plugin.yamlHandler.worlds.contains((worldname +".firstspawn"))){

			plugin.yamlHandler.worlds.set(worldname +".firstspawn", null);

			plugin.yamlHandler.saveWorlds();

			plugin.playerInfo(player,  "The first spawn location of this world is removed!");
			plugin.playerInfo(player,  "Now refering to world spawn.");
		}else{
			plugin.playerInfo(player,  "There's no first spawnpoint set in this world!");
		}
		return true;
	}
}
