package vg.civcraft.mc.namelayer.database;

import static vg.civcraft.mc.namelayer.NameLayerPlugin.log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.jline.internal.Log;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.GroupType;
import vg.civcraft.mc.namelayer.group.groups.PrivateGroup;
import vg.civcraft.mc.namelayer.group.groups.PublicGroup;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupManagerDao {
	private Database db;
	protected NameLayerPlugin plugin = NameLayerPlugin.getInstance();
	
	public GroupManagerDao(Database db){
		this.db = db;
		if (db.isConnected()){
			initializeStatements();
			checkUpdate();
			initializeProcedures();
		}
	}
	
	public void checkUpdate(){
		long begin_time = System.currentTimeMillis();
		log(Level.INFO, "Checking Database to see if update is needed!");
		int ver = checkVersion(plugin.getName());
		db.execute("create table if not exists db_version (db_version int not null," +
				"update_time varchar(24),"
				+ "plugin_name varchar(40)," +
				"primary key (db_version));");
		if (ver == 0){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Performing database update to version 1!\n" +
					"This may take a long time depending on how big your database is.");
			db.execute("alter table faction drop `version`;");
			db.execute("alter table faction add type int default 0;");
			db.execute("create table faction_id("
					+ "group_id int not null AUTO_INCREMENT,"
					+ "group_name varchar(255),"
					+ "primary key(group_id));");

			db.execute("create table if not exists permissions(" +
					"group_id varchar(255) not null," +
					"role varchar(40) not null," +
					"tier varchar(255) not null," +
					"unique key (group_id, role));");
			db.execute("delete from faction where `name` is null;");
			db.execute("delete from faction_member where faction_name is null;");
			db.execute("delete from moderator where faction_name is null;");
			db.execute("insert into faction_id (group_name) select `name` from faction;");
			db.execute("alter table faction add group_name varchar(255) default null;");
			db.execute("update faction g set g.group_name = g.name;");
			db.execute("alter table faction drop `name`;");
			db.execute("alter table faction add primary key group_primary_key (group_name);");
			db.execute("drop table personal_group;");
			db.execute("alter table faction_member change member_name member_name varchar(36);");
			db.execute("alter table faction_member add role varchar(10) not null default 'MEMBERS';");
			db.execute("alter table faction_member add group_id int not null;");
			db.execute("delete fm.* from faction_member fm where not exists " // deletes any non faction_id entries.
					+ "(select fi.group_id from faction_id fi "
					+ "where fi.group_name = fm.faction_name limit 1);");
			db.execute("update faction_member fm set fm.group_id = (select fi.group_id from faction_id fi "
					+ "where fi.group_name = fm.faction_name limit 1);");
			db.execute("alter table faction_member add unique key uq_meber_faction(member_name, group_id);");
			db.execute("alter table faction_member drop index uq_faction_member_1;");
			db.execute("alter table faction_member drop faction_name;");
			db.execute("insert ignore into faction_member (group_id, member_name, role)" +
					"select g.group_id, m.member_name, 'MODS' from moderator m "
					+ "inner join faction_id g on g.group_name = m.faction_name");
			db.execute("insert into faction_member (group_id, member_name, role)"
					+ "select fi.group_id, f.founder, 'OWNER' from faction f "
					+ "inner join faction_id fi on fi.group_name = f.group_name;");
			db.execute("drop table moderator;");
			db.execute("alter table faction change `type` group_type varchar(40) not null default 'PRIVATE';");
			db.execute("update faction set group_type = 'PRIVATE';");
			db.execute("alter table faction change founder founder varchar(36);");
			db.execute("alter table db_version add plugin_name varchar(40);");
			db.execute("alter table db_version change db_version db_version int not null;"); // for some reason db_version may have an auto increment on it.
			db.execute("alter table db_version drop primary key;");
			db.execute("insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'OWNER', "
					+ "'DOORS CHESTS BLOCKS OWNER ADMINS MODS MEMBERS PASSWORD SUBGROUP PERMS DELETE MERGE LIST_PERMS TRANSFER CROPS' "
					+ "from faction_id f;");
			db.execute("insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'ADMINS', "
					+ "'DOORS CHESTS BLOCKS MODS MEMBERS PASSWORD LIST_PERMS CROPS' "
					+ "from faction_id f;");	
			db.execute("insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'MODS', "
					+ "'DOORS CHESTS BLOCKS MEMBERS CROPS' "
					+ "from faction_id f;");
			db.execute("insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'MEMBERS', "
					+ "'DOORS CHESTS' "
					+ "from faction_id f;");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version one took " + (System.currentTimeMillis() - first_time) / 1000 + " seconds.");
		}
		if (ver == 1){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Performing database creation!");
			db.execute("create table if not exists faction_id("
					+ "group_id int not null AUTO_INCREMENT,"
					+ "group_name varchar(255),"
					+ "primary key(group_id));");
			/* In the faction table we use group names. This is important because when merging other groups
			 * it will create multiple same group_names within the faction_id table. The benefits are that when other
			 * tables come looking for a group they always find the right one due to their only being one group with a name.
			 */
			db.execute("create table if not exists faction(" +
					"group_name varchar(255)," +
					"founder varchar(36)," +
					"password varchar(255) default null," +
					"discipline_flags int(11) not null," +
					"group_type varchar(40) not null default 'PRIVATE'," +
					"primary key(group_name));");
			db.execute("create table if not exists faction_member(" +
					"group_id int not null," +
					"member_name varchar(36)," +
					"role varchar(10) not null default 'MEMBERS'," +
					"unique key (group_id, member_name));");
			db.execute("create table if not exists blacklist(" +
					"member_name varchar(36) not null," +
					"group_id varchar(255) not null);");
			db.execute("create table if not exists permissions(" +
					"group_id varchar(255) not null," +
					"role varchar(40) not null," +
					"tier varchar(255) not null," +
					"unique key (group_id, role));");
			db.execute("create table if not exists subgroup(" +
					"group_id varchar(255) not null," +
					"sub_group_id varchar(255) not null," +
					"unique key (group_id, sub_group_id));");
			// Procedures may not be initialized yet.
			Bukkit.getScheduler().scheduleSyncDelayedTask(NameLayerPlugin.getInstance(), new Runnable(){

				@Override
				public void run() {
					Group g = getGroup(NameLayerPlugin.getSpecialAdminGroup());
					if (g == null)
						createGroup(NameLayerPlugin.getSpecialAdminGroup(), null, null, GroupType.PRIVATE);
					else {
						for (UUID uuid: g.getAllMembers())
							g.removeMember(uuid);
					}
				}
				
			}, 1);
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version two took " + (System.currentTimeMillis() - first_time) / 1000 + " seconds.");
		}
		if (ver == 2){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to Version three.");
			
			db.execute("create table if not exists toggleAutoAccept("
					+ "uuid varchar(36) not null,"
					+ "primary key key_uuid(uuid));");
			
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version three took " + (System.currentTimeMillis() - first_time) / 1000 + " seconds.");
		}
		if (ver == 3){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to Version four.");
			db.execute("alter table faction_id add index `faction_id_index` (group_name);");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version four took " + (System.currentTimeMillis() - first_time) / 1000 + " seconds.");
		}
		if (ver == 4){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to Version five.");
			db.execute("alter table faction_member add index `faction_member_index` (group_id);");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version five took " + (System.currentTimeMillis() - first_time) / 1000 + " seconds.");
		}
		if (ver == 5){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database upadting to version six.");
			db.execute("create table if not exists default_group(" + 
					"uuid varchar(40) NOT NULL," +
					"defaultgroup varchar(255) NOT NULL,"+
					"primary key key_uuid(uuid))");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version five took " + (System.currentTimeMillis() - first_time) /1000 + " seconds.");
		}
		log(Level.INFO, "Database update took " + (System.currentTimeMillis() - begin_time) / 1000 + " seconds.");
	}

	public void initializeProcedures(){
		db.execute("drop procedure if exists deletegroupfromtable;");
		db.execute("create definer=current_user procedure deletegroupfromtable(" +
				"in groupName varchar(36),"
				+ "in specialAdminGroup varchar(36)" +
				") sql security invoker begin " +
				"delete fm.* from faction_member fm "
				+ "inner join faction_id fi on fm.group_id = fi.group_id "
				+ "where fi.group_name = groupName;" +
				"delete b.* from blacklist b "
				+ "inner join faction_id fi on b.group_id = fi.group_id "
				+ "where fi.group_name = groupName;" +
				"delete s.* from subgroup s "
				+ "inner join faction_id fi on s.group_id = fi.group_id "
				+ "where fi.group_name = groupName;" +
				"delete p.* from permissions p "
				+ "inner join faction_id fi on p.group_id = fi.group_id "
				+ "where fi.group_name = groupName;"
				+ "update faction f set f.group_name = specialAdminGroup "
				+ "where f.group_name = specialAdminGroup;"
				+ "update faction_id set group_name = specialAdminGroup where group_name = groupName;"
				+ "delete from faction where group_name = groupName;" +
				"end;");
		db.execute("drop procedure if exists mergeintogroup;");
		// needs to be set with inner jons
		db.execute("create definer=current_user procedure mergeintogroup(" +
				"in groupName varchar(255), in tomerge varchar(255)) " +
				"sql security invoker begin " +
				"update ignore faction_member fm "
				+ "inner join faction_id fi on fi.group_name = groupName "
				+ "inner join faction_id fii on fii.group_name = tomerge "
				+ "set fm.group_id = fi.group_id "
				+ "where fm.group_id = fii.group_id;" +
				"delete fm.* from faction_member fm "
				+ "inner join faction_id fi on fi.group_name = tomerge "
				+ "where fm.group_id = fi.group_id;"
				+ "delete from faction where group_name = tomerge;"
				+ "update faction_id fi "
				+ "inner join faction_id fii on fii.group_name = tomerge "
				+ "set fi.group_name = groupName "
				+ "where fi.group_id = fii.group_id;"
				+ "end;");
		db.execute("drop procedure if exists createGroup;");
		db.execute("create definer=current_user procedure createGroup("
				 + "in group_name varchar(255)," +
				"in founder varchar(36)," +
				"in password varchar(255)," +
				"in discipline_flags int(11)," +
				"in group_type varchar(40))"
				+ "sql security invoker begin "
				+ "insert into faction_id(group_name) values (group_name);"
				+ "insert into faction(group_name, founder, password, discipline_flags," +
				"group_type) values (group_name, founder, password, discipline_flags, group_type);"
				+ "insert into faction_member (member_name, role, group_id) "
				+ "select founder, 'OWNER', f.group_id from faction_id f where f.group_name = group_name;"
				+ "end;");
	}
	
	private PreparedStatement version, updateVersion;
	
	private PreparedStatement createGroup, getGroup, getAllGroupsNames, deleteGroup;
	
	private PreparedStatement addMember, getMembers, removeMember, updatePassword;
	
	private PreparedStatement addSubGroup, getSubGroups, getSuperGroup, removeSubGroup;
	
	private PreparedStatement addPerm, getPerms, updatePerm;
	
	private PreparedStatement mergeGroup;
	
	private PreparedStatement countGroups;
	
	private PreparedStatement addAutoAcceptGroup, getAutoAcceptGroup, removeAutoAcceptGroup;
	
	private PreparedStatement setDefaultGroup, changeDefaultGroup, getDefaultGroup;
	
	public void initializeStatements(){
		version = db.prepareStatement("select max(db_version) as db_version from db_version where plugin_name=?");
		updateVersion = db.prepareStatement("insert into db_version (db_version, update_time, plugin_name) values (?,?,?)"); 
		
		createGroup = db.prepareStatement("call createGroup(?,?,?,?,?)");
		getGroup = db.prepareStatement("select group_name, founder, password, discipline_flags, group_type " +
				"from faction where group_name = ?");
		getAllGroupsNames = db.prepareStatement("select f.group_name from faction_id f "
				+ "inner join faction_member fm on f.group_id = fm.group_id "
				+ "where fm.member_name = ?");
		deleteGroup = db.prepareStatement("call deletegroupfromtable(?, ?)");

		addMember = db.prepareStatement("insert into faction_member(" +
				"group_id, member_name, role) select group_id, ?, ? from "
				+ "faction_id where group_name = ?");
		getMembers = db.prepareStatement("select fm.member_name from faction_member fm "
				+ "inner join faction_id id on id.group_name = ? "
				+ "where fm.group_id = id.group_id and fm.role = ?");
		removeMember = db.prepareStatement("delete fm.* from faction_member fm "
				+ "inner join faction_id fi on fi.group_id = fm.group_id "
				+ "where fm.member_name = ? and fi.group_name =?");
		
		addSubGroup = db.prepareStatement("insert into subgroup (group_id, sub_group_id) " +
				"select g.group_id, sg.group_id from faction_id g "
				+ "inner join faction_id sg on sg.group_name = ? "
				+ "where g.group_name = ?");
		getSubGroups = db.prepareStatement("select f.group_name from faction_id f "
				+ "inner join faction_id sf on sf.group_name = ? "
				+ "inner join subgroup sg on sg.group_id = sf.group_id "
				+ "where f.group_id = sg.sub_group_id");
		getSuperGroup = db.prepareStatement("select f.group_name from faction_id f "
				+ "inner join faction_id sf on sf.group_name = ? "
				+ "inner join subgroup sg on sg.sub_group_id = sf.group_id "
				+ "where f.group_id = sg.group_id");
		removeSubGroup = db.prepareStatement("delete from subgroup "
				+ "where group_id = (select group_id from faction_id where group_name = ?)"
				+ " and sub_group_id = (select group_id from faction_id where group_name = ?)");

		addPerm = db.prepareStatement("insert into permissions (group_id, role, tier) "
				+ "select g.group_id, ?, ? from faction_id g where g.group_name = ?");
		getPerms = db.prepareStatement("select p.role, p.tier from permissions p "
				+ "inner join faction_id fi on fi.group_name = ? "
				+ "where p.group_id = fi.group_id");
		updatePerm = db.prepareStatement("update permissions p set p.tier = ? "
				+ "where p.group_id = (select g.group_id from faction_id g where g.group_name = ? limit 1) and p.role = ?");
		
		countGroups = db.prepareStatement("select count(*) as count from faction");
		
		mergeGroup = db.prepareStatement("call mergeintogroup(?,?)");
		
		updatePassword = db.prepareStatement("update faction set `password` = ? "
				+ "where group_name = ?");
		
		addAutoAcceptGroup = db.prepareStatement("insert into toggleAutoAccept(uuid)"
				+ "values(?)");
		getAutoAcceptGroup = db.prepareStatement("select uuid from toggleAutoAccept "
				+ "where uuid = ?");
		removeAutoAcceptGroup = db.prepareStatement("delete from toggleAutoAccept where uuid = ?");
		
		setDefaultGroup = db.prepareStatement("insert into default_group values(?, ?)");
		
		changeDefaultGroup = db.prepareStatement("update default_group set defaultgroup = ? where uuid = ?");
	
		
		getDefaultGroup = db.prepareStatement("select defaultgroup from default_group "
				+ "where uuid = ?");
	}
	/**
	 * Checks the version of a specific plugin's db.
	 * @param name- The name of the plugin.
	 * @return Returns the version of the plugin or 0 if none was found.
	 */
	public int checkVersion(String name){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			version.setString(1, name);
			ResultSet set = version.executeQuery();
			if (!set.next()) 
				return 0;
			return set.getInt("db_version");
		} catch (SQLException e) {
			// table doesnt exist
			return 0;
		}
	}
	/**
	 * Updates the version number for a plugin. You must specify what 
	 * the current version number is.
	 * @param version- The current version of the plugin.
	 * @param pluginname- The plugin name.
	 * @return Returns the new version of the db.
	 */
	public synchronized int updateVersion(int version, String pluginname){
		NameLayerPlugin.reconnectAndReintializeStatements();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			updateVersion.setInt(1, version+ 1);
			updateVersion.setString(2, sdf.format(new Date()));
			updateVersion.setString(3, pluginname);
			updateVersion.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ++version;
	}
	
	public synchronized void createGroup(String group, UUID owner, String password, GroupType type){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			String own = null;
			if (owner != null)
				own = owner.toString();
			createGroup.setString(1, group);
			createGroup.setString(2, own);
			createGroup.setString(3, password);
			createGroup.setInt(4, 0);
			createGroup.setString(5, type.name());
			createGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized Group getGroup(String groupName){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			getGroup.setString(1, groupName);
			ResultSet set = getGroup.executeQuery();
			if (!set.next()) return null;
			String name = set.getString(1);
			String uuid = set.getString(2);
			UUID owner = null;
			if (uuid != null)
				owner = UUID.fromString(uuid);
			boolean dis = set.getInt(4) != 0;
			String password = set.getString(3);
			GroupType type = GroupType.getGroupType(set.getString(5));
			Group g = null;
			switch(type){
			case PRIVATE:
				g = new PrivateGroup(name, owner, dis, password);
				break;
			case PUBLIC:
				g = new PublicGroup(name, owner, dis);
				break;
			default:
				g = new Group(name, owner, dis, password, type);
			}
			return g;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized List<String> getGroupNames(UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
		List<String> groups = new ArrayList<String>();
		try {
			getAllGroupsNames.setString(1, uuid.toString());
			ResultSet set = getAllGroupsNames.executeQuery();
			while(set.next())
				groups.add(set.getString(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return groups;
	}
	
	public synchronized void deleteGroup(String groupName){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			deleteGroup.setString(1, groupName);
			deleteGroup.setString(2, NameLayerPlugin.getSpecialAdminGroup());
			deleteGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void addMember(UUID member, String faction, PlayerType role){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			addMember.setString(1, member.toString());
			addMember.setString(2, role.name());
			addMember.setString(3, faction);
			addMember.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized List<UUID> getAllMembers(String groupName, PlayerType role){
		NameLayerPlugin.reconnectAndReintializeStatements();
		List<UUID> members = new ArrayList<UUID>();
		try {
			getMembers.setString(1, groupName);
			getMembers.setString(2, role.name());
			ResultSet set = getMembers.executeQuery();
			while(set.next()){
				String uuid = set.getString(1);
				if (uuid == null)
					continue;
				members.add(UUID.fromString(uuid));
			}
			return members;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return members;
	}
	
	public synchronized void removeMember(UUID member, String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			removeMember.setString(1, member.toString());
			removeMember.setString(2, group);
			removeMember.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void addSubGroup(String group, String subGroup){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			addSubGroup.setString(1, group);
			addSubGroup.setString(2, subGroup);
			addSubGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized List<Group> getSubGroups(String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		List<Group> groups = new ArrayList<Group>();
		try {
			getSubGroups.setString(1, group);
			ResultSet set = getSubGroups.executeQuery();
			while (set.next()){
				Group g = GroupManager.getGroup(set.getString(1));
				groups.add(g);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return groups;
	}
	
	public synchronized Group getSuperGroup(String subGroup){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			getSuperGroup.setString(1, subGroup);
			ResultSet set = getSuperGroup.executeQuery();
			if (!set.next()) 
				return null;
			return GroupManager.getGroup(set.getString(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized void removeSubGroup(String group, String subGroup){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			removeSubGroup.setString(1, group);
			removeSubGroup.setString(2, subGroup);
			removeSubGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void addPermission(String groupName, String role, String values){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			addPerm.setString(1, role);
			addPerm.setString(2, values);
			addPerm.setString(3, groupName);
			addPerm.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized Map<PlayerType, List<PermissionType>> getPermissions(String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		Map<PlayerType, List<PermissionType>> perms = new HashMap<PlayerType, List<PermissionType>>();
		try {
			getPerms.setString(1, group);
			ResultSet set = getPerms.executeQuery();
			while(set.next()){
				PlayerType type = PlayerType.getPlayerType(set.getString(1));
				String length = set.getString(2);
				String[] multiPerms = length.split(" ");
				List<PermissionType> listPerm = new ArrayList<PermissionType>();
				for (String x: multiPerms)
					listPerm.add(PermissionType.getPermissionType(x));
				perms.put(type, listPerm);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return perms;
	}
	
	public synchronized void updatePermissions(String group, PlayerType pType, String perms){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			updatePerm.setString(1, perms);
			updatePerm.setString(2, group);
			updatePerm.setString(3, pType.name());
			updatePerm.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized int countGroups(){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			ResultSet set = countGroups.executeQuery();
			return set.next() ? set.getInt("count") : 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public synchronized void mergeGroup(String groupName, String toMerge){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			mergeGroup.setString(1, groupName);
			mergeGroup.setString(2, toMerge);
			mergeGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void updatePassword(String groupName, String password){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			updatePassword.setString(1, password);
			updatePassword.setString(2, groupName);
			updatePassword.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the uuid to the db if they should auto accept groups when invited.
	 * @param uuid
	 */
	public synchronized void autoAcceptGroups(UUID uuid){
		try {
			addAutoAcceptGroup.setString(1, uuid.toString());
			addAutoAcceptGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param uuid- The UUID of the player.
	 * @return Returns true if they should auto accept.
	 */
	public synchronized boolean shouldAutoAcceptGroups(UUID uuid){
		try {
			getAutoAcceptGroup.setString(1, uuid.toString());
			ResultSet set = getAutoAcceptGroup.executeQuery();
			return set.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public synchronized void removeAutoAcceptGroup(UUID uuid){
		try {
			removeAutoAcceptGroup.setString(1, uuid.toString());
			removeAutoAcceptGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void setDefaultGroup(UUID uuid, String groupName){
		try {
			setDefaultGroup.setString(1, uuid.toString());
			setDefaultGroup.setString(2, groupName );
			setDefaultGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void changeDefaultGroup(UUID uuid, String groupName){
		try {
			changeDefaultGroup.setString(1, groupName);
			changeDefaultGroup.setString(2, uuid.toString());
			changeDefaultGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getDefaultGroup(UUID uuid) {
		try {
			getDefaultGroup.setString(1, uuid.toString());
			ResultSet set = getDefaultGroup.executeQuery();
			if(!set.next()) return null;
			String group = set.getString(1);
			return group;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
