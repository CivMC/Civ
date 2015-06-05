package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class GroupChat extends PlayerCommandMiddle{
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private CivChat2CommandHandler handler = CivChat2.getCivChat2CommandHandler();
	
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
		if(args.length <1){
			//check if player is in groupchat and move them to normal chat
			logger.debug("Checking if name=[" + playerName + "] is groupchatting");
			if(isGroupChatting){
				sender.sendMessage(ChatColor.RED + "You have been moved to global chat");
				chatMan.removeGroupChat(playerName);
				return true;
			}
			else {
				handler.helpPlayer(this, sender);
				return true;
			}
		}
		Group group = gm.getGroup(args[0]);
		if(group == null){
			sender.sendMessage(ChatColor.RED + "There is no group with that name.");
			return true;
		}
		if(!group.isMember(uuid)){
			sender.sendMessage(ChatColor.RED + "You are not a member of that group.");
			return true;
		}
		if(args.length == 1){	
			if(isGroupChatting){
				//player already groupchatting check if its this group
				Group curGroup = gm.getGroup(chatMan.getGroupChatting(playerName));
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
				chatMan.addGroupChat(playerName, group.getName());
				return true;
			}
			
		} else if (args.length > 1){
			StringBuilder chatMsg = new StringBuilder();
			for(int i = 1; i < args.length; i++){
				chatMsg.append(args[i]);
				chatMsg.append(" ");
			}
			logger.debug("checking if name=[" + playerName + "] is groupchatting");
			if(isGroupChatting){
				//player already groupchatting check if its this group
				Group curGroup = gm.getGroup(chatMan.getGroupChatting(playerName));
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
		// TODO Auto-generated method stub
		return null;
	}	

}
