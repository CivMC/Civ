package com.untamedears.JukeAlert.command.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.util.IgnoreList;


public class JaMuteCommand extends PlayerCommand {
	public JaMuteCommand() {
		super("jamute");
		setDescription("Mutes/Unmutes notifications from a given snitch group.");
		setUsage("/jamute <group>");
		setArgumentRange(0,1);
		setIdentifier("jamute");
	}
	
   @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {   	
        	if (args.length == 0) {
        		sendIgnoreGroupList(sender);
        	} else {
        		toggleIgnore(sender, args[0].toString());
        	}

        } else {
            sender.sendMessage(ChatColor.RED + " You do not have the ability to ignore groups!");
        }
        return true;
    }
   
   private void toggleIgnore(CommandSender sender, String groupName) {
       final Player player = (Player)sender;
       final UUID accountId = player.getUniqueId();
       if (groupName.equals("*")) {
           if (IgnoreList.toggleIgnoreAll(accountId)) {
               player.sendMessage("Ignoring all groups!");
           } else {
               player.sendMessage("Stopped ignoring all groups!");
           }
           return;
       }

       if(plugin.getJaLogger().getMutedGroups(accountId) == null){
    	   //no groups mute first group
    	   plugin.getJaLogger().muteGroups(accountId, groupName);
    	   player.sendMessage("Added group \"" + groupName + "\" to ignore list! \n "
       	   		+ "Use /jamute on this group again to unmute");
    	   return;
       }
       
       List<String> groups = (Arrays.asList(plugin.getJaLogger().getMutedGroups(accountId).split("\\s+")));
       
       if(groups.contains(groupName)){
    	     //unmute the group if its in their list
    	   plugin.getJaLogger().removeIgnoredGroup(groupName,  accountId);
    	   player.sendMessage("Removed group \"" + groupName + "\" from ignore list!");
       }else{
    	   //add the group
    	   plugin.getJaLogger().updateMutedGroups(accountId, groupName);
    	   player.sendMessage("Added group \"" + groupName + "\" to ignore list! \n "
    	   		+ "Use /jamute on this group again to unmute");
    	   
       }
   }

    private void sendIgnoreGroupList(CommandSender sender) {
        final Player player = (Player) sender;
        final UUID accountId = player.getUniqueId();
        if (IgnoreList.doesPlayerIgnoreAll(accountId)) {
            sender.sendMessage("* Ignoring all groups *");
            return;
        }
         //IgnoreList.GetGroupIgnoreListByPlayer(accountId);
        //new pull from db to get ignored groups
        String ignoredGroups = this.plugin.getJaLogger().getMutedGroups(accountId);
        this.plugin.log("Ignored Groups for Player is: " + ignoredGroups);
        if(ignoredGroups == null){
        	sender.sendMessage("* No Group Ignores");
        }
        else{
        
	        String[] groups = ignoredGroups.split("\\s+");
	        StringBuilder sb = new StringBuilder();
        	for(String g : groups) {
        		sb.append(g);
        		sb.append(", ");
        		
        		if (sb.length() > 100) {
        			sb.delete(101, sb.length());
        			sb.append("...  ");
        			break;
        		}
        	}
        	sb.delete(sb.length()-2, sb.length());
	        
	        sb.insert(0,  "Ignore List:  ");
	        
	        sender.sendMessage(sb.toString());
        }
    }
}
