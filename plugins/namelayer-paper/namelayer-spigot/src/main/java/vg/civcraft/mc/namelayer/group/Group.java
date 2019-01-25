package vg.civcraft.mc.namelayer.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;

public class Group {
	
	private static GroupManagerDao db;
	
	private String name;
	private String password;
	private UUID owner;
	private boolean isDisciplined; // if true, prevents any interactions with this group
	private boolean isValid = true;  // if false, then group has recently been deleted and is invalid
	private int id;
	private Set<Integer> ids = Sets.<Integer>newConcurrentHashSet();
		
	private Group supergroup;
	private Set<Group> subgroups = Sets.<Group>newConcurrentHashSet();
	private Map<UUID, PlayerType> players = Maps.<UUID, PlayerType>newHashMap();
	private Map<UUID, PlayerType> invites = Maps.<UUID, PlayerType>newHashMap();
	
	public Group(String name, UUID owner, boolean disciplined, 
			String password, int id) {
		if (db == null) {
			db = NameLayerPlugin.getGroupManagerDao();
		}
		
		this.name = name;
		this.password = password;
		this.owner = owner;
		this.isDisciplined = disciplined;
		
		if (name == null) {
			this.ids.add(id);
			this.id = id;
			return;
		}
	
		for (PlayerType permission : PlayerType.values()) {
			List<UUID> list = db.getAllMembers(name, permission);
			for (UUID uuid : list) {
				players.put(uuid, permission);
			}
		}
		
		// This returns list of ids w/ id holding largest # of players at top.
		List<Integer> allIds = db.getAllIDs(name);
		if (allIds != null && allIds.size() > 0) {
			this.ids.addAll(allIds);
			this.id = allIds.get(0); // default "root" id is the one with the players.
		} else {
			this.ids.add(id);
			this.id = id; // otherwise just use what we're given
		}
		
		// only get subgroups, supergroups will set themselves
		for (Group subgroup : GroupManager.getSubGroups(name)) {
			link(this, subgroup, false);
		}
	}
	
	public void prepareForDeletion() {
		unlink(supergroup, this);
		for (Group subgroup : subgroups) {
			unlink(this, subgroup);
		}
	}
	
	/**
	 * Returns all the uuids of the members in this group.
	 * @return Returns all the uuids.
	 */
	public List<UUID> getAllMembers() {
		return Lists.newArrayList(players.keySet());
	}
	
	/**
	 * Returns all the UUIDS of a group's PlayerType.
	 * @param type- The PlayerType of a group that you want the UUIDs of.
	 * @return Returns all the UUIDS of the specific PlayerType.
	 */
	public List<UUID> getAllMembers(PlayerType type) {
		List<UUID> uuids = Lists.newArrayList();;
		for (Map.Entry<UUID, PlayerType> entry : players.entrySet()) {
			if (entry.getValue() == type) {
				uuids.add(entry.getKey());
			}
		}
		return uuids;
	}
	
	/**
	 * Gives the uuids of the members whose name starts with the given
	 * String, this is not case-sensitive
	 * 
	 * @param prefix start of the players name
	 * @return list of all players whose name starts with the given string
	 */
	public List<UUID> getMembersByName(String prefix) {
		List<UUID> uuids = Lists.newArrayList();
		List<UUID> members = getAllMembers();
		
		prefix = prefix.toLowerCase();
		for (UUID member : members) {
			String name = NameAPI.getCurrentName(member);
			if (name.toLowerCase().startsWith(prefix)) {
				uuids.add(member);
			}
		}
		return uuids;
	}
	
	/**
	 * Gives the uuids of players who are in this group and whos name is
	 * within the given range. 
	 * @param lowerLimit lexicographically lowest acceptable name
	 * @param upperLimit lexicographically highest acceptable name
	 * @return list of uuids of all players in the group whose name is within the given range
	 */
	public List<UUID> getMembersInNameRange(String lowerLimit, String upperLimit) {
		List<UUID> uuids = Lists.newArrayList();
		List<UUID> members = getAllMembers();
		
		for (UUID member : members) {
			String name = NameAPI.getCurrentName(member);
			if (name.compareToIgnoreCase(lowerLimit) >= 0 
					&& name.compareToIgnoreCase(upperLimit) <= 0) {
				uuids.add(member);
			}
		}
		return uuids;
	}
	
