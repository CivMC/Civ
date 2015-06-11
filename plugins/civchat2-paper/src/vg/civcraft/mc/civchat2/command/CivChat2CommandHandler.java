package vg.civcraft.mc.civchat2.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.command.Command;
import vg.civcraft.mc.civchat2.command.commands.Exit;
import vg.civcraft.mc.civchat2.command.commands.GroupChat;
import vg.civcraft.mc.civchat2.command.commands.Ignore;
import vg.civcraft.mc.civchat2.command.commands.IgnoreGroup;
import vg.civcraft.mc.civchat2.command.commands.IgnoreList;
import vg.civcraft.mc.civchat2.command.commands.Reply;
import vg.civcraft.mc.civchat2.command.commands.Tell;
import vg.civcraft.mc.civchat2.command.commands.Afk;

public class CivChat2CommandHandler {
	
	public Map<String, Command> commands = new HashMap<String, Command>();
	
	public void registerCommands(){
		addCommands(new Tell("tell"));
		addCommands(new Afk("afk"));
		addCommands(new Reply("reply"));
		addCommands(new GroupChat("groupc"));
		addCommands(new Ignore("ignore"));
		addCommands(new IgnoreGroup("ignoregroup"));
		addCommands(new IgnoreList("ignorelist"));
		addCommands(new Exit("exit"));
	}
	
	public void addCommands(Command command){
		commands.put(command.getIdentifier().toLowerCase(), command);
	}
	
	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args){
		if(commands.containsKey(cmd.getName().toLowerCase())){
			Command command = (Command) commands.get(cmd.getName().toLowerCase());
			if(args.length < command.getMinArguments() || args.length > command.getMaxArguments()){
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}
	
	public void helpPlayer(Command command, CommandSender sender){
		StringBuilder sb = new StringBuilder();
		
		String cmd = sb.append(ChatColor.RED)
						.append("Command: ")
						.append(command.getName().toString())
						.toString();
		sb.delete(0, sb.length());
		
		String desc = sb.append(ChatColor.RED)
						.append("Description: ")
						.append(command.getDescription().toString())
						.toString();
		sb.delete(0, sb.length());
		
		String usage = sb.append(ChatColor.RED)
						.append("Usage: ")
						.append(command.getUsage().toString())
						.toString();
		sb.delete(0, sb.length());
		
		sender.sendMessage(cmd);
		sender.sendMessage(desc);
		sender.sendMessage(usage);
	}
}
