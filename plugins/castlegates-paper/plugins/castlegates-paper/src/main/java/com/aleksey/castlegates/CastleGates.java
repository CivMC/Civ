/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.aleksey.castlegates.bastion.BastionManager;
import com.aleksey.castlegates.bastion.IBastionManager;
import com.aleksey.castlegates.bastion.NoBastionManager;
import com.aleksey.castlegates.citadel.CitadelManager;
import com.aleksey.castlegates.citadel.ICitadelManager;
import com.aleksey.castlegates.citadel.NoCitadelManager;
import com.aleksey.castlegates.command.CastleGatesCommand;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.jukealert.IJukeAlertManager;
import com.aleksey.castlegates.jukealert.JukeAlertManager;
import com.aleksey.castlegates.jukealert.NoJukeAlertManager;
import com.aleksey.castlegates.listener.BlockListener;
import com.aleksey.castlegates.listener.EntityListener;
import com.aleksey.castlegates.manager.CastleGatesManager;
import com.aleksey.castlegates.manager.ConfigManager;
import com.aleksey.castlegates.orebfuscator.IOrebfuscatorManager;
import com.aleksey.castlegates.orebfuscator.NoOrebfuscatorManager;
import com.aleksey.castlegates.orebfuscator.OrebfuscatorManager;

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
        	getLogger().log(Level.INFO, "Citadel plugin is found");
        } else {
        	citadelManager = new NoCitadelManager();
        	getLogger().log(Level.INFO, "Citadel plugin is NOT found");
        }

        if(getServer().getPluginManager().getPlugin("Bastion") != null) {
        	bastionManager = new BastionManager();
        	bastionManager.init();
        	getLogger().log(Level.INFO, "Bastion plugin is found");
        } else {
        	bastionManager = new NoBastionManager();
        	getLogger().log(Level.INFO, "Bastion plugin is NOT found");
        }

        if(getServer().getPluginManager().getPlugin("JukeAlert") != null) {
        	jukeAlertManager = new JukeAlertManager();
        	getLogger().log(Level.INFO, "JukeAlert plugin is found");
        } else {
        	jukeAlertManager = new NoJukeAlertManager();
        	getLogger().log(Level.INFO, "JukeAlert plugin is NOT found");
        }

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

    private void createOrebfuscatorManager() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Orebfuscator4");

        if(plugin == null) {
        	orebfuscatorManager = new NoOrebfuscatorManager();
        	getLogger().log(Level.INFO, "Orebfuscator plugin is NOT found");
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
	        	getLogger().log(Level.INFO, "Orebfuscator plugin is found");
	        } else {
	        	orebfuscatorManager = new NoOrebfuscatorManager();
	        	getLogger().log(Level.INFO, "Orebfuscator plugin is found but old versions are NOT supported. You need to use 4.1.1 version or newer.");
	        }
        } catch (Exception e) {
        	orebfuscatorManager = new NoOrebfuscatorManager();
        	getLogger().log(Level.INFO, "Orebfuscator plugin is found but this version is NOT supported.");
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
