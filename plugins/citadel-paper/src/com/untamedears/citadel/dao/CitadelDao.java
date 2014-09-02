package com.untamedears.citadel.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.PersistenceException;

import org.bukkit.Chunk;
import org.bukkit.Location;
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
import com.untamedears.citadel.entity.BlackListing;
import com.untamedears.citadel.entity.DbVersion;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionDelete;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Member;
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
                Faction.class, Member.class, FactionMember.class,
                PlayerReinforcement.class, ReinforcementKey.class,
                PersonalGroup.class, Moderator.class, 
                BlackListing.class);
    }

    public Object save(Object object) {
        getDatabase().save(object);
        return object;
    }

    public void delete(Object object) {
        getDatabase().delete(object);
    }

    public Faction getOrCreateFaction(String name, String founder) {
        Faction faction = findGroupByName(name);
        if (faction == null) {
            faction = new Faction(name, founder);
            save(faction);
        }
        return faction;
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
    
    public boolean blackListPlayer(String player, Faction group){
    	return getDatabase().createQuery(BlackListing.class, "find faction, player where faction = :faction and player = :player")
    			.setParameter("faction", group.getName())
    			.setParameter("player", player)
    			.findRowCount() > 0;
    }
    
    public int countReinforcements(){
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from reinforcement").findUnique();
    	return row.getInteger("count");  
    }
    
    public int countGroups(){
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from faction").findUnique();
    	return row.getInteger("count");  
    }

	public int countPlayerGroups(String playerName) {
    	SqlRow row = getDatabase().createSqlQuery("select count(*) as count from faction where founder = :founder")
    			.setParameter("founder", playerName)
    			.findUnique();
    	return row.getInteger("count"); 
	}

	public Set<Member> findAllMembers() {
		return getDatabase().createQuery(Member.class, "find member")
    			.findSet();
	}
	
	public Member findMember(String memberName){
		return getDatabase().createQuery(Member.class, "find member where member_name = :memberName")
				.setParameter("memberName", memberName)
				.findUnique();
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
	
	public void addPlayerToBlackList(String group, String Player){
		SqlUpdate update = getDatabase().createSqlUpdate("INSERT INTO blacklist (`faction`, player) VALUES (:faction, :Player)")
				.setParameter("faction", group)
				.setParameter("Player", Player);
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

	public void removePlayerFromBlackList(String groupName, String player){
		SqlUpdate update = getDatabase().createSqlUpdate("delete from blacklist where `faction` = :groupName " +
				"AND player = :Player")
				.setParameter("groupName", groupName)
				.setParameter("Player", player);
		getDatabase().execute(update);
	}
	
    public Set<FactionDelete> loadFactionDeletions() {
        return getDatabase()
            .createQuery(FactionDelete.class, "find faction_delete")
            .findSet();
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
        	
        	SqlUpdate createTable = getDatabase().createSqlUpdate(
        			"CREATE TABLE IF NOT EXISTS blacklist " +
        			"(`id` int(10) NOT NULL AUTO_INCREMENT, `faction` VARCHAR(255) NOT NULL, player VARCHAR(255) NOT NULL, " +
        			"PRIMARY KEY (`id`))");
        	getDatabase().execute(createTable);
        	
        	dbVersion = advanceDbVersion(dbVersion);
        }
        
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
