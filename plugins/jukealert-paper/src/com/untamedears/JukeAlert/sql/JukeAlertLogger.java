/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.JukeAlert.sql;

import com.untamedears.JukeAlert.JukeAlert;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;

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
                + "PRIMARY KEY (`snitch_id`));");
    }
    
    //Logs the snitch being placed at World, x, y, z in the database.
    public void logSnitchPlace(String world, String group, double x, double y, double z) {
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
