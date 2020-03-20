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
		registerPermission(name, defaultPermLevels, null);
	}
	
	public static void registerPermission(String name, List <PlayerType> defaultPermLevels, String description) {
		registerPermission(name, defaultPermLevels, description, true);
	}
	
	public static void registerPermission(String name, List <PlayerType> defaultPermLevels, String description, boolean canBeBlacklisted) {
		if (name == null ) {
			Bukkit.getLogger().severe("Could not register permission, name was null");
			return;
		}
		if (permissionByName.get(name) != null) {
			Bukkit.getLogger().severe("Could not register permission " + name + ". It was already registered");
			return;
		}
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
			p = new PermissionType(name, id, defaultPermLevels, description, canBeBlacklisted);
			NameLayerPlugin.getGroupManagerDao().registerPermission(p);
			NameLayerPlugin.getGroupManagerDao().addNewDefaultPermission(defaultPermLevels, p);
		}
		else {
			//already in db, so use existing id
			p = new PermissionType(name, id, defaultPermLevels, description, canBeBlacklisted);
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
		
		//allows adding/removing members
		registerPermission("MEMBERS", (LinkedList <PlayerType>)modAndAbove.clone(), "Allows inviting new members and removing existing members");
		//allows blacklisting/unblacklisting players and viewing the blacklist
		registerPermission("BLACKLIST", (LinkedList <PlayerType>)modAndAbove.clone(), "Allows viewing this group's blacklist, adding players to the blacklist "
				+ "and removing players from the blacklist");
		//allows adding/removing mods
		registerPermission("MODS", (LinkedList <PlayerType>)adminAndAbove.clone(), "Allows inviting new mods and removing existing mods");
		//allows adding/modifying a password for the group
		registerPermission("PASSWORD", (LinkedList <PlayerType>)adminAndAbove.clone(), "Allows viewing this groups password and changing or removing it");
		//allows to list the permissions for each permission group
		registerPermission("LIST_PERMS", (LinkedList <PlayerType>)adminAndAbove.clone(), "Allows viewing how permission for this group are set up");
		//allows to see general group stats
		registerPermission("GROUPSTATS", (LinkedList <PlayerType>)adminAndAbove.clone(), "Gives access to various group statistics such as member "
				+ "counts by permission type, who owns the group etc.");
		//allows to add/remove admins
		registerPermission("ADMINS", (LinkedList <PlayerType>)owner.clone(), "Allows inviting new admins and removing existing admins");
		//allows to add/remove owners
		registerPermission("OWNER", (LinkedList <PlayerType>)owner.clone(), "Allows inviting new owners and removing existing owners");
		//allows to modify the permissions for different permissions groups
		registerPermission("PERMS", (LinkedList <PlayerType>)owner.clone(), "Allows modifying permissions for this group");
		//allows deleting the group
		registerPermission("DELETE", (LinkedList <PlayerType>)owner.clone(), "Allows deleting this group");
		//allows merging the group with another one
		registerPermission("MERGE", (LinkedList <PlayerType>)owner.clone(), "Allows merging this group into another or merging another group into this one");
		//allows linking this group to another
		registerPermission("LINKING", (LinkedList <PlayerType>)owner.clone(), "Allows linking this group to another group as a supergroup or a subgroup");
		//allows opening the gui
		registerPermission("OPEN_GUI", (LinkedList <PlayerType>)all.clone(), "Allows opening the GUI for this group");
		
		
		//perm level given to members when they join with a password
		registerPermission("JOIN_PASSWORD", members);
	}
	
	private String name;
	private List <PlayerType> defaultPermLevels;
	private int id;
	private String description;
	private boolean canBeBlacklisted;

	private PermissionType(String name, int id, List <PlayerType> defaultPermLevels, String description, boolean canBeBlacklisted) {
		this.name = name;
		this.id = id;
		this.defaultPermLevels = defaultPermLevels;
		this.description = description;
		this.canBeBlacklisted = canBeBlacklisted;
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
	
	public String getDescription() {
		return description;
	}

	public boolean getCanBeBlacklisted() {
		return canBeBlacklisted;
	}
}
