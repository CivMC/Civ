package com.jjj5311.minecraft.civchat2.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.jjj5311.minecraft.civchat2.command.CivChat2Command;
import com.jjj5311.minecraft.civchat2.command.commands.Tell;

public class CivChat2CommandHandler {
	
	public Map<String, CivChat2Command> commands = new HashMap<String, CivChat2Command>();
	
	public void registerCommands(){
		addCommands(new Tell("Tell"));
	}
	
	public void addCommands(CivChat2Command command){
		commands.put(command.getIdentifier().toLowerCase(), command);
	}
	
	public boolean execute(CommandSender sender, Command cmd, String[] args){
		if(commands.containsKey(cmd.getName().toLowerCase())){
			CivChat2Command command = (CivChat2Command) commands.get(cmd.getName().toLowerCase());
			if(args.length < command.getMinArguments() || args.length > command.getMaxArguments()){
				helpPlayer(command, sender);
				return true;
			}
		}
		return true;
	}
	
	public void helpPlayer(CivChat2Command command, CommandSender sender){
		String cmd = ChatColor.RED + "Command: " + command.getName();
		String desc = ChatColor.RED + "Description: " + command.getDescription();
		String usage = ChatColor.RED + "Usage: " + command.getUsage();
		sender.sendMessage(cmd);
		sender.sendMessage(desc);
		sender.sendMessage(usage);
	}
}
