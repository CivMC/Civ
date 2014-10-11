package vg.civcraft.mc.database;

import static vg.civcraft.mc.NameLayerPlugin.log;

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

import vg.civcraft.mc.ConfigManager;
import vg.civcraft.mc.GroupManager;
import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameLayerPlugin;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.group.GroupType;
import vg.civcraft.mc.group.groups.Private;
import vg.civcraft.mc.permission.PermissionType;

public class GroupManagerDao {
	private Database db;
	protected NameLayerPlugin plugin = NameLayerPlugin.getInstance();
	
	private int maxCountForExecuting = ConfigManager.getMaxFlushCount();
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
		int ver = checkVersion();
		if (ver == 0){
			log(Level.INFO, "Performing database update to version 1!\n" +
					"This may take a long time depending on how big your database is.");
			db.execute("alter table faction drop `version`;");
			db.execute("alter table faction add type int default 0;");
			db.execute("drop table personal_group;");
			db.execute("alter table faction_member add role varchar(10) not null default \"MEMBER\"");
			db.execute("insert into faction_member (faction_name, member_name, role)" +
					"select m.faction_name, m.member_name, \"MOD\" from moderator m;");
			db.execute("drop table moderator;");
			db.execute("alter table db_version add plugin_name varchar(40);");
			db.execute("alter table db_version drop primary key;");
			ver = updateVersion(ver, plugin.getName());
		}
		if (ver == 1){
			log(Level.INFO, "Performing database creation!");
			db.execute("create table if not exists faction(" +
					"name varchar(255) not null," +
					"founder varchar(36) not null," +
					"password varchar(255) default null," +
					"discipline_flags int(11) not null," +
					"type varchar(40) not null default 0," +
					"primary key(name));");
			db.execute("create table if not exists faction_member(" +
					"faction_name varchar(255) not null," +
					"member_name varchar(36) not null," +
					"role varchar(10) not null default \"MEMBER\"," +
					"unique key (faction_name, member_name));");
			db.execute("create table if not exists blacklist(" +
					"member_name varchar(36) not null," +
					"faction_name varchar(255) not null);");
			db.execute("create table if not exists permissions(" +
					"faction varchar(255) not null," +
					"role varchar(40) not null," +
					"tier varchar(255) not null," +
					"unique key (faction, role));");
			db.execute("create table if not exists subgroup(" +
					"faction varchar(255) not null," +
					"sub_faction varchar(255) not null," +
					"unique key (faction, sub_faction));");
			db.execute("create table if not exists db_version (db_version int not null," +
					"update_time varchar(24),"
					+ "plugin_name varchar(40)," +
					"primary key (db_version));");
			ver = updateVersion(ver, plugin.getName());
		}
	}

	public void initializeProcedures(){
		db.execute("drop procedure if exists deletegroupfromtable;");
		db.execute("create definer=current_user procedure deletegroupfromtable(" +
				"in groupName varchar(36)" +
				") sql security invoker begin " +
				"delete from faction where `name` = groupName;" +
				"delete from faction_member where faction_name = groupName;" +
				"delete from blacklist where faction_name = groupName;" +
				"delete from subgroup where faction = groupName;" +
				"delete from permissions where faction = groupName;" +
				"end;");
		db.execute("drop procedure if exists mergeintogroup;");
		db.execute("create definer=current_user procedure mergeintogroup(" +
				"in groupName varchar(255), in tomerge varchar(255)) " +
				"sql security invoker begin " +
				"update ignore faction_member set faction_name = tomerge where faction_name = groupName;" +
				"delete from faction where name = groupName;"
				+ "end;");
	}
	
	private PreparedStatement version, updateVersion;
	
	private PreparedStatement createGroup, getGroup, getAllGroupsNames, deleteGroup;
	
	private PreparedStatement addMember, getMembers, removeMember;
	
	private PreparedStatement addSubGroup, getSubGroups, getSuperGroup, removeSubGroup;
	
	private PreparedStatement addPerm, getPerms, updatePerm;
	
	private PreparedStatement mergeGroup;
	
	private PreparedStatement countGroups;
	
	public void initializeStatements(){
		version = db.prepareStatement("select max(db_version) as db_version from db_version where plugin_name=?");
		updateVersion = db.prepareStatement("insert into db_version (db_version, update_time, plugin_name) values (?,?,?)"); 
		
		createGroup = db.prepareStatement("insert into faction(name, founder, password, disipline_flags," +
				"type) values (?,?,?,?,?)");
		getGroup = db.prepareStatement("select name, founder, password, disipline_flags, type " +
				"from faction where name = ?");
		getAllGroupsNames = db.prepareStatement("select faction_name from faction_member where member_name = ?");
		deleteGroup = db.prepareStatement("call deletegroupfromtable(?)");
		
		addMember = db.prepareStatement("insert into faction_member(" +
				"faction_name, member_name, role) values (?, ?, ?)");
		getMembers = db.prepareStatement("select member_name from faction_member where faction_name = ? and role = ?");
		removeMember = db.prepareStatement("delete from faction_member where member_name = ? and faction_name = ?");
		
		addSubGroup = db.prepareStatement("insert into subgroup (faction, sub_faction) " +
				"values (?,?)");
		getSubGroups = db.prepareStatement("select sub_faction from subgroup where faction = ?");
		getSuperGroup = db.prepareStatement("select faction from subgroup where sub_group = ?");
		removeSubGroup = db.prepareStatement("delete from subgroup where faction = ? and sub_faction = ?");

		addPerm = db.prepareStatement("insert into permissions (faction, role, tier) values (?, ?, ?)");
		getPerms = db.prepareStatement("select role, tier from permissions where faction = ?");
		updatePerm = db.prepareStatement("update permissions set tier = ? where faction = ? and role = ?");
		
		countGroups = db.prepareStatement("select count(*) as count from faction");
		
		mergeGroup = db.prepareStatement("call mergeintogroup(?,?)");
	}
	
	public int checkVersion(){
		try {
			version.setString(1, plugin.getName());
			ResultSet set = version.executeQuery();
			if (!set.next()) 
				return 0;
			return set.getInt("db_version");
		} catch (SQLException e) {
			// table doesnt exist
			return 0;
		}
	}
	
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
	
	public void createGroup(String faction, UUID owner, String password, GroupType type){
		try {
			createGroup.setString(1, faction);
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
				g = new Private(name, owner, dis, password);
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
	
	private int memberCount = 0;
	public void addMember(UUID member, String faction, PlayerType role){
		try {
			if (memberCount >= maxCountForExecuting)
				flushAddMember();
			addMember.setString(1, faction);
			addMember.setString(2, member.toString());
			addMember.setString(3,role.name());
			addMember.addBatch();
			memberCount++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushAddMember(){
		try {
			addMember.executeBatch();
			memberCount = 0;
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
	
	private int removeMemberCount = 0; 
	public void removeMember(UUID member, String group){
		try {
			if (removeMemberCount >= maxCountForExecuting)
				flushRemoveMember();
			removeMember.setString(1, member.toString());
			removeMember.setString(2, group);
			removeMember.addBatch();
			removeMemberCount++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushRemoveMember(){
		try {
			removeMember.executeBatch();
			removeMemberCount = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int countAddSubGroup = 0;
	public void addSubGroup(String group, String subGroup){
		try {
			if (countAddSubGroup >= maxCountForExecuting)
				flushAddSubGroup();
			addSubGroup.setString(1, group);
			addSubGroup.setString(2, subGroup);
			addSubGroup.addBatch();
			countAddSubGroup++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushAddSubGroup(){
		try {
			addSubGroup.executeBatch();
			countAddSubGroup = 0;
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
				Group g = GroupManager.getGroup(set.getString("sub_faction"));
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
			return GroupManager.getGroup(set.getString("sub_group"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private int countRemoveSubGroup = 0;
	public void removeSubGroup(String group, String subGroup){
		try {
			if (countRemoveSubGroup >= maxCountForExecuting)
				flushRemoveSubGroup();
			removeSubGroup.setString(1, group);
			removeSubGroup.setString(2, subGroup);
			removeSubGroup.addBatch();
			countRemoveSubGroup++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushRemoveSubGroup(){
		try {
			removeSubGroup.executeBatch();
			countRemoveSubGroup = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private int countAddPermissionCount = 0;
	public void addPermission(String groupName, String role, String values){
		try {
			if (countAddPermissionCount >= maxCountForExecuting)
			addPerm.setString(1, groupName);
			addPerm.setString(2, role);
			addPerm.setString(3, values);
			addPerm.addBatch();
			countAddPermissionCount++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushAddPermission(){
		try {
			addPerm.executeBatch();
			countAddPermissionCount = 0;
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
	
	private int countUpdatePerm = 0;
	public void updatePermissions(String group, PlayerType pType, String perms){
		try {
			if (countUpdatePerm >= maxCountForExecuting)
				flushUpdatePermissions();
			updatePerm.setString(1, perms);
			updatePerm.setString(2, group);
			updatePerm.setString(3, pType.name());
			updatePerm.addBatch();
			countUpdatePerm++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushUpdatePermissions(){
		try {
			updatePerm.executeBatch();
			countUpdatePerm = 0;
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
}