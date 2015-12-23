package vg.civcraft.mc.civchat2.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupChat extends PlayerCommand{
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private CivChat2CommandHandler handler = (CivChat2CommandHandler) plugin.getCivChat2CommandHandler();
	
	public GroupChat(String name) {
		super(name);
		setIdentifier("groupc");
		setDescription("This command is used to join a citadel groupchat");
		setUsage("/groupc <groupname> (message)");
		setArguments(0,100);
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
		UUID uuid = NameAPI.getUUID(player.getName());
		String playerName = player.getName();
		GroupManager gm = NameAPI.getGroupManager();
		boolean isGroupChatting = true;
		logger.debug("chatMan = [" + chatMan.toString() + "]");
		if(chatMan.getGroupChatting(playerName) == null){
			isGroupChatting = false;
		}
		Group group;
		boolean defGroup = false;
		if(args.length <1){
			//check if player is in groupchat and move them to normal chat
			logger.debug("Checking if name=[" + playerName + "] is groupchatting");
			if(isGroupChatting){
				sender.sendMessage(ChatColor.RED + "You have been moved to global chat");
				chatMan.removeGroupChat(playerName);
				return true;
			}
			else {
				String grpName = gm.getDefaultGroup(uuid);
				if (grpName != null) {
					group = gm.getGroup(grpName);
					defGroup = true;
				}
				else {
					handler.helpPlayer(this, sender);
					return true;
				}
			}
		}
		else {
			group = GroupManager.getGroup(args[0]);
		}
		if(group == null){
			sender.sendMessage(ChatColor.RED + "There is no group with that name.");
			return true;
		}
		if(!group.isMember(uuid)){
			sender.sendMessage(ChatColor.RED + "You are not a member of that group.");
			return true;
		}
		if (chatMan.isIgnoringGroup(sender.getName(), group)){
			sender.sendMessage(ChatColor.RED + "You need to unignore group: "+group.getName());
			return true;
		}
		if(args.length == 1){	
			if(isGroupChatting){
				//player already groupchatting check if its this group
				Group curGroup = GroupManager.getGroup(chatMan.getGroupChatting(playerName));
				if(curGroup == group){
					sender.sendMessage(ChatColor.RED + "You are already chatting in that group.");
					return true;
				}
				else{
					sender.sendMessage(ChatColor.RED + "You have changed to groupchat: " + group.getName());
					chatMan.removeGroupChat(playerName);
					chatMan.addGroupChat(playerName, group.getName());
					return true;
				}
			}
			else{
				sender.sendMessage(ChatColor.RED + "You have been moved to groupchat: " + group.getName());
				String chattingWith = chatMan.getChannel(playerName);
				if (chattingWith != null) {
					chatMan.removeChannel(playerName);
				}
				chatMan.addGroupChat(playerName, group.getName());
				return true;
			}
			
		} else if (args.length > 1){
			StringBuilder chatMsg = new StringBuilder();
			for(int i = defGroup ? 0 : 1; i < args.length; i++){
				chatMsg.append(args[i]);
				chatMsg.append(" ");
			}
			logger.debug("checking if name=[" + playerName + "] is groupchatting");
			if(isGroupChatting){
				//player already groupchatting check if its this group
				Group curGroup = GroupManager.getGroup(chatMan.getGroupChatting(playerName));
				if(curGroup == group){
					chatMan.sendGroupMsg(playerName, chatMsg.toString(), group);
					return true;
				}
				else{
					sender.sendMessage(ChatColor.RED + "You have changed to groupchat: " + group.getName());
					chatMan.removeGroupChat(playerName);
					chatMan.addGroupChat(playerName, group.getName());
					chatMan.sendGroupMsg(playerName, chatMsg.toString(), group);
					return true;
				}
			}
			else{
				sender.sendMessage(ChatColor.RED + "You have been moved to groupchat: " + group.getName());
				String chattingWith = chatMan.getChannel(playerName);
				if (chattingWith != null) {
					chatMan.removeChannel(playerName);
				}
				chatMan.addGroupChat(playerName, group.getName());
				chatMan.sendGroupMsg(playerName, chatMsg.toString(), group);
				return true;
			}
		}
		
		handler.helpPlayer(this, sender);
		
		return true;
		
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		if (arg1.length == 0)
			return null;
		if (!(arg0 instanceof Player)) {
			arg0.sendMessage("This command can not be accessed from the console");
			return null;
		}
		List<String> groupsToReturn = new ArrayList<String>();
		UUID uuid = NameAPI.getUUID(arg0.getName());
		GroupManager gm = NameAPI.getGroupManager();
		List<String> groups = gm.getAllGroupNames(uuid);
		for(String group:groups) {
			if(group.toLowerCase().startsWith(arg1[0].toLowerCase())) {
				groupsToReturn.add(group);
			}
		}
		return groupsToReturn;	
	}	

}
