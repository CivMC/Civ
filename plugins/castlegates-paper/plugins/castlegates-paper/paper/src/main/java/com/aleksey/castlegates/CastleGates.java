/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.aleksey.castlegates.engine.CastleGatesManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

public class CastleGates extends JavaPlugin {
    private static CastleGates _instance;
    public static CastleGates getInstance() {
    	return _instance;
    }

    private static CastleGatesManager _manager;
    public static CastleGatesManager getManager() {
    	return _manager;
    }

    private static ConfigManager _configManager;
    public static ConfigManager getConfigManager() {
    	return _configManager;
    }

    private static ICitadelManager _citadelManager;
    public static ICitadelManager getCitadelManager() {
    	return _citadelManager;
    }

    private static IBastionManager _bastionManager;
    public static IBastionManager getBastionManager() {
    	return _bastionManager;
    }

    private static IJukeAlertManager _jukeAlertManager;
    public static IJukeAlertManager getJukeAlertManager() {
    	return _jukeAlertManager;
    }

    public static Logger getPluginLogger() {
    	return _instance.getLogger();
    }

    @Override
    public void onEnable() {
    	_instance = this;
        _manager = new CastleGatesManager();
        _configManager = new ConfigManager(getLogger());

        enableCivPlugins();

        // Load configurations
        _configManager.load(getConfig());
        saveConfig();

        SqlDatabase db = initDatabase();

        if(db == null) return;

        if(!_manager.init(db))
            return;

        _citadelManager.init();

        // register events
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
    }

    private void enableCivPlugins() {
        if(getServer().getPluginManager().getPlugin("Citadel") != null) {
            _citadelManager = new CitadelManager();
            getLogger().log(Level.INFO, "Citadel plugin found.");
        } else {
            _citadelManager = new NoCitadelManager();
            getLogger().log(Level.INFO, "Citadel plugin NOT found.");
        }

        if(getServer().getPluginManager().getPlugin("JukeAlert") != null) {
            _jukeAlertManager = new JukeAlertManager();
            getLogger().log(Level.INFO, "JukeAlert plugin found.");
        } else {
            _jukeAlertManager = new NoJukeAlertManager();
            getLogger().log(Level.INFO, "JukeAlert plugin NOT found.");
        }

        if(getServer().getPluginManager().getPlugin("Bastion") != null) {
            _bastionManager = new BastionManager();
            _bastionManager.init();
            getLogger().log(Level.INFO, "Bastion plugin found.");
        } else {
            _bastionManager = new NoBastionManager();
            getLogger().log(Level.INFO, "Bastion plugin NOT found.");
        }
    }

    private SqlDatabase initDatabase() {
    	SqlDatabase db = new SqlDatabase(
    		_configManager.getDatabase().host,
    		_configManager.getDatabase().port,
    		_configManager.getDatabase().db,
    		_configManager.getDatabase().user,
    		_configManager.getDatabase().password,
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
        _manager.close();
        _citadelManager.close();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CastleGatesCommand.onCommand(sender, command, label, args);
    }

    public static void runTask(Runnable task) {
        _instance.getServer().getScheduler().runTask(_instance, task);
    }
}
