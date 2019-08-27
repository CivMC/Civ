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
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaView;
import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigCommand;
import vg.civcraft.mc.civmodcore.scoreboard.ScoreBoardListener;

public final class CivModCorePlugin extends ACivMod {

	private static CivModCorePlugin instance;
	private GlobalChunkMetaManager chunkMetaManager;

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		// Save default resources
		saveResource("enchantments.csv", false);
		// Register listeners
		registerListener(new ClickableInventoryListener());
		registerListener(new ChatListener());
		registerListener(new ScoreBoardListener());
		// Register commands, which must be done traditionally
		// We can't use command annotations here as the annotation processor isn't available yet
		this.newCommandHandler.registerCommand(new ConfigCommand());
		// Load APIs
		ItemAPI.loadItemNames();
		new NiceNames().loadNames();
		new DialogManager();
		ConfigurationSerialization.registerClass(ManagedDatasource.class);
		ManagedDatasource db;
		try {
			db = (ManagedDatasource) getConfig().get("database");
		}
		catch (Exception e) {
			db = null;
		}
		ChunkDAO dao = new ChunkDAO(db, this);
		if (dao.updateDatabase()) {
			chunkMetaManager = new GlobalChunkMetaManager(dao);
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		ChunkMetaAPI.saveAll();
		// De-register listeners
		HandlerList.unregisterAll(this);
		// Unload APIs
		ItemAPI.resetItemNames();
		ConfigurationSerialization.unregisterClass(ManagedDatasource.class);
		instance = null;
	}

	public static CivModCorePlugin getInstance() {
		return instance;
	}
	
	public GlobalChunkMetaManager getChunkMetaManager() {
		return chunkMetaManager;
	}

}
