package vg.civcraft.mc.civmodcore;

import java.sql.SQLException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.HumanEntity;
import vg.civcraft.mc.civmodcore.chat.dialog.DialogManager;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.events.CustomEventMapper;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MoreTags;
import vg.civcraft.mc.civmodcore.inventory.items.PotionUtils;
import vg.civcraft.mc.civmodcore.inventory.items.SpawnEggUtils;
import vg.civcraft.mc.civmodcore.inventory.items.TreeTypeUtils;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.world.locations.global.CMCWorldDAO;
import vg.civcraft.mc.civmodcore.world.locations.global.WorldIDManager;
import vg.civcraft.mc.civmodcore.maps.MapColours;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.commands.ConfigCommand;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardListener;
import vg.civcraft.mc.civmodcore.utilities.SkinCache;
import vg.civcraft.mc.civmodcore.world.WorldTracker;
import vg.civcraft.mc.civmodcore.world.operations.ChunkOperationManager;

public final class CivModCorePlugin extends ACivMod {

	private static CivModCorePlugin instance;

	private GlobalChunkMetaManager chunkMetaManager;
	private ManagedDatasource database;
	private WorldIDManager worldIdManager;
	private CommandManager commands;
	private SkinCache skinCache;

	@Override
	public void onEnable() {
		instance = this;
		ConfigurationSerialization.registerClass(DatabaseCredentials.class);
		// Save default resources
		saveDefaultResource("enchants.yml");
		saveDefaultResource("materials.yml");
		saveDefaultResource("potions.csv");
		saveDefaultConfig();
		super.onEnable();
		// Load Database
		try {
			this.database = ManagedDatasource.construct(this, (DatabaseCredentials) getConfig().get("database"));
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
		catch (final Exception error) {
			warning("Cannot get database from config.", error);
			this.database = null;
		}
		String scoreboardHeader = ChatColor.translateAlternateColorCodes('&', getConfig().getString("scoreboardHeader","  Info  "));
		ScoreBoardAPI.setDefaultHeader(scoreboardHeader);
		// Register listeners
		registerListener(new ClickableInventoryListener());
		registerListener(DialogManager.INSTANCE);
		registerListener(new ScoreBoardListener());
		registerListener(new CustomEventMapper());
		registerListener(new WorldTracker());
		registerListener(ChunkOperationManager.INSTANCE);
		// Register commands
		this.commands = new CommandManager(this);
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
		MapColours.init();
		this.skinCache = new SkinCache(this, getConfig().getInt("skin-download-threads", Runtime.getRuntime().availableProcessors() / 2));
	}

	@Override
	public void onDisable() {
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
		if (this.commands != null) {
			this.commands.reset();
			this.commands = null;
		}
		this.skinCache.shutdown();
		ConfigurationSerialization.unregisterClass(DatabaseCredentials.class);
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

	public SkinCache getSkinCache() {
		return this.skinCache;
	}

}
