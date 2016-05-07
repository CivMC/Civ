package me.josvth.randomspawn.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.commands.*;

public class CommandHandler implements CommandExecutor{

	final RandomSpawn plugin;

	HashMap<String,AbstractCommand> commands = new HashMap<String,AbstractCommand>();

	public CommandHandler(RandomSpawn instance) {
		plugin = instance;
		
		plugin.getCommand("randomspawn").setExecutor(this);
		plugin.getCommand("rs").setExecutor(this);
		
		registerCommands(new AbstractCommand[]{
				new BedsCommand(plugin), 
				new DisableCommand(plugin), 
				new EnableCommand(plugin), 
				new FirstJoinCommand(plugin), 
				new HelpCommand(plugin), 
				new InfoCommand(plugin), 
				new KeepSpawnsCommand(plugin), 
				new ReloadCommand(plugin), 
				new SetAreaCommand(plugin), 
				new SetFirstSpawnCommand(plugin), 
				new TpFirstSpawnCommand(plugin), 
				new UnsetFirstSpawnCommand(plugin),
				new SpawnCommand(plugin),
				new AddSpawnPointCommand(plugin),
				new RemoveSpawnPointCommand(plugin)
		});
	}

	private void registerCommands(AbstractCommand[] abstractCommands) {

		for(AbstractCommand abstractCommand : abstractCommands){
			commands.put(abstractCommand.getName(), abstractCommand);
			List<String> aliases = abstractCommand.getAliases();
			if (abstractCommand.getAliases() != null){
				for(String alias: aliases){
					commands.put(alias, abstractCommand);
				}
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {	
					
		if (args.length == 0 || !commands.containsKey(args[0])) return false;
		
		AbstractCommand abstractCommand = commands.get(args[0]);
		
		if (!(sender instanceof Player) && args[0] != null && !args[0].equals( "reload" ) ){
			sender.sendMessage("This command can only be used in game!");
			return true;
		}
		
		if (abstractCommand.getPermission() != null && !sender.hasPermission(abstractCommand.getPermission())){
			sender.sendMessage("You don't have the permission to use this command!");
			return true;
		}
		
		if (abstractCommand.onCommand(sender, Arrays.asList(args).subList(1, args.length)) == false && abstractCommand.getUsage() != null){
			sender.sendMessage(abstractCommand.getUsage());
		}
		
		return true;
	}
}
