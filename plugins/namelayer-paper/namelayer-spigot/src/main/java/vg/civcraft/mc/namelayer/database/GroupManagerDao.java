package vg.civcraft.mc.namelayer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.google.common.collect.Lists;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;
/**
 * First guinea pig of conversion to ManagedDatasource.
 *
 */
public class GroupManagerDao {
	private Logger logger;
	private ManagedDatasource db;
	protected NameLayerPlugin plugin = NameLayerPlugin.getInstance();
	
	private static final String removeCycles = "delete a from subgroup a join faction_id a2 ON a.group_id = a2.group_id "
				+ "JOIN subgroup b JOIN faction_id b2 on b.sub_group_id = b2.group_id where a2.group_name = b2.group_name;";
	private static final String createGroup = "call createGroup(?,?,?,?)";
	private static final String getGroup = "select f.group_name, f.founder, f.password, f.discipline_flags, fi.group_id, f.last_timestamp " +
				"from faction f "
				+ "inner join faction_id fi on fi.group_name = f.group_name "
				+ "where f.group_name = ?";
	private static final String getGroupIDs = "SELECT f.group_id, count(DISTINCT fm.member_name) AS sz FROM faction_id f "
				+ "INNER JOIN faction_member fm ON f.group_id = fm.group_id WHERE f.group_name = ? GROUP BY f.group_id ORDER BY sz DESC";
	private static final String getGroupById = "select f.group_name, f.founder, f.password, f.discipline_flags, fi.group_id, f.last_timestamp " +
				"from faction f "
				+ "inner join faction_id fi on fi.group_id = ? "
				+ "where f.group_name = fi.group_name";
	private static final String getAllGroupsNames = "select f.group_name from faction_id f "
				+ "inner join faction_member fm on f.group_id = fm.group_id "
				+ "where fm.member_name = ?";
	private static final String deleteGroup = "call deletegroupfromtable(?, ?)";

	private static final String addMember = "insert into faction_member(" +
				"group_id, member_name, role) select group_id, ?, ? from "
				+ "faction_id where group_name = ?";
	private static final String getMembers = "select fm.member_name from faction_member fm "
				+ "inner join faction_id id on id.group_name = ? "
				+ "where fm.group_id = id.group_id and fm.role = ?";
	private static final String removeMember = "delete fm.* from faction_member fm "
				+ "inner join faction_id fi on fi.group_id = fm.group_id "
				+ "where fm.member_name = ? and fi.group_name =?";
		
	private static final String removeAllMembers = "delete fm.* from faction_member fm "
				+ "inner join faction_id fi on fi.group_id = fm.group_id "
				+ "where fi.group_name =?";
		
		// So this will link all instances (name/id pairs) of the subgroup to all instances (name/id pairs) of the supergroup.
	private static final String addSubGroup = "INSERT INTO subgroup (group_id, sub_group_id) "
				+ "SELECT super.group_id, sub.group_id "
				+ "FROM faction_id super "
				+ "INNER JOIN faction_id sub "
				+ "ON sub.group_name = ? "
				+ "WHERE super.group_name = ?";
		
		// This undoes the above. It unlinks all instances (name/id pairs) of the subgroup from all instances (name/id pairs) of the supergroup.
	private static final String removeSubGroup ="DELETE FROM subgroup "
				+ "WHERE group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?) "
				+ "AND sub_group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?)";
		
		// This lists all unique subgroups (names) for all instances (name/id pairs) of the supergroup.
	private static final String getSubGroups = "SELECT DISTINCT sub.group_name FROM faction_id sub "
				+ "INNER JOIN faction_id super "
				+ "ON super.group_name = ? "
				+ "INNER JOIN subgroup other "
				+ "ON other.group_id = super.group_id "
				+ "WHERE sub.group_id = other.sub_group_id";
		
		// This lists all unique supergroups (names) which are parent(s) for all instances (name/id pairs) of the subgroup. 
		// I expect most implementations to ignore if this has multiple results; a "safe" implementation will check.
	private static final String getSuperGroup ="SELECT DISTINCT f.group_name FROM faction_id f "
				+ "INNER JOIN faction_id sf ON sf.group_name = ? "
				+ "INNER JOIN subgroup sg ON sg.group_id = sf.group_id "
				+ "WHERE f.group_id = sg.sub_group_id";
		
		// returns count of unique names, but not (name / id pairs) of all groups.
	private static final String countGroups = "select count(DISTINCT group_name) as count from faction";
		
		// returns count of unique names of groups owned by founder
	private static final String countGroupsFromUUID = "select count(DISTINCT group_name) as count from faction where founder = ?";
		
	private static final String mergeGroup = "call mergeintogroup(?,?)";
		
	private static final String updatePassword = "update faction set `password` = ? "
				+ "where group_name = ?";
		
	private static final String updateOwner = "update faction set founder = ? "
				+ "where group_name = ?";
		
	private static final String updateDisciplined = "update faction set discipline_flags = ? "
				+ "where group_name = ?";
		
	private static final String addAutoAcceptGroup = "insert into toggleAutoAccept(uuid)"
				+ "values(?)";
	private static final String getAutoAcceptGroup = "select uuid from toggleAutoAccept "
				+ "where uuid = ?";
	private static final String removeAutoAcceptGroup = "delete from toggleAutoAccept where uuid = ?";
		
	private static final String loadAllAutoAcceptGroup = "select uuid from toggleAutoAccept;";
		
	private static final String setDefaultGroup = "insert into default_group values(?, ?)";
		
	private static final String changeDefaultGroup = "update default_group set defaultgroup = ? where uuid = ?";
	
		
	private static final String getDefaultGroup = "select defaultgroup from default_group "
				+ "where uuid = ?";
	private static final String getAllDefaultGroups = "select uuid,defaultgroup from default_group";
		
	private static final String loadGroupsInvitations = "select uuid, groupName, role from group_invitation";
		
	private static final String addGroupInvitation = "insert into group_invitation(uuid, groupName, role) values(?, ?, ?) on duplicate key update role=values(role), date=now();";
		
	private static final String removeGroupInvitation = "delete from group_invitation where uuid = ? and groupName = ?";
		
	private static final String loadGroupInvitation = "select role from group_invitation where uuid = ? and groupName = ?";
		
	private static final String loadGroupInvitationsForGroup = "select uuid,role from group_invitation where groupName=?";
		
