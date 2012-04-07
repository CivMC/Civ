package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisableCommand extends RandomSpawnCommandExecutor{
	
	public DisableCommand(){
		name = "disable";
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player) sender;
		String worldname = player.getWorld().getName();
		plugin.yamlHandler.worlds.set(worldname + ".randomspawnon", null);
		plugin.yamlHandler.saveWorlds();
		plugin.playerInfo(player, "Random Spawn is now disabled in this world!");
		return true;
	}
}
