package vg.civcraft.mc.namelayer.database;

import static vg.civcraft.mc.namelayer.NameLayerPlugin.log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.google.common.collect.Lists;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
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
		if (ver == 0)
			try {
				db.execute("create table db_version (db_version int not null," +
						"update_time varchar(24),"
						+ "plugin_name varchar(40));", true);
				ver = 1;
			} catch (SQLException e) {
			}
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
						createGroup(NameLayerPlugin.getSpecialAdminGroup(), null, null);
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
					"uuid varchar(36) NOT NULL," +
					"defaultgroup varchar(255) NOT NULL,"+
					"primary key key_uuid(uuid))");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version six took " + (System.currentTimeMillis() - first_time) /1000 + " seconds.");
		}
		if (ver == 6){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to version seven.");
			db.execute("create table if not exists group_invitation(" + 
					"uuid varchar(36) NOT NULL," +
					"groupName varchar(255) NOT NULL,"+
					"role varchar(10) NOT NULL default 'MEMBERS'," +
					"date datetime NOT NULL default NOW()," +
					"constraint UQ_uuid_groupName unique(uuid, groupName))");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version seven took " + (System.currentTimeMillis() - first_time) /1000 + " seconds.");
		}
		if (ver == 7){
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to version eight.");
			db.execute("alter table faction add last_timestamp datetime NOT NULL default NOW();");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version eight took " + (System.currentTimeMillis() - first_time) /1000 + " seconds.");
		}
		if (ver == 8) {
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to version nine, fixing datatypes.");
			db.execute("alter table blacklist modify column group_id int;");
			db.execute("alter table permissions modify column group_id int;");
			db.execute("alter table subgroup modify column group_id int, modify column sub_group_id int;");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version nine took " + (System.currentTimeMillis() - first_time) /1000 + " seconds.");			
		}
		if (ver == 9) {
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to version ten, adding table to keep track of name changes");
			db.execute("create table if not exists nameLayerNameChanges(uuid varchar(36) not null, oldName varchar(32) not null, newName varchar(32) not null, primary key(uuid));");
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version ten took " + (System.currentTimeMillis() - first_time) /1000 + " seconds.");
		}
		if (ver == 10) {
			long first_time = System.currentTimeMillis();
			log(Level.INFO, "Database updating to version eleven, reworking permission system");
			db.execute("create table if not exists permissionByGroup(group_id int not null,role varchar(40) not null,perm_id int not null, primary key(group_id,role,perm_id));");
			db.execute("create table if not exists permissionIdMapping(perm_id int not null, name varchar(64) not null,primary key(perm_id));");
			db.execute("alter table faction drop column group_type");
			PreparedStatement permInit = db.prepareStatement("insert into permissionByGroup(group_id,role,perm_id) values(?,?,?);");
			try {
				Map <String, Integer> permIds = new HashMap<String, Integer>();
				PreparedStatement getOldPerms = db.prepareStatement("select * from permissions");
				ResultSet res = getOldPerms.executeQuery();
				int maximumId = 0;
				while(res.next()) {
					int groupId = res.getInt(1);
					String role = res.getString(2);
					String permList = res.getString(3);
					String [] perms = permList.split(" ");
					for(String p : perms) {
						if (!p.equals("")) {
							if(p.equals("BLOCKS")) {
								//this permission was renamed and now includes less functionality than previously
								p = "REINFORCE";
							}
							Integer id = permIds.get(p);
							if (id == null) {
								//unknown perm, so we register it
								id = maximumId + 1;
								maximumId = id;
								registerPermission.setInt(1, maximumId);
								registerPermission.setString(2, p);
								registerPermission.execute();
								permIds.put(p, id);
							}
							permInit.setInt(1, groupId);
							permInit.setString(2, role);
							permInit.setInt(3, id);
							permInit.execute();
						}
					}
				}
			}
			catch (SQLException e) {
				
			}
			ver = updateVersion(ver, plugin.getName());
			log(Level.INFO, "Database update to Version eleven took " + (System.currentTimeMillis() - first_time) /1000 + " seconds.");
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
				+ "where fi.group_name = groupName;" +
				"update faction f set f.group_name = specialAdminGroup "
				+ "where f.group_name = specialAdminGroup;" +
				"update faction_id set group_name = specialAdminGroup where group_name = groupName;" +
				"delete from faction where group_name = groupName;" +
				"end;");
		db.execute("drop procedure if exists mergeintogroup;");
		// needs to be set with inner joins
		db.execute("create definer=current_user procedure mergeintogroup(" +
				"in groupName varchar(255), in tomerge varchar(255)) " +
				"sql security invoker begin " +
				"DECLARE destID, tmp int;" +
				"SELECT f.group_id, count(DISTINCT fm.member_name) AS sz INTO destID, tmp FROM faction_id f "
				+ "INNER JOIN faction_member fm ON f.group_id = fm.group_id WHERE f.group_name = groupName GROUP BY f.group_id ORDER BY sz DESC LIMIT 1;" +
				"update ignore faction_member fm " // move all members from group From to To
				+ "inner join faction_id fii on fii.group_name = tomerge "
				+ "set fm.group_id = destID "
				+ "where fm.group_id = fii.group_id;" +
				/*"UPDATE faction_member fm " // update roles in dest on overlaps to be from merged group
				+ "INNER JOIN (SELECT faction_member fq JOIN faction_id fii ON fii.group_name = tomerge) fmerg "
				+ "ON fm.member_name = fmerge.member_name"
				+ "SET fm.role = fmerg.role WHERE fm.group_id = destID;" +*/
				"DELETE fm.* from faction_member fm " // Remove those "overlap" members left behind by IGNORE
				+ "inner join faction_id fi on fi.group_name = tomerge "
				+ "where fm.group_id = fi.group_id;" +
				/*"DELETE FROM subgroup s " // If this was a subgroup for someone, unlink. subgroups to new group.
				+ "WHERE sub_group_id IN " // TODO: might be double effort?
				+ "(SELECT group_id from faction_id where group_name = tomerge);" +*/ // handled using unlink
				"UPDATE subgroup s " // If it was a subgroup's supergroup, redirect
				+ "SET s.group_id = destID "
				+ "WHERE s.group_id IN "
				+ "(SELECT group_id from faction_id where group_name = tomerge);" +
				"delete from faction where group_name = tomerge;" + // Remove "faction" record of From
				"update faction_id fi " // Point "faction_id" records to TO's Name instead
				+ "inner join faction_id fii on fii.group_name = tomerge "
				+ "set fi.group_name = groupName "
				+ "where fi.group_id = fii.group_id;" +
				"end;");
		db.execute("drop procedure if exists createGroup;");
		db.execute("create definer=current_user procedure createGroup(" + 
				"in group_name varchar(255), " +
				"in founder varchar(36), " +
				"in password varchar(255), " +
				"in discipline_flags int(11)) " +
				"sql security invoker " +
				"begin" +
				" if (select (count(*) = 0) from faction_id q where q.group_name = group_name) is true then" + 
				"  insert into faction_id(group_name) values (group_name); " +
				"  insert into faction(group_name, founder, password, discipline_flags) values (group_name, founder, password, discipline_flags);" + 
				"  insert into faction_member (member_name, role, group_id) select founder, 'OWNER', f.group_id from faction_id f where f.group_name = group_name; " +
				"  select f.group_id from faction_id f where f.group_name = group_name; " +
				" end if; " +
				"end;");
	}
	
	private PreparedStatement version, updateVersion;
	
	private PreparedStatement createGroup, getGroup, getGroupById, getAllGroupsNames, deleteGroup, getAllGroupIds;
	
	private PreparedStatement addMember, getMembers, removeMember, updatePassword, updateOwner;
	
	private PreparedStatement addSubGroup, getSubGroups, getSuperGroup, removeSubGroup;
	
	private PreparedStatement mergeGroup;
	
	private PreparedStatement countGroups, countGroupsFromUUID;
	
	private PreparedStatement addAutoAcceptGroup, getAutoAcceptGroup, removeAutoAcceptGroup;
	
	private PreparedStatement setDefaultGroup, changeDefaultGroup, getDefaultGroup;
	
	private PreparedStatement loadGroupsInvitations, addGroupInvitation, removeGroupInvitation, loadGroupInvitation;
	
	private PreparedStatement getGroupNameFromRole, updateLastTimestamp, getPlayerType, getTimestamp;
	
	private PreparedStatement getGroupIDs;
	
	private PreparedStatement logNameChange, checkForNameChange;
	
	private PreparedStatement addPermission, getPermission, removePermission, registerPermission, getPermissionMapping, addPermissionById;
	
	private PreparedStatement addBlacklistMember, removeBlackListMember, getBlackListMembers;
	
	public void initializeStatements(){
		version = db.prepareStatement("select max(db_version) as db_version from db_version where plugin_name=?");
		updateVersion = db.prepareStatement("insert into db_version (db_version, update_time, plugin_name) values (?,?,?)"); 
		
		createGroup = db.prepareStatement("call createGroup(?,?,?,?)");
		getGroup = db.prepareStatement("select f.group_name, f.founder, f.password, f.discipline_flags, fi.group_id " +
				"from faction f "
				+ "inner join faction_id fi on fi.group_name = f.group_name "
				+ "where f.group_name = ?");
		getGroupIDs = db.prepareStatement("SELECT f.group_id, count(DISTINCT fm.member_name) AS sz FROM faction_id f "
				+ "INNER JOIN faction_member fm ON f.group_id = fm.group_id WHERE f.group_name = ? GROUP BY f.group_id ORDER BY sz DESC");
		getGroupById = db.prepareStatement("select f.group_name, f.founder, f.password, f.discipline_flags, fi.group_id " +
				"from faction f "
				+ "inner join faction_id fi on fi.group_id = ? "
				+ "where f.group_name = fi.group_name");
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
		
		// So this will link all instances (name/id pairs) of the subgroup to all instances (name/id pairs) of the supergroup.
		addSubGroup = db.prepareStatement(
				"INSERT INTO subgroup (group_id, sub_group_id) "
				+ "SELECT super.group_id, sub.group_id "
				+ "FROM faction_id super "
				+ "INNER JOIN faction_id sub "
				+ "ON sub.group_name = ? "
				+ "WHERE super.group_name = ?");
		
		// This undoes the above. It unlinks all instances (name/id pairs) of the subgroup from all instances (name/id pairs) of the supergroup.
		removeSubGroup = db.prepareStatement(
				"DELETE FROM subgroup "
				+ "WHERE group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?) "
				+ "AND sub_group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?)");
		
		// This lists all unique subgroups (names) for all instances (name/id pairs) of the supergroup.
		getSubGroups = db.prepareStatement(
				"SELECT DISTINCT sub.group_name FROM faction_id sub "
				+ "INNER JOIN faction_id super "
				+ "ON super.group_name = ? "
				+ "INNER JOIN subgroup other "
				+ "ON other.group_id = super.group_id "
				+ "WHERE sub.group_id = other.sub_group_id");
		
		// This lists all unique supergroups (names) which are parent(s) for all instances (name/id pairs) of the subgroup. 
		// I expect most implementations to ignore if this has multiple results; a "safe" implementation will check.
		getSuperGroup = db.prepareStatement(
				"SELECT DISTINCT f.group_name FROM faction_id f "
				+ "INNER JOIN faction_id sf ON sf.group_name = ? "
				+ "INNER JOIN subgroup sg ON sg.group_id = sf.group_id "
				+ "WHERE f.group_id = sg.sub_group_id");
		
		// returns count of unique names, but not (name / id pairs) of all groups.
		countGroups = db.prepareStatement("select count(DISTINCT group_name) as count from faction");
		
		// returns count of unique names of groups owned by founder
		countGroupsFromUUID = db.prepareStatement("select count(DISTINCT group_name) as count from faction where founder = ?");
		
		mergeGroup = db.prepareStatement("call mergeintogroup(?,?)");
		
		updatePassword = db.prepareStatement("update faction set `password` = ? "
				+ "where group_name = ?");
		
		updateOwner = db.prepareStatement("update faction set founder = ? "
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
		
		loadGroupsInvitations = db.prepareStatement("select uuid, groupName, role from group_invitation");
		
		addGroupInvitation = db.prepareStatement("insert into group_invitation(uuid, groupName, role) values(?, ?, ?) on duplicate key update role=values(role), date=now();");
		
		removeGroupInvitation = db.prepareStatement("delete from group_invitation where uuid = ? and groupName = ?");
		
		loadGroupInvitation = db.prepareStatement("select role from group_invitation where uuid = ? and groupName = ?");
		
		// Gets all unique names (not instances) of groups having this member at that role.
		getGroupNameFromRole = db.prepareStatement("SELECT DISTINCT faction_id.group_name FROM faction_member "
								+ "inner join faction_id on faction_member.group_id = faction_id.group_id "
								+ "WHERE member_name = ? "
								+ "AND role = ?;");
		
		// Gets the "most recent" updated group from all groups that share the name.
		getTimestamp = db.prepareStatement("SELECT MAX(faction.last_timestamp) FROM faction "
								+ "WHERE group_name = ?;");
		
		// updates "most recent" of all groups with a given name.
		updateLastTimestamp = db.prepareStatement("UPDATE faction SET faction.last_timestamp = NOW() "
								+ "WHERE group_name = ?;");
		
		// Breaking the pattern. Here we directly access a role based on _group ID_ rather then group_name. TODO: evaluate safety.
		getPlayerType = db.prepareStatement("SELECT role FROM faction_member "
						+ "WHERE group_id = ? "
                        + "AND member_name = ?;");
		logNameChange = db.prepareStatement("insert into nameLayerNameChanges (uuid,oldName,newName) values(?,?,?);");
		checkForNameChange = db.prepareStatement("select * from nameLayerNameChanges where uuid=?;");
		
		addPermission = db.prepareStatement("insert into permissionByGroup(group_id,role,perm_id) select g.group_id, ?, ? from faction_id g where g.group_name = ?;");
		addPermissionById = db.prepareStatement("insert into permissionByGroup(group_id,role,perm_id) values(?,?,?);");
		getPermission = db.prepareStatement("select pg.role,pg.perm_id from permissionByGroup pg inner join faction_id fi on fi.group_name=? "
				+ "where pg.group_id = fi.group_id");
		removePermission = db.prepareStatement("delete from permissionByGroup where group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?) and role=? and perm_id=?;");
		registerPermission = db.prepareStatement("insert into permissionIdMapping(perm_id,name) values(?,?);"); 
		getPermissionMapping = db.prepareStatement("select * from permissionIdMapping;");
		
		addBlacklistMember = db.prepareStatement("insert into blacklist(group_id, member_name) select group_id,? from faction_id where group_name=?;");
		removeBlackListMember = db.prepareStatement("delete from blacklist WHERE group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?) and member_name=?;");
		getBlackListMembers = db.prepareStatement("select b.member_name from blacklist b inner join faction_id fi on fi.group_name=? where b.group_id=fi.group_id;");
		
		getAllGroupIds = db.prepareStatement("select group_id from faction_id");
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
			plugin.getLogger().log(Level.WARNING, "Problem accessing db_version table", e);
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
			plugin.getLogger().log(Level.WARNING, "Problem updating version", e);
		}
		return ++version;
	}
	
	public synchronized int createGroup(String group, UUID owner, String password){
		NameLayerPlugin.reconnectAndReintializeStatements();
		int ret = -1;
		try {
			String own = null;
			if (owner != null)
				own = owner.toString();
			createGroup.setString(1, group);
			createGroup.setString(2, own);
			createGroup.setString(3, password);
			createGroup.setInt(4, 0);
			ResultSet set = createGroup.executeQuery();
			ret = set.next() ? set.getInt("f.group_id") : -1;
			plugin.getLogger().log(Level.INFO, "Created group {0} w/ id {1} for {2}", 
					new Object[] {group, ret, own});
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem creating group " + group, e);
			ret = -1;
		}
		
		return ret;
	}
	
	public synchronized Group getGroup(String groupName){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			getGroup.clearParameters();
			getGroup.setString(1, groupName);
			try (ResultSet set = getGroup.executeQuery()) {
				if (!set.next()) {
					return null;
				}
				
				String name = set.getString(1);
				String uuid = set.getString(2);
				UUID owner = (uuid != null) ? UUID.fromString(uuid) : null;
				boolean discipline = set.getInt(4) != 0;
				String password = set.getString(3);
				int id = set.getInt(5);
				
				Group g = new Group(name, owner, discipline, password, id);;
				// other group IDs cached via the constructor.
				return g;
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting group " + groupName, e);
		}
		return null;
	}
	
	public synchronized Group getGroup(int groupId){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			getGroupById.setInt(1, groupId);
			ResultSet set = getGroupById.executeQuery();
			if (!set.next()) return null;
			String name = set.getString(1);
			String uuid = set.getString(2);
			UUID owner = null;
			if (uuid != null)
				owner = UUID.fromString(uuid);
			boolean dis = set.getInt(4) != 0;
			String password = set.getString(3);
			int id = set.getInt(5);
			Group g = new Group(name, owner, dis, password, id);
			return g;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting group " + groupId, e);
		}
		return null;
	}
	
	public synchronized List<String> getGroupNames(UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
		List<String> groups = new ArrayList<String>();
		try {
			getAllGroupsNames.setString(1, uuid.toString());
			ResultSet set = getAllGroupsNames.executeQuery();
			while(set.next()) {
				groups.add(set.getString(1));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting player's groups " + uuid, e);
		}
		return groups;
	}
	
	public synchronized List<String> getGroupNames(UUID uuid, String role){
		NameLayerPlugin.reconnectAndReintializeStatements();
		List<String> groups = new ArrayList<String>();
		try {
			getGroupNameFromRole.setString(1, uuid.toString());
			getGroupNameFromRole.setString(2, role);
			ResultSet set = getGroupNameFromRole.executeQuery();
			while(set.next()) {
				groups.add(set.getString(1));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting player " + uuid + " groups by role " + role, e);
		}
		return groups;
	}
	
	public synchronized Timestamp getTimestamp(String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		Timestamp timestamp = null;
		try{
			getTimestamp.setString(1, group);
			ResultSet set = getTimestamp.executeQuery();
			if(set.next())
				timestamp = set.getTimestamp(1);
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting group timestamp " + group, e);
		}
		
		return timestamp;
	}
	
	public synchronized PlayerType getPlayerType(int groupid, UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
		PlayerType ptype = null;
		try {
			getPlayerType.setInt(1, groupid);
			getPlayerType.setString(2, uuid.toString());
			ResultSet set = getPlayerType.executeQuery();
			if(set.next()){
				ptype = PlayerType.getPlayerType(set.getString(1));
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting player " + uuid + " type within group " + groupid, e);
		}
		return ptype;
	}
	
	public synchronized void updateTimestamp(String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			updateLastTimestamp.setString(1, group);
			updateLastTimestamp.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem updating timestamp for group " + group, e);
		}
	}
	
	public synchronized void deleteGroup(String groupName){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			deleteGroup.setString(1, groupName);
			deleteGroup.setString(2, NameLayerPlugin.getSpecialAdminGroup());
			deleteGroup.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem deleting group " + groupName, e);
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
			plugin.getLogger().log(Level.WARNING, "Problem adding " + member + " as " + role.toString() 
					+ " to group " + faction, e);
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
				if (uuid == null) {
					continue;
				}
				members.add(UUID.fromString(uuid));
			}
			return members;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting all " + role.toString() 
					+ " for group " + groupName, e);
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
			plugin.getLogger().log(Level.WARNING, "Problem removing " + member + " from group " + group, e);
		}
	}
	
	public synchronized void addSubGroup(String group, String subGroup){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			addSubGroup.setString(1, subGroup);
			addSubGroup.setString(2, group);
			addSubGroup.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem adding subgroup " + subGroup
					+ " to group " + group, e);
		}
	}
	
	public synchronized List<Group> getSubGroups(String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		List<Group> groups = new ArrayList<Group>();
		try {
			getSubGroups.clearParameters();
			getSubGroups.setString(1, group);
			
			List<String> subgroups = Lists.newArrayList();
			try (ResultSet set = getSubGroups.executeQuery()) {
				while (set.next()) {
					subgroups.add(set.getString(1));
				}
			}
			
			for (String groupname : subgroups) {				
				Group g = null;
				if (GroupManager.hasGroup(groupname)) {
					g = GroupManager.getGroup(groupname);
				} else {
					g = getGroup(groupname);
				}
				
				if (g != null) {
					groups.add(g);
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting subgroups for group " + group, e);
		}
		return groups;
	}
	
	public synchronized Group getSuperGroup(String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			getSuperGroup.clearParameters();
			getSuperGroup.setString(1, group);
			try (ResultSet set = getSuperGroup.executeQuery()) {
				if (!set.next()) {
					return null;
				}
				String supergroup = set.getString(1);
				if (GroupManager.hasGroup(supergroup)) {
					return GroupManager.getGroup(supergroup);
				} else {
					return getGroup(supergroup);
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting superGroup for group " + group, e);
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
			plugin.getLogger().log(Level.WARNING, "Removing subgroup " + subGroup
					+ " from group " + group, e);
		}
	}

	public synchronized void addPermission(String groupName, String role, List <PermissionType> perms){
		NameLayerPlugin.reconnectAndReintializeStatements();
		for(PermissionType perm : perms) {
			try {
				addPermission.setString(1, role);
				addPermission.setInt(2, perm.getId());
				addPermission.setString(3, groupName);
				addPermission.execute();
			} catch (SQLException e) {
				plugin.getLogger().log(Level.WARNING, "Problem adding " + role + " with " + perms
						+ " to group " + groupName, e);
			
			}
		}
	}
	
	public synchronized Map<PlayerType, List<PermissionType>> getPermissions(String group){
		NameLayerPlugin.reconnectAndReintializeStatements();
		Map<PlayerType, List<PermissionType>> perms = new HashMap<PlayerType, List<PermissionType>>();
		try {
			getPermission.setString(1, group);
			ResultSet set = getPermission.executeQuery();
			while(set.next()){
				PlayerType type = PlayerType.getPlayerType(set.getString(1));
				List<PermissionType> listPerm = perms.get(type);
				if (listPerm == null) {
					listPerm = new ArrayList<PermissionType>();
					perms.put(type, listPerm);
				}
				int id = set.getInt(2);
				PermissionType perm = PermissionType.getPermission(id);
				if (perm != null && !listPerm.contains(perm)) {
					listPerm.add(perm);
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting permissions for group " + group, e);
		}
		return perms;
	}
	
	public synchronized void removePermission(String group, PlayerType pType, PermissionType perm){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			removePermission.setString(1, group);
			removePermission.setString(2, pType.name());
			removePermission.setInt(3, perm.getId());
			removePermission.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem removing permissions for group " + group
					+ " on playertype " + pType.name(), e);
		}
	}
	
	public synchronized void registerPermission(PermissionType perm) {
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			registerPermission.setInt(1, perm.getId());
			registerPermission.setString(2, perm.getName());
			registerPermission.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem register permission " + perm.getName(), e);
		}
	}
	
	public synchronized Map<Integer, String> getPermissionMapping() {
		NameLayerPlugin.reconnectAndReintializeStatements();
		Map <Integer,String> perms = new TreeMap<Integer, String>();
		try {
			ResultSet res = getPermissionMapping.executeQuery();
			while (res.next()) {
				perms.put(res.getInt(1), res.getString(2));
			}
		}
		catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting permissions from db", e);
		}
		return perms;
	}
	
	public synchronized void addNewDefaultPermission(List <PlayerType> playerTypes, PermissionType perm) {
		try {
			ResultSet set = getAllGroupIds.executeQuery();
			List <PermissionType> perms = new LinkedList<PermissionType>();
			perms.add(perm);
			while(set.next()) {
				int groupId = set.getInt(1);
				for(PlayerType pType:playerTypes) {
					addPermissionById.setInt(1, groupId);
					addPermissionById.setString(2, pType.name());
					addPermissionById.setInt(3, perm.getId());
					addPermissionById.execute();
				}
			}
		}
		catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Error initiating default perms for permission " + perm + " for player types " + playerTypes, e);
		}
	}
	
	public synchronized int countGroups(){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			ResultSet set = countGroups.executeQuery();
			return set.next() ? set.getInt("count") : 0;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem counting groups", e);
		}
		return 0;
	}
	
	public synchronized int countGroups(UUID uuid){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try{
			countGroupsFromUUID.setString(1, uuid.toString());
			ResultSet set = countGroupsFromUUID.executeQuery();
			return set.next() ? set.getInt("count") : 0;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem counting groups for " + uuid, e);
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
			plugin.getLogger().log(Level.WARNING, "Problem merging group " + toMerge
					+ " into " + groupName, e);
		}
	}
	
	public synchronized void updatePassword(String groupName, String password){
		NameLayerPlugin.reconnectAndReintializeStatements();
		try {
			updatePassword.setString(1, password);
			updatePassword.setString(2, groupName);
			updatePassword.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem updating password for group " + groupName, e);
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
			plugin.getLogger().log(Level.WARNING, "Problem setting autoaccept for " + uuid, e);
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
			plugin.getLogger().log(Level.WARNING, "Problem getting autoaccept for " + uuid, e);
		}
		return false;
	}
	
	public synchronized void removeAutoAcceptGroup(UUID uuid){
		try {
			removeAutoAcceptGroup.setString(1, uuid.toString());
			removeAutoAcceptGroup.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem removing autoaccept for " + uuid, e);
		}
	}
	
	public synchronized void setDefaultGroup(UUID uuid, String groupName){
		try {
			setDefaultGroup.setString(1, uuid.toString());
			setDefaultGroup.setString(2, groupName );
			setDefaultGroup.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem setting user " + uuid 
					+ " default group to " + groupName, e);
		}
	}
	
	public synchronized void changeDefaultGroup(UUID uuid, String groupName){
		try {
			changeDefaultGroup.setString(1, groupName);
			changeDefaultGroup.setString(2, uuid.toString());
			changeDefaultGroup.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem changing user " + uuid
					+ " default group to " + groupName, e);
		}
	}

	public synchronized String getDefaultGroup(UUID uuid) {
		try {
			getDefaultGroup.setString(1, uuid.toString());
			ResultSet set = getDefaultGroup.executeQuery();
			if(!set.next()) return null;
			String group = set.getString(1);
			return group;
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem getting default group for " + uuid, e);
		}
		return null;
	}
	
	/**
	 * Use this method to override the current founder of a group.
	 * @param uuid This is the uuid of the player.
	 * @param group This is the group that we are changing the founder of.
	 */
	public synchronized void setFounder(UUID uuid, Group group) {
		try {
			updateOwner.setString(1, uuid.toString());
			updateOwner.setString(2, group.getName());
			updateOwner.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem setting founder of group " + group.getName() 
					+ " to " + uuid, e);
		}
	}
	
	public synchronized void addGroupInvitation(UUID uuid, String groupName, String role){
		try {
			addGroupInvitation.setString(1, uuid.toString());
			addGroupInvitation.setString(2, groupName);
			addGroupInvitation.setString(3, role); 
			addGroupInvitation.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem adding group " + groupName + " invite for "
					+ uuid + " with role " + role, e);
		}
	}
	
	public synchronized void removeGroupInvitation(UUID uuid, String groupName){
		try {
			removeGroupInvitation.setString(1, uuid.toString());
			removeGroupInvitation.setString(2, groupName);
			removeGroupInvitation.execute();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem removing group " + groupName + " invite for "
					+ uuid, e);
		}
	}
	
	
	/**
	 * Use this method to load a specific invitation to a group without the notification. 
	 * @param playerUUID The uuid of the invited player.
	 * @param group The group the player was invited to. 
	 */
	public synchronized void loadGroupInvitation(UUID playerUUID, Group group){
		if(group == null){
			return;
		}
		
		try {
			loadGroupInvitation.setString(1, playerUUID.toString());
			loadGroupInvitation.setString(2, group.getName());
			ResultSet set = loadGroupInvitation.executeQuery();
			while(set.next()){
				String role = set.getString("role");
				PlayerType type = null;
				if(role != null){
					type = PlayerType.getPlayerType(role);
				}
				group.addInvite(playerUUID, type, false);
			}
		} catch(SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem loading group " + group.getName() 
					+ " invite for " + playerUUID, e);
		}
	}
	
	/**
	 * Use this method to load all invitations to all groups.
	 */
	public synchronized void loadGroupsInvitations(){
		try {
			ResultSet set = loadGroupsInvitations.executeQuery();
			while(set.next()){
				String uuid = set.getString("uuid");
				String group = set.getString("groupName");
				String role = set.getString("role");
				UUID playerUUID = null;
				if (uuid != null){
					playerUUID = UUID.fromString(uuid);
				}
				Group g = null;
				if(group != null){
					g = GroupManager.getGroup(group);
				}
				PlayerType type = null;
				if(role != null){
					type = PlayerType.getPlayerType(role);
				}
				
				if(g != null){
					g.addInvite(playerUUID, type, false);
					PlayerListener.addNotification(playerUUID, g);
				}
			}
		} catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Problem loading all group invitations", e);
		}
	}
	
	public synchronized void logNameChange(UUID uuid, String oldName, String newName) {
		try {
			logNameChange.setString(1, uuid.toString());
			logNameChange.setString(2, oldName);
			logNameChange.setString(3, newName);
			logNameChange.execute();
		}
		catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to log a name change for {0} from {1} -> {2}", new Object[]{uuid, oldName, newName});
			plugin.getLogger().log(Level.WARNING, "Exception during change.", e);
		}
	}
	
	public synchronized boolean hasChangedNameBefore(UUID uuid) {
		try {
			checkForNameChange.setString(1, uuid.toString());
			ResultSet set = checkForNameChange.executeQuery();
			if (set.next()) {
				return true;
			}
			return false;
		}
		catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Failed to check if {0} has previously changed names", uuid);
			plugin.getLogger().log(Level.WARNING, "Exception during check.", e);
			//just to make sure
			return true;
		}
	}
	
	public synchronized void addBlackListMember(String groupName, UUID player) {
		try {
			addBlacklistMember.setString(1, player.toString());
			addBlacklistMember.setString(2, groupName);
			addBlacklistMember.execute();
		}
		catch(SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Unable to add black list member " + player + " to group " + groupName, e);
		}
	}
	
	public synchronized void removeBlackListMember(String groupName, UUID player) {
		try {
			removeBlackListMember.setString(1, groupName);
			removeBlackListMember.setString(2, player.toString());
			removeBlackListMember.execute();
		}
		catch(SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Unable to remove black list member " + player + " to group " + groupName, e);
		}
	}
	
	public synchronized Set<UUID> getBlackListMembers(String groupName) {
		Set<UUID> uuids = new HashSet<UUID>();
		try {
			getBlackListMembers.setString(1, groupName);
			ResultSet set = getBlackListMembers.executeQuery();
			while (set.next()) {
				uuids.add(UUID.fromString(set.getString(1)));
			}
		}
		catch (SQLException e) {
			plugin.getLogger().log(Level.WARNING, "Unable to retrieve black list members for group " + groupName, e);
		}
		return uuids;
	}

	/**
	 * Gets all the IDs for this group name, sorted by "size" in membercount.
	 * Ideally only one groupname/id has members and the rest are shadows, but in any case
	 * we arbitrarily define primacy as the one with the most members for ease of accounting
	 * and backwards compatibility.
	 *  
	 * @param groupName
	 * @return
	 */
	public List<Integer> getAllIDs(String groupName) {
		if (groupName == null) {
			return null;
		}
		try {
			getGroupIDs.setString(1, groupName);
			ResultSet set = getGroupIDs.executeQuery();
			LinkedList<Integer> ids = new LinkedList<Integer>();
			
			while (set.next()) {
				ids.add(set.getInt(1));
			}
			
			return ids;
		} catch (SQLException se) {
			plugin.getLogger().log(Level.WARNING, "Unable to fully load group ID set", se);
		}
		return null;
	}
	
}
