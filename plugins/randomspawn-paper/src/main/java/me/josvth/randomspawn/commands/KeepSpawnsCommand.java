package me.josvth.randomspawn.commands;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KeepSpawnsCommand extends AbstractCommand {

	public KeepSpawnsCommand(RandomSpawn instance){
		super(instance, "keepfirstspawns");
	}

	public boolean onCommand(CommandSender sender, List<String> args){

		Player player = (Player) sender;		
		String worldname = player.getWorld().getName();

		if (args.size() == 0){
			if (plugin.yamlHandler.worlds.getBoolean(worldname + ".keeprandomspawns", false)){
				plugin.yamlHandler.worlds.set(worldname + ".keeprandomspawns", false);
				plugin.playerInfo(player, "Keep random spawns is now disabled.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}else
			{
				plugin.yamlHandler.worlds.set(worldname + ".keeprandomspawns", true);
				plugin.playerInfo(player, "Random Spawn will now save the spawn locations.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
		}
		if (args.size() == 1){
			if (args.get(0).matches("true")){
				plugin.yamlHandler.worlds.set(worldname + ".keeprandomspawns", true);
				plugin.playerInfo(player, "Random Spawn will now save the spawn locations.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
			if (args.get(0).matches("false")){
				plugin.yamlHandler.worlds.set(worldname + ".keeprandomspawns", false);
				plugin.playerInfo(player, "Keep random spawns is now disabled.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
		}

		return false;
	}
}
