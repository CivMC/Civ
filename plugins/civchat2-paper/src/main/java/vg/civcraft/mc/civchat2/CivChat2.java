package vg.civcraft.mc.civchat2;

import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.listeners.CivChat2Listener;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2FileLogger;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jjj5311
 *
 */
public class CivChat2 extends ACivMod {

	private static CivChat2 instance;

	private CivChat2Log log;
	private CivChat2Config config;
	private CivChat2Manager chatMan;
	private CivChat2Listener chatListener;
	private CivChat2SettingsManager civChat2SettingsManager;
	private CivChat2FileLogger fileLog;
	private CivChatDAO databaseManager;

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		saveDefaultConfig();
		reloadConfig();
		config = new CivChat2Config(getConfig());
		log = new CivChat2Log();
		log.initializeLogger(instance);
		fileLog = new CivChat2FileLogger();
		databaseManager = new CivChatDAO();
		civChat2SettingsManager = new CivChat2SettingsManager();
		chatMan = new CivChat2Manager(instance);
		log.debug("Debug Enabled");
		chatListener = new CivChat2Listener(chatMan);
		registerNameLayerPermissions();
		registerCivChatEvents();
	}

	@Override
	public void onDisable() {
	}

	public CivChat2Manager getCivChat2Manager() {
		return chatMan;
	}

	public boolean debugEnabled() {
		return config.getDebug();
	}

	public CivChat2Log getCivChat2Log() {
		return log;
	}

	private void registerCivChatEvents() {
		getServer().getPluginManager().registerEvents(chatListener, this);
	}

	public void registerNameLayerPermissions() {
		List<PlayerType> memberAndAbove = Arrays.asList(PlayerType.MEMBERS, PlayerType.MODS, PlayerType.ADMINS , PlayerType.OWNER);
		PermissionType.registerPermission("READ_CHAT", new ArrayList<>(memberAndAbove), 
				"Allows receiving messages sent in the group chat");
		PermissionType.registerPermission("WRITE_CHAT", new ArrayList<>(memberAndAbove), 
				"Allows sending messages to the group chat");
	}

	public static CivChat2 getInstance() {
		return instance;
	}

	public CivChat2SettingsManager getCivChat2SettingsManager(){
		return civChat2SettingsManager;
	}

	public CivChat2Config getPluginConfig() {
		return config;
	}

	public CivChat2FileLogger getCivChat2FileLogger() {
		return fileLog;
	}

	public CivChatDAO getDatabaseManager() {
		return this.databaseManager;
	}
}
