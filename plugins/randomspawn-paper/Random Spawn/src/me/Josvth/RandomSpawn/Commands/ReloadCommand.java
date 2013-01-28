package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.command.CommandSender;

public class ReloadCommand extends AbstractCommand {
	
	public ReloadCommand(RandomSpawn instance){
		super(instance, "reload");
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){

		if (args.size() == 0) {
			plugin.yamlHandler.loadYamls();
			sender.sendMessage( "Random Spawn configurations reloaded!" );
			return true;
		}

		if (args.get(0).matches("config")) {
			plugin.yamlHandler.loadConfig();
			sender.sendMessage( "Random Spawn config file is reloaded!");
			return true;
		}

		if (args.get(0).matches("worlds")) {
			plugin.yamlHandler.loadWorlds();
			sender.sendMessage( "Random Spawn worlds file reloaded!");
			return true;
		}
		
		return false;
	}
}
