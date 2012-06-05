package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnableCommand extends AbstractCommand{
	
	public EnableCommand(RandomSpawn instance){
		super(instance,"enable");
	}

	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player) sender;
		String worldname = player.getWorld().getName();
		List<String> spawnFlags = plugin.yamlHandler.worlds.getStringList(worldname + ".randomspawnon");
		spawnFlags.add("respawn");
		
		plugin.yamlHandler.worlds.set(worldname + ".randomspawnon", spawnFlags);
		
		if (!(plugin.yamlHandler.worlds.contains(worldname +".spawnarea"))){
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-min", -100);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.x-max", 100);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-min", -100);
			plugin.yamlHandler.worlds.set(worldname + ".spawnarea.z-max", 100);
		}
		
		plugin.yamlHandler.saveWorlds();
		plugin.playerInfo(player, "Random Spawn is now enabled in this world!");
		
		return true;
	}
}
