package me.josvth.randomspawn.commands;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnsetFirstSpawnCommand extends AbstractCommand{
	
	public UnsetFirstSpawnCommand(RandomSpawn instance){
		super(instance,"unsetfirstspawn");
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