		// Gets all unique names (not instances) of groups having this member at that role.
	private static final String getGroupNameFromRole = "SELECT DISTINCT faction_id.group_name FROM faction_member "
								+ "inner join faction_id on faction_member.group_id = faction_id.group_id "
								+ "WHERE member_name = ? "
								+ "AND role = ?;";
		
		
		// updates "most recent" of all groups with a given name.
	private static final String updateLastTimestamp = "UPDATE faction SET faction.last_timestamp = NOW() "
								+ "WHERE group_name = ?;";
		
		// Breaking the pattern. Here we directly access a role based on _group ID_ rather then group_name. TODO: evaluate safety.
	private static final String getPlayerType = "SELECT role FROM faction_member "
						+ "WHERE group_id = ? "
                        + "AND member_name = ?;";
	private static final String logNameChange = "insert into nameLayerNameChanges (uuid,oldName,newName) values(?,?,?);";
	private static final String checkForNameChange = "select * from nameLayerNameChanges where uuid=?;";
		
	private static final String addPermission = "insert into permissionByGroup(group_id,role,perm_id) select g.group_id, ?, ? from faction_id g where g.group_name = ?;";
	private static final String addPermissionById = "insert into permissionByGroup(group_id,role,perm_id) values(?,?,?);";
	
	private static final String addDefaultPermission = "insert into permissionByGroup(group_id,role,perm_id) select group_id,?,? from faction_id group by group_id";

	private static final String getPermission = "select pg.role,pg.perm_id from permissionByGroup pg inner join faction_id fi on fi.group_name=? "
				+ "where pg.group_id = fi.group_id";
	private static final String removePermission = "delete from permissionByGroup where group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?) and role=? and perm_id=?;";
	private static final String registerPermission = "insert into permissionIdMapping(perm_id,name) values(?,?);"; 
	private static final String getPermissionMapping = "select * from permissionIdMapping;";
		
	private static final String addBlacklistMember = "insert into blacklist(group_id, member_name) select group_id,? from faction_id where group_name=?;";
	private static final String removeBlackListMember = "delete from blacklist WHERE group_id IN (SELECT group_id FROM faction_id WHERE group_name = ?) and member_name=?;";
	private static final String getBlackListMembers = "select b.member_name from blacklist b inner join faction_id fi on fi.group_name=? where b.group_id=fi.group_id;";
		
	private static final String getAllGroupIds = "select group_id from faction_id";


	public GroupManagerDao(Logger logger, ManagedDatasource db){
		this.logger = logger;
		this.db = db;
	}
	
