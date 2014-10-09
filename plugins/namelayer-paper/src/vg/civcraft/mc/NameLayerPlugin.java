package vg.civcraft.mc;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.command.CommandHandler;
import vg.civcraft.mc.database.AssociationList;
import vg.civcraft.mc.database.Database;
import vg.civcraft.mc.database.SaveManager;
import vg.civcraft.mc.listeners.AssociationListener;
import vg.civcraft.mc.misc.GameProfileModifier;


public class NameLayerPlugin extends JavaPlugin{
	private static AssociationList associations;
	private static SaveManager saveManager;
	private static NameLayerPlugin instance;
	private CommandHandler handle;
	private Database db;
	private GameProfileModifier game= new GameProfileModifier();

	private FileConfiguration config;
	@Override
	public void onEnable() {
		instance = this;
		config = getConfig();
		if (!new File(this.getDataFolder() + "config.yml").exists())
			this.saveDefaultConfig();
		new ConfigManager();
		loadDatabases();
		new NameAPI(new GroupManager());
		registerListeners();
	    handle = new CommandHandler();
	    handle.addCommands();
	}
	
	public void registerListeners(){
		getServer().getPluginManager().registerEvents(new AssociationListener(), this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return handle.execute(sender, label, args);
	}
	
	public void onDisable() {
		saveManager.flushDataToDB();
	}
	
	public static NameLayerPlugin getInstance(){
		return instance;
	}
	
	public void loadDatabases(){
		String host = config.getString("sql.hostname");
		int port = config.getInt("sql.port");
		String dbname = config.getString("sql.dbname");
		String username = config.getString("sql.username");
		String password = config.getString("sql.password");
		db = new Database(host, port, dbname, username, password, getLogger());
		db.connect();
		if (!db.isConnected()){
			NameLayerPlugin.log(Level.WARNING, "Could not connect to DataBase, shutting down!");
			Bukkit.getPluginManager().disablePlugin(this); // Why have it try connect, it can't
		}
		associations = new AssociationList(db);
		saveManager = new SaveManager(db);
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
	
	public static AssociationList getAssociationList(){
		return associations;
	}
	
	public static SaveManager getSaveManager(){
		return saveManager;
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
	
	public static void insertVersionNum(int version, String pluginName){
		saveManager.updateVersion(version, pluginName);
	}
	
	public static int getVersionNum(String pluginName){
		return saveManager.checkVersion();
	}
}
