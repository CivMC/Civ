package vg.civcraft.mc.civmodcore;

import java.sql.SQLException;

import org.bukkit.configuration.serialization.ConfigurationSerialization;

import vg.civcraft.mc.civmodcore.api.EnchantmentNames;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.chatDialog.ChatListener;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.global.CMCWorldDAO;
import vg.civcraft.mc.civmodcore.locations.global.WorldIDManager;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigCommand;
import vg.civcraft.mc.civmodcore.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.scoreboard.side.ScoreBoardListener;
import vg.civcraft.mc.civmodcore.serialization.NBTSerialization;

public final class CivModCorePlugin extends ACivMod {

	private static CivModCorePlugin instance;
	private GlobalChunkMetaManager chunkMetaManager;
	private ManagedDatasource database;
	private WorldIDManager worldIdManager;

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		// Save default resources
		saveDefaultResource("enchantments.csv");
		// Register listeners
		registerListener(new ClickableInventoryListener());
		registerListener(new ChatListener());
		registerListener(new ScoreBoardListener());
		// Register commands, which must be done traditionally
		// We can't use command annotations here as the annotation processor isn't available yet
		this.newCommandHandler.registerCommand(new ConfigCommand());
		ConfigurationSerialization.registerClass(ManagedDatasource.class);
		// Load Database
		try {
			database = (ManagedDatasource) getConfig().get("database");
		}
		catch (Exception error) {
			warning("Cannot get database from config.", error);
			database = null;
		}
		// Load APIs
		ItemNames.loadItemNames();
		EnchantmentNames.loadEnchantmentNames();
		BottomLineAPI.init();
		if (database != null) {
			CMCWorldDAO dao = new CMCWorldDAO(database, this);
			if (dao.updateDatabase()) {
				worldIdManager = new WorldIDManager(dao);
				chunkMetaManager = new GlobalChunkMetaManager(dao, worldIdManager);
				info("Setup database successfully");
			}
			else {
				warning("Could not setup database");
			}
		}
		else {
			warning("Could not setup database, none specified in config");
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		// Unload APIs
		ItemNames.resetItemNames();
		EnchantmentNames.resetEnchantmentNames();
		NBTSerialization.clearAllRegistrations();
		ChunkMetaAPI.saveAll();
		chunkMetaManager = null;
		// Disconnect database
		if (database != null) {
			try {
				database.close();
			}
			catch (SQLException error) {
				warning("Was unable to close the database.", error);
			}
			database = null;
		}
		PlayerSettingAPI.saveAll();
		ConfigurationSerialization.unregisterClass(ManagedDatasource.class);
		instance = null;
	}

	public static CivModCorePlugin getInstance() {
		return instance;
	}
	
	public GlobalChunkMetaManager getChunkMetaManager() {
		return chunkMetaManager;
	}
	
	public WorldIDManager getWorldIdManager() {
		return worldIdManager;
	}

}
