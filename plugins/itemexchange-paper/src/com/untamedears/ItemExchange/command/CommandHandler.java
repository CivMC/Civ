package com.untamedears.ItemExchange.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import com.untamedears.ItemExchange.command.commands.CreateCommand;


/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class CommandHandler {

	private Map<String, Command> commands = new LinkedHashMap<String, Command>();
	private Map<String, Command> identifiers = new HashMap<String, Command>();

	public void registerCommands() {
		this.addCommand(new CreateCommand());
	}

	public void addCommand(Command command){
		this.commands.put(command.getName().toLowerCase(), command);
		for(String ident : command.getIdentifiers()){
			this.identifiers.put(ident.toLowerCase(), command);
		}
	}
	
	public boolean dispatch(CommandSender sender, String label, String[] args){
		for(int argsIncluded = args.length; argsIncluded >= 0; argsIncluded--){
			StringBuilder identifier = new StringBuilder(label);
			for(int i = 0; i < argsIncluded; i++){
				identifier.append(" ").append(args[i]);
			}

			Command cmd = getCmdFromIdent(identifier.toString(), sender);
			if(cmd == null){
				continue;
			}
			String[] realArgs = (String[])Arrays.copyOfRange(args, argsIncluded, args.length);

			if(!cmd.isInProgress(sender)){
				if((realArgs.length < cmd.getMinArguments()) || (realArgs.length > cmd.getMaxArguments())){
					displayCommandHelp(cmd, sender);
					return true;
				}
				if((realArgs.length > 0) && (realArgs[0].equals("?"))){
					displayCommandHelp(cmd, sender);
					return true;
				}
			}
			
			try {
				cmd.execute(sender, realArgs);
			} catch(Exception e){
				e.printStackTrace();
			}
			return true;
		}
		return true;
	}
	
	private void displayCommandHelp(Command cmd, CommandSender sender){
		sender.sendMessage(new StringBuilder().append("§cCommand:§e " ).append(cmd.getName()).toString());
		sender.sendMessage(new StringBuilder().append("§cDescription:§e " ).append(cmd.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append("§cUsage:§e ").append(cmd.getUsage()).toString());
	}

	private Command getCmdFromIdent(String ident, CommandSender executor) {
		ident = ident.toLowerCase();
		if(this.identifiers.containsKey(ident)){
			return (Command)this.identifiers.get(ident);
		}
		
		for(Command cmd : this.commands.values()){
			if(cmd.isIdentifier(executor, ident)){
				return cmd;
			}
		}
		
		return null;
	}
}
