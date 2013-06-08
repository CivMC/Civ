/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.storage;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.chat.ChatFiller;
import com.untamedears.JukeAlert.group.GroupMediator;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.model.LoggedAction;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.tasks.GetSnitchInfoTask;
import com.untamedears.JukeAlert.util.SparseQuadTree;
import com.untamedears.citadel.entity.Faction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Dylan Holmes
 */
public class JukeAlertLogger {

    private ConfigManager configManager;
    private Database db;
    private String snitchsTbl;
    private String snitchDetailsTbl;
    private PreparedStatement getSnitchIdFromLocationStmt;
    private PreparedStatement getAllSnitchesStmt;
    private PreparedStatement getLastSnitchID;
    private PreparedStatement getSnitchLogStmt;
    private PreparedStatement deleteSnitchLogStmt;
    private PreparedStatement insertSnitchLogStmt;
    private PreparedStatement insertNewSnitchStmt;
    private PreparedStatement deleteSnitchStmt;
    private PreparedStatement updateGroupStmt;
    private PreparedStatement updateCuboidVolumeStmt;
    private PreparedStatement updateSnitchNameStmt;
    private PreparedStatement updateSnitchGroupStmt;
    private int logsPerPage;
    private int lastSnitchID;
    // The following are used by SnitchEnumerator
    protected JukeAlert plugin;
    protected GroupMediator groupMediator;
    protected PreparedStatement getAllSnitchesByWorldStmt;

