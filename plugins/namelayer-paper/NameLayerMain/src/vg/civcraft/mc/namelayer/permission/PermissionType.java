package vg.civcraft.mc.namelayer.permission;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Bukkit;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;


public class PermissionType {
	
	private static Map <String, PermissionType> permissionByName;
	private static Map <Integer, PermissionType> permissionById;
	private static int maximumExistingId;
	
	public static void initialize() {
		permissionByName = new HashMap<String, PermissionType>();
		permissionById = new TreeMap<Integer, PermissionType>();
		maximumExistingId = 0;
		registerNameLayerPermissions();
	}
	
	public static PermissionType getPermission(String name) {
		return permissionByName.get(name);
	}
	
	public static PermissionType getPermission(int id) {
		return permissionById.get(id);
	}
	
	public static void registerPermission(String name, List <PlayerType> defaultPermLevels) {
		if (name == null ) {
			Bukkit.getLogger().severe("Could not register permission, name was null");
			return;
		}
		if (permissionByName.get(name) != null) {
			Bukkit.getLogger().severe("Could not register permission " + name + ". It was already registered");
			return;
		}
		Map <Integer,String> permMapping = NameLayerPlugin.getGroupManagerDao().getPermissionMapping();
		int id = -1;
		Map <Integer,String> dbRegisteredPerms = NameLayerPlugin.getGroupManagerDao().getPermissionMapping();
		for(Entry <Integer,String> perm : dbRegisteredPerms.entrySet()) {
			if (perm.getValue().equals(name)) {
				id = perm.getKey();
				break;
			}
		}
		PermissionType p;
		if (id == -1) {
			//not in db yet
			id = maximumExistingId + 1;
			while(dbRegisteredPerms.get(id) != null) {
				id++;
			}
			maximumExistingId = id;
			p = new PermissionType(name, id, defaultPermLevels);
			NameLayerPlugin.getGroupManagerDao().registerPermission(p);
			NameLayerPlugin.getGroupManagerDao().addNewDefaultPermission(defaultPermLevels, p);
		}
		else {
			//already in db, so use existing id
			p = new PermissionType(name, id, defaultPermLevels);
		}
		permissionByName.put(name, p);
		permissionById.put(id, p);
	}
	
	public static Collection<PermissionType> getAllPermissions() {
		return permissionByName.values();
	}
	
	private static void registerNameLayerPermissions() {
		LinkedList <PlayerType> members = new LinkedList<GroupManager.PlayerType>();
		LinkedList <PlayerType> modAndAbove = new LinkedList<GroupManager.PlayerType>();
		LinkedList <PlayerType> adminAndAbove = new LinkedList<GroupManager.PlayerType>();
		LinkedList <PlayerType> owner = new LinkedList<GroupManager.PlayerType>();
		LinkedList <PlayerType> all = new LinkedList <GroupManager.PlayerType>();
		members.add(PlayerType.MEMBERS);
		modAndAbove.add(PlayerType.MODS);
		modAndAbove.add(PlayerType.ADMINS);
		modAndAbove.add(PlayerType.OWNER);
		adminAndAbove.add(PlayerType.ADMINS);
		adminAndAbove.add(PlayerType.OWNER);
		owner.add(PlayerType.OWNER);
		all.add(PlayerType.MEMBERS);
		all.add(PlayerType.MODS);
		all.add(PlayerType.ADMINS);
		all.add(PlayerType.OWNER);
		//clone the list every time so changing the list of one perm later doesn't affect other perms
		//also not saving them to the db, because that handled by the groupmanager dao itself, which isnt
		//even initialized at this point
		
		//allows adding/removing members
		registerPermission("MEMBERS", (LinkedList <PlayerType>)modAndAbove.clone());
		//allows blacklisting/unblacklisting players and viewing the blacklist
		registerPermission("BLACKLIST", (LinkedList <PlayerType>)modAndAbove.clone());
		//allows adding/removing mods
		registerPermission("MODS", (LinkedList <PlayerType>)adminAndAbove.clone());
		//allows adding/modifying a password for the group
		registerPermission("PASSWORD", (LinkedList <PlayerType>)adminAndAbove.clone());
		//allows to list the permissions for each permission group
		registerPermission("LIST_PERMS", (LinkedList <PlayerType>)adminAndAbove.clone());
		//allows to see general group stats
		registerPermission("GROUPSTATS", (LinkedList <PlayerType>)adminAndAbove.clone());
		//allows to add/remove admins
		registerPermission("ADMINS", (LinkedList <PlayerType>)owner.clone());
		//allows to add/remove owners
		registerPermission("OWNER", (LinkedList <PlayerType>)owner.clone());
		//allows to modify the permissions for different permissions groups
		registerPermission("PERMS", (LinkedList <PlayerType>)owner.clone());
		//allows deleting the group
		registerPermission("DELETE", (LinkedList <PlayerType>)owner.clone());
		//allows merging the group with another one
		registerPermission("MERGE", (LinkedList <PlayerType>)owner.clone());
		//allows transferring this group to a new primary owner
		registerPermission("TRANSFER", (LinkedList <PlayerType>)owner.clone());
		//allows linking this group to another
		registerPermission("LINKING", (LinkedList <PlayerType>)owner.clone());
		//allows opening the gui
		registerPermission("OPEN_GUI", (LinkedList <PlayerType>)all.clone());
		
		
		//perm level given to members when they join with a password
		registerPermission("JOIN_PASSWORD", members);
	}
	
	private String name;
	private List <PlayerType> defaultPermLevels;
	private int id;
	
	private PermissionType(String name, int id, List <PlayerType> defaultPermLevels) {
		this.name = name;
		this.id = id;
		this.defaultPermLevels = defaultPermLevels;
	}
	
	public String getName() {
		return name;
	}
	
	public List <PlayerType> getDefaultPermLevels() {
		return defaultPermLevels;
	}
	
	public int getId() {
		return id;
	}
	
	/**
	public static String getStringOfTypes() {
		StringBuilder perms = new StringBuilder();
		for (String perm: BY_NAME.keySet()) {
			perms.append(perm);
			perms.append(" ");
		}
		return perms.toString();
	}
	
	public static void displayPermissionTypes(Player p) {
		p.sendMessage(ChatColor.RED 
				+ "That PermissionType does not exists.\n"
				+ "The current types are: " + getStringOfTypes());
	} **/
}
