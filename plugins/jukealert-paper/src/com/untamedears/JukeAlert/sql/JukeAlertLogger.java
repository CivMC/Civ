/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.sql;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.JukeAlertSnitch;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Dylan Holmes
 */
public class JukeAlertLogger {

    private JukeAlert plugin;
    private Database db;
    private String snitchsTbl;
    private String snitchDetailsTbl;
    private PreparedStatement getSnitchIdFromLocationStmt;
    private PreparedStatement getSnitchLogStmt;
    private PreparedStatement insertSnitchLogStmt;
    private PreparedStatement insertNewSnitchStmt;
    private PreparedStatement deleteSnitchStmt;
    private PreparedStatement updateGroupStmt;
    private PreparedStatement updateCuboidVolumeStmt;

    public JukeAlertLogger(JukeAlert plugin) {
        this.plugin = plugin;

        Configuration c = plugin.getConfig();

        String host = c.getString("db.host");
        String dbname = c.getString("db.name");
        String user = c.getString("db.user");
        String pass = c.getString("db.pass");
        String prefix = c.getString("db.prefix");

        if (host == null) {
            host = "localhost";
            c.set("db.host", host);
        }

        if (dbname == null) {
            dbname = "mydb";
            c.set("db.name", dbname);
        }

        if (user == null) {
            user = "root";
            c.set("db.user", user);
        }

        if (pass == null) {
            pass = "admin";
            c.set("db.pass", pass);
        }

        if (prefix == null) {
            prefix = "pvp_";
            c.set("db.prefix", prefix);
        }

        plugin.saveConfig();

        snitchsTbl = prefix + "snitchs";
        snitchDetailsTbl = prefix + "snitch_details";

        db = new Database(host, dbname, user, pass, prefix, this.plugin.getLogger());
        boolean connected = db.connect();
        if (connected) {
            genTables();
            initializeStatements();
        } else {
        	this.plugin.getLogger().log(Level.SEVERE, "Could not connect to the database! Fill out your config.yml!");
        }
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
                + "`snitch_world` tinyint NOT NULL,"
                + "`snitch_x` int(10) NOT NULL,"
                + "`snitch_y` int(10) NOT NULL,"
                + "`snitch_z` int(10) NOT NULL,"
                + "`snitch_group` varchar(40) NOT NULL,"
                + "`snitch_cuboid_x` int(10) NOT NULL,"
                + "`snitch_cuboid_y` int(10) NOT NULL,"
                + "`snitch_cuboid_z` int(10) NOT NULL,"
                + "`snitch_should_log` BOOL,"
                + "PRIMARY KEY (`snitch_id`));");
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
                + "`snitch_logged_initated_user`  char(16) NOT NULL,"
                + "`snitch_logged_victim_user` char(16), "
                + "`snitch_logged_x` int(10), "
                + "`snitch_logged_Y` int(10), "
                + "`snitch_logged_z` int(10), "
                + "`snitch_logged_blockid` smallint unsigned,"
                + "PRIMARY KEY (`snitch_details_id`));");
    }

    private void initializeStatements() {
    	// statement to get LIMIT entries from the snitchesDetailsTbl based on a snitch_id from the main snitchesTbl
        getSnitchLogStmt = db.prepareStatement(String.format(
            "SELECT * FROM %s"
            + " WHERE snitch_location=? GROUP BY snitch_location ORDER BY snitch_log_time ASC LIMIT ?",
            snitchDetailsTbl));
        
        // statement to get the ID of a snitch in the main snitchsTbl based on a Location (x,y,z, world)
        getSnitchIdFromLocationStmt = db.prepareStatement(String.format("SELECT snitch_id FROM %s"
        		+ "WHERE snitch_x=? AND snitch_y=? AND snitch_z=? AND snitch_world=?", snitchsTbl));
        
        insertSnitchLogStmt = db.prepareStatement(String.format(
            "INSERT INTO %s (snitch_location, snitch_log_time, snitch_info) VALUES(?, ?, ?)",
            snitchDetailsTbl));
        insertNewSnitchStmt = db.prepareStatement(String.format(
            "INSERT INTO %s (snitch_world, snitch_x, snitch_y, snitch_z, snitch_group, snitch_cuboid_x, snitch_cuboid_y, snitch_cuboid_z)"
            + " VALUES(?, ?, ?, ?, ?, 11, 11, 11)",
            snitchsTbl));
        deleteSnitchStmt = db.prepareStatement(String.format(
            "DELETE FROM %s WHERE snitch_world=? AND snitch_x=? AND snitch_y=? AND snitch_z=?",
            snitchsTbl));
        updateGroupStmt = db.prepareStatement(String.format(
            "UPDATE %s SET snitch_group=? WHERE snitch_world=? AND snitch_x=? AND snitch_y=? AND snitch_z=?",
            snitchsTbl));
        updateCuboidVolumeStmt = db.prepareStatement(String.format(
            "UPDATE %s SET snitch_cuboid_x=?, snitch_cuboid_y=?, snitch_cuboid_z=?"
            + " WHERE snitch_world=? AND snitch_x=? AND snitch_y=? AND snitch_z=?",
            snitchsTbl));
    }

    public static String snitchKey(final Location loc) {
        return String.format(
            "World: %s X: %d Y: %d Z: %d",
            loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Gets @limit events about that snitch. 
     * @param loc - the location of the snitch
     * @param offset - the number of entries to start at (10 means you start at the 10th entry and go to @limit)
     * @param limit - the number of entries to limit
     * @return a Map of String/Date objects of the snitch entries, formatted nicely
     */
    public Map<String, Date> getSnitchInfo(Location loc, int limit) {
        Map<String, Date> info = new HashMap<String, Date>();

        	// get the snitch's ID based on the location, then use that to get the snitch details from the snitchesDetail table
        	int interestedSnitchId = -1;
        	try {
        		// params are x(int), y(int), z(int), world(tinyint), column returned: snitch_id (int)
        		getSnitchIdFromLocationStmt.setInt(1, loc.getBlockX());
        		getSnitchIdFromLocationStmt.setInt(2, loc.getBlockY());
        		getSnitchIdFromLocationStmt.setInt(3, loc.getBlockZ());
        		getSnitchIdFromLocationStmt.setByte(4,  (byte)loc.getWorld().getEnvironment().getId());
        		
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
        			// we got a snitch id from the location, so now get the records that we want from the snitches detail table
        			try {
	        	        	
	                    getSnitchLogStmt.setString(1, snitchKey(loc));
	                    getSnitchLogStmt.setInt(2, limit);
	                    ResultSet set = getSnitchLogStmt.executeQuery();
	                    while (set.next()) {
	                        info.put(set.getString("snitch_info"), set.getDate("snitch_log_time"));
	                    }
	                } catch (SQLException ex) {
	                    this.plugin.getLogger().log(Level.SEVERE, "Could not get Snitch Details from the snitchesDetail table using the snitch id " + interestedSnitchId, ex);
	                    // rethrow
	                    throw ex;
	                }
        		} // end if..else (didFind)
        		
        	} catch (SQLException ex1) {
        		 this.plugin.getLogger().log(Level.SEVERE, "Could not get Snitch Details! loc: " + loc, ex1);
        	}
        	
        	

        return info;
    }

    //Logs info to a specific snitch with a time stamp.
    /*
     * ------DATE-----------DETAIL------
     * 2013-4-24 12:14:35 : Bob made an entry at [Nether(X: 56 Y: 87 Z: -1230)]
     * 2013-4-25 12:14:35 : Bob broke a chest at X: 896 Y: 1 Z: 8501
     * 2013-4-28 12:14:35 : Bob killed Trevor.
     * ----Type /ja more to see more----
     */
    public void logSnitchInfo(String info, JukeAlertSnitch snitch) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        try {
            insertSnitchLogStmt.setString(1, snitchKey(snitch.getLoc()));
            insertSnitchLogStmt.setTimestamp(2, time);
            insertSnitchLogStmt.setString(3, info);
            insertSnitchLogStmt.execute();
        } catch (SQLException ex) {
        	this.plugin.getLogger().log(Level.SEVERE, "Could not create snitch log entry!", ex);
        }
    }

    public void logSnitchEntityKill(JukeAlertSnitch snitch, Player player, Entity entity) {
        logSnitchInfo(String.format("%s killed a %s.", player.getName(), entity.toString()), snitch);
    }

    /**
     * @param player
     * @param victim
     */
    public void logSnitchPlayerKill(JukeAlertSnitch snitch, Player player, Player victim) {
        logSnitchInfo(String.format("%s killed %s.", player.getName(), victim.getName()), snitch);
    }

    /**
     * @param player
     * @param field
     */
    public void logSnitchEntry(JukeAlertSnitch snitch, Location loc, Player player) {
        logSnitchInfo(String.format(
            "%s made an entry at [%s%s %s(%sX: %d Y: %d Z: %d%s)]", player.getName(), ChatColor.AQUA, loc.getWorld().getName(),
                ChatColor.RESET, ChatColor.RED, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), ChatColor.RESET),
            snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBlockBreak(JukeAlertSnitch snitch, Player player, Block block) {
        Location loc = block.getLocation();
        logSnitchInfo(String.format(
            "%s broke a %s at X: %d Y: %d Z: %d.", player.getName(), block.getType().toString(), loc.getBlockX(),
                loc.getBlockY(), loc.getBlockZ()),
            snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBucketEmpty(JukeAlertSnitch snitch, Player player, Location loc, ItemStack item) {
        logSnitchInfo(String.format(
            "%s emptied a %s at X: %d Y: %d Z: %d.", player.getName(), item.getType().toString(), loc.getBlockX(),
                loc.getBlockY(), loc.getBlockZ()),
            snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBucketFill(JukeAlertSnitch snitch, Player player, Block block) {
        Location loc = block.getLocation();
        logSnitchInfo(String.format(
            "%s filled a bucket of %s at X: %d Y: %d Z: %d.", player.getName(), block.getType().toString(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
            snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBlockPlace(JukeAlertSnitch snitch, Player player, Block block) {
        Location loc = block.getLocation();
        logSnitchInfo(String.format(
            "%s placed a %s at X: %d Y: %d Z: %d.", player.getName(), block.getType().toString(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()),
            snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchUsed(JukeAlertSnitch snitch, Player player, Block block) {
        Location loc = block.getLocation();
        logSnitchInfo(String.format(
            "%s used a snitch at X: %d Y: %d Z: %d.", player.getName(), loc.getBlockX(),
                loc.getBlockY(), loc.getBlockZ()),
            snitch);
    }

    //Logs the snitch being placed at World, x, y, z in the database.
    public void logSnitchPlace(String world, String group, int x, int y, int z) {
        try {
            insertNewSnitchStmt.setString(1, world);
            insertNewSnitchStmt.setInt(2, x);
            insertNewSnitchStmt.setInt(3, y);
            insertNewSnitchStmt.setInt(4, z);
            insertNewSnitchStmt.setString(5, group);
            insertNewSnitchStmt.execute();
        } catch (SQLException ex) {
        	this.plugin.getLogger().log(Level.SEVERE, "Could not create new snitch in DB!", ex);
        }
    }

    //Removes the snitch at the location of World, X, Y, Z from the database.
    public void logSnitchBreak(String world, double x, double y, double z) {
        try {
            deleteSnitchStmt.setString(1, world);
            deleteSnitchStmt.setInt(2, (int)Math.floor(x));
            deleteSnitchStmt.setInt(3, (int)Math.floor(y));
            deleteSnitchStmt.setInt(4, (int)Math.floor(z));
            deleteSnitchStmt.execute();
        } catch (SQLException ex) {
        	this.plugin.getLogger().log(Level.SEVERE, "Could not log Snitch break!", ex);
        }
    }

    //Changes the group of which the snitch is registered to at the location of loc in the database.
    public void updateGroupSnitch(Location loc, String group) {
        try {
            updateGroupStmt.setString(1, group);
            updateGroupStmt.setString(2, loc.getWorld().getName());
            updateGroupStmt.setInt(3, loc.getBlockX());
            updateGroupStmt.setInt(4, loc.getBlockY());
            updateGroupStmt.setInt(5, loc.getBlockZ());
            updateGroupStmt.execute();
        } catch (SQLException ex) {
        	this.plugin.getLogger().log(Level.SEVERE, "Could not update Snitch group!", ex);
        }
    }

    //Updates the cuboid size of the snitch in the database.
    public void updateCubiodSize(Location loc, int x, int y, int z) {
        try {
            updateCuboidVolumeStmt.setInt(1, x);
            updateCuboidVolumeStmt.setInt(2, y);
            updateCuboidVolumeStmt.setInt(3, z);
            updateCuboidVolumeStmt.setString(4, loc.getWorld().getName());
            updateCuboidVolumeStmt.setInt(5, loc.getBlockX());
            updateCuboidVolumeStmt.setInt(6, loc.getBlockY());
            updateCuboidVolumeStmt.setInt(7, loc.getBlockZ());
            updateCuboidVolumeStmt.execute();
        } catch (SQLException ex) {
        	this.plugin.getLogger().log(Level.SEVERE, "Could not update Snitch cubiod size!", ex);
        }
    }
}
