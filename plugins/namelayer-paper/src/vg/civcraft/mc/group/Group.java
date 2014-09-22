package vg.civcraft.mc.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameTrackerPlugin;
import vg.civcraft.mc.database.SaveManager;

public class Group {
	
	protected String groupName;
	private UUID ownerUUID;
	private boolean isDisiplined;
	private String password;
	private GroupType type;
	
	protected SaveManager db = NameTrackerPlugin.getSaveManager();
	
	protected Map<PlayerType, List<UUID>> players = new HashMap<PlayerType, List<UUID>>();
	
	public Map<UUID, PlayerType> invitations = new HashMap<UUID, PlayerType>();
	
	public Group(String name, UUID owner, boolean disiplined, String password, GroupType type){
		groupName = name;
		ownerUUID = owner;
		isDisiplined = disiplined;
		this.password = password;
		this.type = type;
		for (PlayerType t: PlayerType.values()){
			List<UUID> list;
			list = db.getAllMembers(name, t);
			players.put(t, list);
		}
	}
	
	public List<UUID> getAllMembers(){
		List<UUID> uuids = new ArrayList<UUID>();
		for (PlayerType type: players.keySet())
			uuids.addAll(players.get(type));
		return uuids;
	}
	
	public void addInvite(UUID uuid, PlayerType type){
		invitations.put(uuid, type);
	}
	
	public PlayerType getInvite(UUID uuid){
		if (!invitations.containsKey(uuid))
			return null;
		return invitations.get(uuid);
	}
	
	public void removeRemoveInvite(UUID uuid){
		invitations.remove(uuid);
	}

	public void addMember(UUID uuid, PlayerType type){
		db.addMember(uuid, groupName, type);
		players.get(type).add(uuid);
	}
	
	public boolean isMember(UUID uuid, PlayerType type){
		return players.get(type).contains(uuid);
	}
	
	public void removeMember(UUID uuid, PlayerType type){
		db.removeMember(uuid, groupName);
		players.get(type).remove(uuid);
	}
	
	public boolean isOwner(UUID uuid){
		return ownerUUID.equals(uuid);
	}
	
	public void setOwner(UUID uuid){
		ownerUUID = uuid;
	}
	
	public UUID getOwner(){
		return ownerUUID;
	}
	
	public String getName(){
		return groupName;
	}
	
	public boolean isDisiplined(){
		return isDisiplined;
	}
	
	public void setDisiplined(boolean value){
		isDisiplined = value;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public boolean isPassword(String password){
		return this.password.equals(password);
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setType(GroupType type){
		this.type = type;
	}
	
	public GroupType getType(){
		return type;
	}
	
	public PlayerType getPlayerType(UUID uuid){
		for (PlayerType t: players.keySet()){
			if (players.get(t).contains(uuid))
				return t;
		}
		return null;
	}
}