	/**
	 * Gives a list of the members of this group, excluding the inherited members.
	 * @return List of UUIDs of the current players in this group
	 */
	public List<UUID> getCurrentMembers() {
		return Lists.newArrayList(players.keySet());
	}
	
	public List<UUID> getCurrentMembers(PlayerType rank) {
		List<UUID> uuids = Lists.newArrayList();
		
		for (Map.Entry<UUID, PlayerType> entry : players.entrySet()) {
			if (entry.getValue() == rank) {
				uuids.add(entry.getKey());
			}
		}
		return uuids;
	}
	
	/**
	 * @return Returns the SubGroups in this group.
	 */
	public List<Group> getSubgroups() {
		return Lists.newArrayList(subgroups);
	}
		
	/**
	 * Checks if a sub group is on a group.
	 * @param group- The SubGroup.
	 * @return Returns true if it has that subgroup.
	 */
	public boolean hasSubGroup(Group group){
		return subgroups.contains(group);
	}
		
	/**
	 * @return Returns the SubGroup for this group if there is one, null otherwise.
	 */
	public Group getSuperGroup() {
		return supergroup;
	}
	
	/**
	 * @return Returns if this group has a super group or not.
	 */
	public boolean hasSuperGroup() {
		return supergroup != null;
	}
	
	/**
	 * Checks if the given Group is a supergroup of this group and this
	 * group's supergroups.
	 * @param group - Group to check as supergroup.
	 * @return true if it is a supergroup, false otherwise.
	 */
	public boolean hasSuperGroup(Group group) {
		if (supergroup == null) {
			return false;
		} else if (supergroup == group) {
			return true;
		}
		return supergroup.hasSuperGroup(group);
	}
		
	/**
	 * Adds the player to be allowed to join a group into a specific PlayerType.
	 * @param uuid- The UUID of the player.
	 * @param type- The PlayerType they will be joining.
	 */
	public void addInvite(UUID uuid, PlayerType type){
		addInvite(uuid, type, true);
	}
	
	/**
	 * Adds the player to be allowed to join a group into a specific PlayerType.
	 * @param uuid- The UUID of the player.
	 * @param type- The PlayerType they will be joining.
	 * @param saveToDB - save the invitation to the DB. 
	 */
	public void addInvite(UUID uuid, PlayerType type, boolean saveToDB){
		invites.put(uuid, type);
		if(saveToDB){
			db.addGroupInvitation(uuid, name, type.name());
		}
	}
	
	/**
	 * Get's the PlayerType of an invited Player.
	 * @param uuid- The UUID of the player.
	 * @return Returns the PlayerType or null.
	 */
	public PlayerType getInvite(UUID uuid) {
		if (!invites.containsKey(uuid)) {
			db.loadGroupInvitation(uuid, this);
		}
		return invites.get(uuid);
	}
	
	/**
	 * Removes the invite of a Player
	 * @param uuid- The UUID of the player.
	 * @param saveToDB - remove the invitation from the DB. 
	 */
	public void removeInvite(UUID uuid){
		removeInvite(uuid, true);
	}
	
	/**
	 * Removes the invite of a Player
	 * @param uuid- The UUID of the player.
	 * @param saveToDB - remove the invitation from the DB. 
	 */
	public void removeInvite(UUID uuid, boolean saveToDB){
		invites.remove(uuid);
		if(saveToDB){
			db.removeGroupInvitation(uuid, name);
		}
	}
	/**
	 * Checks if the player is in the Group or not.
	 * @param uuid- The UUID of the player.
	 * @return Returns true if the player is a member, false otherwise.
	 */
	public boolean isMember(UUID uuid) {
		return players.containsKey(uuid);
	}

	/**
	 * Checks if the player is in the Group's PlayerType or not.
	 * @param uuid- The UUID of the player.
	 * @param type- The PlayerType wanted.
	 * @return Returns true if the player is a member of the specific playertype, otherwise false.
	 */
	public boolean isMember(UUID uuid, PlayerType type) {
		if (players.containsKey(uuid))
			return players.get(uuid).equals(type);
		return false;
	}

