/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.storage;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.ConfigManager;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
    private ConfigManager configManager;
    private Database db;
    private String snitchsTbl;
    private String snitchDetailsTbl;
    private PreparedStatement getSnitchIdFromLocationStmt;
    private PreparedStatement getAllSnitchesStmt;
    private PreparedStatement getSnitchLogStmt;
    private PreparedStatement insertSnitchLogStmt;
    private PreparedStatement insertNewSnitchStmt;
    private PreparedStatement deleteSnitchStmt;
    private PreparedStatement updateGroupStmt;
    private PreparedStatement updateCuboidVolumeStmt;

    public JukeAlertLogger() {
    	plugin = JukeAlert.getInstance();
    	configManager = plugin.getConfigManager();
    	
        String host   = configManager.getHost();
        String dbname = configManager.getDatabase();
        String username = configManager.getUsername();
        String password = configManager.getPassword();
        String prefix = configManager.getPrefix();

        snitchsTbl = prefix + "snitchs";
        snitchDetailsTbl = prefix + "snitch_details";

        db = new Database(host, dbname, username, password, prefix, this.plugin.getLogger());
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
                + "`snitch_logged_initiated_user`  char(16) NOT NULL,"
                + "`snitch_logged_victim_user` char(16), "
                + "`snitch_logged_x` int(10), "
                + "`snitch_logged_Y` int(10), "
                + "`snitch_logged_z` int(10), "
                + "`snitch_logged_blockid` smallint unsigned,"
                + "PRIMARY KEY (`snitch_details_id`));");
    }

    private void initializeStatements() {

    	getAllSnitchesStmt = db.prepareStatement(String.format(
    		"SELECT * FROM %s", snitchsTbl
    	));

        // statement to get LIMIT entries OFFSET from a number from the snitchesDetailsTbl based on a snitch_id from the main snitchesTbl
        // LIMIT ?,? means offset followed by max rows to return 
        getSnitchLogStmt = db.prepareStatement(String.format(
            "SELECT * FROM %s"
            + " WHERE snitch_id=? ORDER BY snitch_log_time ASC LIMIT ?,?",
            snitchDetailsTbl));
        
        // statement to get the ID of a snitch in the main snitchsTbl based on a Location (x,y,z, world)
        getSnitchIdFromLocationStmt = db.prepareStatement(String.format("SELECT snitch_id FROM %s"
        		+ "WHERE snitch_x=? AND snitch_y=? AND snitch_z=? AND snitch_world=?", snitchsTbl));
        
        // statement to insert a log entry into the snitchesDetailsTable
        insertSnitchLogStmt = db.prepareStatement(String.format(
            "INSERT INTO %s (snitch_id, snitch_log_time, snitch_logged_action, snitch_logged_initated_user," +
            " snitch_logged_victim_user, snitch_logged_x, snitch_logged_y, snitch_logged_z, snitch_logged_blockid) " +
            " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
            snitchDetailsTbl));
        
        // 
        insertNewSnitchStmt = db.prepareStatement(String.format(
            "INSERT INTO %s (snitch_world, snitch_x, snitch_y, snitch_z, snitch_group, snitch_cuboid_x, snitch_cuboid_y, snitch_cuboid_z)"
            + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
            snitchsTbl));
        
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
    }

    public static String snitchKey(final Location loc) {
        return String.format(
            "World: %s X: %d Y: %d Z: %d",
            loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    public List<Snitch> getAllSnitches() {
    	List<Snitch> snitches = new ArrayList<Snitch>();
    	try {
    		ResultSet set = getAllSnitchesStmt.executeQuery();
    		Snitch snitch = null;
    		while(set.next()) {
    			World world = this.plugin.getServer().getWorld(set.getString("snitch_world"));
    			double x = set.getInt("snitch_x");
    			double y = set.getInt("snitch_y");
    			double z = set.getInt("snitch_z");
    			String groupName = set.getString("snitch_faction");
    			
    			Faction faction = Citadel.getGroupManager().getGroup(groupName);
    			Location location = new Location(world, x, y, z);
    			
    			snitch = new Snitch(location, faction);
    			snitches.add(snitch);
    		}
    	} catch (SQLException ex) {
    		this.plugin.getLogger().log(Level.SEVERE, "Could not get all Snitches!");
    	}
    	return snitches;
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
	        	        // params are snitch_id (int), returns everything
	                    getSnitchLogStmt.setInt(1, interestedSnitchId);
	                    
	                    ResultSet set = getSnitchLogStmt.executeQuery();
	                    didFind = false;
	                    while (set.next()) {
	                    	didFind = true;
	                    	// TODO: need a function to create a string based upon what things we have / don't have in this result set
	                    	// so like if we have a block place action, then we include the x,y,z, but if its a KILL action, then we just say
	                    	// x killed y, etc
	                    	String resultString = String.format("%s did action %i", set.getString("snitch_logged_initated_user"), (int)set.getByte("snitch_logged_action"));
	                        info.put(resultString, set.getDate("snitch_log_time"));
	                    }
	                    if (!didFind) {
	                    	// Output something like 'no snitch action recorded" or something
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

    
    /**
     * Logs info to a specific snitch with a time stamp.
     * 
     * example: 
     * 
     * ------DATE-----------DETAIL------
     * 2013-4-24 12:14:35 : Bob made an entry at [Nether(X: 56 Y: 87 Z: -1230)]
     * 2013-4-25 12:14:35 : Bob broke a chest at X: 896 Y: 1 Z: 8501
     * 2013-4-28 12:14:35 : Bob killed Trevor.
     * ----Type /ja more to see more----
     * 
     * @param snitch - the snitch that recorded this event, required
     * @param block - the block that was part of the event, if there was one , null if no block was part of the event
     * @param date - the date this event occurred , required
     * @param action - the action that took place in this event
     * @param initiatedUser - the user who initiated the event, required
     * @param victimUser - the user who was victim of the event, can be null
     */
    public void logSnitchInfo(JukeAlertSnitch snitch, Block block, Date date, LoggedAction action, String initiatedUser, String victimUser) {
    	
        try {
        	insertSnitchLogStmt.setInt(1,  snitch.getId());
        	insertSnitchLogStmt.setDate(2, new java.sql.Date(date.getTime()));
        	insertSnitchLogStmt.setByte(3,  (byte)action.getLoggedActionId());
        	insertSnitchLogStmt.setString(4,  initiatedUser);
        	insertSnitchLogStmt.setString(5, victimUser);
        	insertSnitchLogStmt.setInt(6,  block.getLocation().getBlockX());
        	insertSnitchLogStmt.setInt(7,  block.getLocation().getBlockY());
        	insertSnitchLogStmt.setInt(8,  block.getLocation().getBlockZ());
        	insertSnitchLogStmt.setShort(9,  (short) block.getTypeId());
        	
        	insertSnitchLogStmt.execute();
        } catch (SQLException ex) {
        	this.plugin.getLogger().log(Level.SEVERE, String.format("Could not create snitch log entry! with snitch %s, " +
        			"block %s, date %s, initiatedUser %s, victimUser %s", snitch, block, date, initiatedUser, victimUser), ex);
        }
    }

    public void logSnitchEntityKill(Snitch snitch, Player player, Entity entity) {
        logSnitchInfo(String.format("%s killed a %s.", player.getName(), entity.toString()), snitch);
    }

    /**
     * @param player
     * @param victim
     */
    public void logSnitchPlayerKill(Snitch snitch, Player player, Player victim) {
        logSnitchInfo(String.format("%s killed %s.", player.getName(), victim.getName()), snitch);
    }

    /**
     * @param player
     * @param field
     */
    public void logSnitchEntry(Snitch snitch, Location loc, Player player) {
        logSnitchInfo(String.format(
            "%s made an entry at [%s%s %s(%sX: %d Y: %d Z: %d%s)]", player.getName(), ChatColor.AQUA, loc.getWorld().getName(),
                ChatColor.RESET, ChatColor.RED, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), ChatColor.RESET),
            snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBlockBreak(Snitch snitch, Player player, Block block) {
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
    public void logSnitchBucketEmpty(Snitch snitch, Player player, Location loc, ItemStack item) {
        logSnitchInfo(String.format(
            "%s emptied a %s at X: %d Y: %d Z: %d.", player.getName(), item.getType().toString(), loc.getBlockX(),
                loc.getBlockY(), loc.getBlockZ()),
            snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBucketFill(Snitch snitch, Player player, Block block) {
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
    public void logSnitchBlockPlace(Snitch snitch, Player player, Block block) {
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
    public void logSnitchUsed(Snitch snitch, Player player, Block block) {
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
            insertNewSnitchStmt.setInt(6, configManager.getDefaultCuboidSize());
            insertNewSnitchStmt.setInt(7, configManager.getDefaultCuboidSize());
            insertNewSnitchStmt.setInt(8, configManager.getDefaultCuboidSize());
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
