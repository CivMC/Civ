package com.untamedears.jukealert.database;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchFactoryType;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.appender.AbstractSnitchAppender;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import com.untamedears.jukealert.model.appender.LeverToggleAppender;
import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.SingleBlockAPIView;
import vg.civcraft.mc.civmodcore.world.locations.global.GlobalLocationTracker;
import vg.civcraft.mc.civmodcore.world.locations.global.GlobalTrackableDAO;
import vg.civcraft.mc.civmodcore.world.locations.global.WorldIDManager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class JukeAlertDAO extends GlobalTrackableDAO<Snitch> {

    public static final int NOT_YET_INSERTED_ID = -1;

    public JukeAlertDAO(@NotNull final ManagedDatasource datasource) {
        super(CivLogger.getLogger(JukeAlertDAO.class), Objects.requireNonNull(datasource));
    }

    @Override
    public void registerMigrations() {
        // legacy format
        db.registerMigration(1, false,
            "CREATE TABLE IF NOT EXISTS snitchs (snitch_id int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "snitch_world varchar(40) NOT NULL, snitch_name varchar(40) NOT NULL, snitch_x int(10) NOT NULL,"
                + "snitch_y int(10) NOT NULL, snitch_z int(10) NOT NULL, snitch_group varchar(255) NOT NULL,"
                + "snitch_cuboid_x int(10) NOT NULL, snitch_cuboid_y int(10) NOT NULL, snitch_cuboid_z int(10) NOT NULL,"
                + "snitch_should_log tinyint(1) DEFAULT NULL, last_semi_owner_visit_date datetime NOT NULL, "
                + "allow_triggering_lever bit(1) NOT NULL, soft_delete tinyint(1) NOT NULL DEFAULT '0', "
                + "PRIMARY KEY (snitch_id), KEY idx_y (snitch_y), KEY idx_last_visit (last_semi_owner_visit_date,snitch_should_log))",
            "CREATE TABLE IF NOT EXISTS snitch_details (snitch_details_id int(10) unsigned NOT NULL AUTO_INCREMENT, "
                + " snitch_id int(10) unsigned NOT NULL, snitch_log_time datetime DEFAULT NULL, "
                + "snitch_logged_action tinyint(3) unsigned NOT NULL, snitch_logged_initiated_user varchar(16) NOT NULL, "
                + "snitch_logged_victim_user varchar(16) DEFAULT NULL, snitch_logged_x int(10) DEFAULT NULL,"
                + "snitch_logged_Y int(10) DEFAULT NULL, snitch_logged_z int(10) DEFAULT NULL,"
                + "snitch_logged_materialid smallint(5) unsigned DEFAULT NULL, soft_delete tinyint(1) NOT NULL DEFAULT '0',"
                + "log_hour mediumint(9) DEFAULT NULL, PRIMARY KEY (snitch_details_id),  KEY idx_snitch_id (snitch_id),"
                + "KEY idx_log_time (snitch_log_time), KEY idx_log_hour (log_hour), "
                + "CONSTRAINT fk_snitchs_snitch_id FOREIGN KEY (snitch_id) REFERENCES snitchs (snitch_id) "
                + "ON DELETE CASCADE ON UPDATE CASCADE)");

        db.registerMigration(2, false, new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    Map<Integer, Integer> oldToNewId = new HashMap<>();
                    try (Connection insertConn = db.getConnection();
                         PreparedStatement selectSnitches = insertConn.prepareStatement(
                             "select snitch_x,snitch_y,snitch_z,snitch_world,snitch_name,snitch_should_log,last_semi_owner_visit_date,"
                                 + "snitch_group,allow_triggering_lever,snitch_id from snitchs order by snitch_id asc");
                         ResultSet rs = selectSnitches.executeQuery();
                         PreparedStatement insertSnitch = insertConn.prepareStatement(
                             "insert into ja_snitches (group_id, type_id, x, y , z, world_id, name) "
                                 + "values(?,?, ?,?,?, ?, ?);",
                             Statement.RETURN_GENERATED_KEYS);) {
                        try (PreparedStatement deleteExisting = insertConn.prepareStatement("delete from ja_snitches")) {
                            // in case this migration failed before some of the data might already have
                            // migrated, which we want to undo
                            deleteExisting.execute();
                        }

                        WorldIDManager worldIdMan = CivModCorePlugin.getInstance().getWorldIdManager();
                        while (rs.next()) {
                            int x = rs.getInt(1);
                            int y = rs.getInt(2);
                            int z = rs.getInt(3);
                            String worldName = rs.getString(4);
                            String name = rs.getString(5);
                            boolean logging = rs.getBoolean(6);
                            long lastVisit = rs.getTimestamp(7).getTime();
                            String groupName = rs.getString(8);
                            boolean triggerLever = rs.getBoolean(9);
                            int oldId = rs.getInt(10);

                            short worldID = worldIdMan.getInternalWorldId(Bukkit.getWorld(worldName));
                            if (worldID == -1) {
                                logger.severe("Failed to find world id for world with name " + worldName);
                                return false;
                            }
                            int snitchType = logging ? 1 : 0;
                            Group group = GroupManager.getGroup(groupName);
                            if (group == null) {
                                continue;
                            }
                            int groupId = group.getGroupId();

                            insertSnitch.setInt(1, groupId);
                            insertSnitch.setInt(2, snitchType);
                            insertSnitch.setInt(3, x);
                            insertSnitch.setInt(4, y);
                            insertSnitch.setInt(5, z);
                            insertSnitch.setShort(6, worldID);
                            insertSnitch.setString(7, name);
                            insertSnitch.execute();
                            try (ResultSet keySet = insertSnitch.getGeneratedKeys()) {
                                if (!keySet.next()) {
                                    logger.severe(
                                        "Inserting snitch at " + x + " " + y + " " + z + " did not generate an id");
                                    return false;
                                }
                                int id = rs.getInt(1);
                                setRefreshTimer(id, lastVisit);
                                if (triggerLever) {
                                    setToggleLever(id, true);
                                }
                                oldToNewId.put(oldId, id);
                            }
                        }
                    }
                    try (Connection insertConn = db.getConnection();
                         PreparedStatement selectSnitches = insertConn.prepareStatement(
                             "select snitch_id, snitch_log_time, snitch_logged_action, snitch_logged_initiated_user, snitch_logged_victim_user,"
                                 + "snitch_logged_x, snitch_logged_y, snitch_logged_z from snitch_details order by snitch_details_id asc");
                         ResultSet rs = selectSnitches.executeQuery();
                         PreparedStatement insertSnitch = insertConn.prepareStatement(
                             "insert into ja_snitch_entries (snitch_id, type_id, uuid, x, y , z, creation_time,"
                                 + "victim) values(?,?,?, ?,?,?, ?,?);")) {
                        try (PreparedStatement deleteExisting = insertConn
                            .prepareStatement("delete from ja_snitch_entries")) {
                            // in case this migration failed before some of the data might already have
                            // migrated, which we want to undo
                            deleteExisting.execute();
                        }
                        int batchCounter = 0;
                        while (rs.next()) {
                            int oldId = rs.getInt(1);
                            long logTime = rs.getTimestamp(2).getTime();
                            byte actionType = rs.getByte(3);
                            String actor = rs.getString(4);
                            String victim = rs.getString(5);
                            int x = rs.getInt(6);
                            int y = rs.getInt(7);
                            int z = rs.getInt(8);

                            actor = ChatColor.stripColor(actor);
                            UUID actorUUID = NameAPI.getUUID(actor);
                            if (actorUUID == null) {
                                actorUUID = UUID.fromString("8326bc56-1ed9-40ff-8f24-46bf3e300e51");
                            }
                            int newSnitchId = oldToNewId.get(oldId);
                            switch (actionType) {
                                case 0:
                                    try {
                                        EntityType.valueOf(victim);
                                    } catch (IllegalArgumentException e) {
                                        victim = EntityType.PLAYER.toString();
                                    }
                                    break;
                                case 1:
                                case 2:
                                    victim = Material.STONE.toString();
                                    break;
                                case 3:
                                    victim = Material.WATER.toString();
                                    break;
                                case 4:
                                    victim = Material.WATER_BUCKET.toString();
                                    break;
                                case 6:
                                case 8:
                                    // entirely skip, these shouldn't even exist
                                    continue;
                                case 9:
                                    victim = Material.CHEST.toString();
                                    break;
                                case 12:
                                    // no IE support atm, might readd later
                                    continue;
                            }

                            insertSnitch.setInt(1, newSnitchId);
                            insertSnitch.setInt(2, actionType);
                            insertSnitch.setString(3, actorUUID.toString());
                            insertSnitch.setInt(4, x);
                            insertSnitch.setInt(5, y);
                            insertSnitch.setInt(6, z);
                            insertSnitch.setTimestamp(7, new Timestamp(logTime));
                            insertSnitch.setString(8, victim);
                            insertSnitch.addBatch();
                            if (batchCounter > 10000) {
                                batchCounter = 0;
                                insertSnitch.executeBatch();
                            }
                            batchCounter++;
                        }
                        insertSnitch.executeBatch();
                    }
                    return true;
                }
            }, "create table if not exists ja_snitches (id int not null auto_increment primary key, group_id int, "
                + "type_id int not null, x int not null, y int not null, z int not null, "
                + "world_id smallint unsigned not null, name varchar(255),"
                + "index snitchLocLookUp(x,y,z, world_id), unique uniqueLoc (world_id, x, y ,z));",
            "create table if not exists ja_snitch_actions(id int not null auto_increment primary key, name varchar(255) not null,"
                + "constraint unique_name unique(name));",
            "delete from ja_snitch_actions",
            "create table if not exists ja_snitch_entries (id int not null auto_increment primary key, "
                + "snitch_id int, type_id int references ja_snitch_actions(id), "
                + "uuid char(36) not null, x int not null, y int not null, z int not null, creation_time timestamp not null,"
                + "victim varchar(255), index `snitch_action_index`(snitch_id));",
            "create table if not exists ja_snitch_refresh (id int primary key references ja_snitches(id) on delete cascade,"
                + "last_refresh timestamp not null)",
            "create table if not exists ja_snitch_lever (id int primary key references ja_snitches(id) on delete cascade,"
                + "toggle_lever bool not null)",
            "ALTER TABLE ja_snitch_actions AUTO_INCREMENT = 16",
            "insert into ja_snitch_actions(id,name) values(0, 'KILL_MOB'),(1,'BLOCK_PLACE'),(2,'BLOCK_BREAK'),(3,'FILL_BUCKET'),(4,'EMPTY_BUCKET'),"
                + "(5,'ENTRY'),(7,'IGNITE_BLOCK'),(9,'OPEN_CONTAINER'),(10,'LOGIN'),(11,'LOGOUT'),(13,'DESTROY_VEHICLE'),"
                + "(14,'MOUNT_ENTITY'),(15,'DISMOUNT_ENTITY')",
            "delete from snitchs using snitchs, snitchs s2 where snitchs.snitch_id < s2.snitch_id "
                + "and snitchs.snitch_x = s2.snitch_x and snitchs.snitch_y = s2.snitch_y and snitchs.snitch_z = s2.snitch_z and snitchs.snitch_world=s2.snitch_world");
        db.registerMigration(3, false, "delete from ja_snitches where group_id = -1");
    }

    // ------------------------------------------------------------
    // Snitches
    // ------------------------------------------------------------

    @Override
    public void insert(@NotNull final Snitch snitch) {
        if (snitch.getId() != NOT_YET_INSERTED_ID) {
            this.logger.warning("Skipping snitch insert of [" + snitch + "] because its id is already set :s");
            return;
        }
        final Group snitchGroup = snitch.getGroup();
        if (snitchGroup == null) {
            this.logger.warning("Skipping snitch insert of [" + snitch + "] because its group is null!");
            return;
        }
        final Location snitchLocation = snitch.getLocation();
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO ja_snitches (group_id,type_id,x,y,z,world_id,name) VALUES (?,?,?,?,?,?,?);",
                 Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, snitchGroup.getGroupId());
            statement.setInt(2, snitch.getType().getID());
            statement.setInt(3, snitchLocation.getBlockX());
            statement.setInt(4, snitchLocation.getBlockY());
            statement.setInt(5, snitchLocation.getBlockZ());
            statement.setShort(6, getWorldID(snitchLocation));
            statement.setString(7, snitch.getName());
            statement.execute();
            try (final ResultSet results = statement.getGeneratedKeys()) {
                if (!results.next()) {
                    throw new IllegalStateException(
                        "Inserting snitch [" + snitch + "] didn't return a new snitch id!");
                }
                snitch.setId(results.getInt(1));
            }
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to insert snitch [" + snitch + "]!", throwable);
        }
        snitch.persistAppenders();
    }

    @Override
    public void update(@NotNull final Snitch snitch) {
        if (snitch.getId() == NOT_YET_INSERTED_ID) {
            this.logger.warning("Skipping snitch update of [" + snitch + "] because its id is invalid!");
            return;
        }
        final Group snitchGroup = snitch.getGroup();
        if (snitchGroup == null) {
            delete(snitch);
            snitch.setCacheState(CacheState.DELETED);
            return;
        }
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "UPDATE ja_snitches SET name = ?, group_id = ? WHERE id = ?;")) {
            statement.setString(1, snitch.getName());
            statement.setInt(2, snitchGroup.getGroupId());
            statement.setInt(3, snitch.getId());
            statement.execute();
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to update snitch [" + snitch + "]", throwable);
        }
        snitch.persistAppenders();
    }

    @Override
    public void delete(@NotNull final Snitch snitch) {
        if (snitch.getId() == NOT_YET_INSERTED_ID) {
            this.logger.warning("Skipping snitch deletion of [" + snitch + "] because its id is invalid!");
            return;
        }
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM ja_snitches WHERE id = ?;")) {
            statement.setInt(1, snitch.getId());
            statement.execute();
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to delete snitch [" + snitch + "]", throwable);
        }
    }

    @Override
    public void loadAll(@NotNull final Consumer<Snitch> callback) {
        final SnitchTypeManager snitchTypeManager = JukeAlert.getInstance().getSnitchConfigManager();
        final SnitchManager snitchManager = JukeAlert.getInstance().getSnitchManager();
        final WorldIDManager worldIDManager = CivModCorePlugin.getInstance().getWorldIdManager();
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "SELECT ja_snitches.id, x, y, z, world_id, type_id, group_id, name, last_refresh, toggle_lever FROM ja_snitches" +
                     " LEFT JOIN ja_snitch_refresh ON ja_snitches.id = ja_snitch_refresh.id" +
                     " LEFT JOIN ja_snitch_lever ON ja_snitches.id = ja_snitch_lever.id");
             final ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                final int snitchID = results.getInt(1);
                final int snitchX = results.getInt(2);
                final int snitchY = results.getInt(3);
                final int snitchZ = results.getInt(4);
                final short snitchWorldID = results.getShort(5);
                final World snitchWorld = worldIDManager.getWorldByInternalID(snitchWorldID);
                if (snitchWorld == null) {
                    this.logger.warning(
                        "Could not load world [" + snitchWorldID + "] for snitch [" + snitchID + "]");
                    continue;
                }
                final int snitchTypeID = results.getInt(6);
                final SnitchFactoryType snitchType = snitchTypeManager.getConfig(snitchTypeID);
                if (snitchType == null) {
                    this.logger.warning(
                        "Could not load snitch type [" + snitchTypeID + "] for snitch [" + snitchID + "]");
                    continue;
                }
                final int groupID = results.getInt(7);
                if (groupID == -1) {
                    this.logger.warning(
                        "Could not load snitch group [" + groupID + "] for snitch [" + snitchID + "]");
                    continue;
                }
                final String snitchName = results.getString(8);

                Timestamp lastRefreshTimestamp = results.getTimestamp(9);
                long lastRefresh = lastRefreshTimestamp == null ? -1L : lastRefreshTimestamp.getTime();

                boolean toggleLever = results.getBoolean(10);

                // Add the snitch to the system
                Snitch snitch = snitchType.create(snitchID,
                    new Location(snitchWorld, snitchX, snitchY, snitchZ),
                    snitchName, groupID, false);
                callback.accept(snitch);
                snitchManager.addSnitchToQuadTree(snitch);
                DormantCullingAppender dormantCullingAppender = snitch.getAppender(DormantCullingAppender.class);
                if (dormantCullingAppender != null) {
                    dormantCullingAppender.setLastRefresh(lastRefresh);
                }
                LeverToggleAppender leverToggleAppender = snitch.getAppender(LeverToggleAppender.class);
                if (leverToggleAppender != null) {
                    leverToggleAppender.setShouldToggle(toggleLever);
                }
                snitch.applyToAppenders(AbstractSnitchAppender::postSetup);
            }
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to load snitches from database", throwable);
        }
    }

    private Map<Location, Snitch> INTERNAL_snitchMap;

    @SuppressWarnings("unchecked")
    @NotNull
    public Stream<Snitch> loadSnitchesByGroupID(@Nullable final IntList groupIDs) {
        if (CollectionUtils.isEmpty(groupIDs)) {
            return Stream.empty();
        }
        if (INTERNAL_snitchMap == null) {
            final SnitchManager snitchManager = JukeAlert.getInstance().getSnitchManager();
            // Get SingleBlockAPIView instance
            final Field SM_API_FIELD = FieldUtils.getDeclaredField(SnitchManager.class, "api", true);
            final SingleBlockAPIView<Snitch> singleBlockAPIView;
            try {
                singleBlockAPIView = (SingleBlockAPIView<Snitch>) SM_API_FIELD.get(snitchManager);
            } catch (final IllegalAccessException throwable) {
                throw new IllegalStateException(
                    "Could not retrieve " + SingleBlockAPIView.class.getSimpleName(),
                    throwable);
            }
            // Get GlobalLocationTracker instance
            final Field SBAV_TRACKER_FIELD = FieldUtils.getDeclaredField(SingleBlockAPIView.class, "tracker", true);
            final GlobalLocationTracker<Snitch> globalLocationTracker;
            try {
                globalLocationTracker = (GlobalLocationTracker<Snitch>) SBAV_TRACKER_FIELD.get(singleBlockAPIView);
            } catch (final IllegalAccessException throwable) {
                throw new IllegalStateException(
                    "Could not retrieve " + GlobalLocationTracker.class.getSimpleName(),
                    throwable);
            }
            // Get internal map
            final Field GLT_TRACKED_FIELD = FieldUtils.getDeclaredField(GlobalLocationTracker.class, "tracked", true);
            try {
                INTERNAL_snitchMap = (Map<Location, Snitch>) GLT_TRACKED_FIELD.get(globalLocationTracker);
            } catch (final IllegalAccessException throwable) {
                throw new IllegalStateException(
                    "Could not retrieve internal tracking map",
                    throwable);
            }
        }
        return INTERNAL_snitchMap.values().parallelStream()
            .filter((snitch) -> {
                final Group group = snitch.getGroup();
                return group != null && !Collections.disjoint(groupIDs, group.getGroupIds());
            });
    }

    // ------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------

    public int getOrCreateActionID(@NotNull final String name) {
        Objects.requireNonNull(name);
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "SELECT id FROM ja_snitch_actions WHERE name = ?;")) {
            statement.setString(1, name);
            try (final ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1);
                }
            }
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to check for existence of action in db", throwable);
            return NOT_YET_INSERTED_ID;
        }
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO ja_snitch_actions (name) VALUES (?);",
                 Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.execute();
            try (final ResultSet results = statement.getGeneratedKeys()) {
                if (results.next()) {
                    return results.getInt(1);
                }
                this.logger.warning("Failed to insert action");
                return NOT_YET_INSERTED_ID;
            }
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to insert action into db", throwable);
            return NOT_YET_INSERTED_ID;
        }
    }

    // ------------------------------------------------------------
    // Logs
    // ------------------------------------------------------------

    /**
     * Loads <b>ALL</b> the logs for a given snitch, with some caveats.
     *
     * @param snitch           The snitch to load the logs for.
     * @param allowedActionAge The maximum allowed age (as a UNIX timestamp) for actions.
     * @param actionLimit      The maximum number of actions to load.
     */
    public List<LoggableAction> loadLogs(@NotNull final Snitch snitch,
                                         final long allowedActionAge,
                                         final int actionLimit) {
        final int snitchId = snitch.getId();
        if (snitchId == NOT_YET_INSERTED_ID) {
            throw new IllegalArgumentException("Cannot load logs for unknown snitch!");
        }
        final List<LoggableAction> result = new ArrayList<>();
        final LoggedActionFactory factory = JukeAlert.getInstance().getLoggedActionFactory();
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "SELECT jsa.name, jse.uuid, jse.x, jse.y, jse.z, jse.creation_time, jse.victim, jse.id "
                     + "FROM ja_snitch_entries jse STRAIGHT_JOIN ja_snitch_actions jsa ON "
                     + "jse.type_id = jsa.id WHERE snitch_id = ? AND jse.creation_time >= ? "
                     + "ORDER BY jse.creation_time DESC LIMIT " + Math.max(actionLimit, 1) + ";")) {
            statement.setInt(1, snitchId);
            statement.setDate(2, new Date(allowedActionAge));
            try (final ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    final String actionType = results.getString(1);
                    final UUID perpetratorUUID = UUID.fromString(results.getString(2));
                    final int incidentX = results.getInt(3);
                    final int incidentY = results.getInt(4);
                    final int incidentZ = results.getInt(5);
                    final long incidentTime = results.getTimestamp(6).getTime();
                    final String extra = results.getString(7);
                    final int incidentID = results.getInt(8);
                    final LoggableAction action = factory.produce(snitch, actionType, perpetratorUUID,
                        new Location(snitch.getLocation().getWorld(), incidentX, incidentY, incidentZ),
                        incidentTime, extra);
                    if (action != null) {
                        action.setID(incidentID);
                        result.add(action);
                    }
                }
            }
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to load snitch logs from db", throwable);
            return new ArrayList<>(0);
        }
        return result;
    }

    /**
     * Inserts a new snitch log to the database.
     *
     * @param actionTypeID The internal ID of the action type.
     * @param snitch       The snitch to save the log to.
     * @param actionData   The data of the action to store in the database.
     * @return Returns the snitch log's new database ID.
     */
    public int insertLog(final int actionTypeID,
                         @NotNull final Snitch snitch,
                         @NotNull final LoggedActionPersistence actionData) {
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO ja_snitch_entries (snitch_id,type_id,uuid,x,y,z,creation_time,victim) "
                     + "VALUES (?,?,?,?,?,?,?,?);",
                 Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, snitch.getId());
            statement.setInt(2, actionTypeID);
            statement.setString(3, actionData.actorUUID().toString());
            statement.setInt(4, actionData.locationX());
            statement.setInt(5, actionData.locationY());
            statement.setInt(6, actionData.locationZ());
            statement.setTimestamp(7, new Timestamp(actionData.timestamp()));
            statement.setString(8, actionData.extra());
            statement.execute();
            try (final ResultSet results = statement.getGeneratedKeys()) {
                if (results.next()) {
                    return results.getInt(1);
                }
                this.logger.severe("Failed to insert snitch log, no key retrieved");
            }
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to insert new snitch log", throwable);
        }
        return NOT_YET_INSERTED_ID;
    }

    /**
     * Deletes a particular log from the database.
     *
     * @param log The log to delete.
     */
    public void deleteLog(@NotNull final LoggableAction log) {
        if (log.getID() == NOT_YET_INSERTED_ID) {
            return;
        }
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM ja_snitch_entries WHERE id = ?;")) {
            statement.setInt(1, log.getID());
            statement.execute();
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to delete snitch log", throwable);
        }
    }

    /**
     * Deletes <b>ALL</b> logs for a given snitch.
     *
     * @param snitch The snitch to delete all logs for.
     */
    public void deleteAllLogsForSnitch(@NotNull final Snitch snitch) {
        final int snitchID = snitch.getId();
        if (snitchID == NOT_YET_INSERTED_ID) {
            throw new IllegalArgumentException("Cannot delete logs for unknown snitch!");
        }
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM ja_snitch_entries WHERE snitch_id = ?;")) {
            statement.setInt(1, snitchID);
            statement.execute();
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to delete snitch log", throwable);
        }
    }

    /**
     * Deletes <b>ALL</b> old logs for a given snitch.
     *
     * @param snitch           The snitch to delete all old logs for.
     * @param allowedSnitchAge The maximum allowed age (as a UNIX timestamp), all actions dated before this will be
     *                         deleted.
     */
    public void deleteOldLogsForSnitch(@NotNull final Snitch snitch,
                                       final long allowedSnitchAge) {
        final int snitchID = snitch.getId();
        if (snitchID == NOT_YET_INSERTED_ID) {
            throw new IllegalArgumentException("Cannot delete logs for unknown snitch!");
        }
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "DELETE FROM ja_snitch_entries WHERE snitch_id = ? AND creation_time < ?;")) {
            statement.setInt(1, snitchID);
            statement.setDate(2, new Date(allowedSnitchAge));
            statement.execute();
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to delete snitch log", throwable);
        }
    }

    // ------------------------------------------------------------
    // Refresh Timer
    // ------------------------------------------------------------

    public void setRefreshTimer(final int snitchID,
                                final long timestamp) {
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO ja_snitch_refresh (id,last_refresh) VALUES (?,?) " +
                     "ON DUPLICATE KEY UPDATE last_refresh = ?;")) {
            statement.setInt(1, snitchID);
            statement.setTimestamp(2, new Timestamp(timestamp));
            statement.setTimestamp(3, new Timestamp(timestamp));
            statement.execute();
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to update refresh timer for snitch [" + snitchID + "]", throwable);
        }
    }

    // ------------------------------------------------------------
    // Toggle Lever
    // ------------------------------------------------------------

    public void setToggleLever(final int snitchID,
                               final boolean toggle) {
        try (final Connection connection = this.db.getConnection();
             final PreparedStatement statement = connection.prepareStatement(
                 "INSERT INTO ja_snitch_lever (id,toggle_lever) VALUES (?,?) " +
                     "ON DUPLICATE KEY UPDATE toggle_lever = ?;")) {
            statement.setInt(1, snitchID);
            statement.setBoolean(2, toggle);
            statement.setBoolean(3, toggle);
            statement.execute();
        } catch (final SQLException throwable) {
            this.logger.log(Level.SEVERE, "Failed to update toggle lever for snitch [" + snitchID + "]", throwable);
        }
    }

}
