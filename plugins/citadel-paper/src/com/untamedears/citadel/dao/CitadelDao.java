package com.untamedears.citadel.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import javax.persistence.PersistenceException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.avaje.ebean.LogLevel;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.lennardf1989.bukkitex.MyDatabase;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.DbUpdateAction;
import com.untamedears.citadel.entity.DbVersion;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionDelete;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.citadel.entity.PersonalGroup;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/18/12
 * Time: 3:58 PM
 * 
 * Last modified by JonnyD
 * 7/18/12
 */
public class CitadelDao extends MyDatabase {
	private static final int CHUNK_SIZE = 16;

    private String sqlLogDirectory;
    private boolean sqlEnableLog;

    public static String MakeChunkId(Chunk chunk) {
        return String.format("%s:%d:%d", chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public CitadelDao(JavaPlugin plugin) {
        super(plugin);

        Configuration config = plugin.getConfig();
        sqlLogDirectory = config.getString("database.logdirectory", "sql-logs");
        sqlEnableLog = config.getBoolean("database.enablefilelog", false);

        initializeDatabase(
                config.getString("database.driver"),
                config.getString("database.url"),
                config.getString("database.username"),
                config.getString("database.password"),
                config.getString("database.isolation"),
                config.getBoolean("database.logging", false),
                config.getBoolean("database.rebuild", false)
        );

        config.set("database.rebuild", false);
        plugin.saveConfig();
    }

    @Override
    protected List<Class<?>> getDatabaseClasses() {
        return Arrays.asList(
                DbVersion.class, FactionDelete.class,
                Faction.class, FactionMember.class,
                PlayerReinforcement.class, ReinforcementKey.class,
                PersonalGroup.class, Moderator.class);
    }

    public Object save(Object object) {
        getDatabase().save(object);
        return object;
    }

    public void delete(Object object) {
        getDatabase().delete(object);
    }

    public Set<Faction> findGroupsByFounder(String founder){
    	return getDatabase().createQuery(Faction.class, "find faction where founder = :founder")
    			.setParameter("founder", founder)
    			.findSet();
    }
    
    public Set<FactionMember> findGroupsByMember(String memberName){
    	return getDatabase().createQuery(FactionMember.class, "find factionMember where memberName = :memberName")
    			.setParameter("memberName", memberName)
    			.findSet();
    }
    
    public Set<Moderator> findGroupsByModerator(String memberName){
    	return getDatabase().createQuery(Moderator.class, "find moderator where memberName = :memberName")
    			.setParameter("memberName", memberName)
    			.findSet();
    }

	public Set<Faction> findAllGroups() {
		return getDatabase().createQuery(Faction.class, "find factionMember")
				.findSet();
	}
	
	public Set<FactionMember> findAllGroupMembers(){
		return getDatabase().createQuery(FactionMember.class, "find factionMember")
				.findSet();
	}
	
	public Faction findGroup(String groupName){
		return getDatabase().createQuery(Faction.class, "find faction where name = :groupName")
				.setParameter("groupName", groupName)
				.findUnique();
	}

    public Faction findGroupByName(String name) {
        return getDatabase().createQuery(Faction.class, "find faction where name = :name")
                .setParameter("name", name)
                .findUnique();
    }
    
    public boolean hasGroupMember(String groupName, String memberName) {
        return getDatabase().createQuery(FactionMember.class, "find factionMember where faction_name = :groupName" +
        		" and member_name = :memberName")
        		.setParameter("groupName", groupName)
                .setParameter("memberName", memberName)
                .findRowCount() > 0;
    }

    public FactionMember findGroupMember(String groupName, String memberName) {
        return getDatabase().createQuery(FactionMember.class, "find factionMember where memberName = :memberName")
                .setParameter("memberName", memberName)
                .findUnique();
    }
    
    public Set<FactionMember> findMembersOfGroup(String groupName) {
        return getDatabase().createQuery(FactionMember.class, "find factionMember where factionName = :groupName")
        		.setParameter("groupName", groupName)
                .findSet();
    }
    
    public Set<IReinforcement> findReinforcementsByGroup(String groupName){
        Set<PlayerReinforcement> result = getDatabase()
            .createQuery(PlayerReinforcement.class, "find reinforcement where name = :groupName")
    		.setParameter("groupName", groupName)
    		.findSet();    	
        for (PlayerReinforcement pr : result) {
            pr.setDbAction(DbUpdateAction.NONE);
        }
        return new TreeSet<IReinforcement>(result);
    }
    
    public List<? extends IReinforcement> findAllReinforcements(){
        List<PlayerReinforcement> result = getDatabase()
            .createQuery(PlayerReinforcement.class, "find reinforcement")
    		.findList();
        for (PlayerReinforcement pr : result) {
            pr.setDbAction(DbUpdateAction.NONE);
        }
        return new ArrayList<IReinforcement>(result);
    }

    public IReinforcement findReinforcement(Block block) {
        return findReinforcement(block.getLocation());
    }

    public IReinforcement findReinforcement(Location location) {
        return getDatabase().createQuery(PlayerReinforcement.class, "find reinforcement where x = :x and y = :y and z = :z and world = :world")
                .setParameter("x", location.getX())
                .setParameter("y", location.getY())
                .setParameter("z", location.getZ())
                .setParameter("world", location.getWorld().getName())
                .findUnique();
    }

    public TreeSet<IReinforcement> findReinforcementsInChunk(Chunk c){
        String chunkId = MakeChunkId(c);
    	Set<PlayerReinforcement> result = getDatabase()
                .createQuery(
                    PlayerReinforcement.class,
                    "find reinforcement where chunk_id = :chunk_id")
    			.setParameter("chunk_id", chunkId)
    			.findSet();
        // This manually resets each reinforcement DB state. The ORM calls the
        //  object's property setter methods which incorrectly flags the object
        //  for SAVE.
        for (PlayerReinforcement pr : result) {
            pr.setDbAction(DbUpdateAction.NONE);
        }
        return new TreeSet<IReinforcement>(result);
    }

    public void moveReinforcements(String from, String target){
    	SqlUpdate update = getDatabase().createSqlUpdate("UPDATE reinforcement SET name = :target, security_level = 1" +
    			" WHERE name = :from")
    			.setParameter("target", target)
    			.setParameter("from", from);
    	getDatabase().execute(update);
    }
    
    public int countReinforcements(){
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from reinforcement").findUnique();
    	return row.getInteger("count");  
    }
    
    public int countGroups(){
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from faction").findUnique();
    	return row.getInteger("count");  
    }

	public int countPlayerGroups(UUID accountId) {
    	SqlRow row = getDatabase().createSqlQuery(
                "select count(*) as count from faction where founder = :founder and (discipline_flags & 0x02) = 0")
    			.setParameter("founder", accountId.toString())
    			.findUnique();
    	return row.getInteger("count"); 
	}

	public Set<PersonalGroup> findAllPersonalGroups() {
		return getDatabase().createQuery(PersonalGroup.class, "find personalGroup")
				.findSet();
	}

	public void addGroup(String groupName) {
		SqlUpdate update = getDatabase().createSqlUpdate("INSERT INTO faction (name, founder) VALUES (:groupName, 'Gu3rr1lla')")
				.setParameter("groupName", groupName);
		getDatabase().execute(update);		
	}

	public PersonalGroup findPersonalGroup(String ownerName) {
		return getDatabase().createQuery(PersonalGroup.class, "find personalGroup where owner_name = :ownerName")
				.setParameter("ownerName", ownerName)
				.findUnique();
	}
	
    public boolean hasGroupModerator(String groupName, String memberName) {
        return getDatabase().createQuery(Moderator.class, "find moderator where faction_name = :groupName" +
        		" and member_name = :memberName")
        		.setParameter("groupName", groupName)
                .setParameter("memberName", memberName)
                .findRowCount() > 0;
    }

	public Set<Moderator> findModeratorsOfGroup(String groupName) {
		return getDatabase().createQuery(Moderator.class, "find moderator where faction_name = :groupName")
				.setParameter("groupName", groupName)
				.findSet();
	}
	
	public void removeAllMembersFromGroup(String groupName){
		SqlUpdate update = getDatabase().createSqlUpdate("delete from faction_member where faction_name = :groupName")
				.setParameter("groupName", groupName);
		getDatabase().execute(update);
	}
	
	public void removeAllModeratorsFromGroup(String groupName){
		SqlUpdate update = getDatabase().createSqlUpdate("delete from moderator where faction_name = :groupName")
				.setParameter("groupName", groupName);
		getDatabase().execute(update);
	}
	

	public Set<FactionDelete> loadFactionDeletions() {
		return getDatabase()
				.createQuery(FactionDelete.class, "find faction_delete")
				.findSet();
	}
 
    
    /**
     * @author GFQ
     * @date 4/25/2014
     *
     * @brief Performs batch reinforcement updates for deleted factions
     * 
     * When a faction is deleted, there could potentially be millions of reinforcement records
     * that would need to be updated with a new group name. This is handled by adding it 
     * to a separate faction_delete table, and setting the delete flag for that faction. 
     * This batch method is then run on plugin startup.
     * 
     * This function gets a set of all factions that are marked as deleted and performs
     * a series of reinforcement update queries. If the group no longer has any existing
     * reinforcement records then the group is deleted. If the total batch time exceeds
     * the given time limit then the loop exits and more work will be done on the next
     * restart. 
     */
    public void batchRemoveDeletedGroups() {

    	final int BATCH_UPDATE_SIZE = Citadel.getConfigManager().getBatchUpdateSize();
    	final int BATCH_TIMEOUT_MS = Citadel.getConfigManager().getBatchUpdateTimeoutMs();
    	
    	// Mark the start time
    	long startTime = System.currentTimeMillis();    	
    	
    	// Get all the groups that are in the faction_delete table and marked as delete in the main faction able
    	String joinQuery = String.format("select * from faction_delete left join (faction) "
    			+ "on (faction.name = faction_delete.deleted_faction and faction.discipline_flags & %d = %d)", Faction.kDeletedFlag, Faction.kDeletedFlag);
    	Set<SqlRow> groups = getDatabase().createSqlQuery(joinQuery).findSet();
    	
    	for (SqlRow groupRow : groups) {
    		String groupName = groupRow.getString("deleted_faction");
    		String personalGroup = groupRow.getString("personal_group");
    		int recordsLeft = 1;
    		
    		// Do batch deletes in groups of BATCH_UPDATE_SIZE while there are records remaining and we're inside our time limit
    		while (recordsLeft > 0 && System.currentTimeMillis() - startTime <= BATCH_TIMEOUT_MS) {
	    		// Get how many records still need to be updated from the deleted group name to the private group name    			
    	    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from reinforcement where name = :name")
    	    			.setParameter("name", groupName)
    	    			.findUnique();
    	    	recordsLeft = row.getInteger("count"); 
	        	
    	    	getDatabase().beginTransaction();
	        	
	        	if (recordsLeft > 0) {
	        		getDatabase().createSqlUpdate("update reinforcement set name = :newName where name = :oldName limit :limit")
	        		.setParameter("newName", personalGroup)
	        		.setParameter("oldName", groupName)
	        		.setParameter("limit", BATCH_UPDATE_SIZE)
	        		.execute();
	        	} else {
	        		// No more records to update, now we can safely delete the group
	        		getDatabase().createSqlUpdate("delete from faction_delete where deleted_faction = :name")
        				.setParameter("name",groupName)
        				.execute();
	        		
	        		getDatabase().createSqlUpdate("delete from faction where name = :name")
	        			.setParameter("name", groupName)
	        			.execute();
	        	}
	        	
	        	getDatabase().commitTransaction();
    		}
    	}
    }

    public Map<UUID, String> loadAccountIdMap() {
        final String sql = "SELECT account, name FROM citadel_account_id_map;";
        Map<UUID, String> result = new TreeMap<UUID, String>();
        List<SqlRow> resultSet = getDatabase().createSqlQuery(sql).findList();
        for (SqlRow row : resultSet) {
            final String accountIdStr = row.getString("account");
            final String playerName = row.getString("name");
            UUID accountId;
            try {
                accountId = UUID.fromString(accountIdStr);
            } catch (Exception ex) {
                Citadel.info(String.format("Invalid accountId for %s: %s", playerName, accountIdStr));
                continue;
            }
            result.put(accountId, playerName);
        }
        return result;
    }

    public void associatePlayerAccount(UUID accountId, String playerName) {
        SqlUpdate sql = getDatabase().createSqlUpdate(
            "DELETE FROM citadel_account_id_map WHERE account = :uuid")
            .setParameter("uuid", accountId.toString());
        getDatabase().execute(sql);

        sql = getDatabase().createSqlUpdate(
            "INSERT INTO citadel_account_id_map (account, name) VALUES (:uuid, :pname)")
            .setParameter("uuid", accountId.toString())
            .setParameter("pname", playerName);
        getDatabase().execute(sql);
    }

    public void updateDatabase() {
        RawSql rawVersionQuery = RawSqlBuilder
            .parse("SELECT MAX(db_version) AS db_version FROM db_version")
            .columnMapping("MAX(db_version)", "dbVersion")
            .create();
        Query<DbVersion> dbVersionQuery = getDatabase().find(DbVersion.class);
        dbVersionQuery.setRawSql(rawVersionQuery);
        DbVersion dbVersion = null;
        try {
            dbVersion = dbVersionQuery.findUnique();
        } catch (PersistenceException ex) {
            // table doesn't exist
        }
        if (dbVersion != null) {
            // The previous query didn't actually grab the entire object due
            // to the aggregation so retrieve the real object now.
            dbVersion = getDatabase().createQuery(
                DbVersion.class,
                "find db_version where db_version = :ver")
                    .setParameter("ver", dbVersion.getDbVersion())
                    .findUnique();
        }

        if (dbVersion == null) {
            Citadel.info("Updating to DB v2");
            //this for when Citadel 2.0 is loaded after an older version of Citadel
            //was previously installed
            SqlUpdate createMemberTable = getDatabase().createSqlUpdate(
                "CREATE TABLE IF NOT EXISTS member "
                + "(member_name varchar(255) NOT NULL, PRIMARY KEY (member_name))");
            getDatabase().execute(createMemberTable);

            SqlUpdate createModeratorTable = getDatabase().createSqlUpdate(
                "CREATE TABLE IF NOT EXISTS moderator "
                + "(member_name varchar(255) NOT NULL, faction_name varchar(255) NOT NULL)");
            getDatabase().execute(createModeratorTable);

            SqlUpdate createPersonalGroupTable = getDatabase().createSqlUpdate(
                "CREATE TABLE IF NOT EXISTS personal_group "
                + "(group_name varchar(255) NOT NULL, owner_name varchar(255) NOT NULL)");
            getDatabase().execute(createPersonalGroupTable);

            try {
                SqlUpdate alterFactionAddPassword = getDatabase().createSqlUpdate
                    ("ALTER TABLE faction ADD password varchar(255) DEFAULT NULL");
                getDatabase().execute(alterFactionAddPassword);
            } catch(PersistenceException e){
                //column already exists
            }

            try {
                SqlUpdate addReinforcementVersion = getDatabase().createSqlUpdate(
                    "ALTER TABLE reinforcement ADD COLUMN version INT NOT NULL DEFAULT 0");
                getDatabase().execute(addReinforcementVersion);
            } catch(PersistenceException e){
                //column already exists
            }

            try {
                // The initial add column statement is our indicator if the DB
                //  needs this reconstruction.
                SqlUpdate addReinforcementChunkId = getDatabase().createSqlUpdate(
                    "ALTER TABLE reinforcement ADD COLUMN chunk_id VARCHAR(255)");
                getDatabase().execute(addReinforcementChunkId);

                addReinforcementChunkId = getDatabase().createSqlUpdate(
                    "UPDATE reinforcement SET chunk_id = " +
                    "CONCAT(world, ':', CONVERT(IF(x >= 0, x, x - 15) DIV 16, CHAR), ':'," +
                    "CONVERT(IF(z >= 0, z, z - 15) DIV 16, CHAR))");
                getDatabase().execute(addReinforcementChunkId);

                addReinforcementChunkId = getDatabase().createSqlUpdate(
                    "ALTER TABLE reinforcement ADD INDEX ix_chunk_id (chunk_id)");
                getDatabase().execute(addReinforcementChunkId);
            } catch(PersistenceException e){
                //column already exists
            }

            try {
                SqlUpdate addFactionDisabled = getDatabase().createSqlUpdate(
                    "ALTER TABLE faction ADD COLUMN discipline_flags TINYINT NOT NULL DEFAULT 0");
                getDatabase().execute(addFactionDisabled);
            } catch(PersistenceException e){
                //column already exists
            }

            try {
                SqlUpdate addChunkidIdx = getDatabase().createSqlUpdate(
                    "ALTER TABLE reinforcement ADD INDEX idx_reinforcement_chunkid (chunk_id)");
                getDatabase().execute(addChunkidIdx);
            } catch(PersistenceException e){
                //index already exists
            }

            try {
                SqlUpdate addReinforcementVersion = getDatabase().createSqlUpdate(
                    "ALTER TABLE faction ADD COLUMN version INT NOT NULL DEFAULT 0");
                getDatabase().execute(addReinforcementVersion);
            } catch(PersistenceException e){
                //column already exists
            }

            try {
                SqlUpdate addReinforcementMaturationTime = getDatabase().createSqlUpdate(
                    "ALTER TABLE reinforcement ADD COLUMN maturation_time INT NOT NULL DEFAULT 0");
                getDatabase().execute(addReinforcementMaturationTime);
            } catch(PersistenceException e){
                //column already exists
            }

            try {
                SqlUpdate addReinforcementInsecurity = getDatabase().createSqlUpdate(
                    "ALTER TABLE reinforcement ADD COLUMN insecure BIT NOT NULL DEFAULT 0");
                getDatabase().execute(addReinforcementInsecurity);
            } catch(PersistenceException e){
                //column already exists
            }

            SqlUpdate createVersionTable = getDatabase().createSqlUpdate(
                "CREATE TABLE IF NOT EXISTS db_version "
                + "(db_version INT NOT NULL, update_time varchar(24), "
                + "PRIMARY KEY (db_version))");
            getDatabase().execute(createVersionTable);

            // The version table is empty, create a new object just for
            // passing to advance for boot strapping.
            dbVersion = new DbVersion();
            dbVersion.setDbVersion(1);
            dbVersion = advanceDbVersion(dbVersion);
        }

        if (dbVersion.getDbVersion() == 2) {
            Citadel.info("Updating to DB v3");

            SqlUpdate createTable = getDatabase().createSqlUpdate(
                "CREATE TABLE IF NOT EXISTS faction_delete "
                + "(deleted_faction VARCHAR(255) NOT NULL, personal_group VARCHAR(255), "
                + "PRIMARY KEY (deleted_faction))");
            getDatabase().execute(createTable);

            dbVersion = advanceDbVersion(dbVersion);
        }

        if (dbVersion.getDbVersion() == 3) {
            Citadel.info("Updating to DB v4");

            SqlUpdate dropTable = getDatabase().createSqlUpdate("DROP TABLE member;");
            getDatabase().execute(dropTable);

            dbVersion = advanceDbVersion(dbVersion);
        }

        if (dbVersion.getDbVersion() == 4) {
            Citadel.info("Updating to DB v5");
            performAccountIdUpdate();
            dbVersion = advanceDbVersion(dbVersion);
        }
    }

    private void performAccountIdUpdate() {
        SqlUpdate sql;

        Citadel.info("WARNING: This will take a while. Citadel is converting");
        Citadel.info("account names to account UUIDs.");

        final Map<UUID, String> account_id_map = retrieveKnownAccountIdMap();

        Citadel.info("Importing player name -> account ID mappings");
        sql = getDatabase().createSqlUpdate(
            "CREATE TABLE IF NOT EXISTS citadel_account_id_map "
            + "(account CHAR(36) NOT NULL, name VARCHAR(16) NOT NULL, "
            + "PRIMARY KEY (account), UNIQUE KEY ix_name (name))");
        getDatabase().execute(sql);
        ArrayList<Map.Entry<UUID, String>> batch = new ArrayList<Map.Entry<UUID, String>>(50);
        for (Map.Entry<UUID, String> account : account_id_map.entrySet()) {
            batch.add(account);
            if (batch.size() >= 50) {
                insertAccountsIntoTmpAccountTbl(batch);
                batch.clear();
                batch.ensureCapacity(50);
            }
        }
        if (batch.size() > 0)
        	insertAccountsIntoTmpAccountTbl(batch);

        Citadel.info("Converting faction table");
        sql = getDatabase().createSqlUpdate(
            "UPDATE faction AS f "
            + "JOIN citadel_account_id_map AS m ON m.name = LOWER(f.founder) "
            + "SET founder=m.account");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "ALTER TABLE faction MODIFY founder CHAR(36) NOT NULL");
        getDatabase().execute(sql);

        Citadel.info("Converting moderator table");
        sql = getDatabase().createSqlUpdate(
            "DROP INDEX uq_moderator_1 ON moderator");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "UPDATE moderator AS f "
            + "JOIN citadel_account_id_map AS m ON m.name = LOWER(f.member_name) "
            + "SET member_name=m.account");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "DELETE FROM moderator WHERE member_name NOT REGEXP "
            + "'[[:xdigit:]]{8}-[[:xdigit:]]{4}-[[:xdigit:]]{4}-[[:xdigit:]]{4}-[[:xdigit:]]{12}'");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "ALTER TABLE moderator MODIFY member_name CHAR(36) NOT NULL");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "CREATE UNIQUE INDEX uq_moderator_1 ON moderator (`faction_name`,`member_name`);");
        getDatabase().execute(sql);

        Citadel.info("Converting faction_member table");
        sql = getDatabase().createSqlUpdate(
            "DROP INDEX uq_faction_member_1 ON faction_member");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "UPDATE faction_member AS f "
            + "JOIN citadel_account_id_map AS m ON m.name = LOWER(f.member_name) "
            + "SET member_name=m.account");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "DELETE FROM faction_member WHERE member_name NOT REGEXP "
            + "'[[:xdigit:]]{8}-[[:xdigit:]]{4}-[[:xdigit:]]{4}-[[:xdigit:]]{4}-[[:xdigit:]]{12}'");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "ALTER TABLE faction_member MODIFY member_name CHAR(36) NOT NULL");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "CREATE UNIQUE INDEX uq_faction_member_1 ON faction_member (`faction_name`,`member_name`);");
        getDatabase().execute(sql);

        Citadel.info("Converting personal_group table");
        sql = getDatabase().createSqlUpdate(
            "UPDATE personal_group AS f "
            + "JOIN citadel_account_id_map AS m ON m.name = LOWER(f.owner_name) "
            + "SET owner_name=m.account");
        getDatabase().execute(sql);
        sql = getDatabase().createSqlUpdate(
            "ALTER TABLE personal_group MODIFY owner_name CHAR(36) NOT NULL");
        getDatabase().execute(sql);
    }

    protected void insertAccountsIntoTmpAccountTbl(final List<Map.Entry<UUID, String>> batch) {
        // As the Mojang ID format is the UUID string without dashes and the
        //  MySql binary value format is '0x' prepended to a string of hexadecimal
        //  chars, this will just the Mojang UUID converter.
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT IGNORE INTO citadel_account_id_map (account, name) VALUES ");
        boolean first = true;
        for (Map.Entry<UUID, String> account : batch) {
            if (first) {
                sb.append(String.format(
                    "(UPPER('%s'),LOWER('%s'))", account.getKey().toString(), account.getValue()));
                first = false;
            } else {
                sb.append(String.format(
                    ",(UPPER('%s'),LOWER('%s'))", account.getKey().toString(), account.getValue()));
            }
        }
        sb.append(";");
        SqlUpdate batchInsert = getDatabase().createSqlUpdate(sb.toString());
        getDatabase().execute(batchInsert);
    }

    public Map<UUID, String> retrieveKnownAccountIdMap() {
        Map<UUID, String> map = new TreeMap<UUID, String>();
        for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            map.put(player.getUniqueId(), player.getName());
        }
        return map;
    }

    protected DbVersion advanceDbVersion(DbVersion currentVersion) {
        DbVersion newVersion = new DbVersion();
        newVersion.setDbVersion(currentVersion.getDbVersion() + 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        newVersion.setUpdateTime(sdf.format(new Date()));
        getDatabase().save(newVersion);
        return newVersion;
    }

    protected void prepareDatabaseAdditionalConfig(DataSourceConfig dataSourceConfig, ServerConfig serverConfig) {
        if (sqlEnableLog) {
            serverConfig.setLoggingLevel(LogLevel.SQL);
            serverConfig.setLoggingDirectory(sqlLogDirectory);
        }
    }
}
