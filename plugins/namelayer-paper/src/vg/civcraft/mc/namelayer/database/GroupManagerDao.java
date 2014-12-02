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
			initializeProcedures();
			checkUpdate();
		}
	}
	
	public void checkUpdate(){
		log(Level.INFO, "Checking Database to see if update is needed!");
		int ver = checkVersion(plugin.getName());
		if (ver == 0){
			log(Level.INFO, "Performing database update to version 1!\n" +
					"This may take a long time depending on how big your database is.");
			db.execute("alter table faction drop `version`;");
			db.execute("alter table faction add type int default 0;");
			db.execute("create table faction_id("
					+ "group_id int not null AUTO_INCREMENT,"
					+ "group_name varchar(255),"
					+ "primary key(group_id));");
			
			db.execute("insert into faction_id (group_name) select `name` from faction;");
			db.execute("alter table faction add group_name varchar(255) default null;");
			db.execute("update faction g set g.group_name = `g.name`;");
			db.execute("alter table faction drop `name`");
			db.execute("drop table personal_group;");
			db.execute("alter table faction_member add role varchar(10) not null default 'MEMBER'");
			db.execute("insert into faction_member (group_id, member_name, role)" +
					"select g.group_id, m.member_name, 'MOD' from moderator m "
					+ "inner join faction_id g on g.group_name = m.faction_name");
			db.execute("drop table moderator;");
			db.execute("alter table faction change `type` group_type varchar(40) not null;");
			db.execute("alter table db_version add plugin_name varchar(40);");
			db.execute("alter table db_version drop primary key;");
			ver = updateVersion(ver, plugin.getName());
		}
		if (ver == 1){
			log(Level.INFO, "Performing database creation!");
			db.execute("create table faction_id("
					+ "group_id int not null AUTO_INCREMENT,"
					+ "group_name varchar(255),"
					+ "primary key(group_id));");
			/* In the faction table we use group names. This is important because when merging other groups
			 * it will create multiple same group_names within the faction_id table. The benefits are that when other
			 * tables come looking for a group they always find the right one due to their only being one group with a name.
			 */
			db.execute("create table if not exists faction(" +
					"group_name varchar(255)," +
					"founder varchar(36) not null," +
					"password varchar(255) default null," +
					"discipline_flags int(11) not null," +
					"group_type varchar(40) not null," +
					"primary key(name));");
			db.execute("create table if not exists faction_member(" +
					"group_id varchar(255) not null," +
					"member_name varchar(36) not null," +
					"role varchar(10) not null default 'MEMBER'," +
					"unique key (faction_name, member_name));");
			db.execute("create table if not exists blacklist(" +
					"member_name varchar(36) not null," +
					"group_id varchar(255) not null);");
			db.execute("create table if not exists permissions(" +
					"group_id varchar(255) not null," +
					"role varchar(40) not null," +
					"tier varchar(255) not null," +
					"unique key (faction, role));");
			db.execute("create table if not exists subgroup(" +
					"group_id varchar(255) not null," +
					"sub_group_id varchar(255) not null," +
					"unique key (group_id, sub_group_id));");
			db.execute("create table if not exists db_version (db_version int not null," +
					"update_time varchar(24),"
					+ "plugin_name varchar(40)," +
					"primary key (db_version));");
			createGroup(NameLayerPlugin.getSpecialAdminGroup(), null, null, GroupType.PRIVATE);
			ver = updateVersion(ver, plugin.getName());
		}
	}

	public void initializeProcedures(){
		db.execute("drop procedure if exists deletegroupfromtable;");
		db.execute("create definer=current_user procedure deletegroupfromtable(" +
				"in groupName varchar(36)" +
				") sql security invoker begin "
				+ "declare group_id int;"
				+ "set group_id = (select f.group_id from faction_id f where f.group_name = groupName);" +
				"delete from faction f where f.group_id = group_id;" +
				"delete from faction_member fm where fm.group_id = group_id;" +
				"delete from blacklist b where b.group_id = group_id;" +
				"delete from subgroup s where s.group_id = group_id;" +
				"delete from permissions p where p.group_id = group_id;"
				+ "delete from faction_id f where f.group_name = groupName;" +
				"end;");
		db.execute("drop procedure if exists mergeintogroup;");
		db.execute("create definer=current_user procedure mergeintogroup(" +
				"in groupName varchar(255), in tomerge varchar(255)) " +
				"sql security invoker begin "
				+ "declare group_id int;"
				+ "declare merge_group_id int;"
				+ "set group_id = (select f.group_id from faction_id f where f.group_name = groupName);"
				+ "set merge_group_id = (select f.group_id from faction_id f where f.group_name = mergeintogroup);" +
				"update ignore faction_member fm set fm.group_id = group_id where fm.group_id = merge_group_id;" +
				"delete from faction_member f where f.group_id = merge_group_id;"
				+ "delete from faction where group_name = tomerge;"
				+ "update faction_id set group_name = groupName where group_id = merge_group_id;"
				+ "end;");
		db.execute("drop procedure if exists createGroup;");
		db.execute("create definer=current_user procedure createGroup("
				 + "in group_name varchar(255)," +
				"in founder varchar(36)," +
				"in password varchar(255)," +
				"in discipline_flags int(11)," +
				"in group_type varchar(40))"
				+ "sql security invoker begin "
				+ "insert into faction_id(group_name);"
				+ "insert into faction(group_name, founder, password, disipline_flags," +
				"group_type) values (group_name, founder, password, discipline_flags, group_type);");
	}
	
	private PreparedStatement version, updateVersion;
	
	private PreparedStatement createGroup, getGroup, getAllGroupsNames, deleteGroup;
	
	private PreparedStatement addMember, getMembers, removeMember, updatePassword;
	
	private PreparedStatement addSubGroup, getSubGroups, getSuperGroup, removeSubGroup;
	
	private PreparedStatement addPerm, getPerms, updatePerm;
	
	private PreparedStatement mergeGroup;
	
	private PreparedStatement countGroups;
	
	public void initializeStatements(){
		version = db.prepareStatement("select max(db_version) as db_version from db_version where plugin_name=?");
		updateVersion = db.prepareStatement("insert into db_version (db_version, update_time, plugin_name) values (?,?,?)"); 
		
		createGroup = db.prepareStatement("call createGroup(?,?,?,?,?)");
		getGroup = db.prepareStatement("select group_name, founder, password, disipline_flags, group_type " +
				"from faction where group_name = ?");
		getAllGroupsNames = db.prepareStatement("select f.group_name from faction_id f "
				+ "inner join faction_member fm on f.group_id = fm.group_id "
				+ "where fm.member_name = ?");
		deleteGroup = db.prepareStatement("call deletegroupfromtable(?)");

		addMember = db.prepareStatement("insert into faction_member(" +
				"group_id, member_name, role) select group_id, ?, ? from "
				+ "faction_id where group_name = ?");
		getMembers = db.prepareStatement("select member_name from faction_member fm "
				+ "where fm.group_id = (select f.group_id from faction_id f where group_name = ?) "
				+ "and fm.role = ?");
		removeMember = db.prepareStatement("delete from faction_member where member_name = ? and "
				+ "group_id = (select group_id from faction_id where group_name = ?)");
		
		addSubGroup = db.prepareStatement("insert into subgroup (group_id, sub_group_id) " +
				"select g.group_id, sg.group_id from faction_id g "
				+ "inner join faction_id sg on sg.group_name = ? "
				+ "where g.group_name = ?");
		getSubGroups = db.prepareStatement("select f.group_name from faction_id f "
				+ "inner join faction_id sf on sf.group_name = ? "
				+ "inner join subgroup sg on sg.group_id = sf.group_id "
				+ "where f.group_id = sg.sub_group_id");
		getSuperGroup = db.prepareStatement("select f.group_name from faction_id f "
				+ "inner join faction_id sf on sf.sub_group_id = ? "
				+ "inner join subgroup sg on sg.sub_group_id = sf.group_id "
				+ "where f.group_id = sg.group_id");
		removeSubGroup = db.prepareStatement("delete from subgroup "
				+ "where group_id = (select group_id from faction_id where group_name = ?)"
				+ " and sub_group_id = (select group_id from faction_id where group_name = ?)");

		addPerm = db.prepareStatement("insert into permissions (group_id, role, tier) "
				+ "select g.group_id, ?, ? from faction_id g where g.group_name = ?");
		getPerms = db.prepareStatement("select role, tier from permissions "
				+ "where group_id = (select g.group_id from faction_id g where g.group_name = ?)");
		updatePerm = db.prepareStatement("update permissions set tier = ? "
				+ "where group_id = (select g.group_id from faction_id where group_name = ?) and role = ?");
		
		countGroups = db.prepareStatement("select count(*) as count from faction");
		
		mergeGroup = db.prepareStatement("call mergeintogroup(?,?)");
		
		updatePassword = db.prepareStatement("update from faction set password = ? "
				+ "where group_name = ?");
	}
	/**
	 * Checks the version of a specific plugin's db.
	 * @param name- The name of the plugin.
	 * @return Returns the version of the plugin or 0 if none was found.
	 */
	public int checkVersion(String name){
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
	public int updateVersion(int version, String pluginname){
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
		return version++;
	}
	
	public void createGroup(String group, UUID owner, String password, GroupType type){
		try {
			createGroup.setString(1, group);
			createGroup.setString(2, owner.toString());
			createGroup.setString(3, password);
			createGroup.setInt(4, 0);
			createGroup.setString(5, type.name());
			createGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Group getGroup(String groupName){
		try {
			getGroup.setString(1, groupName);
			ResultSet set = getGroup.executeQuery();
			if (!set.next()) return null;
			String name = set.getString(1);
			UUID owner = UUID.fromString(set.getString(2));
			boolean dis = set.getInt(4) == 0;
			String password = set.getString(3);
			GroupType type = GroupType.valueOf(set.getString(5));
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
	
	public List<String> getGroupNames(UUID uuid){
		List<String> groups = new ArrayList<String>();
		try {
			getAllGroupsNames.setString(1, uuid.toString());
			ResultSet set = getAllGroupsNames.executeQuery();
			while(set.next())
				groups.add(set.getString("faction_name"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return groups;
	}
	
	// Remember to come back and make it remove members, mods, admins, ect from the group too
	public void deleteGroup(String groupName){
		try {
			deleteGroup.setString(1, groupName);
			deleteGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addMember(UUID member, String faction, PlayerType role){
		try {
			addMember.setString(1, member.toString());
			addMember.setString(2,role.name());
			addMember.setString(3, faction);
			addMember.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<UUID> getAllMembers(String groupName, PlayerType role){
		List<UUID> members = new ArrayList<UUID>();
		try {
			getMembers.setString(1, groupName);
			getMembers.setString(2, role.name());
			ResultSet set = getMembers.executeQuery();
			while(set.next())
				members.add(UUID.fromString(set.getString("faction_member")));
			return members;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return members;
	}
	
	public void removeMember(UUID member, String group){
		try {
			removeMember.setString(1, member.toString());
			removeMember.setString(2, group);
			removeMember.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addSubGroup(String group, String subGroup){
		try {
			addSubGroup.setString(1, group);
			addSubGroup.setString(2, subGroup);
			addSubGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Group> getSubGroups(String group){
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
	
	public Group getSuperGroup(String subGroup){
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
	
	public void removeSubGroup(String group, String subGroup){
		try {
			removeSubGroup.setString(1, group);
			removeSubGroup.setString(2, subGroup);
			removeSubGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addPermission(String groupName, String role, String values){
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
	
	public Map<PlayerType, List<PermissionType>> getPermissions(String group){
		Map<PlayerType, List<PermissionType>> perms = new HashMap<PlayerType, List<PermissionType>>();
		try {
			getPerms.setString(1, group);
			ResultSet set = getPerms.executeQuery();
			while(set.next()){
				PlayerType type = PlayerType.valueOf(set.getString("role"));
				String length = set.getString("tier");
				String[] multiPerms = length.split(" ");
				List<PermissionType> listPerm = new ArrayList<PermissionType>();
				for (String x: multiPerms)
					listPerm.add(PermissionType.valueOf(x));
				perms.put(type, listPerm);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return perms;
	}
	
	public void updatePermissions(String group, PlayerType pType, String perms){
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
	
	public int countGroups(){
		try {
			ResultSet set = countGroups.executeQuery();
			return set.next() ? set.getInt("count") : 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public void mergeGroup(String groupName, String toMerge){
		try {
			mergeGroup.setString(1, groupName);
			mergeGroup.setString(2, groupName);
			mergeGroup.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updatePassword(String groupName, String password){
		try {
			updatePassword.setString(1, password);
			updatePassword.setString(2, groupName);
			updatePassword.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}