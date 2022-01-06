package vg.civcraft.mc.namelayer.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;


public class PermissionType {
	
	private static Map <String, PermissionType> permissionByName;
	private static Map <Integer, PermissionType> permissionById;
	private static int maximumExistingId;
	
	public static void initialize() {
		permissionByName = new HashMap<>();
		permissionById = new TreeMap<>();
		maximumExistingId = 0;
		Map <Integer,String> dbRegisteredPerms = NameLayerPlugin.getGroupManagerDao().getPermissionMapping();
		for(Entry <Integer,String> perm : dbRegisteredPerms.entrySet()) {
			int id = perm.getKey();
			String name = perm.getValue();
			maximumExistingId = Math.max(maximumExistingId, id);
			internalRegisterPermission(id, name, new ArrayList<>(), null, true);
		}
		registerNameLayerPermissions();
	}
	
	public static PermissionType getPermission(String name) {
		return permissionByName.get(name);
	}
	
	public static PermissionType getPermission(int id) {
		return permissionById.get(id);
	}
	
	public static PermissionType registerPermission(String name, List<PlayerType> defaultPermLevels) {
		return registerPermission(name, defaultPermLevels, null, true);
	}
	
	public static PermissionType registerPermission(String name, List <PlayerType> defaultPermLevels, String description) {
		return registerPermission(name, defaultPermLevels, description, true);
	}
	
	public static PermissionType registerPermission(String name, List <PlayerType> defaultPermLevels, String description, boolean canBeBlacklisted) {
		if (name == null ) {
			Bukkit.getLogger().severe("Could not register permission, name was null");
			return null;
		}
		PermissionType existing = permissionByName.get(name);
		if (existing != null) {
			existing.update(defaultPermLevels, description, canBeBlacklisted);
			return existing;
		}
		//not in db yet
		int id = maximumExistingId + 1;
		maximumExistingId = id;
		PermissionType perm = internalRegisterPermission(id, name, defaultPermLevels, description, canBeBlacklisted);
		NameLayerPlugin.getGroupManagerDao().registerPermission(perm);
		if (!defaultPermLevels.isEmpty()) {
			NameLayerPlugin.getGroupManagerDao().addNewDefaultPermission(defaultPermLevels, perm);
		}
		return perm;
	}

	private static PermissionType internalRegisterPermission(int id, String name, List <PlayerType> defaultPermLevels, String description, boolean canBeBlackListed) {
		PermissionType p = new PermissionType(name, id, defaultPermLevels, description, canBeBlackListed);
		permissionByName.put(name, p);
		permissionById.put(id, p);
		return p;
	}
	
	public static Collection<PermissionType> getAllPermissions() {
		return permissionByName.values();
	}
	
	private static void registerNameLayerPermissions() {
		List<PlayerType> members = Arrays.asList(PlayerType.MEMBERS);
		List<PlayerType> modAndAbove = Arrays.asList(PlayerType.MODS, PlayerType.ADMINS, PlayerType.OWNER);
		List<PlayerType> adminAndAbove = Arrays.asList(PlayerType.ADMINS, PlayerType.OWNER);
		List<PlayerType> owner = Arrays.asList(PlayerType.OWNER);
		List<PlayerType> all = Arrays.asList(PlayerType.MEMBERS, PlayerType.MODS, PlayerType.ADMINS, PlayerType.OWNER);

		//clone the list every time so changing the list of one perm later doesn't affect other perms
		
		//allows adding/removing members
		registerPermission("MEMBERS", new ArrayList<>(modAndAbove), "Allows inviting new members and removing existing members");
		//allows blacklisting/unblacklisting players and viewing the blacklist
		registerPermission("BLACKLIST", new ArrayList<>(modAndAbove), "Allows viewing this groups blacklist, adding players to the blacklist "
				+ "and removing players from the blacklist");
		//allows adding/removing mods
		registerPermission("MODS", new ArrayList<>(adminAndAbove), "Allows inviting new mods and removing existing mods");
		//allows adding/modifying a password for the group
		registerPermission("PASSWORD", new ArrayList<>(adminAndAbove), "Allows viewing this groups password and changing or removing it");
		//allows to list the permissions for each permission group
		registerPermission("LIST_PERMS", new ArrayList<>(adminAndAbove), "Allows viewing how permissions for this group are set up");
		//allows to see general group stats
		registerPermission("GROUPSTATS", new ArrayList<>(adminAndAbove), "Gives access to various group statistics such as member "
				+ "counts by permission type, who owns the group etc");
		//allows to add/remove admins
		registerPermission("ADMINS", new ArrayList<>(owner), "Allows inviting new admins and removing existing admins");
		//allows to add/remove owners
		registerPermission("OWNER", new ArrayList<>(owner), "Allows inviting new owners and removing existing owners");
		//allows to modify the permissions for different permissions groups
		registerPermission("PERMS", new ArrayList<>(owner), "Allows modifying permissions for this group");
		//allows deleting the group
		registerPermission("DELETE", new ArrayList<>(owner), "Allows deleting this group");
		//allows merging the group with another one
		registerPermission("MERGE", new ArrayList<>(owner), "Allows merging this group into another or merging another group into this one");
		//allows linking this group to another
		registerPermission("LINKING", new ArrayList<>(owner), "Allows linking this group to another group as a supergroup or a subgroup");
		//allows opening the gui
		registerPermission("OPEN_GUI", new ArrayList<>(all), "Allows opening the GUI for this group");

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


	
	public void update(List <PlayerType> defaultPermLevels, String description, boolean canBeBlacklisted) {
		this.defaultPermLevels = defaultPermLevels;
		this.description = description;
		this.canBeBlacklisted = canBeBlacklisted;
	}
	
	public boolean getCanBeBlacklisted() {
		return canBeBlacklisted;
	}
}
