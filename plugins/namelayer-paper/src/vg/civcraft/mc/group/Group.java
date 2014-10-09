package vg.civcraft.mc.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameLayerPlugin;
import vg.civcraft.mc.database.SaveManager;

public class Group {
	
	protected String groupName;
	private UUID ownerUUID;
	private boolean isDisiplined;
	private String password;
	private GroupType type;
	
	protected SaveManager db = NameLayerPlugin.getSaveManager();
	
	protected Map<UUID, PlayerType> players = new HashMap<UUID, PlayerType>();
	
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
			for (UUID uuid: list)
				players.put(uuid, t);
		}
	}
	
	public List<UUID> getAllMembers(){
		List<UUID> uuids = new ArrayList<UUID>();
		uuids.addAll(players.keySet());
		return uuids;
	}
	
	public List<UUID> getAllMembers(PlayerType type){
		List<UUID> uuids = new ArrayList<UUID>();
		for (UUID uu: players.keySet())
			if (players.get(uu) == type)
				uuids.add(uu);
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
		players.put(uuid, type);
	}
	
	public boolean isMember(UUID uuid){
		return players.get(uuid) != null;
	}
	
	public boolean isMember(UUID uuid, PlayerType type){
		if (players.get(uuid) != null)
			return players.get(uuid).equals(type);
		return false;
	}
	
	public void removeMember(UUID uuid){
		db.removeMember(uuid, groupName);
		players.remove(uuid);
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
		return players.get(uuid);
	}
}
