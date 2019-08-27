package vg.civcraft.mc.civmodcore;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.chatDialog.ChatListener;
import vg.civcraft.mc.civmodcore.chatDialog.DialogManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.itemHandling.NiceNames;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkDAO;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigCommand;
import vg.civcraft.mc.civmodcore.scoreboard.ScoreBoardListener;

import java.sql.SQLException;

public final class CivModCorePlugin extends ACivMod {

	private static CivModCorePlugin instance = null;
	private static GlobalChunkMetaManager chunkMetaManager = null;
	private static ManagedDatasource database = null;

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
		// Load Database
		try {
			database = (ManagedDatasource) getConfig().get("database");
		}
		catch (Exception error) {
			warning("Cannot get database from config.", error);
			database = null;
		}
		// Load APIs
		ItemAPI.loadItemNames();
		new NiceNames().loadNames();
		new DialogManager();
		ConfigurationSerialization.registerClass(ManagedDatasource.class);
		if (database != null) {
			ChunkDAO dao = new ChunkDAO(database, this);
			if (dao.updateDatabase()) {
				chunkMetaManager = new GlobalChunkMetaManager(dao);
			}
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		// De-register listeners
		HandlerList.unregisterAll(this);
		// Unload APIs
		ItemAPI.resetItemNames();
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
		ConfigurationSerialization.unregisterClass(ManagedDatasource.class);
		instance = null;
	}

	public static CivModCorePlugin getInstance() {
		return instance;
	}
	
	public static GlobalChunkMetaManager getChunkMetaManager() {
		return chunkMetaManager;
	}

}
