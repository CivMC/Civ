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
		if (plugin.getYamlHandler().worlds.contains((worldname +".firstspawn"))){

			plugin.getYamlHandler().worlds.set(worldname +".firstspawn", null);

			plugin.getYamlHandler().saveWorlds();

			plugin.playerInfo(player,  "The first spawn location of this world is removed!");
			plugin.playerInfo(player,  "Now refering to world spawn.");
		}else{
			plugin.playerInfo(player,  "There's no first spawnpoint set in this world!");
		}
		return true;
	}
}
