package me.josvth.randomspawn.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.josvth.randomspawn.RandomSpawn;

public class FirstJoinCommand extends AbstractCommand{

	public FirstJoinCommand(RandomSpawn instance){
		super(instance, "firstjoin");
	}

	public boolean onCommand(CommandSender sender, List<String> args){

		Player player = (Player) sender;
		String worldName = player.getWorld().getName();
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
		
		if (args.size() == 0){
			if (randomSpawnFlags.contains("firstjoin")){
				randomSpawnFlags.remove("firstjoin");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo(player, "Random Spawn will not spawn new players.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}else{
				randomSpawnFlags.add("firstjoin");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo(player, "Random Spawn will random spawn new players.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
		}
		if (args.size() == 1){
			if (args.get(0).matches("true")){
				randomSpawnFlags.remove("firstjoin");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo(player, "Random Spawn will not spawn new players.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
			if (args.get(0).matches("false")){
				randomSpawnFlags.remove("firstjoin");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo(player, "Random Spawn will not spawn new players.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
		}
		return false;
	}
}
