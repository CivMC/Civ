package vg.civcraft.mc.namelayer;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.namelayer.command.CommandHandler;
import vg.civcraft.mc.namelayer.database.AssociationList;
import vg.civcraft.mc.namelayer.database.Database;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.listeners.AssociationListener;
import vg.civcraft.mc.namelayer.misc.GameProfileModifier;


public class NameLayerPlugin extends JavaPlugin{
	private static AssociationList associations;
	private static GroupManagerDao groupManagerDao;
	private static NameLayerPlugin instance;
	private CommandHandler handle;
	private Database db;
	private GameProfileModifier game= new GameProfileModifier();

	@Override
	public void onEnable() {
		instance = this;
		if (!new File(this.getDataFolder() + "config.yml").exists())
			this.saveDefaultConfig();
		new NameLayerConfigManager();
		loadDatabases();
		new NameAPI(new GroupManager());
		registerListeners();
	    handle = new CommandHandler();
	    handle.registerCommands();
	}
	
	public void registerListeners(){
		getServer().getPluginManager().registerEvents(new AssociationListener(), this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return handle.execute(sender, label, args);
	}
	
	public void onDisable() {
		
	}
	
	public static NameLayerPlugin getInstance(){
		return instance;
	}
	
	public void loadDatabases(){
		String host = NameLayerConfigManager.getMySQLHostName();
		int port = NameLayerConfigManager.getMySQLPort();
		String dbname = NameLayerConfigManager.getMySQLDbname();
		String username = NameLayerConfigManager.getMySQLUsername();
		String password = NameLayerConfigManager.getMySQLPassword();
		db = new Database(host, port, dbname, username, password, getLogger());
		db.connect();
		if (!db.isConnected()){
			NameLayerPlugin.log(Level.WARNING, "Could not connect to DataBase, shutting down!");
			Bukkit.getPluginManager().disablePlugin(this); // Why have it try connect, it can't
		}
		associations = new AssociationList(db);
		groupManagerDao = new GroupManagerDao(db);
	}
	
	private String packageName = getClass().getPackage().getName();
	private String version = packageName.substring(packageName.lastIndexOf('.') + 1);
	// sets the player name in the gameprofile
	@EventHandler(priority=EventPriority.LOWEST)
	public void loginEvent(PlayerLoginEvent event){
		if (!version.equals("v1_7_R4"))
			return;
		game.setPlayerProfile(event.getPlayer());
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
			Bukkit.getLogger().log(level, "[NameTracker:] Info follows\n" +
			message);
		else if (level == Level.WARNING)
			Bukkit.getLogger().log(level, "[NameTracker:] Warning follows\n" +
					message);
		else if (level == Level.SEVERE)
			Bukkit.getLogger().log(level, "[NameTracker:] Stack Trace follows\n --------------------------------------\n" +
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
}
