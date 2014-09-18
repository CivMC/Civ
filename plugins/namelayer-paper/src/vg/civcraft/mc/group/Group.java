package vg.civcraft.mc.group;

import java.util.List;
import java.util.UUID;

import vg.civcraft.mc.NameTrackerPlugin;
import vg.civcraft.mc.database.SaveManager;

public class Group {
	
	private String groupName;
	private UUID ownerUUID;
	private boolean isDisiplined;
	private String password;
	private GroupType type;
	
	protected SaveManager db = NameTrackerPlugin.getSaveManager();
	
	private List<UUID> members;
	private List<UUID> mods;
	private List<UUID> admins;
	
	public Group(String name, UUID owner, boolean disiplined, String password, GroupType type){
		groupName = name;
		ownerUUID = owner;
		isDisiplined = disiplined;
		this.password = password;
		this.type = type;
		members = db.getAllMembers(name);
		mods = db.getAllMods(name);
		admins = db.getAdmins(name);
	}

	// Everything dealing with members
	public void addMember(UUID uuid){
		db.addMember(uuid, groupName);
		members.add(uuid);
	}
	
	public boolean isMember(UUID uuid){
		return members.contains(uuid);
	}
	
	public void removeMember(UUID uuid){
		db.removeMember(uuid, groupName);
		members.remove(uuid);
	}
	
	public List<UUID> getAllMembers(){
		return members;
	}
	
	// Everything dealing with admins
	public void addAdmin(UUID uuid){
		db.addAdmin(groupName, uuid);
		admins.add(uuid);
	}
	
	public boolean isAdmin(UUID uuid){
		return admins.contains(uuid);
	}
	
	public void removeAdmin(UUID uuid){
		db.removeAdmin(groupName, uuid);
		admins.remove(uuid);
	}
	
	// Everything dealing with mods
	public void addMod(UUID uuid){
		db.addMod(uuid, groupName);
		mods.add(uuid);
	}
	
	public boolean isMod(UUID uuid){
		return mods.contains(uuid);
	}
	
	public void removeMod(UUID uuid){
		db.removeMod(groupName, uuid);
		mods.remove(uuid);
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
}
