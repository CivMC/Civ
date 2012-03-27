package me.Josvth.RandomSpawn;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.Josvth.RandomSpawn.Commands.*;

public class RandomSpawnCommandHandler implements CommandExecutor{

	RandomSpawn plugin;

	HashMap<String,RandomSpawnCommandExecutor> commands = new HashMap<String,RandomSpawnCommandExecutor>();

	public RandomSpawnCommandHandler(RandomSpawn instance) {
		plugin = instance;
		
		plugin.getCommand("randomspawn").setExecutor(this);
		plugin.getCommand("rs").setExecutor(this);
		
		registerCommands(new RandomSpawnCommandExecutor[]{
				new RandomSpawnBedsCommand(), new RandomSpawnDisableCommand(), new RandomSpawnEnableCommand(), new RandomSpawnFirstJoinCommand(), new RandomSpawnHelpCommand(), new RandomSpawnInfoCommand(), new RandomSpawnKeepSpawnsCommand(), new RandomSpawnReloadCommand(), new RandomSpawnSetAreaCommand(), new RandomSpawnSetFirstSpawnCommand(), new RandomSpawnTpFirstSpawnCommand(), new RandomSpawnUnsetFirstSpawnCommand()
		});
	}

	private void registerCommands(RandomSpawnCommandExecutor[] executors) {

		for(RandomSpawnCommandExecutor executor : executors){
			executor.setPlugin(plugin);
			commands.put(executor.name, executor);
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
		
		if (!sender.hasPermission(commandExecutor.getPermission())){
			sender.sendMessage("You don't have the permission to use this command!");
		}
		
		if (commandExecutor.onCommand(sender, Arrays.asList(args).subList(1, args.length)) == false){
			sender.sendMessage(commandExecutor.getUsage());
		}
		
		return true;
	}
}
