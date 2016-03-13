package vg.civcraft.mc.namelayer.permission;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

/*
 * To add or remove perms add them to this list.
 * Then either modify or leave the default perms given to players
 * in the GroupManager class method initiateDefaultPerms()
 * 
 * Also remember that you need to modify the database to
 * add the new permission type to the owners so people can
 * actually modify the groups.
 */
public enum PermissionType {

	DOORS, // Handles access to doors
	CHESTS, // Handles access to chests
	BLOCKS, // Handles access to breaking or remove blocks
	ADMINS, // Handles having access to adding or removing admins
	MODS, // Handles having access to adding or removing mods
	MEMBERS, // I hope you would know what this does
	OWNER, // ...
	PASSWORD, // yep you guessed it, gives access to adding or removing passwords
	SUBGROUP, // Add subgroup
	PERMS, // Have control to modify permissions
	DELETE, // Delete the current group
	JOIN_PASSWORD, // Give this permission to the PlayerType you want to give players when they join with a password
	MERGE, // Gives the player permission to merge the group.
	LIST_PERMS, // Allows the player to use the command to list the perms of a PlayerType
	TRANSFER, // Allows the player to transfer the group
	CROPS, // Allows access to crops, mainly used for citadel.
	GROUPSTATS, //Allows access to nlgs command for group
	LINKING; // Allows linking and unlinking of super groups and subgroups
	
	private final static Map<String, PermissionType> BY_NAME = Maps.newHashMap();
	
	static {
		for (PermissionType perm : values()) {
			BY_NAME.put(perm.name(), perm);
		}
	}
		
	public static PermissionType getPermissionType(String type){
		return BY_NAME.get(type);
	}
	
	public static String getStringOfTypes() {
		StringBuilder perms = new StringBuilder();
		for (String perm: BY_NAME.keySet()) {
			perms.append(perm);
			perms.append(" ");
		}
		return perms.toString();
	}
	
	public static void displayPermissionTypes(Player p) {
		p.sendMessage(ChatColor.RED 
				+ "That PermissionType does not exists.\n"
				+ "The current types are: " + getStringOfTypes());
	}
}
