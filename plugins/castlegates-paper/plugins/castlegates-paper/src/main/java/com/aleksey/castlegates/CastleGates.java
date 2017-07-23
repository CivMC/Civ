/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.aleksey.castlegates.engine.CastleGatesManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.aleksey.castlegates.plugins.bastion.BastionManager;
import com.aleksey.castlegates.plugins.bastion.IBastionManager;
import com.aleksey.castlegates.plugins.bastion.NoBastionManager;
import com.aleksey.castlegates.plugins.citadel.CitadelManager;
import com.aleksey.castlegates.plugins.citadel.ICitadelManager;
import com.aleksey.castlegates.plugins.citadel.NoCitadelManager;
import com.aleksey.castlegates.command.CastleGatesCommand;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.plugins.jukealert.IJukeAlertManager;
import com.aleksey.castlegates.plugins.jukealert.JukeAlertManager;
import com.aleksey.castlegates.plugins.jukealert.NoJukeAlertManager;
import com.aleksey.castlegates.listener.BlockListener;
import com.aleksey.castlegates.listener.EntityListener;
import com.aleksey.castlegates.config.ConfigManager;
import com.aleksey.castlegates.plugins.orebfuscator.IOrebfuscatorManager;
import com.aleksey.castlegates.plugins.orebfuscator.NoOrebfuscatorManager;
import com.aleksey.castlegates.plugins.orebfuscator.OrebfuscatorManager;

public class CastleGates extends JavaPlugin {
    private static CastleGates instance;
    public static CastleGates getInstance() {
    	return instance;
    }

    private static CastleGatesManager manager;
    public static CastleGatesManager getManager() {
    	return manager;
    }

    private static ConfigManager configManager;
    public static ConfigManager getConfigManager() {
    	return configManager;
    }

    private static ICitadelManager citadelManager;
    public static ICitadelManager getCitadelManager() {
    	return citadelManager;
    }

    private static IBastionManager bastionManager;
    public static IBastionManager getBastionManager() {
    	return bastionManager;
    }

    private static IJukeAlertManager jukeAlertManager;
    public static IJukeAlertManager getJukeAlertManager() {
    	return jukeAlertManager;
    }

    private static IOrebfuscatorManager orebfuscatorManager;
    public static IOrebfuscatorManager getOrebfuscatorManager() {
    	return orebfuscatorManager;
    }

    public static Logger getPluginLogger() {
    	return instance.getLogger();
    }

    @Override
    public void onEnable() {
    	instance = this;
        manager = new CastleGatesManager();
        configManager = new ConfigManager(getLogger());

        if(getServer().getPluginManager().getPlugin("Citadel") != null) {
        	citadelManager = new CitadelManager();
        	getLogger().log(Level.INFO, "Citadel plugin found.");
        } else {
        	citadelManager = new NoCitadelManager();
        	getLogger().log(Level.INFO, "Citadel plugin NOT found.");
        }

        if(getServer().getPluginManager().getPlugin("JukeAlert") != null) {
        	jukeAlertManager = new JukeAlertManager();
        	getLogger().log(Level.INFO, "JukeAlert plugin found.");
        } else {
        	jukeAlertManager = new NoJukeAlertManager();
        	getLogger().log(Level.INFO, "JukeAlert plugin NOT found.");
        }

        createBastionManager();
        createOrebfuscatorManager();

        // Load configurations
        configManager.load(getConfig());
        saveConfig();

        SqlDatabase db = initDatabase();

        if(db == null) return;

        if(!manager.init(db)) return;

        citadelManager.init();

        // register events
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
    }

    private void createBastionManager() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Bastion");

        if(plugin == null) {
            bastionManager = new NoBastionManager();
        	getLogger().log(Level.INFO, "Bastion plugin NOT found.");
        	return;
        }

        try {
	        String[] versionParts = plugin.getDescription().getVersion().split("\\.");
	        int majorVersion = Integer.parseInt(versionParts[0]);
	        int majorRevision = Integer.parseInt(versionParts[1]);
	        int minorVersion = Integer.parseInt(versionParts[2].split("\\-")[0]);

	        if(majorVersion > 2
	        		|| majorVersion == 2 && majorRevision > 2
	        		|| majorVersion == 2 && majorRevision == 2 && minorVersion >= 0
	        		)
	        {
                bastionManager = new BastionManager();
                bastionManager.init();
	        	getLogger().log(Level.INFO, "Bastion plugin found.");
	        } else {
                bastionManager = new NoBastionManager();
	        	getLogger().log(Level.WARNING, ChatColor.YELLOW + "Bastion plugin found but old versions are NOT supported. You need to use the 2.2.0 version or newer.");
	        }
        } catch (Exception e) {
            bastionManager = new NoBastionManager();
        	getLogger().log(Level.INFO, ChatColor.YELLOW + "Bastion plugin found but this version is NOT supported.");
        }
    }

    private void createOrebfuscatorManager() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Orebfuscator4");

        if(plugin == null) {
            orebfuscatorManager = new NoOrebfuscatorManager();
            getLogger().log(Level.INFO, "Orebfuscator plugin NOT found.");
            return;
        }

        try {
            String[] versionParts = plugin.getDescription().getVersion().split("\\.");
            int majorVersion = Integer.parseInt(versionParts[0]);
            int majorRevision = Integer.parseInt(versionParts[1]);
            int minorVersion = Integer.parseInt(versionParts[2].split("\\-")[0]);

            if(majorVersion > 4
                    || majorVersion == 4 && majorRevision > 1
                    || majorVersion == 4 && majorRevision == 1 && minorVersion > 0
                    )
            {
                orebfuscatorManager = new OrebfuscatorManager();
                getLogger().log(Level.INFO, "Orebfuscator plugin found.");
            } else {
                orebfuscatorManager = new NoOrebfuscatorManager();
                getLogger().log(Level.WARNING, ChatColor.YELLOW + "Orebfuscator plugin found but old versions are NOT supported. You need to use the 4.1.1 version or newer.");
            }
        } catch (Exception e) {
            orebfuscatorManager = new NoOrebfuscatorManager();
            getLogger().log(Level.INFO, ChatColor.YELLOW + "Orebfuscator plugin found but this version is NOT supported.");
        }
    }

    private SqlDatabase initDatabase() {
    	SqlDatabase db = new SqlDatabase(
    		configManager.getDatabase().host,
    		configManager.getDatabase().port,
    		configManager.getDatabase().db,
    		configManager.getDatabase().user,
    		configManager.getDatabase().password,
    		getLogger()
    		);

    	if(!db.connect()) return null;

    	if(!db.initDb()) {
    		db.close();
    		return null;
    	}

    	return db;
    }

    @Override
    public void onDisable() {
        manager.close();
        citadelManager.close();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CastleGatesCommand.onCommand(sender, command, label, args);
    }

    public static void runTask(Runnable task) {
        instance.getServer().getScheduler().runTask(instance, task);
    }
}
