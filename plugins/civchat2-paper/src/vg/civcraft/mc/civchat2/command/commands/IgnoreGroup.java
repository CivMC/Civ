package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

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

public class IgnoreGroup extends PlayerCommandMiddle{
	private CivChat2 plugin = CivChat2.getInstance();
	private CivChat2Manager chatMan;
	private CivChat2Log logger = CivChat2.getCivChat2Log();
	private CivChat2CommandHandler handler = CivChat2.getCivChat2CommandHandler();
	
	public IgnoreGroup(String name) {
		super(name);
		setIdentifier("ignoregroup");
		setDescription("This command is used to toggle ignoring a group");
		setUsage("/ignoregroup <GroupName>");
		setArguments(1,1);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args){
		chatMan = plugin.getCivChat2Manager();
		if(!(sender instanceof Player)){
			//console man sending chat... 
			sender.sendMessage(ChatColor.YELLOW + "You must be a player to perform that command.");
			return true;
		}
		
		if(!(args.length == 1)){
			handler.helpPlayer(this, sender); 
			return true;
		}
		
		Group group = null;
		Player player = (Player) sender;
		GroupManager gm = NameAPI.getGroupManager();
		group = GroupManager.getGroup(args[0]);
		if(group == null){
			//no player exists with that name
			sender.sendMessage(ChatColor.RED + "No Group exists with that name");
			return true;
		}
		String ignore = group.getName();
		String name = NameAPI.getCurrentName(player.getUniqueId());
		String curGroup = chatMan.getGroupChatting(name);
		if(chatMan.addIgnoringGroup(name, ignore)){
			//Player added to list
			String debugMessage = "Player ignored Group, Player: " + name + " Group: " + ignore;
			logger.debug(debugMessage);
			sender.sendMessage(ChatColor.YELLOW + "You have ignored: " + ignore);
			String gchat = chatMan.getGroupChatting(name);
			if (gchat != null){
				if (gchat.equals(ignore)){
					chatMan.removeGroupChat(name);
					sender.sendMessage(ChatColor.RED + "You have been moved to global chat");
				}
			}
			return true;
		} else{
			//player removed from list
			String debugMessage = "Player un-ignored Group, Player: " + name + " Group: " + ignore;
			logger.debug(debugMessage);
			sender.sendMessage(ChatColor.YELLOW + "You have removed: " + ignore + " from ignoring list");
			return true;
		}		
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		// TODO Auto-generated method stub
		return null;
	}	

}
