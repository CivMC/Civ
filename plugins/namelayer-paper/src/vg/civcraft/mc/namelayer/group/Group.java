package vg.civcraft.mc.namelayer.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;

public class Group {
	
	protected String groupName;
	private UUID ownerUUID;
	private boolean isDisciplined;
	private String password;
	private GroupType type;
	private boolean valid = true;
	private int id;
	
	protected GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
	
	protected Map<UUID, PlayerType> players = new HashMap<UUID, PlayerType>();
	
	private Map<UUID, PlayerType> invitations = new HashMap<UUID, PlayerType>();
	
	public Group(String name, UUID owner, boolean disiplined, String password, GroupType type, int id){
		groupName = name;
		ownerUUID = owner;
		isDisciplined = disiplined;
		this.password = password;
		this.type = type;
		for (PlayerType t: PlayerType.values()){
			List<UUID> list;
			list = db.getAllMembers(name, t);
			for (UUID uuid: list){
				players.put(uuid, t);
			}
		}
		this.id = id;
	}
	/**
	 * Returns all the uuids of the members in this group.
	 * @return Returns all the uuids.
	 */
	public List<UUID> getAllMembers(){
		List<UUID> uuids = new ArrayList<UUID>(players.keySet());
		return uuids;
	}
	/**
	 * Returns all the UUIDS of a group's PlayerType.
	 * @param type- The PlayerType of a group that you want the UUIDs of.
	 * @return Returns all the UUIDS of the specific PlayerType.
	 */
	public List<UUID> getAllMembers(PlayerType type){
		List<UUID> uuids = new ArrayList<UUID>();
		for (UUID uu: players.keySet())
			if (players.get(uu) == type)
				uuids.add(uu);
		return uuids;
	}
	/**
	 * Adds the player to be allowed to join a group into a specific PlayerType.
	 * @param uuid- The UUID of the player.
	 * @param type- The PlayerType they will be joining.
	 */
	public void addInvite(UUID uuid, PlayerType type){
		invitations.put(uuid, type);
	}
	/**
	 * Get's the PlayerType of an invited Player.
	 * @param uuid- The UUID of the player.
	 * @return Returns the PlayerType or null.
	 */
	public PlayerType getInvite(UUID uuid){
		if (!invitations.containsKey(uuid))
			return null;
		return invitations.get(uuid);
	}
	/**
	 * Removes the invite of a Player
	 * @param uuid- The UUID of the player.
	 */
	public void removeRemoveInvite(UUID uuid){
		invitations.remove(uuid);
	}
	/**
	 * Adds a member to a group.
	 * @param uuid- The uuid of the player.
	 * @param type- The PlayerType to add. If a preexisting PlayerType is found, it will be overwritten.
	 */
	public void addMember(UUID uuid, PlayerType type){
		if (isMember(uuid, type))
			db.removeMember(uuid, groupName);
		db.addMember(uuid, groupName, type);
		players.put(uuid, type);
	}
	/**
	 * Checks if the player is in the Group or not.
	 * @param uuid- The UUID of the player.
	 * @return Returns true if the player is a member, false otherwise.
	 */
	public boolean isMember(UUID uuid){
		return players.containsKey(uuid);
	}
	/**
	 * Checks if the player is in the Group's PlayerType or not.
	 * @param uuid- The UUID of the player.
	 * @param type- The PlayerType wanted.
	 * @return Returns true if the player is a member of the specific playertype, otherwise false.
	 */
	public boolean isMember(UUID uuid, PlayerType type){
		if (players.containsKey(uuid))
			return players.get(uuid).equals(type);
		return false;
	}
	/**
	 * Removes the Player from the Group.
	 * @param uuid- The UUID of the Player.
	 */
	public void removeMember(UUID uuid){
		db.removeMember(uuid, groupName);
		players.remove(uuid);
	}
	/**
	 * Checks if the player is the owner.
	 * @param uuid- The UUID to Compare.
	 * @return Returns true if the UUID equals that to of the owner, false otherwise.
	 */
	public boolean isOwner(UUID uuid){
		return ownerUUID.equals(uuid);
	}
	/**
	 * Sets the owner of the group.
	 * @param uuid- The UUID of the Player.
	 */
	public void setOwner(UUID uuid){
		ownerUUID = uuid;
	}
	/**
	 * @return Returns the UUID of the Owner.
	 */
	public UUID getOwner(){
		return ownerUUID;
	}
	/**
	 * @return Returns the group name.
	 */
	public String getName(){
		return groupName;
	}
	/**
	 * @return Returns true if the group is Disciplined, false otherwise.
	 */
	public boolean isDisciplined(){
		return isDisciplined;
	}
	/**
	 * Sets a group to be disciplined or not.
	 * @param value- either true to set the group disciplined or false to disable it.
	 */
	public void setDisciplined(boolean value){
		isDisciplined = value;
	}
	/**
	 * Sets the password for a group. Set the parameter as null to remove the password.
	 * @param password- The password of the group.
	 */
	public void setPassword(String password){
		this.password = password;
		db.updatePassword(groupName, password);
	}
	/**
	 * Checks if a string equals the password of a group.
	 * @param password- The password to compare.
	 * @return Returns true if they equal, otherwise false.
	 */
	public boolean isPassword(String password){
		return this.password.equals(password);
	}
	/**
	 * @return Returns the password of the group or null if none.
	 */
	public String getPassword(){
		return password;
	}
	/**
	 * Sets the GroupType of a group. This shouldn't be called except from
	 * the constructor.
	 * @param type- The GroupType of the group.
	 */
	public void setType(GroupType type){
		this.type = type;
	}
	/**
	 * @return Returns the GroupType of the group.
	 */
	public GroupType getType(){
		return type;
	}
	/**
	 * @param uuid- The UUID of the player.
	 * @return Returns the PlayerType of a UUID.
	 */
	public PlayerType getPlayerType(UUID uuid){
		return players.get(uuid);
	}
	/**
	 * Sets whether this group is valid or not.  Should only be called when a group is deleted so other plugins know
	 * that this group is no longer accurate. 
	 * @param valid
	 */
	public void setValid(boolean valid){
		this.valid = valid;
	}
	/**
	 * Checks whether or not a group is valid.
	 * @return True if it is valid false if recently deleted.
	 */
	public boolean isValid(){
		return valid;
	}
	/**
	 * Sets the default group for a player
	 * @param uuid- The UUID of the player.
	 */
	public String setDefaultGroup(UUID uuid){
		db.setDefaultGroup(uuid,  groupName);
		return groupName;
	}
	
	public String changeDefaultGroup(UUID uuid){
		db.changeDefaultGroup(uuid, groupName);
		return groupName;
	}
	/**
	 * Gets the id for a group.  Keep in mind though if you are trying to get a group_id from a GroupCreateEvent event
	 * it will not be accurate.  You must have a delay for 1 tick for it to work correctly.
	 * @return Returns the group id for a group.
	 */
	public int getGroupId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
}
