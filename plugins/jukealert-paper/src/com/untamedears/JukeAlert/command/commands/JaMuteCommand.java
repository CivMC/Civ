package com.untamedears.JukeAlert.command.commands;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.util.IgnoreList;

public class JaMuteCommand extends PlayerCommand {
	
	public JaMuteCommand() {
		super("jamute");
		setDescription("Mutes notifications from a given snitch group.");
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
        		toggleIgnore(sender, args[0]);
        	}

            return true;
        } else {
            sender.sendMessage(ChatColor.RED + " You do not have the ability to ignore groups!");
            return false;
        }
    }
   
   private void toggleIgnore(CommandSender sender, String groupName) {
	   Boolean addedIgnore = IgnoreList.toggleIgnore(sender.getName(), groupName);
	   
	   if (addedIgnore) {
		   sender.sendMessage("Added group \"" + groupName + "\" to ignore list!");
	   } else {
		   sender.sendMessage("Removed group \"" + groupName + "\" from ignore list!");
	   }
   }

    private void sendIgnoreGroupList(CommandSender sender) {
        Player player = (Player) sender;	
        
        Set<String> groupList = IgnoreList.GetGroupIgnoreListByPlayer(player.getName());
        StringBuilder sb = new StringBuilder();
        if (groupList == null) {
        	sb.append("* No Group Ignores *");
        } else {
        	for(String g : groupList) {
        		sb.append(g);
        		sb.append(", ");
        		
        		if (sb.length() > 100) {
        			sb.delete(101, sb.length());
        			sb.append("...  ");
        			break;
        		}
        	}
        	sb.delete(sb.length()-2, sb.length());
        }
        
        sb.insert(0,  "Ignore List:  ");
        
        sender.sendMessage(sb.toString());
    }
}
