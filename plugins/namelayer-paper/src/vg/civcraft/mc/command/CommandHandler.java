package vg.civcraft.mc.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

public class CommandHandler {
	public Map<String, Command> commands = new HashMap<String, Command>();
	
	public void addCommands(){
		
	}
	
	public boolean execute(CommandSender sender, String label, String[] args){
		if (commands.containsKey(label)){
			Command command = commands.get(label);
			if (args.length < command.getMinArguments() || args.length > command.getMaxArguments()){
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}
	
	public void helpPlayer(Command command, CommandSender sender){
		
	}
}
