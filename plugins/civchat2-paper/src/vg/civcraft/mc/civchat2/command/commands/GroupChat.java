package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupChat extends PlayerCommand{
	private CivChat2Manager chatMan = CivChat2.getCivChat2Manager();
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private CivChat2CommandHandler handler = CivChat2.getCivChat2CommandHandler();
	
	public GroupChat(String name) {
		super(name);
		setIdentifier("groupchat");
		setDescription("This command is used to join a citadel groupchat");
		setUsage("/groupchat <groupname> (message)");
		setArguments(1,2);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args){
		if(!(sender instanceof Player)){
			//console man sending chat... 
			sender.sendMessage(ChatColor.YELLOW + "You must be a player to perform that command.");
			return true;
		}
		
		Group group = null;
		Player player = (Player) sender;
		GroupManager gm = NameAPI.getGroupManager();
		group = gm.getGroup(args[0]);
		if(args.length == 1){
			//player just joining group chat
			if(group == null){
				sender.sendMessage(ChatColor.RED + "There is no group with that name.");
				return true;
			}
			
		}
		handler.helpPlayer(this, sender);
		
		return true;
		
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}	

}
