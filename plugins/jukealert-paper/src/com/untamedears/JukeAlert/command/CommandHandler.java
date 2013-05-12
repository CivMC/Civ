package com.untamedears.JukeAlert.command;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

public class CommandHandler {
	
	private Map<String, Command> commands = new LinkedHashMap<String, Command>();

	public void addCommand(Command command) {
		//JM: Is this sanitized enough? Minecraft technically accepts every ASCII character
		// Maybe do this instead:
		String identifier = command.getIdentifier();
		boolean correctForm = identifier.matches("^[0-9a-zA-Z\\._/]*$");
		if(correctForm){
			this.commands.put(identifier.toLowerCase(), command);
                }//How the fuck did this happen?
//		String identifier = command.getIdentifier().toLowerCase();
//		this.commands.put(identifier, command);
	}

	public boolean dispatch(CommandSender sender, String label, String[] args) {
		if (commands.containsKey(label)) {
			Command command = commands.get(label);
			if (args.length < command.getMinArguments()
					|| args.length > command.getMaxArguments()) {
				this.displayCommandHelp(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		//JM: Maybe throw something on bad dispatch
		return true;
	}

	public void displayCommandHelp(Command command, CommandSender sender) {
		sender.sendMessage(new StringBuilder().append("Command: ")
				.append(command.getName()).toString());
		sender.sendMessage(new StringBuilder().append("Description: ")
				.append(command.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append("Usage: ")
				.append(command.getUsage()).toString());
	}
}
