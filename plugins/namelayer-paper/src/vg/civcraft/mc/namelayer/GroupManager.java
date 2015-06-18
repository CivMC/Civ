package vg.civcraft.mc.namelayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.events.GroupCreateEvent;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.events.GroupTransferEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.misc.Mercury;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionHandler;

public class GroupManager{
	private static GroupManagerDao groupManagerDao;
	private PermissionHandler permhandle;
	
	public GroupManager(){
		groupManagerDao = NameLayerPlugin.getGroupManagerDao();
		permhandle = new PermissionHandler();
	}
	
	private static Map<String, Group> groups = new HashMap<String, Group>();
	/**
	 * Saves the group into caching and saves it into the db. Also fires the GroupCreateEvent.
	 * @param The group to create to db.
	 */
	public int createGroup(Group group){
		GroupCreateEvent event = new GroupCreateEvent(group.getName(), group.getOwner(),
				group.getPassword(), group.getType());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group create event was cancelled for group: " + group.getName());
			return -1;
		}
		int id = groupManagerDao.createGroup(event.getGroupName(), event.getOwner(), 
				event.getPassword(), event.getType());
		initiateDefaultPerms(event.getGroupName()); // give default perms to a newly create group
		return id;
	}
	
	public boolean deleteGroup(String groupName){
		groupName = groupName.toLowerCase();
		Group g = getGroup(groupName);
		GroupDeleteEvent event = new GroupDeleteEvent(g, false);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		deleteGroupPerms(groups.get(groupName));
		groupManagerDao.deleteGroup(groupName);
		groups.remove(groupName);
		event = new GroupDeleteEvent(g, true);
		Bukkit.getPluginManager().callEvent(event);
		g.setDisciplined(true);
		g.setValid(false);
		if (NameLayerPlugin.isMercuryEnabled()){
			String message = "delete " + groupName;
			Mercury.invalidateGroup(message);
		}
		return true;
	}
	
	public void transferGroup(Group g, UUID uuid){
		GroupTransferEvent event = new GroupTransferEvent(g, uuid);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group transfer event was cancelled for group: " 
		+ g.getName());
			return;
		}
		g.addMember(uuid, PlayerType.OWNER);
		g.setOwner(uuid);
		if (NameLayerPlugin.isMercuryEnabled()){
			String message = "transfer " + g.getName();
			Mercury.invalidateGroup(message);
		}
	}
	
	public void mergeGroup(final Group group, final Group toMerge){
		GroupMergeEvent event = new GroupMergeEvent(group, toMerge, false);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group merge event was cancelled for groups: " +
					group.getName() + " and " + toMerge.getName());
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new Runnable(){

			@Override
			public void run() {
				for (PlayerType type: PlayerType.values())
					for (UUID uuid: toMerge.getAllMembers(type))
						if (!group.isMember(uuid))
							group.addMember(uuid, type);
				groups.remove(toMerge.getName());
			}
			
		});
		groupManagerDao.mergeGroup(group.getName(), toMerge.getName());
		deleteGroup(toMerge.getName());
		event = new GroupMergeEvent(group, toMerge, true);
		Bukkit.getPluginManager().callEvent(event);
		toMerge.setDisciplined(true);
		// Fail safe for plugins that dont check if the group is valid or not.
		if (NameLayerPlugin.isMercuryEnabled()){
			String message = "merge " + group.getName() + " " + toMerge.getName();
			Mercury.invalidateGroup(message);
		}
	}
	
	/*
	 * Making this static so I can use it in other places without needing the GroupManager Object.
	 * Saves me code so I can always grab a group if it is already loaded while not needing to check db.
	 */
	public static Group getGroup(String groupName){
		groupName = groupName.toLowerCase();
		if (groups.containsKey(groupName))
			return groups.get(groupName);
		else{ 
			Group group;
			group = groupManagerDao.getGroup(groupName);
			if (group != null)
				groups.put(groupName, group);
			return group;
		}
	}
	/**
	 * Returns the admin group for groups if the group was found to be null.
	 * Good for when you have to have a group that can't be null.
	 * @param The group name for the group
	 * @return Either the group or the special admin group.
	 */
	public static Group getSpecialCircumstanceGroup(String groupName){
		if (groups.containsKey(groupName))
			return groups.get(groupName);
		else{ 
			Group group;
			group = groupManagerDao.getGroup(groupName);
			if (group != null)
				groups.put(groupName, group);
			else
				group = groupManagerDao.getGroup(NameLayerPlugin.getSpecialAdminGroup());
			return group;
		}
	}
	
	public GroupPermission getPermissionforGroup(Group group){
		return permhandle.getGroupPermission(group);
	}
	
	private void deleteGroupPerms(Group group){
		permhandle.deletePerms(group);
	}
	
	public List<String> getAllGroupNames(UUID uuid){
		return groupManagerDao.getGroupNames(uuid);
	}
	
	private void initiateDefaultPerms(String group){
		// for perms follow the order that they are in the enum PlayerType
		String[] perms = {"DOORS CHESTS", "DOORS CHESTS BLOCKS MEMBERS CROPS", "DOORS CHESTS BLOCKS MODS MEMBERS PASSWORD LIST_PERMS CROPS GROUPSTATS",
				"DOORS CHESTS BLOCKS ADMINS OWNER MODS MEMBERS PASSWORD SUBGROUP PERMS DELETE MERGE LIST_PERMS TRANSFER CROPS GROUPSTATS", ""};
		int x = 0;
		for (PlayerType role: PlayerType.values()){
			groupManagerDao.addPermission(group, role.name(), perms[x]);
			x++;
		}
	}
	
	/**
	 * In ascending order
	 * Add an enum here if you wish to add more than the four default tiers of
	 * roles.
	 */
	public enum PlayerType{
		MEMBERS,
		MODS,
		ADMINS,
		OWNER,
		SUBGROUP;// The perm players get when they are added from a super group.
		
		public static PlayerType getPlayerType(String type){
			PlayerType pType = null;
			try{
				pType = PlayerType.valueOf(type.toUpperCase());
			} catch(IllegalArgumentException ex){}
			return pType;
		}
		
		public static void displayPlayerTypes(Player p){
			String types = "";
			for (PlayerType type: PlayerType.values())
				types += type.name() + " ";
			p.sendMessage(ChatColor.RED +"That PlayerType does not exists.\n" +
					"The current types are: " + types);
		}
		
		public static void displayPlayerTypesnllpt(Player p){
			String types = "";
			for (PlayerType type: PlayerType.values())
				types += type.name() + " ";
			p.sendMessage(ChatColor.GREEN + "The current types are: " + types); //dont yell at player for nllpt
		}
		
		public static String toStringName(){
			String x = "";
			for (PlayerType name: PlayerType.values())
				x += name.name() + " ";
			return x;
		}
	}
	
	public String getDefaultGroup(UUID uuid){
		return groupManagerDao.getDefaultGroup(uuid);
	}
	/**
	 * Invalidates a group from cache.
	 * @param group
	 */
	public void invalidateCache(String group){
		groups.get(group).setValid(false);
		groups.remove(group);
	}
}