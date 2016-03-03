package vg.civcraft.mc.namelayer;

import java.sql.Timestamp;
import java.util.ArrayList;
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
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group create event was cancelled, caller passed in null", new Exception());
			return -1;
		}
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
		if (id > -1) {
			initiateDefaultPerms(event.getGroupName()); // give default perms to a newly create group
		}
		return id;
	}
	
	public boolean deleteGroup(String groupName){
		if (groupName == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group delete event was cancelled, caller passed in null", new Exception());
			return false;
		}
		groupName = groupName.toLowerCase();
		Group group = getGroup(groupName);
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group delete event was cancelled, failed to find group", new Exception());
			return false;
		}
		
		GroupDeleteEvent event = new GroupDeleteEvent(group, false);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}
		
		group.prepareForDeletion();
		deleteGroupPerms(group);
		groupManagerDao.deleteGroup(groupName);
		// TODO: consistent handling of name/id pairs here too
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
		if (g == null || uuid == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group transfer event was cancelled, caller passed in null", new Exception());
			return;
		}

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
		if (group == null || toMerge == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group merge event was cancelled, caller passed in null", new Exception());
			return;
		}
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
				// TODO: Clean this up and in general make consistent w/ decided approach
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
		if (name == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group getSubGroups event was cancelled, caller passed in null", new Exception());
			return new ArrayList<Group>();
		}

		List<Group> groups = groupManagerDao.getSubGroups(name);
		for (Group group : groups) {
			// TODO: propogate every subgroup instance to every supergroup instance of the same name.
			groupsByName.put(group.getName().toLowerCase(), group);
			groupsById.put(group.getGroupId(), group);
		}
		return groups;
	}
	
	/*
	 * Making this static so I can use it in other places without needing the GroupManager Object.
	 * Saves me code so I can always grab a group if it is already loaded while not needing to check db.
	 */
	// TODO: Deal with cache as a list. Deprecate this? Or deal with group ID inside the object?
	public static Group getGroup(String name){
		if (name == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group transfer event was cancelled, caller passed in null", new Exception());
			return null;
		}
		
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
				// TODO: Whatever we decide, these should adhere.
				groupsByName.put(group.getName().toLowerCase(), group);
				groupsById.put(groupId, group);
			}
			return group;
		}
	}
	
	public static boolean hasGroup(String groupName) {
		if (groupName == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "HasGroup Name been cancelled? ", new Exception());
			return false;
		}

		return groupsByName.containsKey(groupName.toLowerCase());
	}
	
	/**
	 * Returns the admin group for groups if the group was found to be null.
	 * Good for when you have to have a group that can't be null.
	 * @param name - The group name for the group
	 * @return Either the group or the special admin group.
	 */
	public static Group getSpecialCircumstanceGroup(String name){
		if (g == name || uuid == name) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group transfer event was cancelled, caller passed in null", new Exception());
			return;
		}
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
		if (groupname == null || player == null || perm == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group transfer event was cancelled, caller passed in null", new Exception());
			return false;
		}

		Group group = getGroup(groupname);
		if (group == null) {
			return false;
		}
		
		GroupPermission perms = getPermissionforGroup(group);
		PlayerType rank = group.getPlayerType(player);	
		
		return perms.isAccessible(rank, perm);
	}
			
	// == PERMISSION HANDLING ============================================================= //
	
	// TODO: more nullschecks.
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
		Group g = groupsByName.get(group.toLowerCase());
		if (g != null) {
			g.setValid(false);
			int k = g.getGroupId();
			groupsByName.remove(group.toLowerCase());
			
			// You have a freaking hashmap, use it.
			Group q = groupsById.get(k);
			if (q != null) {
				if (q.getName().equals(g.getName())) {
					groupsById.remove(k);
				} else {
					q = null;
				}
			}
			
			// FALLBACK is hardloop
			if (q == null) { // can't find ID or cache is wrong.
				for (Group x: groupsById.values()) {
					if (x.getName().equals(g.getName())) {
						groupsById.remove(x.getGroupId());
					}
				}
			}
		}
	}
	
	public int countGroups(UUID uuid){
		return groupManagerDao.countGroups(uuid);
	}
	
	public Timestamp getTimestamp(String group){
		return groupManagerDao.getTimestamp(group);
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