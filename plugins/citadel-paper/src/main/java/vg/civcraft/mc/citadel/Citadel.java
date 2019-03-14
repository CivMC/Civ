package vg.civcraft.mc.citadel;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import vg.civcraft.mc.citadel.database.CitadelReinforcementData;
import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.listener.EntityListener;
import vg.civcraft.mc.citadel.listener.InventoryListener;
import vg.civcraft.mc.citadel.listener.RedstoneListener;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.GlobalReinforcementManager;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementTypeManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Citadel extends ACivMod {

	public static final String chestPerm = "CHESTS";

	public static final String bypassPerm = "BYPASS_REINFORCEMENT";
	public static final String cropsPerm = "CROPS";
	public static final String insecurePerm = "INSECURE_REINFORCEMENT";
	public static final String reinforcePerm = "REINFORCE";
	public static final String doorPerm = "DOORS";
	public static final String acidPerm = "ACIDBLOCK";
	public static final String infoPerm = "REINFORCEMENT_INFO";
	private static Citadel instance;

	public static Citadel getInstance() {
		return instance;
	}
	
	private Logger logger;
	private CitadelReinforcementData db;
	private GlobalReinforcementManager worldManager;
	private CitadelConfigManager config;
	private AcidManager acidManager;
	private ReinforcementTypeManager typeManager;

	private PlayerStateManager stateManager;

	/**
	 * @return Acid block manager
	 */
	public AcidManager getAcidManager() {
		return acidManager;
	}

	/**
	 * @return The Database Manager for Citadel.
	 */
	public CitadelReinforcementData getCitadelDatabase() {
		return db;
	}

	public CitadelConfigManager getConfigManager() {
		return config;
	}

	@Override
	public String getPluginName() {
		return "Citadel";
	}

	/**
	 * @return The ReinforcementManager of Citadel.
	 */
	public GlobalReinforcementManager getReinforcementManager() {
		return worldManager;
	}

	public ReinforcementTypeManager getReinforcementTypeManager() {
		return typeManager;
	}

	public PlayerStateManager getStateManager() {
		return stateManager;
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

	public void onDisable() {
		// Pushes all reinforcements loaded to be saved to db.
		worldManager.flushAll();
	}

	public void onEnable() {
		super.onEnable();
		instance = this;
		logger = getLogger();
		if (!Bukkit.getPluginManager().isPluginEnabled("NameLayer")) {
			logger.info("Citadel is shutting down because it could not find NameLayer");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		config = new CitadelConfigManager(this);
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
		worldManager = new GlobalReinforcementManager(db);
		if (!worldManager.setup()) {
			logger.severe("Errors setting up world config, shutting down");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		stateManager = new PlayerStateManager();
		acidManager = new AcidManager(config.getAcidMaterials());
		registerNameLayerPermissions();
		registerListeners();
	}

	/**
	 * Registers the listeners for Citadel.
	 */
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new BlockListener(), this);
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new InventoryListener(), this);
		getServer().getPluginManager().registerEvents(new RedstoneListener(config.getMaxRedstoneDistance()), this);
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
		PermissionType.registerPermission(reinforcePerm, (LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission(acidPerm, (LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission(infoPerm, (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission(bypassPerm, (LinkedList<PlayerType>) modsAndAbove.clone());
		PermissionType.registerPermission(doorPerm, (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission(chestPerm, (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission(cropsPerm, (LinkedList<PlayerType>) membersAndAbove.clone());
		PermissionType.registerPermission(insecurePerm, (LinkedList<PlayerType>) membersAndAbove.clone());
	}
}
