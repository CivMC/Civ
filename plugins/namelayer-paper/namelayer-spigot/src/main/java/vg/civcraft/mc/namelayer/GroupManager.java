package vg.civcraft.mc.namelayer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group create failed, caller passed in null", new Exception());
			return -1;
		}
		GroupCreateEvent event = new GroupCreateEvent(
				group.getName(), group.getOwner(),
				group.getPassword());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group create was cancelled for group: " + group.getName());
			return -1;
		}
		int id = groupManagerDao.createGroup(
				event.getGroupName(), event.getOwner(), 
				event.getPassword());
		if (id > -1) {
			initiateDefaultPerms(event.getGroupName()); // give default perms to a newly create group
			GroupManager.getGroup(id); // force a recache from DB.
			/*group.setGroupIds(groupManagerDao.getAllIDs(event.getGroupName()));
			group.addMember(event.getOwner(), PlayerType.OWNER);
			groupsByName.put(event.getGroupName(), group);
			for (int q : group.getGroupIds()) {
				groupsById.put(q, group);
			}*/
		}
		return id;
	}
	
	public boolean deleteGroup(String groupName){
		if (groupName == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group delete failed, caller passed in null", new Exception());
			return false;
		}
		groupName = groupName.toLowerCase();
		Group group = getGroup(groupName);
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group delete failed, failed to find group " + groupName);
			return false;
		}
		
		// Call once w/ finished false to allow cancellation.
		GroupDeleteEvent event = new GroupDeleteEvent(group, false);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group delete was cancelled for "+ groupName);
			return false;
		}
		
		// Unlinks subgroups.
		group.prepareForDeletion();
		deleteGroupPerms(group);
		groupManagerDao.deleteGroup(groupName);
		groupsByName.remove(groupName);
		for (int id : group.getGroupIds()) {
			groupsById.remove(id);
		}
		
		// Call after actual delete to alert listeners that we're done.
		event = new GroupDeleteEvent(group, true);
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
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group transfer failed, caller passed in null", new Exception());
			return;
		}

		GroupTransferEvent event = new GroupTransferEvent(g, uuid);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group transfer event was cancelled for group: " + g.getName());
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
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group merge failed, caller passed in null", new Exception());
			return;
		} else if (group == toMerge || group.getName().equalsIgnoreCase(toMerge.getName())) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group merge failed, caller passed in null", new Exception());
			return;
		}
		GroupMergeEvent event = new GroupMergeEvent(group, toMerge, false);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group merge event was cancelled for groups: " +
					group.getName() + " and " + toMerge.getName());
			return;
		}

		groupManagerDao.mergeGroup(group.getName(), toMerge.getName());
		// At this point, at the DB level all non-overlap members are in target group, name is reset to target,
		// unique group header record is removed, and faction_id all point to new name.
		/*deleteGroup(toMerge.getName());
		// b/c cache isn't clear yet, this works. 
		// Delete the actual subgroup links from old group
		// Uncaches permissions
		// Attempts to "delete" group (move to special) but can't b/c name is already removed.
		//   Consequently the subgroups, permissions, and blacklist will be unaltered.
		// Removes toMerge from cache.
		// "Disciplines" the memory-copy and sets to invalid.
		 */
		// deleteGroup was wasteful. Pulled the only things it did of value out and put them here:
		if (toMerge.getSuperGroup() != null) {
			Group sup = toMerge.getSuperGroup();
			Group.unlink(sup, toMerge); // need to unlink any supergroup from merge.
			invalidateCache(sup.getName());
		}
		// Merge brings subgroups with, but unlinks the toMerge group out from under any supergroup it had.
		//toMerge.prepareForDeletion();
		for (Group subMerge : toMerge.getSubgroups()) {
			Group.link(group, subMerge, false);
			invalidateCache(subMerge.getName());
		}
		deleteGroupPerms(toMerge);
		toMerge.setDisciplined(true);
		invalidateCache(toMerge.getName()); // Removes merge group from cache & invalidates object
		invalidateCache(group.getName()); // Means next access will requery the group, good.
		
		event = new GroupMergeEvent(group, toMerge, true);
		Bukkit.getPluginManager().callEvent(event);
		//toMerge.setDisciplined(true); // duplicate action, toMerge is set to discipline by deleteGroup()
		// Fail safe for plugins that don't check if the group is valid or not.
		if (NameLayerPlugin.isMercuryEnabled()){
			String message = "merge " + group.getName() + " " + toMerge.getName();
			Mercury.invalidateGroup(message);
		}
	}
	
	public static List<Group> getSubGroups(String name) {
		if (name == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group getSubGroups event failed, caller passed in null", new Exception());
			return new ArrayList<Group>();
		}

		List<Group> groups = groupManagerDao.getSubGroups(name);
		for (Group group : groups) {
			groupsByName.put(group.getName().toLowerCase(), group);
			for (int j : group.getGroupIds()){
				groupsById.put(j, group);
			}
		}
		return groups;
	}
	
	/*
	 * Making this static so I can use it in other places without needing the GroupManager Object.
	 * Saves me code so I can always grab a group if it is already loaded while not needing to check db.
	 */
	public static Group getGroup(String name){
		if (name == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getGroup failed, caller passed in null", new Exception());
			return null;
		}
		
		String lower = name.toLowerCase();
		if (groupsByName.containsKey(lower)) {
			return groupsByName.get(lower);
		} else { 
			Group group = groupManagerDao.getGroup(name);
			if (group != null) {
				groupsByName.put(lower, group);
				for (int j : group.getGroupIds()){
					groupsById.put(j, group);
				}
			} else {
				NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getGroup by Name failed, unable to find the group " + name);
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
				for (int j : group.getGroupIds()){
					groupsById.put(j, group);
				}
			} else {
				NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getGroup by ID failed, unable to find the group " + groupId);
			}
			return group;
		}
	}
	
	public static boolean hasGroup(String groupName) {
		if (groupName == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "HasGroup Name failed, name was null ", new Exception());
			return false;
		}

		if (!groupsByName.containsKey(groupName.toLowerCase())) {
			return (getGroup(groupName.toLowerCase()) != null);
		} else {
			return true;
		}
	}
	
	/**
	 * Returns the admin group for groups if the group was found to be null.
	 * Good for when you have to have a group that can't be null.
	 * @param name - The group name for the group
	 * @return Either the group or the special admin group.
	 */
	public static Group getSpecialCircumstanceGroup(String name){
		if (name == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getSpecialCircumstance failed, caller passed in null", new Exception());
			return null;
		}
		String lower = name.toLowerCase();
		if (groupsByName.containsKey(lower)) {
			return groupsByName.get(lower);
		} else { 
			Group group = groupManagerDao.getGroup(name);
			if (group != null) {
				groupsByName.put(lower, group);
				for (int j : group.getGroupIds()){
					groupsById.put(j, group);
				}
			} else {
				group = groupManagerDao.getGroup(NameLayerPlugin.getSpecialAdminGroup());
			}
			return group;
		}
	}
	
	/**
	 * DO NOT WORK WITH THE PERMISSION OBJECT ITSELF TO DETERMINE ACCESS. Use the methods provided in this class instead, as they
	 * respect all the permission inheritation stuff
	 */
	public GroupPermission getPermissionforGroup(Group group){
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getPermissionForGroup failed, caller passed in null", new Exception());
			return null;
		}
		return permhandle.getGroupPermission(group);
	}
		
	public boolean hasAccess(String groupname, UUID player, PermissionType perm) {
		if (groupname == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "hasAccess failed (access denied), could not find group " + groupname);
			return false;
		}
		return hasAccess(getGroup(groupname), player, perm);
	}
	
	public boolean hasAccess(Group group, UUID player, PermissionType perm) {
		Player p = Bukkit.getPlayer(player);
		if (p != null && (p.isOp() || p.hasPermission("namelayer.admin"))) {
			return true;
		}
		if (group == null || player == null || perm == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "hasAccess failed, caller passed in null", new Exception());
			return false;
		}
		if (!group.isValid()) {
			group = getGroup(group.getName());
			if (group == null) {
				//what happened? who knows?
				return false;
			}
		}
		GroupPermission perms = getPermissionforGroup(group);
		for(PlayerType rank : getRecursivePlayerTypes(group, player)) {
			if (perms.hasPermission(rank, perm)) {
				//player has right rank in the group itself or at least one super group
				return true;
			}
		}		
		return false;
	}
	
	private List<PlayerType> getRecursivePlayerTypes(Group group, UUID player) {
		List<PlayerType> perms = new LinkedList<PlayerType>();
		PlayerType type = group.getPlayerType(player);
		if (type != null) {
			perms.add(type);
		}
		if (group.hasSuperGroup()) {
			perms.addAll(getRecursivePlayerTypes(group.getSuperGroup(), player));
		}
		return perms;
	}
			
	// == PERMISSION HANDLING ============================================================= //
	
	private void deleteGroupPerms(Group group){
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "deleteGroupPerms failed, caller passed in null", new Exception());
			return;
		}
		permhandle.deletePerms(group);
	}
	
	public List<String> getAllGroupNames(UUID uuid){
		if (uuid == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getAllGroupNames failed, caller passed in null", new Exception());
			return new ArrayList<String>();
		}
		return groupManagerDao.getGroupNames(uuid);
	}
	
	private void initiateDefaultPerms(String group){
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "initiateDefaultPerms failed, caller passed in null", new Exception());
			return;
		}
		Map <PlayerType, List <PermissionType>> defaultPermMapping = new HashMap<GroupManager.PlayerType, List<PermissionType>>();
		for(PermissionType perm : PermissionType.getAllPermissions()) {
			for(PlayerType type : perm.getDefaultPermLevels()) {
				List <PermissionType> perms = defaultPermMapping.get(type);
				if (perms == null) {
					perms = new LinkedList<PermissionType>();
					defaultPermMapping.put(type, perms);
				}
				perms.add(perm);
			}
		}
		for (Entry <PlayerType, List <PermissionType>> entry: defaultPermMapping.entrySet()){
			groupManagerDao.addPermission(group, entry.getKey().name(), entry.getValue());
		}
	}
	
	public String getDefaultGroup(UUID uuid){
		if (uuid == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getDefaultGroup was cancelled, caller passed in null", new Exception());
			return null;
		}
		return NameLayerPlugin.getDefaultGroupHandler().getDefaultGroup(uuid);
	}

	/**
	 * Invalidates a group from cache.
	 * @param group
	 */
	public void invalidateCache(String group){
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "invalidateCache failed, caller passed in null", new Exception());
			return;
		}

		Group g = groupsByName.get(group.toLowerCase());
		if (g != null) {
			g.setValid(false);
			List<Integer>k = g.getGroupIds();
			groupsByName.remove(group.toLowerCase());
			NameLayerPlugin.getBlackList().removeFromCache(g.getName());
			
			boolean fail = true;
			// You have a freaking hashmap, use it.
			for (int j : k) {
				if (groupsById.remove(j) != null) {
					fail = false;
				}
			}
			
			// FALLBACK is hardloop
			if (fail) { // can't find ID or cache is wrong.
				for (Group x: groupsById.values()) {
					if (x.getName().equals(g.getName())) {
						groupsById.remove(x.getGroupId());
					}
				}
			}
		} else {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Invalidate cache by name failed, unable to find the group " + group);			
		}
	}
	
	public int countGroups(UUID uuid){
		if (uuid == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "countGroups failed, caller passed in null", new Exception());
			return 0;
		}
		return groupManagerDao.countGroups(uuid);
	}
	
	public Timestamp getTimestamp(String group){
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "getTimeStamp failed, caller passed in null", new Exception());
			return null; 
		}

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
		NOT_BLACKLISTED;//anyone, who is not blacklisted
		
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
		
		public static PlayerType getByID(int id) {
			switch(id) {
			case 0:
				return PlayerType.NOT_BLACKLISTED;
			case 1:
				return PlayerType.MEMBERS;
			case 2:
				return PlayerType.MODS;
			case 3:
				return PlayerType.ADMINS;
			case 4:
				return PlayerType.OWNER;
			default:
				return null;
			}
		}
		
		public static int getID(PlayerType type) {
			if (type == null) {
				return -1;
			}
			switch (type) {
				case NOT_BLACKLISTED:
					return 0;
				case MEMBERS:
					return 1;
				case MODS:
					return 2;
				case ADMINS:
					return 3;
				case OWNER:
					return 4;
				default:
					return -1;
			}
		}
		
		public static String getNiceRankName(PlayerType pType) {
			if (pType == null) {
				return "RANK_ERROR";
			}
			switch (pType) {
			case MEMBERS:
				return "Member";
			case MODS:
				return "Mod";
			case ADMINS:
				return "Admin";
			case OWNER:
				return "Owner";
			case NOT_BLACKLISTED:
				return "Anyone who is not blacklisted";
			}
			return "RANK_ERROR";
		}
	}
}