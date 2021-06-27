package vg.civcraft.mc.namelayer.command;

import co.aikar.commands.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public abstract class PlayerCommandMiddle extends BaseCommand {

	public PlayerCommandMiddle(String name) {
		super(name);
	}

	protected GroupManager gm = NameAPI.getGroupManager();
	
	protected boolean groupIsNull(CommandSender sender, String groupname, Group group) {
	    if (group == null) {
	        sender.sendMessage(String.format(
	                "%sThe group \"%s\" does not exist.", 
	                ChatColor.RED, groupname));
	        return true;
	    }
	    return false;
	}
	}
