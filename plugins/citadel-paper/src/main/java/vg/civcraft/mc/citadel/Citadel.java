package vg.civcraft.mc.citadel;

import java.util.LinkedList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.citadel.database.CitadelReinforcementData;
import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.listener.ChunkLoadListener;
import vg.civcraft.mc.citadel.listener.EntityListener;
import vg.civcraft.mc.citadel.listener.InventoryListener;
import vg.civcraft.mc.citadel.listener.RedstoneListener;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.CitadelSettingManager;
import vg.civcraft.mc.citadel.model.GlobalReinforcementManager;
import vg.civcraft.mc.citadel.model.HologramManager;
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
	public static final String repairPerm = "REPAIR_REINFORCEMENT";

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
	private HologramManager holoManager;
	private CitadelSettingManager settingManager;

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

	public GlobalReinforcementManager getReinforcementManager() {
		return worldManager;
	}

	public ReinforcementTypeManager getReinforcementTypeManager() {
		return typeManager;
	}
	
	public CitadelSettingManager getSettingManager() {
		return settingManager;
	}

	public PlayerStateManager getStateManager() {
		return stateManager;
	}

	public HologramManager getHologramManager() {
		return holoManager;
	}

	public boolean initializeDatabase() {
		ManagedDatasource mds = config.getDatabase();
		if (mds == null) {
			return false;
		}
		db = new CitadelReinforcementData(mds, this, typeManager);
		return db.startUp();
	}

	@Override
	public void onDisable() {
		// Pushes all reinforcements loaded to be saved to db.
		worldManager.flushAll();
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	public void reload() {
		onDisable();
		onEnable();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		logger = getLogger();
		if (!Bukkit.getPluginManager().isPluginEnabled("NameLayer")) {
			logger.info("Citadel is shutting down because it could not find NameLayer");
			Bukkit.shutdown();
			return;
		}
		config = new CitadelConfigManager(this);
		if (!config.parse()) {
			logger.severe("Errors in config file, shutting down");
			Bukkit.shutdown();
			return;
		}
		typeManager = new ReinforcementTypeManager();
		config.getReinforcementTypes().forEach(t -> typeManager.register(t));
		if (!initializeDatabase()) {
			logger.severe("Errors setting up database, shutting down");
			Bukkit.shutdown();
			return;
		}
		worldManager = new GlobalReinforcementManager(db);
		if (!worldManager.setup()) {
			logger.severe("Errors setting up world config, shutting down");
			Bukkit.shutdown();
			return;
		}
		stateManager = new PlayerStateManager();
		acidManager = new AcidManager(config.getAcidMaterials());
		settingManager = new CitadelSettingManager();
		if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			holoManager = new HologramManager();
		}
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
		getServer().getPluginManager().registerEvents(new ChunkLoadListener(), this);
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
		PermissionType.registerPermission(reinforcePerm, (LinkedList<PlayerType>) modsAndAbove.clone(),
				"Allows reinforcing blocks on this group");
		PermissionType.registerPermission(acidPerm, (LinkedList<PlayerType>) modsAndAbove.clone(),
				"Allows activating acid blocks reinforced on this group");
		PermissionType.registerPermission(infoPerm, (LinkedList<PlayerType>) membersAndAbove.clone(),
				"Allows viewing information on reinforcements reinforced on this group");
		PermissionType.registerPermission(bypassPerm, (LinkedList<PlayerType>) modsAndAbove.clone(),
				"Allows bypassing reinforcements reinforced on this group");
		PermissionType.registerPermission(repairPerm, (LinkedList<PlayerType>) modsAndAbove.clone(),
				"Allows repairing reinforcements reinforced on this group");
		PermissionType.registerPermission(doorPerm, (LinkedList<PlayerType>) membersAndAbove.clone(),
				"Allows opening doors reinforced on this group");
		PermissionType.registerPermission(chestPerm, (LinkedList<PlayerType>) membersAndAbove.clone(),
				"Allows opening containers like chests reinforced on this group");
		PermissionType.registerPermission(cropsPerm, (LinkedList<PlayerType>) membersAndAbove.clone(),
				"Allows harvesting crops growing on soil reinforced on this group");
		PermissionType.registerPermission(insecurePerm, (LinkedList<PlayerType>) membersAndAbove.clone(),
				"Allows toggling the insecure flag on reinforcements");
	}
}
