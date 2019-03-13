package vg.civcraft.mc.citadel;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import vg.civcraft.mc.citadel.database.CitadelReinforcementData;
import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.listener.EntityListener;
import vg.civcraft.mc.citadel.listener.InventoryListener;
import vg.civcraft.mc.citadel.listener.WorldListener;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Citadel extends ACivMod {
	
	private static Logger logger;
	
	public static final String chestPerm = "CHESTS";
	public static final String bypassPerm = "BYPASS_REINFORCEMENT";

	private CitadelReinforcementData db;
	private CitadelWorldManager worldManager;
	private GlobalReinforcementManager config;
	private AcidManager acidManager;
	private ReinforcementTypeManager typeManager;
	private static Citadel instance;

	public void onEnable() {
		super.onEnable();
		instance = this;
		logger = getLogger();
		if (!Bukkit.getPluginManager().isPluginEnabled("NameLayer")) {
			logger.info("Citadel is shutting down because it could not find NameLayer");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		config = new GlobalReinforcementManager(this);
		if (!config.parse()) {
			logger.severe("Errors in config file, shutting down");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		typeManager = new ReinforcementTypeManager();
		config.getReinforcementTypes().forEach(t -> typeManager.register(t));
		if (!initializeDatabase()) {
			logger.severe("Errors setting up database, shutting down");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		worldManager = new CitadelWorldManager(db);
		if (!worldManager.setup()) {
			logger.severe("Errors setting up world config, shutting down");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		acidManager = new AcidManager(config.getAcidMaterials());
		registerNameLayerPermissions();
		registerListeners();
	}

	public void onDisable() {
		// Pushes all reinforcements loaded to be saved to db.
		worldManager.flushAll();
	}

	/**
	 * Initializes the database.
	 */
	public boolean initializeDatabase() {
		ManagedDatasource mds = config.getDatabase();
		if (mds == null) {
			return false;
		}
		db = new CitadelReinforcementData(mds, this, typeManager);
		return db.startUp();
	}

	/**
	 * Registers the listeners for Citadel.
	 */
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new WorldListener(), this);
	}

	@SuppressWarnings("unchecked")
	private void registerNameLayerPermissions() {
		LinkedList<PlayerType> membersAndAbove = new LinkedList<PlayerType>();
		membersAndAbove.add(PlayerType.MEMBERS);
		membersAndAbove.add(PlayerType.MODS);
		membersAndAbove.add(PlayerType.ADMINS);
		membersAndAbove.add(PlayerType.OWNER);
		LinkedList<PlayerType> modsAndAbove = new LinkedList<PlayerType>();
		modsAndAbove.add(PlayerType.MODS);
		modsAndAbove.add(PlayerType.ADMINS);
		modsAndAbove.add(PlayerType.OWNER);
		PermissionType.registerPermission("REINFORCE", (LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission("ACIDBLOCK", (LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission("REINFORCEMENT_INFO", (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission(bypassPerm, (LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission("DOORS", (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission(chestPerm, (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission("CROPS", (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission("INSECURE_REINFORCEMENT", (LinkedList<PlayerType>) membersAndAbove.clone());
	}

	/**
	 * @return The ReinforcementManager of Citadel.
	 */
	public CitadelWorldManager getReinforcementManager() {
		return worldManager;
	}

	/**
	 * @return The instance of Citadel.
	 */
	public static Citadel getInstance() {
		return instance;
	}

	/**
	 * @return The Database Manager for Citadel.
	 */
	public CitadelReinforcementData getCitadelDatabase() {
		return db;
	}
	
	public ReinforcementTypeManager getReinforcementTypeManager() {
		return typeManager;
	}

	/**
	 * @return Acid block manager
	 */
	public AcidManager getAcidManager() {
		return acidManager;
	}

	@Override
	public String getPluginName() {
		return "Citadel";
	}
}
