package vg.civcraft.mc.database;

import static vg.civcraft.mc.NameTrackerPlugin.log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

import vg.civcraft.mc.GroupManager;
import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameTrackerPlugin;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.group.GroupType;
import vg.civcraft.mc.permission.PermissionType;

public class GroupManagerDao {
	private Database db;
	protected NameTrackerPlugin plugin = NameTrackerPlugin.getInstance();
	protected FileConfiguration config = plugin.getConfig();
	
	private int maxCountForExecuting = config.getInt("groups.database.maxflushcount");
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
		if (ver == 5){
			log(Level.INFO, "Performing database update to version 6!\n" +
					"This may take a long time depending on how big your database is.");
			db.execute("alter table faction drop `version`;");
			db.execute("alter table faction add type int default 0;");
			db.execute("drop table personal_group;");
			ver++;
		}
		if (ver == 6){
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
					"member_name varchar(36) not null);");
			db.execute("create table if not exists moderator(" +
					"member_name varchar(36) not null," +
					"faction_name varchar(255) not null);");
			db.execute("create table if not exists admins(" +
					"member_name varchar(36) not null," +
					"faction_name varchar(255) not null);");
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
		}
	}

	public void initializeProcedures(){
		db.execute("drop procedure if exists deletegroupfromtable;");
		db.execute("create definer=current_user procedure deletegroupfromtable(" +
				"in groupName varchar(36)" +
				") sql security invoker begin " +
				"delete from faction where `name` = groupName;" +
				"delete from faction_member where faction_name = groupName;" +
				"delete from moderator where faction_name = groupName;" +
				"delete from admins where faction_name = groupName;" +
				"delete from blacklist where faction_name = groupName;" +
				"delete from subgroup where faction = groupName;" +
				"delete from permissions where faction = groupName;" +
				"end;");
	}
	
	private PreparedStatement version;
	
	private PreparedStatement createGroup, getGroup, deleteGroup;
	
	private PreparedStatement addMember, getMembers, removeMember;
	
	private PreparedStatement addMod, getMods, removeMod;
	
	private PreparedStatement addAdmin, getAdmins, removeAdmin;
	
	private PreparedStatement addSubGroup, getSubGroups, getSuperGroup, removeSubGroup;
	
	private PreparedStatement addPerm, getPerms, updatePerm;
	
	public void initializeStatements(){
		version = db.prepareStatement("select max(db_version) as db_version from db_version");
		
		createGroup = db.prepareStatement("insert into faction(name, founder, password, disipline_flags," +
				"type) values (?,?,?,?,?)");
		getGroup = db.prepareStatement("select name, founder, password, disipline_flags, type " +
				"from faction where name = ?");
		deleteGroup = db.prepareStatement("call deletegroupfromtable(?)");
		
		addMember = db.prepareStatement("insert into faction_member(" +
				"faction_name, member_name) values (?, ?)");
		getMembers = db.prepareStatement("select member_name from faction_member where faction_name = ?");
		removeMember = db.prepareStatement("delete from faction_member where member_name = ? and faction_name = ?");
		
		addMod = db.prepareStatement("insert into moderator(" +
				"faction_name, member_name) values (?, ?)");
		getMods = db.prepareStatement("select member_name from moderator where faction_name = ?");
		removeMod = db.prepareStatement("delete from moderator where faction_name = ? and member_name = ?");
		
		addAdmin = db.prepareStatement("insert into admins(" +
				"faction_name, member_name) values (?, ?)");
		getAdmins = db.prepareStatement("select member_name from admins where faction_name = ?");
		removeAdmin = db.prepareStatement("delete from admins where faction_name = ? and member_name = ?");
		
		addSubGroup = db.prepareStatement("insert into subgroup (faction, sub_faction) " +
				"values (?,?)");
		getSubGroups = db.prepareStatement("select sub_faction from subgroup where faction = ?");
		getSuperGroup = db.prepareStatement("select faction from subgroup where sub_group = ?");
		removeSubGroup = db.prepareStatement("delete from subgroup where faction = ? and sub_faction = ?");

		addPerm = db.prepareStatement("insert into permissions (faction, role, tier) values (?, ?, ?)");
		getPerms = db.prepareStatement("select role, tier from permissions where faction = ?");
		updatePerm = db.prepareStatement("update permissions set tier = ? where faction = ? and role = ?");
	}
	
	public int checkVersion(){
		try {
			ResultSet set = version.executeQuery();
			if (!set.next()) 
				return 6;
			return set.getInt("db_version");
		} catch (SQLException e) {
			// table doesnt exist
			return 6;
		}
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
			return new Group(set.getString(1), UUID.fromString(set.getString(2)), set.getInt(4) == 0,
					set.getString(3), GroupType.valueOf(set.getString(5)));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
	public void addMember(UUID member, String faction){
		try {
			if (memberCount >= maxCountForExecuting)
				flushAddMember();
			addMember.setString(1, faction);
			addMember.setString(2, member.toString());
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
	
	public List<UUID> getAllMembers(String groupName){
		List<UUID> members = new ArrayList<UUID>();
		try {
			getMembers.setString(1, groupName);
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
			removeMember.executeQuery();
			removeMemberCount = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int addModCount = 0;
	public void addMod(UUID mod, String group){
		try {
			if (addModCount >= maxCountForExecuting)
				flushAddMod();
			addMod.setString(1, group);
			addMod.setString(2, mod.toString());
			addMod.addBatch();
			addModCount++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushAddMod(){
		try {
			addMod.executeBatch();
			addModCount = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<UUID> getAllMods(String group){
		List<UUID> mods = new ArrayList<UUID>();
		try {
			getMods.setString(1, group);
			ResultSet set = getMods.executeQuery();
			while (set.next())
				mods.add(UUID.fromString(set.getString("faction_member")));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mods;
	}
	
	private int removeModCount = 0;
	public void removeMod(String group, UUID mod){
		try {
			if (removeModCount >= maxCountForExecuting)
				flushRemoveMod();
			removeMod.setString(1, group);
			removeMod.setString(2, mod.toString());
			removeMod.addBatch();
			removeModCount++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushRemoveMod(){
		try {
			removeMod.executeBatch();
			removeModCount = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int addAdminCount = 0;
	public void addAdmin(String group, UUID admin){
		try {
			if (addAdminCount >= maxCountForExecuting)
				flushAddAdmins();
			addAdmin.setString(1, group);
			addAdmin.setString(2, admin.toString());
			addAdmin.addBatch();
			addAdminCount++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushAddAdmins(){
		try {
			addAdmin.executeBatch();
			addAdminCount = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<UUID> getAdmins(String group){
		List<UUID> admins = new ArrayList<UUID>();
		try {
			getAdmins.setString(1, group);
			ResultSet set = getAdmins.executeQuery();
			while (set.next())
				admins.add(UUID.fromString(set.getString("member_name")));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return admins;
	}
	
	private int removeAdminCount = 0;
	public void removeAdmin(String group, UUID admin){
		try {
			if (removeAdminCount >= maxCountForExecuting)
				flushRemoveAdmin();
			removeAdmin.setString(1, group);
			removeAdmin.setString(2, admin.toString());
			removeAdmin.addBatch();
			removeAdminCount++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void flushRemoveAdmin(){
		try {
			removeAdmin.executeBatch();
			removeAdminCount = 0;
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
	updatePerm = db.prepareStatement("update permissions set tier = ? where faction = ? and role = ?");
	public void updatePermissions(String group, )
}