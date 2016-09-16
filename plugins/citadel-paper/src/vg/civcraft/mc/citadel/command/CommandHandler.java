package vg.civcraft.mc.citadel.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.citadel.command.commands.Acid;
import vg.civcraft.mc.citadel.command.commands.AreaReinforce;
import vg.civcraft.mc.citadel.command.commands.Bypass;
import vg.civcraft.mc.citadel.command.commands.Fortification;
import vg.civcraft.mc.citadel.command.commands.Information;
import vg.civcraft.mc.citadel.command.commands.Insecure;
import vg.civcraft.mc.citadel.command.commands.Materials;
import vg.civcraft.mc.citadel.command.commands.Off;
import vg.civcraft.mc.citadel.command.commands.Reinforce;
import vg.civcraft.mc.citadel.command.commands.ReinforcementsGUI;
import vg.civcraft.mc.citadel.command.commands.SetLogging;
import vg.civcraft.mc.citadel.command.commands.Stats;
import vg.civcraft.mc.citadel.command.commands.ToggleEasyMode;
import vg.civcraft.mc.citadel.command.commands.UpdateReinforcements;
import vg.civcraft.mc.civmodcore.command.Command;

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
		addCommands(new Off("Off"));
		addCommands(new Stats("Stats"));
		addCommands(new UpdateReinforcements("UpdateReinforcements"));
		addCommands(new AreaReinforce("AreaReinforce"));
		addCommands(new SetLogging("SetLogging"));
		addCommands(new ToggleEasyMode("ToggleEasyMode"));
		addCommands(new ReinforcementsGUI("ReinforcementGUI"));
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
	 * Is called when a tab is pressed.  Should not be touched by any outside
	 * plugin.
	 * @param sender
	 * @param cmd
	 * @param args
	 * @return
	 */

	public List<String> complete(CommandSender sender, org.bukkit.command.Command cmd, String[] args){
		if (commands.containsKey(cmd.getName().toLowerCase())){
			Command command = commands.get(cmd.getName().toLowerCase());
			return command.tabComplete(sender, args);
		}
		return null;
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
