package vg.civcraft.mc.citadel;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import vg.civcraft.mc.citadel.command.CommandHandler;
import vg.civcraft.mc.citadel.database.CitadelReinforcementData;
import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.listener.EntityListener;
import vg.civcraft.mc.citadel.listener.GroupsListener;
import vg.civcraft.mc.citadel.listener.InventoryListener;
import vg.civcraft.mc.citadel.listener.WorldListener;
import vg.civcraft.mc.citadel.misc.CitadelStatics;
import vg.civcraft.mc.citadel.reinforcementtypes.NaturalReinforcementType;
import vg.civcraft.mc.citadel.reinforcementtypes.NonReinforceableType;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Citadel extends ACivMod{
	private static Logger logger;
	
	private static CitadelReinforcementData db;
	private static ReinforcementManager rm;
	private CommandHandler cHandle;
	private static Citadel instance;
	
	// Calling this for ACivMod
	@Override
	public void onLoad(){
		//super.onLoad();
	}
	
	public void onEnable(){
		//super.onEnable();
		instance = this;
		logger = getLogger();
		if (!Bukkit.getPluginManager().isPluginEnabled("NameLayer")){
			logger.info("Citadel is shutting down because it could not find NameLayer");
			this.getPluginLoader().disablePlugin(this); // shut down
		}
		saveDefaultConfig();
		new CitadelConfigManager(getConfig());
		
		// Grab the values from config
		ReinforcementType.initializeReinforcementTypes();
		NaturalReinforcementType.initializeNaturalReinforcementsTypes();
		NonReinforceableType.initializeNonReinforceableTypes();
		initializeDatabase();
		
		rm = new ReinforcementManager(db);
		
		registerListeners();
		registerCommands();
		registerNameLayerPermissions();
	}
	
	public void onDisable(){
		// Pushes all reinforcements loaded to be saved to db.
		rm.invalidateAllReinforcements();
		CitadelStatics.displayStatisticsToConsole();
	}
	/**
	 * Initializes the database.
	 */
	public void initializeDatabase(){
		String host = CitadelConfigManager.getHostName();
		String user = CitadelConfigManager.getUserName();
		String password = CitadelConfigManager.getPassword();
		int port = CitadelConfigManager.getPort();
		String dbName = CitadelConfigManager.getDBName();
		int poolsize = CitadelConfigManager.getPoolSize();
		long connectionTimeout = CitadelConfigManager.getConnectionTimeout();
		long idleTimeout = CitadelConfigManager.getIdleTimeout();
		long maxLifetime = CitadelConfigManager.getMaxLifetime();
		try {
			ManagedDatasource idb = new ManagedDatasource(this, user, password, host, port,
					dbName, poolsize, connectionTimeout, idleTimeout, maxLifetime);
			idb.getConnection().close();

			db = new CitadelReinforcementData(idb);
			
			try {
				getLogger().log(Level.INFO, "Update prepared, starting database update.");
				db.registerMigrations();
				
				if (!idb.updateDatabase()) {
					getLogger().log(Level.SEVERE, "Update failed, terminating Bukkit.");
					Bukkit.shutdown();
				}
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Update failed, terminating Bukkit. Cause:", e);
				Bukkit.shutdown();
			}

		} catch (Exception se) {
			getLogger().log(Level.WARNING, "Could not connect to database, shutting down!");
			Bukkit.shutdown();
			return;
		}

	}
	/**
	 * Registers the listeners for Citadel.
	 */
	private void registerListeners(){
		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(new GroupsListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new WorldListener(), this);
	}
	
	@SuppressWarnings("unchecked")
	public void registerNameLayerPermissions() {
		LinkedList <PlayerType> membersAndAbove = new LinkedList<PlayerType>();
		membersAndAbove.add(PlayerType.MEMBERS);
		membersAndAbove.add(PlayerType.MODS);
		membersAndAbove.add(PlayerType.ADMINS);
		membersAndAbove.add(PlayerType.OWNER);
		LinkedList <PlayerType> modsAndAbove = new LinkedList<PlayerType>();
		modsAndAbove.add(PlayerType.MODS);
		modsAndAbove.add(PlayerType.ADMINS);
		modsAndAbove.add(PlayerType.OWNER);
		PermissionType.registerPermission("REINFORCE",(LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission("ACIDBLOCK",(LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission("REINFORCEMENT_INFO",(LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission("BYPASS_REINFORCEMENT", (LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission("DOORS",(LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission("CHESTS",(LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission("CROPS",(LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission("INSECURE_REINFORCEMENT",(LinkedList<PlayerType>) membersAndAbove.clone());
	}
	
	
	/**
	 * Registers the commands for Citadel.
	 */
	public void registerCommands(){
		cHandle = new CommandHandler();
		cHandle.registerCommands();
	}

	/**
	 * @return The ReinforcementManager of Citadel.
	 */
	public static ReinforcementManager getReinforcementManager(){
		return rm;
	}
	/**
	 * @return The instance of Citadel.
	 */
	public static Citadel getInstance(){
		return instance;
	}
	/**
	 * @return The Database Manager for Citadel.
	 */
	public static CitadelReinforcementData getCitadelDatabase(){
		return db;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return cHandle.execute(sender, cmd, args);
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
		return cHandle.complete(sender, cmd, args);
	}

	@Override
	public String getPluginName() {
		return "Citadel";
	}
}
