package vg.civcraft.mc.permission;

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
	JOIN_PASSWORD; // Give this permission to the PlayerType you want to give players when they join with a password
}