    public JukeAlertLogger() {
        plugin = JukeAlert.getInstance();
        configManager = plugin.getConfigManager();
        groupMediator = plugin.getGroupMediator();

        String host = configManager.getHost();
        int port = configManager.getPort();
        String dbname = configManager.getDatabase();
        String username = configManager.getUsername();
        String password = configManager.getPassword();
        String prefix = configManager.getPrefix();

        snitchsTbl = prefix + "snitchs";
        snitchDetailsTbl = prefix + "snitch_details";

        db = new Database(host, port, dbname, username, password, prefix, this.plugin.getLogger());
        boolean connected = db.connect();
        if (connected) {
            genTables();
            initializeStatements();
            initializeLastSnitchId();
        } else {
            this.plugin.getLogger().log(Level.SEVERE, "Could not connect to the database! Fill out your config.yml!");
        }

        logsPerPage = configManager.getLogsPerPage();
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                saveAllSnitches();
            }
        }, 20, 20); // 1 sec
    }

    public Database getDb() {
        return db;
    }

    /**
     * Table generator
     */
    private void genTables() {
        //Snitches
        db.execute("CREATE TABLE IF NOT EXISTS `" + snitchsTbl + "` ("
                + "`snitch_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "`snitch_world` varchar(40) NOT NULL,"
                + "`snitch_name` varchar(40) NOT NULL,"
                + "`snitch_x` int(10) NOT NULL,"
                + "`snitch_y` int(10) NOT NULL,"
                + "`snitch_z` int(10) NOT NULL,"
                + "`snitch_group` varchar(40) NOT NULL,"
                + "`snitch_cuboid_x` int(10) NOT NULL,"
                + "`snitch_cuboid_y` int(10) NOT NULL,"
                + "`snitch_cuboid_z` int(10) NOT NULL,"
                + "`snitch_should_log` BOOL,"
                + "PRIMARY KEY (`snitch_id`),"
                + "INDEX `idx_y` (`snitch_y` ASC));");
        //Snitch Details
        // need to know:
        // action: (killed, block break, block place, etc), can't be null
        // person who initiated the action (player name), can't be null
        // victim of action (player name, entity), can be null
        // x, (for things like block place, bucket empty, etc, NOT the snitch x,y,z) can be null
        // y, can be null
        // z, can be null
        // block_id, can be null (block id for block place, block use, block break, etc)
        db.execute("CREATE TABLE IF NOT EXISTS `" + snitchDetailsTbl + "` ("
                + "`snitch_details_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "`snitch_id` int(10) unsigned NOT NULL," // reference to the column in the main snitches table
                + "`snitch_log_time` datetime,"
                + "`snitch_logged_action` tinyint unsigned NOT NULL,"
                + "`snitch_logged_initiated_user` varchar(16) NOT NULL,"
                + "`snitch_logged_victim_user` varchar(16), "
                + "`snitch_logged_x` int(10), "
                + "`snitch_logged_Y` int(10), "
                + "`snitch_logged_z` int(10), "
                + "`snitch_logged_materialid` smallint unsigned," // can be either a block, item, etc
                + "PRIMARY KEY (`snitch_details_id`),"
                + "INDEX `idx_snitch_id` (`snitch_id` ASC),"
                + "CONSTRAINT `fk_snitchs_snitch_id` FOREIGN KEY (`snitch_id`)"
                + "  REFERENCES `snitchs` (`snitch_id`) ON DELETE CASCADE ON UPDATE CASCADE);");
    }

    public PreparedStatement getNewInsertSnitchLogStmt() {
        return db.prepareStatement(String.format("INSERT INTO %s (snitch_id, snitch_log_time, "
                + "snitch_logged_action, snitch_logged_initiated_user,"
                + " snitch_logged_victim_user, snitch_logged_x, snitch_logged_y, snitch_logged_z, snitch_logged_materialid) "
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                snitchDetailsTbl));
    }

    private void initializeStatements() {

        getAllSnitchesStmt = db.prepareStatement(String.format(
                "SELECT * FROM %s", snitchsTbl));

        getAllSnitchesByWorldStmt = db.prepareStatement(String.format(
                "SELECT * FROM %s WHERE snitch_world = ?", snitchsTbl));

        getLastSnitchID = db.prepareStatement(String.format(
                "SHOW TABLE STATUS LIKE '%s'", snitchsTbl));

        // statement to get LIMIT entries OFFSET from a number from the snitchesDetailsTbl based on a snitch_id from the main snitchesTbl
        // LIMIT ?,? means offset followed by max rows to return 
        getSnitchLogStmt = db.prepareStatement(String.format(
                "SELECT * FROM %s"
                + " WHERE snitch_id=? ORDER BY snitch_log_time DESC LIMIT ?,?",
                snitchDetailsTbl));

        // statement to get the ID of a snitch in the main snitchsTbl based on a Location (x,y,z, world)
        getSnitchIdFromLocationStmt = db.prepareStatement(String.format("SELECT snitch_id FROM %s"
                + " WHERE snitch_x=? AND snitch_y=? AND snitch_z=? AND snitch_world=?", snitchsTbl));

        // statement to insert a log entry into the snitchesDetailsTable
        insertSnitchLogStmt = db.prepareStatement(String.format(
                "INSERT INTO %s (snitch_id, snitch_log_time, snitch_logged_action, snitch_logged_initiated_user,"
                + " snitch_logged_victim_user, snitch_logged_x, snitch_logged_y, snitch_logged_z, snitch_logged_materialid) "
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                snitchDetailsTbl));

        // 
        insertNewSnitchStmt = db.prepareStatement(String.format(
                "INSERT INTO %s (snitch_world, snitch_name, snitch_x, snitch_y, snitch_z, snitch_group, snitch_cuboid_x, snitch_cuboid_y, snitch_cuboid_z, snitch_should_log)"
                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                snitchsTbl));

        // 
        deleteSnitchLogStmt = db.prepareStatement(String.format(
                "DELETE FROM %s WHERE snitch_id=?",
                snitchDetailsTbl));

        //
        deleteSnitchStmt = db.prepareStatement(String.format(
                "DELETE FROM %s WHERE snitch_world=? AND snitch_x=? AND snitch_y=? AND snitch_z=?",
                snitchsTbl));

        // 
        updateGroupStmt = db.prepareStatement(String.format(
                "UPDATE %s SET snitch_group=? WHERE snitch_world=? AND snitch_x=? AND snitch_y=? AND snitch_z=?",
                snitchsTbl));

        // 
        updateCuboidVolumeStmt = db.prepareStatement(String.format(
                "UPDATE %s SET snitch_cuboid_x=?, snitch_cuboid_y=?, snitch_cuboid_z=?"
                + " WHERE snitch_world=? AND snitch_x=? AND snitch_y=? AND snitch_z=?",
                snitchsTbl));

        //
        updateSnitchNameStmt = db.prepareStatement(String.format(
                "UPDATE %s SET snitch_name=?"
                + " WHERE snitch_id=?",
                snitchsTbl));

        //
        updateSnitchGroupStmt = db.prepareStatement(String.format(
                "UPDATE %s SET snitch_group=?"
                + " WHERE snitch_id=?",
                snitchsTbl));

    }

    private void initializeLastSnitchId() {
        lastSnitchID = -1;
        try {
            ResultSet rsKey = getLastSnitchID.executeQuery();
            if (rsKey.next()) {
                lastSnitchID = rsKey.getInt("Auto_increment");
            }
        } catch (SQLException ex) {
            lastSnitchID = -1;
        }
        if (lastSnitchID == -1) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not determine the last snitch id!");
        }
    }

    public static String snitchKey(final Location loc) {
        return String.format(
                "World: %s X: %d Y: %d Z: %d",
                loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    protected class SnitchEnumerator implements Enumeration<Snitch> {
        JukeAlertLogger logger_;
        World world_;
        ResultSet rs_;
        Snitch next_;

        public SnitchEnumerator(JukeAlertLogger logger, World world) {
            logger_ = logger;
            world_ = world;
            try {
                getAllSnitchesByWorldStmt.setString(1, world_.getName());
                rs_ = getAllSnitchesByWorldStmt.executeQuery();
                next_ = getNextSnitch();
            } catch (SQLException ex) {
                logger_.plugin.getLogger().log(Level.SEVERE, "Couldn't get first snitch in World " + world_ + "!   " + ex.toString());
                rs_ = null;
                next_ = null;
            }
        }

        @Override
        public boolean hasMoreElements() {
            return next_ != null;
        }

        private Snitch getNextSnitch() {
            try {
                if (rs_ == null || !rs_.next()) {
                    rs_ = null;
                    return null;
                }
                double x = rs_.getInt("snitch_x");
                double y = rs_.getInt("snitch_y");
                double z = rs_.getInt("snitch_z");
                String groupName = rs_.getString("snitch_group");
                Faction group = groupMediator.getGroupByName(groupName);
                Location location = new Location(world_, x, y, z);

                Snitch snitch = new Snitch(location, group, rs_.getBoolean("snitch_should_log"));
                snitch.setId(rs_.getInt("snitch_id"));
                snitch.setName(rs_.getString("snitch_name"));
                return snitch;
            } catch (SQLException ex) {
                logger_.plugin.getLogger().log(Level.SEVERE, "Could not get all Snitches from World " + world_ + "!   " + ex.toString());
                rs_ = null;
            }
            return null;
        }

        @Override
        public Snitch nextElement() {
            if (next_ == null) {
                throw new NoSuchElementException();
            }
            Snitch retval = next_;
            next_ = getNextSnitch();
            if (next_ != null && lastSnitchID < next_.getId()) {
                lastSnitchID = next_.getId();
            }
            return retval;
        }
    }

    public Enumeration<Snitch> getAllSnitches(World world) {
        return new SnitchEnumerator(this, world);
    }

    public void saveAllSnitches() {
        //TODO: Save snitches.
        jukeinfobatch.flush();
    }

    /**
     * Gets
     *
     * @limit events about that snitch.
     * @param loc - the location of the snitch
     * @param offset - the number of entries to start at (10 means you start at
     * the 10th entry and go to
     * @limit)
     * @param limit - the number of entries to limit
     * @return a Map of String/Date objects of the snitch entries, formatted
     * nicely
     */
    public List<String> getSnitchInfo(Location loc, int offset) {
        List<String> info = new ArrayList<String>();

        // get the snitch's ID based on the location, then use that to get the snitch details from the snitchesDetail table
        int interestedSnitchId = -1;
        try {
            // params are x(int), y(int), z(int), world(tinyint), column returned: snitch_id (int)
            getSnitchIdFromLocationStmt.setInt(1, loc.getBlockX());
            getSnitchIdFromLocationStmt.setInt(2, loc.getBlockY());
            getSnitchIdFromLocationStmt.setInt(3, loc.getBlockZ());
            getSnitchIdFromLocationStmt.setString(4, loc.getWorld().getName());

            ResultSet snitchIdSet = getSnitchIdFromLocationStmt.executeQuery();

            // make sure we got a result
            boolean didFind = false;
            while (snitchIdSet.next()) {
                didFind = true;
                interestedSnitchId = snitchIdSet.getInt("snitch_id");
            }

            // only continue if we actually got a result from the first query
            if (!didFind) {
                this.plugin.getLogger().log(Level.SEVERE, "Didn't get any results trying to find a snitch in the snitches table at location " + loc);
            } else {
                GetSnitchInfoTask task = new GetSnitchInfoTask(plugin, interestedSnitchId, offset);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
                return task.getInfo();
            }

        } catch (SQLException ex1) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not get Snitch Details! loc: " + loc, ex1);
        }

        return info;
    }

    public List<String> getSnitchInfo(int snitchId, int offset) {
        List<String> info = new ArrayList<String>();

        try {
            getSnitchLogStmt.setInt(1, snitchId);
            getSnitchLogStmt.setInt(2, offset);
            getSnitchLogStmt.setInt(3, logsPerPage);

            ResultSet set = getSnitchLogStmt.executeQuery();
            if (!set.isBeforeFirst()) {
                System.out.println("No data");
            } else {
                while (set.next()) {
                    // TODO: need a function to create a string based upon what things we have / don't have in this result set
                    // so like if we have a block place action, then we include the x,y,z, but if its a KILL action, then we just say
                    // x killed y, etc
                    info.add(createInfoString(set));

                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not get Snitch Details from the snitchesDetail table using the snitch id " + snitchId, ex);
        }

        return info;
    }

    public Boolean deleteSnitchInfo(Location loc) {
        Boolean completed = false;
        // get the snitch's ID based on the location, then use that to get the snitch details from the snitchesDetail table
        int interestedSnitchId = -1;
        jukeinfobatch.flush();
        try {
            // params are x(int), y(int), z(int), world(tinyint), column returned: snitch_id (int)
            getSnitchIdFromLocationStmt.setInt(1, loc.getBlockX());
            getSnitchIdFromLocationStmt.setInt(2, loc.getBlockY());
            getSnitchIdFromLocationStmt.setInt(3, loc.getBlockZ());
            getSnitchIdFromLocationStmt.setString(4, loc.getWorld().getName());
            ResultSet snitchIdSet = getSnitchIdFromLocationStmt.executeQuery();
            // make sure we got a result
            boolean didFind = false;
            while (snitchIdSet.next()) {
                didFind = true;
                interestedSnitchId = snitchIdSet.getInt("snitch_id");
            }

            // only continue if we actually got a result from the first query
            if (!didFind) {
                this.plugin.getLogger().log(Level.SEVERE, "Didn't get any results trying to find a snitch in the snitches table at location " + loc);
            } else {
                deleteSnitchInfo(interestedSnitchId);
            }

        } catch (SQLException ex1) {
            completed = false;
            this.plugin.getLogger().log(Level.SEVERE, "Could not get Snitch Details! loc: " + loc, ex1);
        }

        return completed;
    }

    public Boolean deleteSnitchInfo(int snitchId) {
        try {
            deleteSnitchLogStmt.setInt(1, snitchId);
            deleteSnitchLogStmt.execute();
            return true;
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not delete Snitch Details from the snitchesDetail table using the snitch id " + snitchId, ex);
            return false;
        }
    }
    public JukeInfoBatch jukeinfobatch = new JukeInfoBatch(this);

    /**
     * Logs info to a specific snitch with a time stamp.
     *
     * example:
     *
     * ------DATE-----------DETAIL------ 2013-4-24 12:14:35 : Bob made an entry
     * at [Nether(X: 56 Y: 87 Z: -1230)] 2013-4-25 12:14:35 : Bob broke a chest
     * at X: 896 Y: 1 Z: 8501 2013-4-28 12:14:35 : Bob killed Trevor. ----Type
     * /ja more to see more----
     *
     * @param snitch - the snitch that recorded this event, required
     * @param material - the block/item/whatever that was part of the event, if
     * there was one , null if no material was part of the event
     * @param loc - the location where this event occured, if any
     * @param date - the date this event occurred , required
     * @param action - the action that took place in this event
     * @param initiatedUser - the user who initiated the event, required
     * @param victimUser - the user who was victim of the event, can be null
     */
    public void logSnitchInfo(Snitch snitch, Material material, Location loc, Date date, LoggedAction action, String initiatedUser, String victimUser) {

        jukeinfobatch.addSet(snitch, material, loc, date, action, initiatedUser, victimUser);

        /*try {
         // snitchid
         insertSnitchLogStmt.setInt(1, snitch.getId());
         // snitch log time
         insertSnitchLogStmt.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
         // snitch logged action
         insertSnitchLogStmt.setByte(3, (byte) action.getLoggedActionId());
         // initiated user
         insertSnitchLogStmt.setString(4, initiatedUser);

         // These columns, victimUser, location and materialid can all be null so check if it is an insert SQL null if it is

         // victim user
         if (victimUser != null) {
         insertSnitchLogStmt.setString(5, victimUser);
         } else {
         insertSnitchLogStmt.setNull(5, java.sql.Types.VARCHAR);
         }

         // location, x, y, z
         if (loc != null) {
         insertSnitchLogStmt.setInt(6, loc.getBlockX());
         insertSnitchLogStmt.setInt(7, loc.getBlockY());
         insertSnitchLogStmt.setInt(8, loc.getBlockZ());
         } else {
         insertSnitchLogStmt.setNull(6, java.sql.Types.INTEGER);
         insertSnitchLogStmt.setNull(7, java.sql.Types.INTEGER);
         insertSnitchLogStmt.setNull(8, java.sql.Types.INTEGER);
         }

         // materialid
         if (material != null) {
         insertSnitchLogStmt.setShort(9, (short) material.getId());
         } else {
         insertSnitchLogStmt.setNull(9, java.sql.Types.SMALLINT);
         }
         Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
         @Override
         public void run() {
         try {
         insertSnitchLogStmt.execute();
         } catch (SQLException ex) {
         Logger.getLogger(JukeAlertLogger.class.getName()).log(Level.SEVERE, null, ex);
         }
         }
         });
         //To change body of generated methods, choose Tools | Templates.

         } catch (SQLException ex) {
         this.plugin.getLogger().log(Level.SEVERE, String.format("Could not create snitch log entry! with snitch %s, "
         + "material %s, date %s, initiatedUser %s, victimUser %s", snitch, material, date, initiatedUser, victimUser), ex);
         }*/
    }

    /**
     * logs a message that someone killed an entity
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that did the killing
     * @param entity - the entity that died
     */
    public void logSnitchEntityKill(Snitch snitch, Player player, Entity entity) {

        // There is no material or location involved in this event
        this.logSnitchInfo(snitch, null, null, new Date(), LoggedAction.KILL, player.getPlayerListName(), entity.getType().toString());
    }

    /**
     * Logs a message that someone killed another player
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that did the killing
     * @param victim - the player that died
     */
    public void logSnitchPlayerKill(Snitch snitch, Player player, Player victim) {
        // There is no material or location involved in this event
        this.logSnitchInfo(snitch, null, null, new Date(), LoggedAction.KILL, player.getPlayerListName(), victim.getPlayerListName());
    }

    /**
     * Logs a message that someone ignited a block within the snitch's field
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that did the ignition
     * @param block - the block that was ignited
     */
    public void logSnitchIgnite(Snitch snitch, Player player, Block block) {
        // There is no material or location involved in this event
        this.logSnitchInfo(snitch, block.getType(), block.getLocation(), new Date(), LoggedAction.IGNITED, player.getPlayerListName(), null);
    }

    /**
     * Logs a message that someone entered the snitch's field
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that entered the snitch's field
     * @param loc - the location of where the player entered
     */
    public void logSnitchEntry(Snitch snitch, Location loc, Player player) {

        // no material or victimUser for this event
        this.logSnitchInfo(snitch, null, loc, new Date(), LoggedAction.ENTRY, player.getPlayerListName(), null);
    }
    
	/**
	 * Logs a message that someone logged in in the snitch's field
	 *
	 * @param snitch - the snitch that recorded this event
	 * @param player - the player that logged in in the snitch's field
	 * @param loc - the location of where the player logged in at
	 */
	public void logSnitchLogin(Snitch snitch, Location loc, Player player) {
		// no material or victimUser for this event
		this.logSnitchInfo(snitch, null, loc, new Date(), LoggedAction.LOGIN, player.getPlayerListName(), null);
	}

	/**
	 * Logs a message that someone logged out in the snitch's field
	 *
	 * @param snitch - the snitch that recorded this event
	 * @param player - the player that logged out in the snitch's field
	 * @param loc - the location of where the player logged out at
	 */
	public void logSnitchLogout(Snitch snitch, Location loc, Player player) {
		// no material or victimUser for this event
		this.logSnitchInfo(snitch, null, loc, new Date(), LoggedAction.LOGOUT, player.getPlayerListName(), null);
	}

    /**
     * Logs a message that someone broke a block within the snitch's field
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that broke the block
     * @param block - the block that was broken
     */
    public void logSnitchBlockBreak(Snitch snitch, Player player, Block block) {

        // no victim user in this event
        this.logSnitchInfo(snitch, block.getType(), block.getLocation(), new Date(), LoggedAction.BLOCK_BREAK, player.getPlayerListName(), null);
    }

    /**
     * Logs a message that someone placed a block within the snitch's field
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that placed the block
     * @param block - the block that was placed
     */
    public void logSnitchBlockPlace(Snitch snitch, Player player, Block block) {
        // no victim user in this event
        this.logSnitchInfo(snitch, block.getType(), block.getLocation(), new Date(), LoggedAction.BLOCK_PLACE, player.getPlayerListName(), null);
    }

    /**
     * Logs a message that someone emptied a bucket within the snitch's field
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that emptied the bucket
     * @param loc - the location of where the bucket empty occurred
     * @param item - the ItemStack representing the bucket that the player
     * emptied
     */
    public void logSnitchBucketEmpty(Snitch snitch, Player player, Location loc, ItemStack item) {
        // no victim user in this event
        this.logSnitchInfo(snitch, item.getType(), loc, new Date(), LoggedAction.BUCKET_EMPTY, player.getPlayerListName(), null);
    }

    /**
     * Logs a message that someone filled a bucket within the snitch's field
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that filled the bucket
     * @param block - the block that was 'put into' the bucket
     */
    public void logSnitchBucketFill(Snitch snitch, Player player, Block block) {
        // TODO: should we take a block or a ItemStack as a parameter here? 
        // JM: I think it'll be fine either way, most griefing is done with with block placement and this could be updated fairly easily

        // no victim user in this event
        this.logSnitchInfo(snitch, block.getType(), block.getLocation(), new Date(), LoggedAction.BUCKET_FILL, player.getPlayerListName(), null);
    }

    /**
     * Logs a message that someone used a block within the snitch's field
     *
     * @param snitch - the snitch that recorded this event
     * @param player - the player that used something
     * @param block - the block that was used
     */
    public void logUsed(Snitch snitch, Player player, Block block) {
        // TODO: what should we use to identify what was used? Block? Material? 
        //JM: Let's keep this consistent with block plament
        this.logSnitchInfo(snitch, block.getType(), block.getLocation(), new Date(), LoggedAction.BLOCK_USED, player.getPlayerListName(), null);
    }

    //Logs the snitch being placed at World, x, y, z in the database.
    public void logSnitchPlace(final String world, final String group, final String name, final int x, final int y, final int z, final boolean shouldLog) {
        final ConfigManager lockedConfigManager = this.configManager;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    jukeinfobatch.flush();
                	synchronized(insertNewSnitchStmt) {
	                    insertNewSnitchStmt.setString(1, world);
	                    insertNewSnitchStmt.setString(2, name);
	                    insertNewSnitchStmt.setInt(3, x);
	                    insertNewSnitchStmt.setInt(4, y);
	                    insertNewSnitchStmt.setInt(5, z);
	                    insertNewSnitchStmt.setString(6, group);
	                    insertNewSnitchStmt.setInt(7, lockedConfigManager.getDefaultCuboidSize());
	                    insertNewSnitchStmt.setInt(8, lockedConfigManager.getDefaultCuboidSize());
	                    insertNewSnitchStmt.setInt(9, lockedConfigManager.getDefaultCuboidSize());
                        insertNewSnitchStmt.setBoolean(10, shouldLog);
	                    insertNewSnitchStmt.execute();
                	}
                } catch (SQLException ex) {
                    Logger.getLogger(JukeAlertLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    //Removes the snitch at the location of World, X, Y, Z from the database.
    public void logSnitchBreak(final String world, final int x, final int y, final int z) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    jukeinfobatch.flush();
                    synchronized (deleteSnitchStmt) {
                        deleteSnitchStmt.setString(1, world);
                        deleteSnitchStmt.setInt(2, (int) Math.floor(x));
                        deleteSnitchStmt.setInt(3, (int) Math.floor(y));
                        deleteSnitchStmt.setInt(4, (int) Math.floor(z));
                        deleteSnitchStmt.execute();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(JukeAlertLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    //Changes the group of which the snitch is registered to at the location of loc in the database.
    public void updateGroupSnitch(final Location loc, final String group) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (updateGroupStmt) {
                        updateGroupStmt.setString(1, group);
                        updateGroupStmt.setString(2, loc.getWorld().getName());
                        updateGroupStmt.setInt(3, loc.getBlockX());
                        updateGroupStmt.setInt(4, loc.getBlockY());
                        updateGroupStmt.setInt(5, loc.getBlockZ());
                        updateGroupStmt.execute();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(JukeAlertLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    //Updates the cuboid size of the snitch in the database.
    public void updateCubiodSize(final Location loc, final int x, final int y, final int z) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (updateCuboidVolumeStmt) {
                        updateCuboidVolumeStmt.setInt(1, x);
                        updateCuboidVolumeStmt.setInt(2, y);
                        updateCuboidVolumeStmt.setInt(3, z);
                        updateCuboidVolumeStmt.setString(4, loc.getWorld().getName());
                        updateCuboidVolumeStmt.setInt(5, loc.getBlockX());
                        updateCuboidVolumeStmt.setInt(6, loc.getBlockY());
                        updateCuboidVolumeStmt.setInt(7, loc.getBlockZ());
                        updateCuboidVolumeStmt.execute();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(JukeAlertLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    //Updates the name of the snitch in the database.
    public void updateSnitchName(final Snitch snitch, final String name) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (updateSnitchNameStmt) {
                        updateSnitchNameStmt.setString(1, name);
                        updateSnitchNameStmt.setInt(2, snitch.getId());
                        updateSnitchNameStmt.execute();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(JukeAlertLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    //Updates the group of the snitch in the database.
    public void updateSnitchGroup(final Snitch snitch, final String group) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (updateSnitchGroupStmt) {
                        updateSnitchGroupStmt.setString(1, group);
                        updateSnitchGroupStmt.setInt(2, snitch.getId());
                        updateSnitchGroupStmt.execute();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(JukeAlertLogger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public Integer getLastSnitchID() {
        return lastSnitchID;
    }

    public void increaseLastSnitchID() {
        lastSnitchID++;
    }

    public void logSnitchBlockBurn(Snitch snitch, Block block) {
        this.logSnitchInfo(snitch, block.getType(), block.getLocation(), new Date(), LoggedAction.BLOCK_BURN, null, snitchDetailsTbl);
    }

    public String createInfoString(ResultSet set) {
        String resultString = ChatColor.RED + "Error!";
        try {
            int id = set.getInt("snitch_details_id");
            String initiator = set.getString("snitch_logged_initiated_user");
            String victim = set.getString("snitch_logged_victim_user");
            int action = (int) set.getByte("snitch_logged_action");
            int material = set.getInt("snitch_logged_materialid");

            int x = set.getInt("snitch_logged_X");
            int y = set.getInt("snitch_logged_Y");
            int z = set.getInt("snitch_logged_Z");

            String timestamp = new SimpleDateFormat("MM-dd HH:mm").format(set.getTimestamp("snitch_log_time"));

            if (action == LoggedAction.ENTRY.getLoggedActionId()) {
            	resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.BLUE + ChatFiller.fillString("Entry", (double) 15), ChatColor.WHITE + ChatFiller.fillString(timestamp, (double) 15));
            } else if (action == LoggedAction.LOGIN.getLoggedActionId()) {
            	resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.GREEN + ChatFiller.fillString("Login", (double) 15), ChatColor.WHITE + ChatFiller.fillString(timestamp, (double) 15));
            } else if (action == LoggedAction.LOGOUT.getLoggedActionId()) {
            	resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.GREEN + ChatFiller.fillString("Logout", (double) 15), ChatColor.WHITE + ChatFiller.fillString(timestamp, (double) 15));
            } else if (action == LoggedAction.BLOCK_BREAK.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.DARK_RED + ChatFiller.fillString("Block Break", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.BLOCK_PLACE.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.DARK_RED + ChatFiller.fillString("Block Place", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.BLOCK_BURN.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.DARK_RED + ChatFiller.fillString("Block Burn", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.IGNITED.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.GOLD + ChatFiller.fillString("Ignited", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.USED.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.GREEN + ChatFiller.fillString("Used", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.BLOCK_USED.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.GREEN + ChatFiller.fillString("Used", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.BUCKET_EMPTY.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.DARK_RED + ChatFiller.fillString("Bucket Empty", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.BUCKET_FILL.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.GREEN + ChatFiller.fillString("Bucket Fill", (double) 15), ChatColor.WHITE + ChatFiller.fillString(String.format("%d [%d %d %d]", material, x, y, z), (double) 30));
            } else if (action == LoggedAction.KILL.getLoggedActionId()) {
                resultString = String.format("  %s %s %s", ChatColor.GOLD + ChatFiller.fillString(initiator, (double) 25), ChatColor.DARK_RED + ChatFiller.fillString("Killed", (double) 15), ChatColor.WHITE + ChatFiller.fillString(victim, (double) 30));
            } else {
                resultString = ChatColor.RED + "Action not found. Please contact your administrator with log ID " + id;
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not get Snitch Details!");
        }

        return resultString;
    }
}
