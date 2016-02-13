package vg.civcraft.mc.namelayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.events.GroupCreateEvent;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.events.GroupTransferEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.misc.Mercury;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionHandler;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupManager{
	
	private static GroupManagerDao groupManagerDao;
	private PermissionHandler permhandle;
	
	private static Map<String, Group> groupsByName = new ConcurrentHashMap<String, Group>();
	private static Map<Integer, Group> groupsById = new ConcurrentHashMap<Integer, Group>();
	
	public GroupManager(){
		groupManagerDao = NameLayerPlugin.getGroupManagerDao();
		permhandle = new PermissionHandler();
	}
	
	/**
	 * Saves the group into caching and saves it into the db. Also fires the GroupCreateEvent.
	 * @param The group to create to db.
	 */
	public int createGroup(Group group){
		GroupCreateEvent event = new GroupCreateEvent(
				group.getName(), group.getOwner(),
				group.getPassword(), group.getType());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group create event was cancelled for group: " + group.getName());
			return -1;
		}
		int id = groupManagerDao.createGroup(
				event.getGroupName(), event.getOwner(), 
				event.getPassword(), event.getType());
		initiateDefaultPerms(event.getGroupName()); // give default perms to a newly create group
		return id;
	}
	
	public boolean deleteGroup(String groupName){
		groupName = groupName.toLowerCase();
		Group group = getGroup(groupName);
		
		GroupDeleteEvent event = new GroupDeleteEvent(group, false);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}
		
		group.prepareForDeletion();
		deleteGroupPerms(group);
		groupManagerDao.deleteGroup(groupName);
		groupsByName.remove(groupName);
		groupsById.remove(group.getGroupId());
		
		event = new GroupDeleteEvent(group, true); //TODO why a second event?
		Bukkit.getPluginManager().callEvent(event);
		
		group.setDisciplined(true);
		group.setValid(false);
		
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
				groupsByName.remove(toMerge.getName());
				groupsById.remove(toMerge.getGroupId());
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
	
	public static List<Group> getSubGroups(String name) {
		List<Group> groups = groupManagerDao.getSubGroups(name);
		for (Group group : groups) {
			groupsByName.put(group.getName().toLowerCase(), group);
			groupsById.put(group.getGroupId(), group);
		}
		return groups;
	}
	
	/*
	 * Making this static so I can use it in other places without needing the GroupManager Object.
	 * Saves me code so I can always grab a group if it is already loaded while not needing to check db.
	 */
	public static Group getGroup(String name){
		String lower = name.toLowerCase();
		if (groupsByName.containsKey(lower)) {
			return groupsByName.get(lower);
		} else { 
			Group group = groupManagerDao.getGroup(name);
			if (group != null) {
				groupsByName.put(lower, group);
				groupsById.put(group.getGroupId(), group);
			}
			return group;
		}
	}
		
	public static Group getGroup(int groupId){
		if (groupsById.containsKey(groupId)) {
			return groupsById.get(groupId);
		} else { 
			Group group = groupManagerDao.getGroup(groupId);
			if (group != null) {
				groupsByName.put(group.getName().toLowerCase(), group);
				groupsById.put(groupId, group);
			}
			return group;
		}
	}
	
	public static boolean hasGroup(String groupName) {
		return groupsByName.containsKey(groupName.toLowerCase());
	}
	
	/**
	 * Returns the admin group for groups if the group was found to be null.
	 * Good for when you have to have a group that can't be null.
	 * @param name - The group name for the group
	 * @return Either the group or the special admin group.
	 */
	public static Group getSpecialCircumstanceGroup(String name){
		String lower = name.toLowerCase();
		if (groupsByName.containsKey(lower)) {
			return groupsByName.get(lower);
		} else { 
			Group group = groupManagerDao.getGroup(name);
			if (group != null) {
				groupsByName.put(lower, group);
				groupsById.put(group.getGroupId(), group);
			} else {
				group = groupManagerDao.getGroup(NameLayerPlugin.getSpecialAdminGroup());
			}
			return group;
		}
	}
	
	public GroupPermission getPermissionforGroup(Group group){
		return permhandle.getGroupPermission(group);
	}
		
	public boolean hasAccess(String groupname, UUID player, PermissionType perm) {
		Group group = getGroup(groupname);
		if (group == null) {
			return false;
		}
		
		GroupPermission perms = getPermissionforGroup(group);
		PlayerType rank = group.getPlayerType(player);	
		
		return perms.isAccessible(rank, perm);
	}
			
	// == PERMISSION HANDLING ============================================================= //
	
	private void deleteGroupPerms(Group group){
		permhandle.deletePerms(group);
	}
	
	public List<String> getAllGroupNames(UUID uuid){
		return groupManagerDao.getGroupNames(uuid);
	}
	
	private void initiateDefaultPerms(String group){
		// for perms follow the order that they are in the enum PlayerType
		String[] perms = {"DOORS CHESTS", 
				"DOORS CHESTS BLOCKS MEMBERS CROPS", 
				"DOORS CHESTS BLOCKS MODS MEMBERS PASSWORD LIST_PERMS CROPS GROUPSTATS",
				"DOORS CHESTS BLOCKS ADMINS OWNER MODS MEMBERS PASSWORD SUBGROUP PERMS DELETE MERGE LIST_PERMS TRANSFER CROPS GROUPSTATS LINKING", 
				""};
		int x = 0;
		for (PlayerType role: PlayerType.values()){
			groupManagerDao.addPermission(group, role.name(), perms[x]);
			x++;
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
		Group g = groupsByName.get(group);
		if (g != null) {
			g.setValid(false);
			groupsByName.remove(group);
			for (Group x: groupsById.values())
				if (x.getName().equals(g.getName()))
				groupsById.remove(x.getGroupId());
		}
	}
	
	public int countGroups(UUID uuid){
		return groupManagerDao.countGroups(uuid);
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
		
		private final static Map<String, PlayerType> BY_NAME = Maps.newHashMap();
		
		static {
			for (PlayerType rank : values()) {
				BY_NAME.put(rank.name(), rank);
			}
		}
		
		public static PlayerType getPlayerType(String type){
			return BY_NAME.get(type.toUpperCase());
		}
		
		public static String getStringOfTypes() {
			StringBuilder ranks = new StringBuilder();
			for (String rank: BY_NAME.keySet()) {
				ranks.append(rank);
				ranks.append(" ");
			}
			return ranks.toString();
		}
		
		public static void displayPlayerTypes(Player p) {
			p.sendMessage(ChatColor.RED 
					+ "That PlayerType does not exists.\n"
					+ "The current types are: " + getStringOfTypes());
		}
		
		public static void displayPlayerTypesnllpt(Player p) {
			p.sendMessage(ChatColor.GREEN 
					+ "The current types are: " + getStringOfTypes()); 
			//dont yell at player for nllpt
		}
	}
}