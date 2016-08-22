package vg.civcraft.mc.namelayer.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.misc.Mercury;

public abstract class PlayerCommandMiddle extends PlayerCommand{

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
	
	public void checkRecacheGroup(Group g){
		if (NameLayerPlugin.isMercuryEnabled()){
			String message = "recache " + g.getName();
			Mercury.invalidateGroup(message);
		}
	}
}
