package vg.civcraft.mc.namelayer;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.namelayer.command.CommandHandler;
import vg.civcraft.mc.namelayer.config.NameConfigListener;
import vg.civcraft.mc.namelayer.config.NameConfigManager;
import vg.civcraft.mc.namelayer.config.annotations.NameConfig;
import vg.civcraft.mc.namelayer.config.annotations.NameConfigType;
import vg.civcraft.mc.namelayer.config.annotations.NameConfigs;
import vg.civcraft.mc.namelayer.database.AssociationList;
import vg.civcraft.mc.namelayer.database.Database;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.listeners.AssociationListener;
import vg.civcraft.mc.namelayer.listeners.MercuryMessageListener;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.misc.ClassHandler;


public class NameLayerPlugin extends JavaPlugin implements NameConfigListener{
	private static AssociationList associations;
	private static GroupManagerDao groupManagerDao;
	private static NameLayerPlugin instance;
	private CommandHandler handle;
	private static Database db;
	private static boolean loadGroups = true;
	private static boolean isMercuryEnabled = false;
	
	private NameConfigManager config;
	
	@NameConfig(name = "groups.enable", def = "true", type = NameConfigType.Bool)
	@Override
	public void onEnable() {
		instance = this;
		isMercuryEnabled = Bukkit.getPluginManager().isPluginEnabled("Mercury");
		config = new NameConfigManager();
		registerListeners();
		loadDatabases();
		new NameAPI(new GroupManager(), associations, config);
	    ClassHandler.Initialize(Bukkit.getServer());
		loadGroups = config.get(this, "groups.enable").getBool();
		if (loadGroups){
			handle = new CommandHandler();
			handle.registerCommands();
		}
	}
	
	public void registerListeners(){
		getServer().getPluginManager().registerEvents(new AssociationListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		if (isMercuryEnabled)
			getServer().getPluginManager().registerEvents(new MercuryMessageListener(), this);
		config.registerListener(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!loadGroups)
			return false;
		return handle.execute(sender, cmd, args);
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
		if (!loadGroups)
			return null;
		return handle.complete(sender, cmd, args);
	}

	public void onDisable() {
		
	}
	
	public static NameLayerPlugin getInstance(){
		return instance;
	}
	
	@NameConfigs({
		@NameConfig(name = "sql.hostname", def = "localhost", type = NameConfigType.String),
		@NameConfig(name = "sql.username"),
		@NameConfig(name = "sql.password"),
		@NameConfig(name = "sql.port", def = "3306", type = NameConfigType.Int),
		@NameConfig(name = "sql.dbname", def = "namelayer")
	})
	public void loadDatabases(){
		String host = config.get(this, "sql.hostname").getString();
		int port = config.get(this, "sql.port").getInt();
		String dbname = config.get(this, "sql.dbname").getString();
		String username = config.get(this, "sql.username").getString();
		String password = config.get(this, "sql.password").getString();
		db = new Database(host, port, dbname, username, password, getLogger());
		db.connect();
		if (!db.isConnected()){
			NameLayerPlugin.log(Level.WARNING, "Could not connect to DataBase, shutting down!");
			Bukkit.getPluginManager().disablePlugin(this); // Why have it try connect, it can't
		}
		associations = new AssociationList(db);
		if (loadGroups)
			groupManagerDao = new GroupManagerDao(db);
	}
	
	public static void reconnectAndReintializeStatements(){
		if (db.isConnected())
			return;
		db.connect();
		associations.initializeStatements();
		if (loadGroups)
			groupManagerDao.initializeStatements();
	}
	
	/**
	 * @return Returns the AssocationList.
	 */
	public static AssociationList getAssociationList(){
		return associations;
	}
	/**
	 * @return Returns the GroupManagerDatabase.
	 */
	public static GroupManagerDao getGroupManagerDao(){
		return groupManagerDao;
	}
	
	public static void log(Level level, String message){
		if (level == Level.INFO)
			Bukkit.getLogger().log(level, "[NameLayer:] Info follows\n" +
			message);
		else if (level == Level.WARNING)
			Bukkit.getLogger().log(level, "[NameLayer:] Warning follows\n" +
					message);
		else if (level == Level.SEVERE)
			Bukkit.getLogger().log(level, "[NameLayer:] Stack Trace follows\n --------------------------------------\n" +
					message +
					"\n --------------------------------------");
	}
	/**
	 * Updates the version number for a plugin. You must specify what 
	 * the current version number is.
	 * @param currentVersion- The current version of the plugin.
	 * @param pluginName- The plugin name.
	 * @return Returns the new version of the db.
	 */
	public static void insertVersionNum(int currentVersion, String pluginName){
		groupManagerDao.updateVersion(currentVersion, pluginName);
	}
	/**
	 * Checks the version of a specific plugin's db.
	 * @param name- The name of the plugin.
	 * @return Returns the version of the plugin or 0 if none was found.
	 */
	public static int getVersionNum(String pluginName){
		return groupManagerDao.checkVersion(pluginName);
	}
	
	public static String getSpecialAdminGroup(){
		return "Name_Layer_Special";
	}
	
	public static boolean isMercuryEnabled(){
		return isMercuryEnabled;
	}
}
