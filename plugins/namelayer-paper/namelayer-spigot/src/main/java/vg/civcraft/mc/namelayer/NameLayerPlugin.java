package vg.civcraft.mc.namelayer;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.Config;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.command.CommandHandler;
import vg.civcraft.mc.namelayer.database.AssociationList;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.AutoAcceptHandler;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.DefaultGroupHandler;
import vg.civcraft.mc.namelayer.listeners.AssociationListener;
import vg.civcraft.mc.namelayer.listeners.MercuryMessageListener;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.misc.ClassHandler;
import vg.civcraft.mc.namelayer.permission.PermissionType;


public class NameLayerPlugin extends ACivMod{
	private static AssociationList associations;
	private static BlackList blackList;
	private static GroupManagerDao groupManagerDao;
	private static DefaultGroupHandler defaultGroupHandler;
	private static NameLayerPlugin instance;
	private static AutoAcceptHandler autoAcceptHandler;
	private CommandHandler handle;
	private static ManagedDatasource db;
	private static boolean loadGroups = true;
	private static int groupLimit = 10;
	private static boolean createGroupOnFirstJoin;
	private Config config;
	private boolean mercuryEnabled;
	
	@CivConfigs({
		@CivConfig(name = "groups.enable", def = "true", type = CivConfigType.Bool),
		@CivConfig(name = "groups.grouplimit", def = "10", type = CivConfigType.Int),
		@CivConfig(name = "groups.creationOnFirstJoin", def = "true", type = CivConfigType.Bool)
	})
	@Override
	public void onEnable() {
		super.onEnable(); // Need to call this to properly initialize this mod
		config = GetConfig();
		loadGroups = config.get("groups.enable").getBool();
		groupLimit = config.get("groups.grouplimit").getInt();
		createGroupOnFirstJoin = config.get("groups.creationOnFirstJoin").getBool();
		instance = this;
		mercuryEnabled = Bukkit.getPluginManager().isPluginEnabled("Mercury");
		loadDatabases();
	    ClassHandler.Initialize(Bukkit.getServer());
		new NameAPI(new GroupManager(), associations);
		registerListeners();
		if (loadGroups){
			PermissionType.initialize();
			blackList = new BlackList();
			groupManagerDao.loadGroupsInvitations();
			defaultGroupHandler = new DefaultGroupHandler();
			autoAcceptHandler = new AutoAcceptHandler(groupManagerDao.loadAllAutoAccept());
			handle = new CommandHandler();
			handle.registerCommands();
		}
	}
	
	// Calling this for ACivMod
	@Override
	public void onLoad(){
		super.onLoad();
	}
	
	public void registerListeners(){
		getServer().getPluginManager().registerEvents(new AssociationListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		if (isMercuryEnabled()){
			getServer().getPluginManager().registerEvents(new MercuryMessageListener(), this);
		}
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
		if (db != null) {
			try {
				db.close();
			} catch (Exception e) {
				getLogger().log(Level.INFO, "Failed to close database gracefully on shutdown.", e);
			}
		}
	}
	
	public static NameLayerPlugin getInstance(){
		return instance;
	}
	
	@CivConfigs({
		@CivConfig(name = "sql.hostname", def = "localhost", type = CivConfigType.String),
		@CivConfig(name = "sql.username", def = "", type = CivConfigType.String),
		@CivConfig(name = "sql.password", def = "", type = CivConfigType.String),
		@CivConfig(name = "sql.port", def = "3306", type = CivConfigType.Int),
		@CivConfig(name = "sql.dbname", def = "namelayer", type = CivConfigType.String),
		@CivConfig(name = "sql.poolsize", def = "10", type = CivConfigType.Int),
		@CivConfig(name = "sql.connection_timeout", def = "10000", type = CivConfigType.Long),
		@CivConfig(name = "sql.idle_timeout", def = "600000", type = CivConfigType.Long),
		@CivConfig(name = "sql.max_lifetime", def = "7200000", type = CivConfigType.Long)
	})
	public void loadDatabases(){
		String host = config.get("sql.hostname").getString();
		int port = config.get("sql.port").getInt();
		String dbname = config.get("sql.dbname").getString();
		String username = config.get("sql.username").getString();
		String password = config.get("sql.password").getString();
		int poolsize = config.get("sql.poolsize").getInt();
		long connectionTimeout = config.get("sql.connection_timeout").getLong();
		long idleTimeout = config.get("sql.idle_timeout").getLong();
		long maxLifetime = config.get("sql.max_lifetime").getLong();
		db = new ManagedDatasource(this, username, password, host, port, dbname,
				poolsize, connectionTimeout, idleTimeout, maxLifetime);
		try {
			db.getConnection().close();
		} catch (Exception se) {
			NameLayerPlugin.log(Level.WARNING, "Could not connect to DataBase, shutting down!");
			Bukkit.shutdown();
		}
		
		// First migration is conversion from old system to new, and lives outside AssociationList and GroupManagerDao.
		db.registerMigration(-2, false,
				new Callable<Boolean>() {
					@Override
					public Boolean call() {
						return false; // Force a failure. Migrations doesn't check the current migration per step, only at beginning.
						// So, we force a shutdown failure on first run. Then on second run, the Migration table will hold the correct values.
					}
				},
				"INSERT INTO managed_plugin_data (plugin_name, current_migration_number, last_migration)"
						+ " SELECT plugin_name, max(db_version), `timestamp` FROM db_version WHERE plugin_name = '" + this.getName() + "' LIMIT 1;");

		associations = new AssociationList(getLogger(), db);
		associations.registerMigrations();
		
		if (loadGroups) {
			groupManagerDao = new GroupManagerDao(getLogger(), db);
			groupManagerDao.registerMigrations();
		}
		
		long begin_time = System.currentTimeMillis();

		try {
			getLogger().log(Level.INFO, "Update prepared, starting database update.");
			if (!db.updateDatabase()) {
				getLogger().log(Level.SEVERE, "Update failed, terminating Bukkit.");
				Bukkit.shutdown();
			}
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Update failed, terminating Bukkit. Cause:", e);
		}

		getLogger().log(Level.INFO, "Database update took {0} seconds", (System.currentTimeMillis() - begin_time) / 1000);

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
	@Deprecated
	public static void insertVersionNum(int currentVersion, String pluginName){
		throw new UnsupportedOperationException("insertVersionNum is no longer supported. Extend CivModCore and use ManagedDatasource"); 
	}
	/**
	 * Checks the version of a specific plugin's db.
	 * @param name- The name of the plugin.
	 * @return Returns the version of the plugin or 0 if none was found.
	 */
	@Deprecated
	public static int getVersionNum(String pluginName){
		throw new UnsupportedOperationException("getVersionNum is no longer supported. Extend CivModCore and use ManagedDatasource");
	}
	
	public static String getSpecialAdminGroup(){
		return "Name_Layer_Special";
	}
	
	public static boolean createGroupOnFirstJoin() {
		return createGroupOnFirstJoin;
	}

	@Override
	protected String getPluginName() {
		return "NameLayerPlugin";
	}

	public static boolean isMercuryEnabled() {
		return getInstance().mercuryEnabled;
	}
	
	public int getGroupLimit(){
		return groupLimit;
	}
	
	public static BlackList getBlackList() {
		return blackList;
	}
	
	public static AutoAcceptHandler getAutoAcceptHandler() {
		return autoAcceptHandler;
	}
	
	public static DefaultGroupHandler getDefaultGroupHandler() {
		return defaultGroupHandler;
	}
}
