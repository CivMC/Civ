package me.Josvth.RandomSpawn.Handlers;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.Josvth.RandomSpawn.RandomSpawn;
import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;
import me.Josvth.RandomSpawn.Commands.*;

public class CommandHandler implements CommandExecutor{

	RandomSpawn plugin;

	HashMap<String,RandomSpawnCommandExecutor> commands = new HashMap<String,RandomSpawnCommandExecutor>();

	public CommandHandler(RandomSpawn instance) {
		plugin = instance;
		
		plugin.getCommand("randomspawn").setExecutor(this);
		plugin.getCommand("rs").setExecutor(this);
		
		registerCommands(new RandomSpawnCommandExecutor[]{
				new BedsCommand(), 
				new DisableCommand(), 
				new EnableCommand(), 
				new FirstJoinCommand(), 
				new RandomSpawnHelpCommand(), 
				new InfoCommand(), 
				new RandomSpawnKeepSpawnsCommand(), 
				new RandomSpawnReloadCommand(), 
				new RandomSpawnSetAreaCommand(), 
				new RandomSpawnSetFirstSpawnCommand(), 
				new RandomSpawnTpFirstSpawnCommand(), 
				new RandomSpawnUnsetFirstSpawnCommand(),
		});
	}

	private void registerCommands(RandomSpawnCommandExecutor[] executors) {

		for(RandomSpawnCommandExecutor executor : executors){
			executor.setPlugin(plugin);
			commands.put(executor.getName(), executor);
			if (executor.getAliases() != null){
				for(String alias : executor.getAliases()){
					commands.put(alias, executor);
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
					
		if (args.length == 0 || !commands.containsKey(args[0])) return false;
		
		RandomSpawnCommandExecutor commandExecutor = commands.get(args[0]);
		
		if (!(sender instanceof Player)){
			sender.sendMessage("This command can only be used in game!");
			return true;
		}
		
		if (commandExecutor.getPermission() != null && !sender.hasPermission(commandExecutor.getPermission())){
			sender.sendMessage("You don't have the permission to use this command!");
			return true;
		}
		
		if (commandExecutor.onCommand(sender, Arrays.asList(args).subList(1, args.length)) == false && commandExecutor.getUsage() != null){
			sender.sendMessage(commandExecutor.getUsage());
		}
		
		return true;
	}
}
