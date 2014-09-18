package vg.civcraft.mc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import vg.civcraft.mc.database.SaveManager;
import vg.civcraft.mc.events.GroupCreateEvent;
import vg.civcraft.mc.events.GroupDeleteEvent;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.group.GroupType;
import vg.civcraft.mc.permission.GroupPermission;
import vg.civcraft.mc.permission.PermissionHandler;

public class GroupManager{
	private static SaveManager groupManagerDao = NameTrackerPlugin.getSaveManager();
	private PermissionHandler permhandle = new PermissionHandler();
	
	private static Map<String, Group> groups = new HashMap<String, Group>();
	
	public void createGroup(Group group){
		GroupCreateEvent event = new GroupCreateEvent(group.getName(), group.getOwner(),
				group.getPassword(), group.getType());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameTrackerPlugin.log(Level.INFO, "Group create event was cancelled for group: " + group.getName());
			return;
		}
		groupManagerDao.createGroup(event.getGroupName(), event.getOwner(), 
				event.getPassword(), event.getType());
		initiateDefaultPerms(event.getGroupName()); // give default perms to a newly create group
	}
	
	public boolean deleteGroup(String groupName){
		GroupDeleteEvent event = new GroupDeleteEvent(groupName);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		deleteGroupPerms(groups.get(groupName));
		groupManagerDao.deleteGroup(groupName);
		groups.remove(groupName);
		return true;
	}
	
	/*
	 * Making this static so I can use it in other places without needing the GroupManager Object.
	 * Saves me code so I can always grab a group if it is already loaded while not needing to check db.
	 */
	public static Group getGroup(String groupName){
		Group group = null;
		if (!groups.containsKey(groupName))
			group = groupManagerDao.getGroup(groupName);
		else{ 
			group = groupManagerDao.getGroup(groupName);
			if (group != null)
				groups.put(groupName, group);
		}
		return group;
	}
	
	public GroupPermission getPermissionforGroup(Group group){
		return permhandle.getGroupPermission(group);
	}
	
	private void deleteGroupPerms(Group group){
		permhandle.deletePerms(group);
	}
	
	private void initiateDefaultPerms(String group){
		// for perms follow the order that they are in the enum PlayerType
		String[] perms = {"DOORS CHESTS", "DOORS CHESTS BLOCKS", "DOORS CHESTS BLOCKS MODS MEMBERS PASSWORD",
				"DOORS CHESTS BLOCKS ADMINS MODS MEMBERS PASSWORD"};
		int x = 0;
		for (PlayerType role: PlayerType.values()){
			groupManagerDao.addPermission(group, role.name(), perms[x]);
			x++;
		}
	}
	
	/*
	 * In ascending order
	 * Add an enum here if you wish to add more than the four default tiers of
	 * roles.
	 */
	public enum PlayerType{
		MEMBER,
		MOD,
		ADMIN,
		OWNER;
	}
}