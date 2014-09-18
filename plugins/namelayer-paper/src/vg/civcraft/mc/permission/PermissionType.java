package vg.civcraft.mc.permission;

/*
 * To add or remove perms add them to this list.
 * Then either modify or leave the default perms given to players
 * in the GroupManager class method initiateDefaultPerms()
 */
public enum PermissionType {

	DOORS, // Handles access to doors
	CHESTS, // Handles access to chests
	BLOCKS, // Handles access to breaking or remove blocks
	ADMINS, // Handles having access to adding or removing admins
	MODS, // Handles having access to adding or removing mods
	MEMBERS, // I hope you would know what this does
	PASSWORD // yep you guessed it, gives access to adding or removing passwords
}
