package vg.civcraft.mc.citadel.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.citadel.command.commands.Acid;
import vg.civcraft.mc.citadel.command.commands.Bypass;
import vg.civcraft.mc.citadel.command.commands.Fortification;
import vg.civcraft.mc.citadel.command.commands.Information;
import vg.civcraft.mc.citadel.command.commands.Insecure;
import vg.civcraft.mc.citadel.command.commands.Materials;
import vg.civcraft.mc.citadel.command.commands.Reinforce;

public class CommandHandler {
	public Map<String, Command> commands = new HashMap<String, Command>();
	/**
	 * Registers the commands for the CommandHandler.
	 */
	public void registerCommands(){
		addCommands(new Acid("Acid"));
		addCommands(new Bypass("Bypass"));
		addCommands(new Fortification("Fortification"));
		addCommands(new Information("Information"));
		addCommands(new Insecure("Insecure"));
		addCommands(new Reinforce("Reinforce"));
		addCommands(new Materials("Materials"));
	}
	
	private void addCommands(Command command){
			commands.put(command.getIdentifier().toLowerCase(), command);
	}
	/**
	 * Is called when a command is executed.  Should not be touched by any outside
	 * plugin.
	 * @param sender
	 * @param cmd
	 * @param args
	 * @return
	 */
	public boolean execute(CommandSender sender, org.bukkit.command.Command cmd, String[] args){
		if (commands.containsKey(cmd.getName().toLowerCase())){
			Command command = commands.get(cmd.getName().toLowerCase());
			if (args.length < command.getMinArguments() || args.length > command.getMaxArguments()){
				helpPlayer(command, sender);
				return true;
			}
			command.execute(sender, args);
		}
		return true;
	}
	/**
	 * Sends a player help message.
	 * @param The Command that was executed.
	 * @param The CommandSender who executed the command.
	 */
	public void helpPlayer(Command command, CommandSender sender){
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Command: " ).append(command.getName()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Description: " ).append(command.getDescription()).toString());
		sender.sendMessage(new StringBuilder().append(ChatColor.RED + "Usage: ").append(command.getUsage()).toString());
	}
}
