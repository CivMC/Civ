package vg.civcraft.mc.namelayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionHandler;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupManager{
	
	private static GroupManagerDao groupManagerDao;
	private PermissionHandler permhandle;
	
	private static Map<String, Group> groupsByName = new ConcurrentHashMap<>();
	private static Map<Integer, Group> groupsById = new ConcurrentHashMap<>();
	
	private static boolean mergingInProgress = false;
	
	public GroupManager(){
		groupManagerDao = NameLayerPlugin.getGroupManagerDao();
		permhandle = new PermissionHandler();
	}
	
	/**
	 * Saves the group into caching and saves it into the db. Also fires the GroupCreateEvent.
	 * @param group the group to create to db.
	 * @return the internal ID of the group created.
	 */
	public int createGroup(Group group){
		return createGroup(group,true);
	}
	
	/**
	 * This will create a group asynchronously. Always saves to database. Pass in a Runnable of type RunnableOnGroup that 
	 * specifies what to run <i>synchronously</i> after the insertion of the group. Your runnable should handle the case where
	 * id = -1 (failure).
	 * 
	 * Note that internally, we setGroupId on the RunnableOnGroup; your run() method should use getGroupId() to retrieve it 
	 * and react to it.
	 * 
	 * @param group the Group placeholder to use in creating a group. Calls GroupCreateEvent synchronously, then insert the
	 *    group asynchronously, then calls the RunnableOnGroup synchronously.
	 * @param postCreate The RunnableOnGroup to run after insertion (whether successful or not!)
	 * @param checkBeforeCreate Checks if the group already exists (asynchronously) prior to creating it. Runs the CreateEvent
	 *    synchronously, then behaves as normal after that (running async create).
	 */
	public void createGroupAsync(final Group group, final RunnableOnGroup postCreate, boolean checkBeforeCreate) {
		if (group == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group create failed, caller passed in null", new Exception());
			postCreate.setGroup(new Group(null, null, true, null, -1, System.currentTimeMillis()));
			Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), postCreate);
		} else {
			if (checkBeforeCreate) {
				// Run check asynchronously. 
				Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new Runnable() {
						@Override
						public void run() {
							// So.... when you `new Group` it makes a ton of DB calls and gets ids and members. If the group exists, ID at this point will be > -1...
							if (group.getGroupId() == -1) {// || getGroup(group.getName()) == null) {
								// group doesn't exist, so schedule create.
								Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), new Runnable() {
										@Override
										public void run() {
											doCreateGroupAsync(group, postCreate);
										}
								});
							} else {
								// group does exist, so run postCreate with failure.
								NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group create failed, group {0} already exists", group.getName());
								postCreate.setGroup(new Group(null, null, true, null, -1, System.currentTimeMillis()));
								Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), postCreate);								
							}
						}
					});
				
			} else {
				doCreateGroupAsync(group, postCreate);
			}
		}
	}
	
	private void doCreateGroupAsync(final Group group, final RunnableOnGroup postCreate) {
		GroupCreateEvent event = new GroupCreateEvent(
				group.getName(), group.getOwner(),
				group.getPassword());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group create was cancelled for group: " + group.getName());
			postCreate.setGroup(new Group(group.getName(), group.getOwner(), true, group.getPassword(), -1, System.currentTimeMillis()));
			Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), postCreate);
		}
		final String name = event.getGroupName();
		final UUID owner = event.getOwner();
		final String password = event.getPassword();
		NameLayerPlugin.getBlackList().initEmptyBlackList(name);
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				int id = internalCreateGroup(group, true, name, owner, password);
				NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Async group create finished for group {0}, id assigned: {1}",
						new Object[]{name, id});
				Group g = GroupManager.getGroup(id);
				postCreate.setGroup(g);
				Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), postCreate);
			}
		});
	}
	
	public int createGroup(Group group, boolean savetodb){
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
		return internalCreateGroup(group, savetodb, event.getGroupName(), event.getOwner(), event.getPassword());
	}
	
	private int internalCreateGroup(Group group, boolean savetodb, String name, UUID owner, String password) {
		int id;
		if (savetodb){
			id = groupManagerDao.createGroup(name, owner, password);
			if (id > -1) {
				initiateDefaultPerms(id); // give default perms to a newly create group
				GroupManager.getGroup(id); // force a recache from DB.
			}
		} else {
			id = group.getGroupId();
		}
		return id;
	}
	
	public boolean deleteGroup(String groupName){
		return deleteGroup(groupName,true);
	}
	
	public boolean deleteGroup(String groupName, boolean savetodb){
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
		groupsByName.remove(group.getName());
		for (int id : group.getGroupIds()) {
			groupsById.remove(id);
		}
		
		// Call after actual delete to alert listeners that we're done.
		event = new GroupDeleteEvent(group, true);
		Bukkit.getPluginManager().callEvent(event);
		
		group.setDisciplined(true);
		group.setValid(false);
		if (savetodb){
			groupManagerDao.deleteGroup(groupName);
		}
		return true;
	}
	
	public void transferGroup(Group g, UUID uuid){
		transferGroup(g,uuid,true);
	}
	
	public void transferGroup(Group g, UUID uuid, boolean savetodb){
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
		if (savetodb){
			g.addMember(uuid, PlayerType.OWNER);
			g.setOwner(uuid);
		} else {
			g.addMember(uuid, PlayerType.OWNER, false);
			g.setOwner(uuid, false);
		}
	}

	/**
	 * Merging is initiated asynchronously on the shard the player currently inhabits. Due to the complexity of keeping
	 * the cache consistent, we're whiffing on this one a bit and _for now_ simply invalidating the cache on servers.
	 *
	 * Eventually, we'll need to go line-by-line through the db code and just replicate in cache. That day is not today.
	 *
	 * @param group the origin group
	 * @param toMerge the group to merge in
	 */
	public void doneMergeGroup(Group group, Group toMerge) {
		if (group == null || toMerge == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group merge failed, caller passed in null", new Exception());
			return;
		}

		// Merge brings subgroups with, but unlinks the toMerge group out from under any supergroup it had.
		// This doesn't update the database but simply updates all _impacted_ groups in cache. The database is
		// already updated for this.
		for (Group subMerge : toMerge.getSubgroups()) {
			Group.link(group, subMerge, false);
		}

		GroupMergeEvent event = new GroupMergeEvent(group, toMerge, true);
		Bukkit.getPluginManager().callEvent(event);

		// Then invalidate. Updating the cache was proving unreliable; we'll address it later.
		GroupManager.invalidateCache(group.getName());
		GroupManager.invalidateCache(toMerge.getName());
	}

	public void mergeGroup(Group group, Group to){
		mergeGroup(group,to,true);
	}
	
	public void mergeGroup(final Group group, final Group toMerge, boolean savetodb){
		if (group == null || toMerge == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group merge failed, caller passed in null", new Exception());
			return;
		} else if (group == toMerge || group.getName().equalsIgnoreCase(toMerge.getName())) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group merge failed, can't merge the same group into itself", new Exception());
			return;
		}
		GroupMergeEvent event = new GroupMergeEvent(group, toMerge, false);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			NameLayerPlugin.log(Level.INFO, "Group merge event was cancelled for groups: " +
					group.getName() + " and " + toMerge.getName());
			return;
		}
		group.isValid();
		group.setDisciplined(true, false);
		toMerge.setDisciplined(true, false);
		
		if (savetodb){
			// This basically just fires starting events and disciplines groups on target server.
			// They then wait for merge to complete. Botched merges will lock groups, basically. :shrug:

			NameLayerPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(
					NameLayerPlugin.getInstance(), new Runnable(){

				@Override
				public void run() {
					groupManagerDao.mergeGroup(group.getName(), toMerge.getName());
					// At this point, at the DB level all non-overlap members are in target group, name is reset to target,
					// unique group header record is removed, and faction_id all point to new name.

					// We handle supergroup right here right now; does its own mercury message to update in cache.
					if (toMerge.getSuperGroup() != null) {
						Group sup = toMerge.getSuperGroup();
						Group.unlink(sup, toMerge); 
						// The above handles the need to unlink any supergroup from merge in DB.
					}

					// Subgroup update is handled in doneMerge, as its a cache-only update.

					deleteGroupPerms(toMerge); // commit perm updates to DB.

					doneMergeGroup(group, toMerge);
				}
			});
		}
	}
	
	public static List<Group> getSubGroups(String name) {
		if (name == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "Group getSubGroups event failed, caller passed in null", new Exception());
			return new ArrayList<>();
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
	 * 
	 * @param group the group to retrieve permissions from
	 * @return the actual permissions for this object or null
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
		if (group == null || perm == null) {
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
		return hasPlayerInheritsPerms(group, player, perm);
	}

	/**
	 * Checks if a player has a permission in a group or one of its parent groups
	 * @param group the group, and its parents etc to check
	 * @param player the player
	 * @param perm the permission to check
	 * @return if the player has the specified permission in a group or one of its parents
	 */
	private boolean hasPlayerInheritsPerms(Group group, UUID player, PermissionType perm) {
		while (group != null) {
			PlayerType type = group.getPlayerType(player);
			if (type != null && getPermissionforGroup(group).hasPermission(type, perm)) {
				return true;
			}
			group = group.getSuperGroup();
		}
		return false;
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
			return new ArrayList<>();
		}
		return groupManagerDao.getGroupNames(uuid);
	}
	
	private void initiateDefaultPerms(Integer groupId){
		if (groupId == null) {
			NameLayerPlugin.getInstance().getLogger().log(Level.INFO, "initiateDefaultPerms failed, caller passed in null", new Exception());
			return;
		}
		Map <PlayerType, List <PermissionType>> defaultPermMapping = new HashMap<GroupManager.PlayerType, List<PermissionType>>();
		for(PermissionType perm : PermissionType.getAllPermissions()) {
			for(PlayerType type : perm.getDefaultPermLevels()) {
				List<PermissionType> perms = defaultPermMapping.computeIfAbsent(type, k -> new ArrayList<>());
				perms.add(perm);
			}
		}
		groupManagerDao.addAllPermissions(groupId, defaultPermMapping);
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
	 * @param group the group to invalidate cache for
	 */
	public static void invalidateCache(String group){
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
					+ "That PlayerType does not exist.\n"
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
