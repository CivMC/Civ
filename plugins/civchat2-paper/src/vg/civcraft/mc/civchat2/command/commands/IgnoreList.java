package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class IgnoreList extends PlayerCommand{
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private CivChat2CommandHandler handler = (CivChat2CommandHandler) plugin.getCivChat2CommandHandler();
	
	public IgnoreList(String name) {
		super(name);
		setIdentifier("ignorelist");
		setDescription("This command is used to list ignored players/groups");
		setUsage("/ignorelist");
		setArguments(0,0);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args){
		chatMan = plugin.getCivChat2Manager();
		if(!(sender instanceof Player)){
			//console man sending chat... 
			sender.sendMessage(ChatColor.YELLOW + "You must be a player to perform that command.");
			return true;
		}
		
		Player player = (Player) sender;
		
		if(!(args.length == 0)){
			handler.helpPlayer(this, sender); 
			return true;
		}
		
		List<String> players = chatMan.getIgnoredPlayers(player.getName());
		List<String> groups = chatMan.getIgnoredGroups(player.getName());
		
		if(players == null){
			//no players ignored
			sender.sendMessage(ChatColor.RED + "You are not Ignoring any players");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Ignored Players: ");
			sb.append("\n");
			for(String s : players){
				sb.append(s);
				sb.append(", ");
			}
			sender.sendMessage(ChatColor.YELLOW + sb.toString());
		}
		
		if(groups == null){
			//no players ignored
			sender.sendMessage(ChatColor.RED + "You are not Ignoring any groups");
			return true;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Ignored Groups: ");
			sb.append("\n");
			for(String s : groups){
				sb.append(s);
				sb.append(", ");
			}
			sender.sendMessage(ChatColor.YELLOW + sb.toString());
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}	

}