	public boolean isCurrentMember(UUID uuid) {
		return players.containsKey(uuid);
	}
	
	public boolean isCurrentMember(UUID uuid, PlayerType rank) {
		if (players.containsKey(uuid)) {
			return players.get(uuid).equals(rank);
		}
		return false;
	}
	
	/**
	 * @param uuid- The UUID of the player.
	 * @return Returns the PlayerType of a UUID.
	 */
	public PlayerType getPlayerType(UUID uuid) {
		PlayerType member = players.get(uuid);
		if (member != null) {
			return member;
		}
		if (NameLayerPlugin.getBlackList().isBlacklisted(this, uuid)) {
			return null;
		}
		return PlayerType.NOT_BLACKLISTED;
	}
	
	public PlayerType getCurrentRank(UUID uuid) {
		return players.get(uuid);
	}

	/**
	 * Adds a member to a group.
	 * @param uuid- The uuid of the player.
	 * @param type- The PlayerType to add. If a preexisting PlayerType is found, 
	 * it will be overwritten.
	 */
	
	public void addMember(UUID uuid, PlayerType type){
		addMember(uuid,type,true);
	}
	
	public void addMember(UUID uuid, PlayerType type, boolean savetodb) {
		if (type == PlayerType.NOT_BLACKLISTED) {
			return;
		}
		if (savetodb) {
			// TODO: Make this atomic. UPDATE, don't remove and add! (Use INSERT ... UPDATE ON DUPLICATE / FAILURE semantic)
			if (isMember(uuid, type)){
				db.removeMember(uuid, name);
			}
			db.addMember(uuid, name, type);
		}
		players.put(uuid, type);
	}

	/**
	 * Removes the Player from the Group.
	 * @param uuid- The UUID of the Player.
	 */
	public void removeMember(UUID uuid){
		removeMember(uuid,true);
	}
	
	public void removeMember(UUID uuid, boolean savetodb) {
		if (savetodb){
			db.removeMember(uuid, name);
		}
		players.remove(uuid);
	}
	
	public void removeAllMembers() {
		removeAllMembers(true);
	}
	
	public void removeAllMembers(boolean savetodb) {
		if (savetodb) {
			db.removeAllMembers(this.name);
		}
		players.clear();
	}