	/**
	 * Not going to lie, I can't make heads or tails out of half of this.
	 */
	public void registerMigrations() {
		db.registerMigration(2, true, 
				"alter table faction drop `version`;",
				"alter table faction add type int default 0;",
				"create table faction_id("
					+ "group_id int not null AUTO_INCREMENT,"
					+ "group_name varchar(255),"
					+ "primary key(group_id));",
				"create table if not exists permissions(" +
					"group_id varchar(255) not null," +
					"role varchar(40) not null," +
					"tier varchar(255) not null," +
					"unique key (group_id, role));",
				"delete from faction where `name` is null;",
				"delete from faction_member where faction_name is null;",
				"delete from moderator where faction_name is null;",
				"insert into faction_id (group_name) select `name` from faction;",
				"alter table faction add group_name varchar(255) default null;",
				"update faction g set g.group_name = g.name;",
				"alter table faction drop `name`;",
				"alter table faction add primary key group_primary_key (group_name);",
				"drop table personal_group;",
				"alter table faction_member change member_name member_name varchar(36);",
				"alter table faction_member add role varchar(10) not null default 'MEMBERS';",
				"alter table faction_member add group_id int not null;",
				"delete fm.* from faction_member fm where not exists " // deletes any non faction_id entries.
					+ "(select fi.group_id from faction_id fi "
					+ "where fi.group_name = fm.faction_name limit 1);",
				"update faction_member fm set fm.group_id = (select fi.group_id from faction_id fi "
					+ "where fi.group_name = fm.faction_name limit 1);",
				"alter table faction_member add unique key uq_meber_faction(member_name, group_id);",
				"alter table faction_member drop index uq_faction_member_1;",
				"alter table faction_member drop faction_name;",
				"insert ignore into faction_member (group_id, member_name, role)" +
					"select g.group_id, m.member_name, 'MODS' from moderator m "
					+ "inner join faction_id g on g.group_name = m.faction_name",
				"insert into faction_member (group_id, member_name, role)"
					+ "select fi.group_id, f.founder, 'OWNER' from faction f "
					+ "inner join faction_id fi on fi.group_name = f.group_name;",
				"drop table moderator;",
				"alter table faction change `type` group_type varchar(40) not null default 'PRIVATE';",
				"update faction set group_type = 'PRIVATE';",
				"alter table faction change founder founder varchar(36);",
				"insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'OWNER', "
					+ "'DOORS CHESTS BLOCKS OWNER ADMINS MODS MEMBERS PASSWORD SUBGROUP PERMS DELETE MERGE LIST_PERMS TRANSFER CROPS' "
					+ "from faction_id f;",
				"insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'ADMINS', "
					+ "'DOORS CHESTS BLOCKS MODS MEMBERS PASSWORD LIST_PERMS CROPS' "
					+ "from faction_id f;",	
				"insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'MODS', "
					+ "'DOORS CHESTS BLOCKS MEMBERS CROPS' "
					+ "from faction_id f;",
				"insert into permissions (group_id, role, tier) "
					+ "select f.group_id, 'MEMBERS', "
					+ "'DOORS CHESTS' "
					+ "from faction_id f;");
				
		db.registerMigration(1, false, 
				new Callable<Boolean>() {
					@Override
					public Boolean call() {
						// Procedures may not be initialized yet.
						Bukkit.getScheduler().scheduleSyncDelayedTask(NameLayerPlugin.getInstance(), new Runnable(){
							@Override
							public void run() {
								Group g = getGroup(NameLayerPlugin.getSpecialAdminGroup());
								if (g == null) {
									createGroup(NameLayerPlugin.getSpecialAdminGroup(), null, null);
								} else {
									removeAllMembers(g.getName());
								}
							}
						});
						return true;
					}
				},
				"create table if not exists faction_id("
					+ "group_id int not null AUTO_INCREMENT,"
					+ "group_name varchar(255),"
					+ "primary key(group_id)) charset=latin1;",
			/* In the faction table we use group names. This is important because when merging other groups
			 * it will create multiple same group_names within the faction_id table. The benefits are that when other
			 * tables come looking for a group they always find the right one due to their only being one group with a name.
			 */
				"create table if not exists faction(" +
					"group_name varchar(255)," +
					"founder varchar(36)," +
					"password varchar(255) default null," +
					"discipline_flags int(11) not null," +
					"group_type varchar(40) not null default 'PRIVATE'," +
					"primary key(group_name)) charset=latin1;",
				"create table if not exists faction_member(" +
					"group_id int not null," +
					"member_name varchar(36)," +
					"role varchar(10) not null default 'MEMBERS'," +
					"unique key (group_id, member_name)) charset=latin1;",
				"create table if not exists blacklist(" +
					"member_name varchar(36) not null," +
					"group_id varchar(255) not null) charset=latin1;",
				"create table if not exists permissions(" +
					"group_id varchar(255) not null," +
					"role varchar(40) not null," +
					"tier varchar(255) not null," +
					"unique key (group_id, role)) charset=latin1;",
				"create table if not exists subgroup(" +
					"group_id varchar(255) not null," +
					"sub_group_id varchar(255) not null," +
					"unique key (group_id, sub_group_id)) charset=latin1;");
			

		db.registerMigration(3, false, 
				"create table if not exists toggleAutoAccept("
					+ "uuid varchar(36) not null,"
					+ "primary key key_uuid(uuid));");

		db.registerMigration(4, false, 
				"alter table faction_id add index `faction_id_index` (group_name);");

		db.registerMigration(5, false, 
				"alter table faction_member add index `faction_member_index` (group_id);");

		db.registerMigration(6, false,
				"create table if not exists default_group(" + 
					"uuid varchar(36) NOT NULL," +
					"defaultgroup varchar(255) NOT NULL,"+
					"primary key key_uuid(uuid))");

		db.registerMigration(7, false,
				"create table if not exists group_invitation(" + 
					"uuid varchar(36) NOT NULL," +
					"groupName varchar(255) NOT NULL,"+
					"role varchar(10) NOT NULL default 'MEMBERS'," +
					"date datetime NOT NULL default NOW()," +
					"constraint UQ_uuid_groupName unique(uuid, groupName)) charset=latin1;");

		db.registerMigration(8, false,
				"alter table faction add last_timestamp datetime NOT NULL default NOW();");

		db.registerMigration(9, false,
				"alter table blacklist modify column group_id int;",
				"alter table permissions modify column group_id int;",
				"alter table subgroup modify column group_id int, modify column sub_group_id int;");

		db.registerMigration(10, false,
				"create table if not exists nameLayerNameChanges(uuid varchar(36) not null, oldName varchar(32) not null, newName varchar(32) not null, primary key(uuid)) charset=latin1;;");

		db.registerMigration(11, false,
				new Callable<Boolean>() {
					@Override
					public Boolean call() {
						try (Connection connection = db.getConnection();
								PreparedStatement permInit = connection.prepareStatement(addPermissionById);
								PreparedStatement permReg = connection.prepareStatement(registerPermission); ) {
							Map <String, Integer> permIds = new HashMap<>();

							List<Object[]> unspool = new ArrayList<>();
							int maximumId = 0;
							try (Statement getOldPerms = connection.createStatement();
									ResultSet res = getOldPerms.executeQuery("SELECT * FROM permissions")) {
								while(res.next()) {
									unspool.add(new Object[]{res.getInt(1), res.getString(2), res.getString(3)});
									if (res.getInt(1) > maximumId) maximumId = res.getInt(1);
								}
							} catch (SQLException e) {
								logger.log(Level.SEVERE, "Failed to get old permissions, things might get a little wonky now.", e);
							}
							
							int maxBatch = 100, count = 0, regadd = 0;
							
							for (Object[] spool : unspool) {
								int groupId = (int) spool[0];
								String role = (String) spool[1];
								String [] perms = ((String) spool[2]).split(" ");
								for(String p : perms) {
									if (!p.equals("")) {
										if(p.equals("BLOCKS")) {
											//this permission was renamed and now includes less functionality than previously
											p = "REINFORCE";
										}
										Integer id = permIds.get(p);
										if (id == null) {
											//unknown perm, so we register it
											id = ++maximumId; // prefix mutator!
											
											permReg.setInt(1, maximumId);
											permReg.setString(2, p);
											permReg.addBatch(); // defer insert.
											
											permIds.put(p, id);
											regadd ++;
										}
										permInit.setInt(1, groupId);
										permInit.setString(2, role);
										permInit.setInt(3, id);
										permInit.addBatch();
										count ++;
										
										if (count > maxBatch) {
											permInit.executeBatch();
											// TODO process warnings / errors
											count = 0;
										}
									}
								}
							}
							if (count > 0) {
								permInit.executeBatch();
								// TODO process warnings / errors
							}
							
							if (regadd > 0) {
								permReg.executeBatch();
								// TODO process warnings / errors
							}

						} catch (SQLException se) {
							logger.log(Level.SEVERE, "Something fatal occured while updating permissions", se);
							return false;
						}
						return true;
					}
				},
				"create table if not exists permissionByGroup(group_id int not null,role varchar(40) not null,perm_id int not null, primary key(group_id,role,perm_id));",
				"create table if not exists permissionIdMapping(perm_id int not null, name varchar(64) not null,primary key(perm_id));",
				"alter table faction drop column group_type");


		db.registerMigration(12, false, 
				"UPDATE faction SET group_name=REPLACE(group_name,'|','');");
		
		db.registerMigration(13, false,
				"drop procedure if exists deletegroupfromtable;",
				"create definer=current_user procedure deletegroupfromtable(" +
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
					"end;",
				"drop procedure if exists mergeintogroup;",
			// needs to be set with inner joins
				"create definer=current_user procedure mergeintogroup(" +
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
					"end;",
				"drop procedure if exists createGroup;",
				"create definer=current_user procedure createGroup(" + 
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

		// Bastion-specific: Remove any instances of non-blacklisted players being allowed to place in bastions.
		db.registerMigration(14, false,
				"DELETE FROM permissionByGroup "
						+ "WHERE role='" + PlayerType.NOT_BLACKLISTED +"' "
						+ "AND perm_id=(SELECT perm_id FROM permissionIdMapping WHERE name='BASTION_PLACE');");
	}
	
	public int createGroup(String group, UUID owner, String password){
		int ret = -1;
		try (Connection connection = db.getConnection();
				PreparedStatement createGroup = connection.prepareStatement(GroupManagerDao.createGroup)){
			String own = null;
			if (owner != null) own = owner.toString();
			createGroup.setString(1, group);
			createGroup.setString(2, own);
			createGroup.setString(3, password);
			createGroup.setInt(4, 0);
			try (ResultSet set = createGroup.executeQuery();)  {
				ret = set.next() ? set.getInt("f.group_id") : -1;
				logger.log(Level.INFO, "Created group {0} w/ id {1} for {2}", new Object[] {group, ret, own});
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem creating group " + group, e);
				ret = -1;
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting up query to create group " + group, e);
			ret = -1;
		}
		
		return ret;
	}
	
	public Group getGroup(String groupName){
		String name = null;
		UUID owner = null;
		boolean discipline = false;
		String password = null;
		int id = -1;
		Timestamp timeStamp = null;
		try (Connection connection = db.getConnection();
				PreparedStatement getGroup = connection.prepareStatement(GroupManagerDao.getGroup)){
			
			getGroup.setString(1, groupName);
			try (ResultSet set = getGroup.executeQuery()){
				if (!set.next()) {
					return null;
				}
				
				name = set.getString(1);
				String uuid = set.getString(2);
				owner = (uuid != null) ? UUID.fromString(uuid) : null;
				discipline = set.getInt(4) != 0;
				password = set.getString(3);
				id = set.getInt(5);
				timeStamp = set.getTimestamp(6);
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting group " + groupName, e);
				return null;
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing query to get group " + groupName, e);
			return null;
		}
		Group g = null;
		try {
			g = new Group(name, owner, discipline, password, id, timeStamp.getTime());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Problem retrieving group " + groupName, e);
		}
		
		// other group IDs cached via the constructor.
		return g;
	}
	
	public Group getGroup(int groupId){
		String name = null;
		UUID owner = null;
		boolean dis = false;
		String password = null;
		int id = -1;
		Timestamp timeStamp = null;
		try (Connection connection = db.getConnection();
				PreparedStatement getGroupById = connection.prepareStatement(GroupManagerDao.getGroupById)){
			getGroupById.setInt(1, groupId);
			try (ResultSet set = getGroupById.executeQuery();) {
				if (!set.next()) {
					return null;
				}

				name = set.getString(1);
				String uuid = set.getString(2);
				owner = null;
				if (uuid != null)
					owner = UUID.fromString(uuid);
				dis = set.getInt(4) != 0;
				password = set.getString(3);
				id = set.getInt(5);
				timeStamp = set.getTimestamp(6);
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting group " + groupId, e);
				return null;
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing query to get group " + groupId, e);
			return null;
		}
		Group g = null;
		try {
			g = new Group(name, owner, dis, password, id, timeStamp.getTime());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Problem retrieving group " + groupId, e);
		}

		return g;
	}
	
	public List<String> getGroupNames(UUID uuid){
		List<String> groups = new ArrayList<String>();
		try (Connection connection = db.getConnection();
				PreparedStatement getAllGroupsNames = connection.prepareStatement(GroupManagerDao.getAllGroupsNames)){
			getAllGroupsNames.setString(1, uuid.toString());
			try (ResultSet set = getAllGroupsNames.executeQuery();) {
				while(set.next()) {
					groups.add(set.getString(1));
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting player's groups " + uuid, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing to get player's groups " + uuid, e);
		}
		return groups;
	}
	
	public List<String> getGroupNames(UUID uuid, String role){
		List<String> groups = new ArrayList<String>();
		try (Connection connection = db.getConnection();
				PreparedStatement getGroupNameFromRole = connection.prepareStatement(GroupManagerDao.getGroupNameFromRole)){
			getGroupNameFromRole.setString(1, uuid.toString());
			getGroupNameFromRole.setString(2, role);
			try (ResultSet set = getGroupNameFromRole.executeQuery();) {
				while(set.next()) {
					groups.add(set.getString(1));
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting player " + uuid + " groups by role " + role, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing to get player " + uuid + " groups by role " + role, e);
		}
		return groups;
	}
	
	public PlayerType getPlayerType(int groupid, UUID uuid){
		PlayerType ptype = null;
		try (Connection connection = db.getConnection();
				PreparedStatement getPlayerType = connection.prepareStatement(GroupManagerDao.getPlayerType)){
			getPlayerType.setInt(1, groupid);
			getPlayerType.setString(2, uuid.toString());
			try (ResultSet set = getPlayerType.executeQuery();) {
				if(set.next()){
					ptype = PlayerType.getPlayerType(set.getString(1));
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting player " + uuid + " type within group " + groupid, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing to get player " + uuid + " type within group " + groupid, e);
		}
		return ptype;
	}
	
	public void updateTimestampAsync(final String group){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				updateTimestamp(group);
			}
			
		});
	}
	
	public void updateTimestamp(String group){
		try (Connection connection = db.getConnection();
				PreparedStatement updateLastTimestamp = connection.prepareStatement(GroupManagerDao.updateLastTimestamp)){
			updateLastTimestamp.setString(1, group);
			updateLastTimestamp.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem updating timestamp for group " + group, e);
		}
	}
	
	public void deleteGroupAsync(final String groupName){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				deleteGroup(groupName);
			}
			
		});
	}
	
	public void deleteGroup(String groupName){
		try (Connection connection = db.getConnection();
				PreparedStatement deleteGroup = connection.prepareStatement(GroupManagerDao.deleteGroup)){
			deleteGroup.setString(1, groupName);
			deleteGroup.setString(2, NameLayerPlugin.getSpecialAdminGroup());
			deleteGroup.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem deleting group " + groupName, e);
		}
	}
	
	public void addMemberAsync(final UUID member, final String faction, final PlayerType role){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				addMember(member,faction,role);
			}
			
		});
	}
	
	public void addMember(UUID member, String faction, PlayerType role){
		try (Connection connection = db.getConnection();
				PreparedStatement addMember = connection.prepareStatement(GroupManagerDao.addMember)){
			addMember.setString(1, member.toString());
			addMember.setString(2, role.name());
			addMember.setString(3, faction);
			addMember.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem adding " + member + " as " + role.toString() 
					+ " to group " + faction, e);
		}			
	}
	
	public List<UUID> getAllMembers(String groupName, PlayerType role){
		List<UUID> members = new ArrayList<UUID>();
		try (Connection connection = db.getConnection();
				PreparedStatement getMembers = connection.prepareStatement(GroupManagerDao.getMembers)){
			getMembers.setString(1, groupName);
			getMembers.setString(2, role.name());
			try (ResultSet set = getMembers.executeQuery();) {
				while(set.next()){
					String uuid = set.getString(1);
					if (uuid == null) {
						continue;
					}
					members.add(UUID.fromString(uuid));
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting all " + role.toString() + " for group " + groupName, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing to get all " + role.toString() + " for group " + groupName, e);
		}
		return members;
	}
	
	public void removeMemberAsync(final UUID member, final String group){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				removeMember(member,group);
			}
			
		});
	}
	
	public void removeMember(UUID member, String group){
		try (Connection connection = db.getConnection();
				PreparedStatement removeMember = connection.prepareStatement(GroupManagerDao.removeMember)){
			removeMember.setString(1, member.toString());
			removeMember.setString(2, group);
			removeMember.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem removing " + member + " from group " + group, e);
		}
	}

	public void removeAllMembersAsync(final String group){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				removeAllMembers(group);
			}
			
		});
	}
	
	public void removeAllMembers(String group){
		try (Connection connection = db.getConnection();
				PreparedStatement removeAllMembers = connection.prepareStatement(GroupManagerDao.removeAllMembers)){
			removeAllMembers.setString(1, group);
			removeAllMembers.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem removing all members from group " + group, e);
		}
	}
	
	public void addSubGroupAsync(final String group, final String subGroup){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				addSubGroup(group,subGroup);
			}
			
		});
	}
	
	public void addSubGroup(String group, String subGroup){
		try (Connection connection = db.getConnection();
				PreparedStatement addSubGroup = connection.prepareStatement(GroupManagerDao.addSubGroup)){
			addSubGroup.setString(1, subGroup);
			addSubGroup.setString(2, group);
			addSubGroup.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem adding subgroup " + subGroup
					+ " to group " + group, e);
		}
		removeCycles();
	}
	
	public List<Group> getSubGroups(String group){
		List<Group> groups = new ArrayList<Group>();
		List<String> subgroups = Lists.newArrayList();
		try (Connection connection = db.getConnection();
				PreparedStatement getSubGroups = connection.prepareStatement(GroupManagerDao.getSubGroups)){
			getSubGroups.setString(1, group);
			
			try (ResultSet set = getSubGroups.executeQuery();){
				while (set.next()) {
					subgroups.add(set.getString(1));
				}
			}			
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem getting subgroups for group " + group, e);
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
		return groups;
	}
	
	public Group getSuperGroup(String group){
		String supergroup = null;
		try (Connection connection = db.getConnection();
				PreparedStatement getSuperGroup = connection.prepareStatement(GroupManagerDao.getSuperGroup)){
			getSuperGroup.setString(1, group);
			try (ResultSet set = getSuperGroup.executeQuery();) {
				if (!set.next()) {
					return null;
				}
				supergroup = set.getString(1);
			} catch (Exception e){
				logger.log(Level.WARNING, "Problem finding or getting superGroup for group " + group, e);
				return null;
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem getting superGroup for group " + group, e);
			return null;
		}
		if (GroupManager.hasGroup(supergroup)) {
			return GroupManager.getGroup(supergroup);
		} else {
			return getGroup(supergroup);
		}
	}
	
	public void removeSubGroupAsync(final String group, final String subgroup){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				removeSubGroup(group,subgroup);
			}
			
		});
	}
	
	public void removeSubGroup(String group, String subGroup){
		try (Connection connection = db.getConnection();
				PreparedStatement removeSubGroup = connection.prepareStatement(GroupManagerDao.removeSubGroup)){
			removeSubGroup.setString(1, group);
			removeSubGroup.setString(2, subGroup);
			removeSubGroup.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Removing subgroup " + subGroup
					+ " from group " + group, e);
		}
	}
	
	public void addAllPermissions(int groupId, Map <PlayerType, List <PermissionType>> perms) {
		try (Connection connection = db.getConnection();
				PreparedStatement addPermissionById = connection.prepareStatement(GroupManagerDao.addPermissionById)){
			for (Entry <PlayerType, List <PermissionType>> entry: perms.entrySet()){
				String role = entry.getKey().name();
				for(PermissionType perm : entry.getValue()) {
					addPermissionById.setInt(1,  groupId);
					addPermissionById.setString(2, role);
					addPermissionById.setInt(3, perm.getId());
					addPermissionById.addBatch();
				}
			}
			
			int[] res = addPermissionById.executeBatch();
			if (res == null) {
				logger.log(Level.WARNING, "Failed to add all permissions to group {0}", groupId);
			} else {
				int cnt = 0;
				for (int r : res) cnt += r;
				logger.log(Level.INFO, "Added {0} of {1} permissions to group {2}",
						new Object[] {cnt, res.length, groupId});
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem adding all permissions to group " + groupId, e);
		}
	}
	
	public void addPermissionAsync(final String gname, final String role, final List <PermissionType> perms){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				addPermission(gname,role,perms);
			}
			
		});
	}

	public void addPermission(String groupName, String role, List <PermissionType> perms) {
		try (Connection connection = db.getConnection();
				PreparedStatement addPermission = connection.prepareStatement(GroupManagerDao.addPermission)){
			for(PermissionType perm : perms) {
				addPermission.setString(1, role);
				addPermission.setInt(2, perm.getId());
				addPermission.setString(3, groupName);
				addPermission.addBatch();
			}
			int[] res = addPermission.executeBatch();
			if (res == null) {
				logger.log(Level.WARNING, "Failed to add all permissions to group {0}, role {1}",
						new Object[] {groupName, role} );
			} else {
				int cnt = 0;
				for (int r : res) cnt += r;
				logger.log(Level.INFO, "Added {0} of {1} permissions to group {2}, role {3}",
						new Object[] {cnt, res.length, groupName, role});
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem adding " + role + " with " + perms
					+ " to group " + groupName, e);
		}
	}
	
	public Map<PlayerType, List<PermissionType>> getPermissions(String group){
		Map<PlayerType, List<PermissionType>> perms = new HashMap<>();
		try (Connection connection = db.getConnection();
				PreparedStatement getPermission = connection.prepareStatement(GroupManagerDao.getPermission)){
			getPermission.setString(1, group);
			try (ResultSet set = getPermission.executeQuery();) {
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
				logger.log(Level.WARNING, "Problem getting permissions for group " + group, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing statement to get permissions for group " + group, e);
		}
		return perms;
	}
	
	public void removePermissionAsync(final String group, final PlayerType ptype, final PermissionType perm){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				removePermission(group,ptype,perm);
			}
			
		});
	}
	
	public void removePermission(String group, PlayerType pType, PermissionType perm){
		try (Connection connection = db.getConnection();
				PreparedStatement removePermission = connection.prepareStatement(GroupManagerDao.removePermission)){
			removePermission.setString(1, group);
			removePermission.setString(2, pType.name());
			removePermission.setInt(3, perm.getId());
			removePermission.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem removing permissions for group " + group
					+ " on playertype " + pType.name(), e);
		}
	}
	
	public void registerPermissionAsync(final PermissionType perm){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				registerPermission(perm);
			}
			
		});
	}
	
	public void registerPermission(PermissionType perm) {
		try (Connection connection = db.getConnection();
				PreparedStatement registerPermission = connection.prepareStatement(GroupManagerDao.registerPermission)){
			registerPermission.setInt(1, perm.getId());
			registerPermission.setString(2, perm.getName());
			registerPermission.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem register permission " + perm.getName(), e);
		}
	}
	
	public Map<Integer, String> getPermissionMapping() {
		Map <Integer,String> perms = new TreeMap<Integer, String>();
		try (Connection connection = db.getConnection();
				Statement getPermissionMapping = connection.createStatement()) {
			try (ResultSet res = getPermissionMapping.executeQuery(GroupManagerDao.getPermissionMapping)) {
				while (res.next()) {
					perms.put(res.getInt(1), res.getString(2));
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting permissions from db", e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem forming statement to get permissions from db", e);
		}
		return perms;
	}
	
	public void addNewDefaultPermissionAsync(final List <PlayerType> ptypes, final PermissionType perm){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				addNewDefaultPermission(ptypes,perm);
			}
			
		});
	}
	
	public void addNewDefaultPermission(List <PlayerType> playerTypes, PermissionType perm) {
		try (Connection connection = db.getConnection();
				PreparedStatement addPermissionById = connection.prepareStatement(GroupManagerDao.addDefaultPermission);) {;
				for(PlayerType pType: playerTypes) {
					addPermissionById.setString(1, pType.name());
					addPermissionById.setInt(2, perm.getId());
					addPermissionById.execute();
				}		
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Error initiating connection to set default perms for permission " + perm + " for player types " + playerTypes, e);
		}
	}
	
	public int countGroups(){
		int ret = 0;
		try (Connection connection = db.getConnection();
				Statement countGroups = connection.createStatement();
				ResultSet set = countGroups.executeQuery(GroupManagerDao.countGroups);){
			ret = set.next() ? set.getInt("count") : 0;
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem counting groups", e);
		}
		return ret;
	}
	
	public int countGroups(UUID uuid){
		int ret = 0;
		try (Connection connection = db.getConnection();
				PreparedStatement countGroupsFromUUID = connection.prepareStatement(GroupManagerDao.countGroupsFromUUID);){
			countGroupsFromUUID.setString(1, uuid.toString());
			try (ResultSet set = countGroupsFromUUID.executeQuery();) {
				ret = set.next() ? set.getInt("count") : 0;
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem counting groups for " + uuid, e);
			} 
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting up statement to count groups for " + uuid, e);
		}
		return ret;
		
	}
	
	public void mergeGroupAsync(final String groupname, final String tomerge){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				mergeGroup(groupname,tomerge);
			}
			
		});
	}
	
	public void mergeGroup(String groupName, String toMerge){
		try (Connection connection = db.getConnection();
				PreparedStatement mergeGroup = connection.prepareStatement(GroupManagerDao.mergeGroup);){
			mergeGroup.setString(1, groupName);
			mergeGroup.setString(2, toMerge);
			mergeGroup.execute();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem merging group " + toMerge + " into " + groupName, e);
		}
		removeCycles();
	}
	
	public void removeCycles() {
		try (Connection connection = db.getConnection();
				PreparedStatement removeCycles = connection.prepareStatement(GroupManagerDao.removeCycles);) {
			int removed = removeCycles.executeUpdate();
			logger.log(Level.INFO, "Removed {0} subgroup cycles", removed);
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to execute cycle removal code!");
		}
	}
	
	public void updatePasswordAsync(final String groupname, final String password){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				updatePassword(groupname,password);
			}
			
		});
	}
	
	public void updatePassword(String groupName, String password){
		try (Connection connection = db.getConnection();
				PreparedStatement updatePassword = connection.prepareStatement(GroupManagerDao.updatePassword);){
			updatePassword.setString(1, password);
			updatePassword.setString(2, groupName);
			updatePassword.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem updating password for group " + groupName, e);
		}
	}
	
	/**
	 * Loads the uuid of all players who have autoaccept for group invites turned on
	 * 
	 * @return All Players who have auto accept turned on
	 */
	public Set <UUID> loadAllAutoAccept() {
		Set <UUID> accepts = new HashSet<UUID>();
		try (Connection connection = db.getConnection();
				PreparedStatement addAutoAcceptGroup = connection.prepareStatement(GroupManagerDao.loadAllAutoAcceptGroup);
				ResultSet rs = addAutoAcceptGroup.executeQuery();){
			while (rs.next()) {
				accepts.add(UUID.fromString(rs.getString(1)));
			}
			
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem loading all autoaccepts", e);
		}
		return accepts;
	}
	
	/**
	 * Adds the uuid to the db if they should auto accept groups when invited.
	 * @param uuid sets up this player by uuid to accept groups async
	 */
	public void autoAcceptGroupsAsync(final UUID uuid){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				autoAcceptGroups(uuid);
			}
		});
	}
	
	public void autoAcceptGroups(final UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement addAutoAcceptGroup = connection.prepareStatement(GroupManagerDao.addAutoAcceptGroup);){
			addAutoAcceptGroup.setString(1, uuid.toString());
			addAutoAcceptGroup.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting autoaccept for " + uuid, e);
		}
	}
	
	/**
	 * @param uuid- The UUID of the player.
	 * @return Returns true if they should auto accept.
	 */
	@Deprecated
	public boolean shouldAutoAcceptGroups(UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement getAutoAcceptGroup = connection.prepareStatement(GroupManagerDao.getAutoAcceptGroup);){
			getAutoAcceptGroup.setString(1, uuid.toString());
			try (ResultSet set = getAutoAcceptGroup.executeQuery();) {
				return set.next();
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting autoaccept for " + uuid, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting up query to get autoaccept for " + uuid, e);
		}
		return false;
	}
	
	public void removeAutoAcceptGroupAsync(final UUID uuid){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				removeAutoAcceptGroup(uuid);
			}
		});
	}
	
	public void removeAutoAcceptGroup(final UUID uuid){
		try (Connection connection = db.getConnection();
				PreparedStatement removeAutoAcceptGroup = connection.prepareStatement(GroupManagerDao.removeAutoAcceptGroup);){
			removeAutoAcceptGroup.setString(1, uuid.toString());
			removeAutoAcceptGroup.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem removing autoaccept for " + uuid, e);
		}
	}
	
	public void setDefaultGroupAsync(final UUID uuid, final String groupname){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				setDefaultGroup(uuid,groupname);
			}
			
		});
	}
	
	public void setDefaultGroup(UUID uuid, String groupName){
		try (Connection connection = db.getConnection();
				PreparedStatement setDefaultGroup = connection.prepareStatement(GroupManagerDao.setDefaultGroup);){
			setDefaultGroup.setString(1, uuid.toString());
			setDefaultGroup.setString(2, groupName );
			setDefaultGroup.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting user " + uuid + " default group to " + groupName, e);
		}
	}
	
	public void changeDefaultGroupAsync(final UUID uuid, final String groupname){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				changeDefaultGroup(uuid,groupname);
			}
			
		});
	}
	
	public void changeDefaultGroup(UUID uuid, String groupName){
		try (Connection connection = db.getConnection();
				PreparedStatement changeDefaultGroup = connection.prepareStatement(GroupManagerDao.changeDefaultGroup);){
			changeDefaultGroup.setString(1, groupName);
			changeDefaultGroup.setString(2, uuid.toString());
			changeDefaultGroup.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem changing user " + uuid + " default group to " + groupName, e);
		}
	}

	public String getDefaultGroup(UUID uuid) {
		String group = null;
		try (Connection connection = db.getConnection();
				PreparedStatement getDefaultGroup = connection.prepareStatement(GroupManagerDao.getDefaultGroup);){
			getDefaultGroup.setString(1, uuid.toString());
			try (ResultSet set = getDefaultGroup.executeQuery();) {
				group = set.getString(1);
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem getting default group for " + uuid, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting up query to get default group for " + uuid, e);
		}
		return group;
	}
	
	public Map <UUID, String> getAllDefaultGroups() {
		Map <UUID, String> groups = null;
		try (Connection connection = db.getConnection();
				Statement getAllDefaultGroups = connection.createStatement();
				ResultSet set = getAllDefaultGroups.executeQuery(GroupManagerDao.getAllDefaultGroups);){
			groups = new TreeMap<UUID, String>();
			while(set.next()) {
				UUID uuid = UUID.fromString(set.getString(1));
				String group = set.getString(2);
				groups.put(uuid, group);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem getting all default groups " , e);
		}
		return groups;
	}
	
	/**
	 * Use this method to override the current founder of a group.
	 * @param uuid This is the uuid of the player.
	 * @param group This is the group that we are changing the founder of.
	 */
	public void setFounderAsync(final UUID uuid, final Group group){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				setFounder(uuid,group);
			}
			
		});
	}
	
	public void setFounder(UUID uuid, Group group) {
		try (Connection connection = db.getConnection();
				PreparedStatement updateOwner = connection.prepareStatement(GroupManagerDao.updateOwner);){
			updateOwner.setString(1, uuid.toString());
			updateOwner.setString(2, group.getName());
			updateOwner.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting founder of group " + group.getName() + " to " + uuid, e);
		}
	}
	
	public void setDisciplinedAsync(final Group group, final boolean disciplined){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				setDisciplined(group,disciplined);
			}
			
		});
	}
	
	public void setDisciplined(Group group, boolean disciplined) {
		try (Connection connection = db.getConnection();
				PreparedStatement updateDisciplined = connection.prepareStatement(GroupManagerDao.updateDisciplined);){
			updateDisciplined.setInt(1, disciplined ? 1 : 0);
			updateDisciplined.setString(2, group.getName());
			updateDisciplined.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem setting disciplined of group " + group.getName() 
					+ " to " + disciplined, e);
		}
	}

	
	public void addGroupInvitationAsync(final UUID uuid, final String groupName, final String role){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				addGroupInvitation(uuid,groupName,role);
			}
			
		});
	}
	
	public void addGroupInvitation(UUID uuid, String groupName, String role){
		try (Connection connection = db.getConnection();
				PreparedStatement addGroupInvitation = connection.prepareStatement(GroupManagerDao.addGroupInvitation);){
			addGroupInvitation.setString(1, uuid.toString());
			addGroupInvitation.setString(2, groupName);
			addGroupInvitation.setString(3, role);
			addGroupInvitation.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem adding group " + groupName + " invite for "
					+ uuid + " with role " + role, e);
		}
	}
	
	public void removeGroupInvitationAsync(final UUID uuid, final String groupName){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				removeGroupInvitation(uuid,groupName);
			}
			
		});
	}
	
	public void removeGroupInvitation(UUID uuid, String groupName){
		try (Connection connection = db.getConnection();
				PreparedStatement removeGroupInvitation = connection.prepareStatement(GroupManagerDao.removeGroupInvitation);){
			removeGroupInvitation.setString(1, uuid.toString());
			removeGroupInvitation.setString(2, groupName);
			removeGroupInvitation.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem removing group " + groupName + " invite for "
					+ uuid, e);
		}
	}
	
	
	/**
	 * Use this method to load a specific invitation to a group without the notification. 
	 * @param playerUUID The uuid of the invited player.
	 * @param group The group the player was invited to. 
	 */
	public void loadGroupInvitationAsync(final UUID playerUUID, final Group group){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				loadGroupInvitation(playerUUID, group);
			}
			
		});
	}
	
	public void loadGroupInvitation(UUID playerUUID, Group group){
		if(group == null) return;
		
		try (Connection connection = db.getConnection();
				PreparedStatement loadGroupInvitation = connection.prepareStatement(GroupManagerDao.loadGroupInvitation);){
			loadGroupInvitation.setString(1, playerUUID.toString());
			loadGroupInvitation.setString(2, group.getName());
			try (ResultSet set = loadGroupInvitation.executeQuery();) {
				while(set.next()){
					String role = set.getString("role");
					PlayerType type = null;
					if(role != null){
						type = PlayerType.getPlayerType(role);
					}
					group.addInvite(playerUUID, type, false);
				}
			} catch(SQLException e) {
				logger.log(Level.WARNING, "Problem loading group " + group.getName() + " invites for " + playerUUID, e);
			}
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Problem preparing query to load group " + group.getName() + 
				" invites for " + playerUUID, e);
		}
	}
	
	public Map<UUID, PlayerType> getInvitesForGroup(String groupName) {
		Map <UUID, PlayerType> invs = new TreeMap<UUID, GroupManager.PlayerType>();
		if (groupName == null) {
			return invs;
		}
		try (Connection connection = db.getConnection();
				PreparedStatement loadGroupInvitationsForGroup = connection.prepareStatement(GroupManagerDao.loadGroupInvitationsForGroup);){
			loadGroupInvitationsForGroup.setString(1, groupName);
			try (ResultSet set = loadGroupInvitationsForGroup.executeQuery();) {
				while(set.next()) {
					String uuid = set.getString(1);
					String role = set.getString(2);
					UUID playerUUID = null;
					if (uuid != null){
						playerUUID = UUID.fromString(uuid);
					}
					PlayerType pType = null;
					if(role != null){
						pType = PlayerType.getPlayerType(role);
					}
					if (uuid != null && pType != null) {
						invs.put(playerUUID, pType);
					}
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Problem loading group invitations for group " + groupName, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Problem preparing statement to load group invitations for group " + groupName, e);
		}
		return invs;
	}
	
	/**
	 * Use this method to load all invitations to all groups.
	 */
	public void loadGroupsInvitations(){
		try (Connection connection = db.getConnection();
				PreparedStatement loadGroupsInvitations = connection.prepareStatement(GroupManagerDao.loadGroupsInvitations);
				ResultSet set = loadGroupsInvitations.executeQuery();) {
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
					// TODO: This triggers subqueries. Don't trigger subqueries inside a query.
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
			logger.log(Level.WARNING, "Problem loading all group invitations", e);
		}
	}
	
	public void logNameChangeAsync(final UUID uuid, final String oldName, final String newName){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				logNameChange(uuid,oldName,newName);
			}
			
		});
	}
	
	public void logNameChange(UUID uuid, String oldName, String newName) {
		try (Connection connection = db.getConnection();
				PreparedStatement logNameChange = connection.prepareStatement(GroupManagerDao.logNameChange);){
			logNameChange.setString(1, uuid.toString());
			logNameChange.setString(2, oldName);
			logNameChange.setString(3, newName);
			logNameChange.executeUpdate();
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to log a name change for {0} from {1} -> {2}", new Object[]{uuid, oldName, newName});
			logger.log(Level.WARNING, "Exception during change.", e);
		}
	}
	
	public boolean hasChangedNameBefore(UUID uuid) {
		boolean ret = false;
		try (Connection connection = db.getConnection();
				PreparedStatement checkForNameChange = connection.prepareStatement(GroupManagerDao.checkForNameChange);){
			checkForNameChange.setString(1, uuid.toString());
			try (ResultSet set = checkForNameChange.executeQuery();) { 
				ret = set.next();
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Failed to check if " + uuid + " has previously changed names", e);
			} 
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Failed to check if {0} has previously changed names", uuid);
			logger.log(Level.WARNING, "Exception during check.", e);
		}
		return ret;
	}
	
	public void addBlackListMemberAsync(final String groupName, final UUID uuid){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				addBlackListMember(groupName,uuid);
			}
			
		});
	}
	
	public void addBlackListMember(String groupName, UUID player) {
		try (Connection connection = db.getConnection();
				PreparedStatement addBlacklistMember = connection.prepareStatement(GroupManagerDao.addBlacklistMember);){
			addBlacklistMember.setString(1, player.toString());
			addBlacklistMember.setString(2, groupName);
			addBlacklistMember.executeUpdate();
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Unable to add black list member " + player + " to group " + groupName, e);
		}
	}
	
	public void removeBlackListMemberAsync(final String gname, final UUID uuid){
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				removeBlackListMember(gname,uuid);
			}
			
		});
	}
	
	public void removeBlackListMember(String groupName, UUID player) {
		try (Connection connection = db.getConnection();
				PreparedStatement removeBlackListMember = connection.prepareStatement(GroupManagerDao.removeBlackListMember);){
			removeBlackListMember.setString(1, groupName);
			removeBlackListMember.setString(2, player.toString());
			removeBlackListMember.executeUpdate();
		} catch(SQLException e) {
			logger.log(Level.WARNING, "Unable to remove black list member " + player + " to group " + groupName, e);
		}
	}
	
	public Set<UUID> getBlackListMembers(String groupName) {
		Set<UUID> uuids = new HashSet<UUID>();
		try (Connection connection = db.getConnection();
				PreparedStatement getBlackListMembers = connection.prepareStatement(GroupManagerDao.getBlackListMembers);){
			getBlackListMembers.setString(1, groupName);
			try (ResultSet set = getBlackListMembers.executeQuery();) {
				while (set.next()) {
					uuids.add(UUID.fromString(set.getString(1)));
				}
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Unable to retrieve black list members for group " + groupName, e);
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "Unable to prepare query to retrieve black list members for group " + groupName, e);
		}
		return uuids;
	}

	/**
	 * Gets all the IDs for this group name, sorted by "size" in membercount.
	 * Ideally only one groupname/id has members and the rest are shadows, but in any case
	 * we arbitrarily define primacy as the one with the most members for ease of accounting
	 * and backwards compatibility.
	 *  
	 * @param groupName the group name to get IDs for
	 * @return the list of IDs for this group name
	 */
	public List<Integer> getAllIDs(String groupName) {
		if (groupName == null) {
			return null;
		}
		try (Connection connection = db.getConnection();
				PreparedStatement getGroupIDs = connection.prepareStatement(GroupManagerDao.getGroupIDs);){
			getGroupIDs.setString(1, groupName);
			try (ResultSet set = getGroupIDs.executeQuery();) {
				List<Integer> ids = new ArrayList<>();
			
				while (set.next()) {
					ids.add(set.getInt(1));
				}
				
				return ids;
			} catch (SQLException se) {
				logger.log(Level.WARNING, "Unable to fully load group ID set", se);
			}
		} catch (SQLException se) {
			logger.log(Level.WARNING, "Unable to prepare query to fully load group ID set", se);
		}
		return null;
	}
	
	
}
