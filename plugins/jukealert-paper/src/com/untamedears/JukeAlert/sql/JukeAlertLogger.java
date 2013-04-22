/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.sql;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.JukeAlertSnitch;
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

        db = new Database(host, dbname, user, pass, prefix);
        boolean connected = db.connect();
        if (connected) {
            genTables();
        } else {
            JukeAlert.log(Level.SEVERE, "Could not connect to the database! Fill out your config.yml!");
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
        db.execute("CREATE TABLE IF NOT EXISTS `" + db.getPrefix() + "snitches` ("
                + "`snitch_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "`snitch_world` varchar(40) NOT NULL,"
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
        db.execute("CREATE TABLE IF NOT EXISTS `" + db.getPrefix() + "snitch_details` ("
                + "`snitch_id` int(10) unsigned NOT NULL AUTO_INCREMENT,"
                + "`snitch_location` varchar(40) NOT NULL,"
                + "`snitch_log_time` datetime,"
                + "`snitch_info` text,"
                + "PRIMARY KEY (`snitch_id`));");
    }

    //Gets @limit events about that snitch. 
    public Map<String, Date> getSnitchInfo(Location loc, int limit) {
        Map<String, Date> info = new HashMap<>();
        String query = "SELECT snitch_info, snitch_log_time"
                + " FROM " + db.getPrefix() + "snitch_details WHERE snitch_location='"
                + "World: " + loc.getWorld().getName() + " X: "
                + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ()
                + "'"
                + "GROUP BY snitch_location LIMIT " + limit + ";";
        ResultSet set = db.getResultSet(query);
        try {
            while (set.next()) {
                info.put(set.getString("snitch_info"), set.getDate("snitch_log_time"));
            }
        } catch (SQLException ex) {
            JukeAlert.log(Level.SEVERE, "Could not get Snitch Details!", ex);
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
        db.execute("INSERT INTO " + db.getPrefix() + "snitch_details "
                + "(snitch_location, snitch_log_time, snitch_info) "
                + "VALUES("
                + "'" + "World: " + snitch.getLoc().getWorld().getName() + " X: "
                + snitch.getLoc().getBlockX() + " Y: " + snitch.getLoc().getBlockY()
                + " Z: " + snitch.getLoc().getBlockZ() + "',"
                + "'" + time.toString() + "',"
                + "'" + info + "'"
                + ")");
    }

    public void logSnitchEntityKill(JukeAlertSnitch snitch, Player player, Entity entity) {
        logSnitchInfo(player.getName() + " killed a " + entity.toString() + ".", snitch);
    }

    /**
     * @param player
     * @param victim
     */
    public void logSnitchPlayerKill(JukeAlertSnitch snitch, Player player, Player victim) {
        logSnitchInfo(player.getName() + " killed " + victim.getName() + ".", snitch);

    }

    /**
     * @param player
     * @param field
     */
    public void logSnitchEntry(JukeAlertSnitch snitch, Location loc, Player player) {
        logSnitchInfo(player.getName() + " made an entry at [" + ChatColor.AQUA + loc.getWorld().getName()
                + ChatColor.RESET + " (" + ChatColor.RED + " X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ChatColor.RESET + ")]", snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBlockBreak(JukeAlertSnitch snitch, Player player, Block block) {
        Location loc = block.getLocation();
        logSnitchInfo(player.getName() + " broke a " + block.getType().toString() + " at X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ".", snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBucketEmpty(JukeAlertSnitch snitch, Player player, Location loc, ItemStack item) {
        logSnitchInfo(player.getName() + " emptied a " + item.getType() + " at X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ".", snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBucketFill(JukeAlertSnitch snitch, Player player, Block block) {
        Location loc = block.getLocation();
        logSnitchInfo(player.getName() + " filled a bucket of " + block.getType().toString() + " at X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ".", snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchBlockPlace(JukeAlertSnitch snitch, Player player, Block block) {
        Location loc = block.getLocation();
        logSnitchInfo(player.getName() + " placed a " + block.getType().toString() + " at X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ".", snitch);
    }

    /**
     * @param player
     * @param block
     */
    public void logSnitchUsed(JukeAlertSnitch snitch, Player player, Block block) {
         Location loc = block.getLocation();
        logSnitchInfo(player.getName() + " used a snitch at X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ() + ".", snitch);
    }

    //Logs the snitch being placed at World, x, y, z in the database.
    public void logSnitchPlace(String world, String group, int x, int y, int z) {
        db.execute("INSERT INTO " + db.getPrefix() + "snitches "
                + "(snitch_world, snitch_x, snitch_y, snitch_z, snitch_group, snitch_cuboid_x, snitch_cuboid_y, snitch_cuboid_z) "
                + "VALUES("
                + "'" + world + "',"
                + "'" + x + "',"
                + "'" + y + "',"
                + "'" + z + "',"
                + "'" + group + "',"
                + "'" + 11 + "',"
                + "'" + 11 + "',"
                + "'" + 11 + "'"
                + ")");
    }

    //Removes the snitch at the location of World, X, Y, Z from the database.
    public void logSnitchBreak(String world, double x, double y, double z) {
        db.execute("DELETE FROM " + db.getPrefix() + "snitches WHERE snitch_world='" + world + "' AND snitch_x='" + x + "' AND snitch_y='" + y + "' AND snitch_z='" + z + "'");
    }

    //Changes the group of which the snitch is registered to at the location of loc in the database.
    public void updateGroupSnitch(Location loc, String group) {
        int lX = loc.getBlockX();
        int lY = loc.getBlockY();
        int lZ = loc.getBlockZ();
        db.execute("UPDATE " + db.getPrefix() + "snitches SET snitch_group='" + group + "' WHERE snitch_world='"
                + loc.getWorld().getName() + "' AND snitch_x='" + lX + "' AND snitch_y='" + lY + "' AND snitch_z='" + lZ + "'");
    }

    //Updates the cuboid size of the snitch in the database.
    public void updateCubiodSize(Location loc, int x, int y, int z) {
        int lX = loc.getBlockX();
        int lY = loc.getBlockY();
        int lZ = loc.getBlockZ();
        db.execute("UPDATE " + db.getPrefix() + "snitches SET snitch_cuboid_x='" + x + "', snitch_cuboid_y='" + y
                + "', snitch_cuboid_z='" + z + "' WHERE snitch_world='" + loc.getWorld().getName() + "' AND snitch_x='" + lX + "' AND snitch_y='" + lY + "' AND snitch_z='" + lZ + "'");
    }

}