	/**
	 * 
	 * @param supergroup
	 * @param subgroup
	 * @return true if linking succeeded, false otherwise.
	 */
	public static boolean link(Group supergroup, Group subgroup, boolean saveToDb) {
		if (supergroup == null || subgroup == null) {
			return false;
		}
				
		if (supergroup.equals(subgroup)) {
			return false;
		}
		
		if (supergroup.hasSuperGroup(subgroup)) {
			return false;
		}
		
		if (subgroup.hasSuperGroup()) {
			unlink(subgroup.supergroup, subgroup);
		}
		subgroup.supergroup = supergroup;
		
		if (!supergroup.hasSubGroup(subgroup)) {
			supergroup.subgroups.add(subgroup);
		}
		if (saveToDb) {		
			db.addSubGroup(supergroup.getName(), subgroup.getName());
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param supergroup
	 * @param subgroup
	 */
	public static boolean unlink(Group supergroup, Group subgroup){
		return unlink(supergroup,subgroup, true);
	}
	public static boolean unlink(Group supergroup, Group subgroup, boolean savetodb) {
		if (supergroup == null || subgroup == null) { 
			return false;
		}
						
		if (subgroup.hasSuperGroup() && subgroup.supergroup.equals(supergroup)) {
			subgroup.supergroup = null;
		}
		
		if (supergroup.hasSubGroup(subgroup)) {
			supergroup.subgroups.remove(subgroup);
		}		
						
		if (savetodb){
			db.removeSubGroup(supergroup.getName(), subgroup.getName());
		}
		
		return true;
	}
	
	public static boolean areLinked(Group supergroup, Group subgroup) {
		if (supergroup == null || subgroup == null) {
			return false;
		}
		Set <String> names = new HashSet<String>();
		Group superG = supergroup;
		while (superG.hasSuperGroup() ) {
		    String superGName = superG.getName();
		    if (superGName.equals(subgroup.getName())) {
		    	return true;
		    }
		    if (names.contains(superGName)) {
		    	NameLayerPlugin.log(Level.WARNING, superGName + " is part of a cycle");
		    	//prevent further linking always if a cycle exists
		    	return true;
		    }
		    names.add(superGName);
		    superG = superG.getSuperGroup();
		}
		return false;
	}
	
	/**
	 * Sets the default group for a player
	 * @param uuid- The UUID of the player.
	 * 
	 */
	public void setDefaultGroup(UUID uuid) {
		NameLayerPlugin.getDefaultGroupHandler().setDefaultGroup(uuid, this);
	}

	public void changeDefaultGroup(UUID uuid) {
		NameLayerPlugin.getDefaultGroupHandler().setDefaultGroup(uuid, this);
	}
	
	// == GETTERS ========================================================================= //
	
	/**
	 * @return Returns the group name.
	 */
	public String getName() { return name; }

	public String getPassword() { return password; }

	/**
	 * Checks if a string equals the password of a group.
	 * @param password- The password to compare.
	 * @return Returns true if they equal, otherwise false.
	 */
	public boolean isPassword(String password) { return this.password.equals(password); }

	/**
	 * @return The UUID of the owner of the group.
	 */
	public UUID getOwner() { return owner; }
	
	/**
	 * @param uuid
	 * @return true if the UUID belongs to the owner of the group, false otherwise.
	 */
	public boolean isOwner(UUID uuid) { return owner.equals(uuid); }

	public boolean isDisciplined() { return isDisciplined; }

	public boolean isValid() { return isValid; }

	/**
	 * Gets the id for a group.
	 * <p>
	 * <b>Note:</b> 
	 * Keep in mind though if you are trying to get a group_id from a GroupCreateEvent event
	 * it will not be accurate. You must have a delay for 1 tick for it to work correctly.
	 * <p>
	 * Also calling the GroupManager.getGroup(int) will return a group that either has that 
	 * group id or the object associated with that id. As such if a group is previously called
	 * and which didn't have the same id as the one called now you could get a different group id.
	 * Example would be System.out.println(GroupManager.getGroup(1).getGroupId()) and that 
	 * could equal something like 2.
	 * @return the group id for a group.
	 */
	public int getGroupId() { return id; }
	
	/**
	 * Addresses issue above somewhat. Allows implementations that need the whole list of Ids
	 * associated with this groupname to get them.
	 * 
	 * @return list of ids paired with this group name.
	 */
	public List<Integer> getGroupIds() { return new ArrayList<Integer>(this.ids); }

	// == SETTERS ========================================================================= //
	
	/**
	 * Sets the password for a group. Set the parameter as null to remove the password.
	 * @param password- The password of the group.
	 */
	public void setPassword(String password){
		setPassword(password,true);
	}
	public void setPassword(String password, boolean savetodb) {
		this.password = password;
		if (savetodb){
			db.updatePassword(name, password);
		}
	}

	/**
	 * Sets the owner of the group.
	 * @param uuid- The UUID of the Player.
	 */
	public void setOwner(UUID uuid){
		setOwner(uuid,true);
	}
	
	public void setOwner(UUID uuid, boolean savetodb) {
		this.owner = uuid;
		if (savetodb){
			db.setFounder(uuid, this);
		}
	}
	
	public void setDisciplined(boolean value){
		setDisciplined(value, true);
	}

	public void setDisciplined(boolean value, boolean savetodb) {
		this.isDisciplined = value;
		if (savetodb){
			db.setDisciplined(this, value);
		}
	}

	public void setValid(boolean valid) { this.isValid = valid; }

	// acts as replace
	public void setGroupId(int id) {
		this.ids.remove(this.id);
		this.id = id;
		if (!ids.contains(this.id)){
			this.ids.add(this.id);
		}
	}

	/**
	 * Updates/replaces the group id list with a new one. Clears the old one, adds these,
	 * and ensures that the "main" id is added to the list as well.
	 */
	public void setGroupIds(List<Integer> ids) {
		this.ids.clear();
		if (ids != null) {
			this.ids.addAll(ids);
		}
		if (!ids.contains(this.id)){
			this.ids.add(this.id);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Group))
			return false;
		Group g = (Group) obj;
		return g.getName().equals(this.getName()); // If they have the same name they are equal.
	}
}
