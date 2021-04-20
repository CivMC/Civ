package vg.civcraft.mc.civmodcore;

import java.sql.SQLException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.HumanEntity;
import vg.civcraft.mc.civmodcore.chat.dialog.DialogManager;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.events.CustomEventMapper;
import vg.civcraft.mc.civmodcore.inventory.gui.self.SelfGUIManager;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MoreTags;
import vg.civcraft.mc.civmodcore.inventory.items.PotionUtils;
import vg.civcraft.mc.civmodcore.inventory.items.SpawnEggUtils;
import vg.civcraft.mc.civmodcore.inventory.items.TreeTypeUtils;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.inventorygui.paged.PagedGUIManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.locations.global.CMCWorldDAO;
import vg.civcraft.mc.civmodcore.locations.global.WorldIDManager;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigCommand;
import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigGetAnyCommand;
import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigSetAnyCommand;
import vg.civcraft.mc.civmodcore.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.scoreboard.side.ScoreBoardListener;
import vg.civcraft.mc.civmodcore.serialization.NBTSerialization;
import vg.civcraft.mc.civmodcore.world.WorldTracker;
import vg.civcraft.mc.civmodcore.world.operations.ChunkOperationManager;

public final class CivModCorePlugin extends ACivMod {

	private static CivModCorePlugin instance;

	private GlobalChunkMetaManager chunkMetaManager;
	private ManagedDatasource database;
	private WorldIDManager worldIdManager;
	private final AikarCommandManager commands;
	private final SelfGUIManager selfGUIManager;

	public CivModCorePlugin() {
		this.commands = new AikarCommandManager(this, false);
		this.selfGUIManager = new SelfGUIManager(this);
	}

	@Override
	public void onEnable() {
		instance = this;
		this.useNewCommandHandler = true;
		ConfigurationSerialization.registerClass(ManagedDatasource.class);
		// Save default resources
		saveDefaultResource("enchants.yml");
		saveDefaultResource("materials.yml");
		saveDefaultResource("potions.csv");
		saveDefaultConfig();
		super.onEnable();
		// Load Database
		try {
			this.database = (ManagedDatasource) getConfig().get("database");
			if (this.database != null) {
				CMCWorldDAO dao = new CMCWorldDAO(this.database, this);
				if (dao.updateDatabase()) {
					this.worldIdManager = new WorldIDManager(dao);
					this.chunkMetaManager = new GlobalChunkMetaManager(dao, this.worldIdManager);
					info("Setup database successfully");
				}
				else {
					warning("Could not setup database");
				}
			}
		}
		catch (Exception error) {
			warning("Cannot get database from config.", error);
			this.database = null;
		}
		String scoreboardHeader = ChatColor.translateAlternateColorCodes('&', getConfig().getString("scoreboardHeader","  Info  "));
		ScoreBoardAPI.setDefaultHeader(scoreboardHeader);
		// Register listeners
		registerListener(new ClickableInventoryListener());
		registerListener(new PagedGUIManager());
		registerListener(DialogManager.INSTANCE);
		registerListener(new ScoreBoardListener());
		registerListener(new CustomEventMapper());
		registerListener(new WorldTracker());
		registerListener(ChunkOperationManager.INSTANCE);
		// Register commands
		this.commands.init();
		this.commands.registerCommand(new ConfigCommand());
		this.commands.registerCommand(ChunkOperationManager.INSTANCE);
		// Load APIs
		EnchantUtils.loadEnchantAbbreviations(this);
		ItemUtils.loadItemNames(this);
		MoreTags.init();
		PotionUtils.init();
		SpawnEggUtils.init();
		TreeTypeUtils.init();
		BottomLineAPI.init();
		this.newCommandHandler.registerCommand(new ConfigSetAnyCommand());
		this.newCommandHandler.registerCommand(new ConfigGetAnyCommand());
		this.selfGUIManager.init();
	}

	@Override
	public void onDisable() {
		this.selfGUIManager.reset();
		Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
		ChunkMetaAPI.saveAll();
		this.chunkMetaManager = null;
		// Disconnect database
		if (this.database != null) {
			try {
				this.database.close();
			}
			catch (SQLException error) {
				warning("Was unable to close the database.", error);
			}
			this.database = null;
		}
		DialogManager.resetDialogs();
		WorldTracker.reset();
		PlayerSettingAPI.saveAll();
		ConfigurationSerialization.unregisterClass(ManagedDatasource.class);
		NBTSerialization.clearAllRegistrations();
		this.commands.reset();
		super.onDisable();
	}

	public static CivModCorePlugin getInstance() {
		return instance;
	}
	
	public GlobalChunkMetaManager getChunkMetaManager() {
		return this.chunkMetaManager;
	}
	
	public WorldIDManager getWorldIdManager() {
		return this.worldIdManager;
	}
	
	public ManagedDatasource getDatabase() {
		return this.database;
	}

}
