package vg.civcraft.mc.civmodcore;

import java.io.File;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import vg.civcraft.mc.civmodcore.chat.dialog.DialogManager;
import vg.civcraft.mc.civmodcore.commands.ChunkMetaCommand;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.commands.StatCommand;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;
import vg.civcraft.mc.civmodcore.inventory.items.SpawnEggUtils;
import vg.civcraft.mc.civmodcore.players.PlayerNames;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardListener;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.commands.ConfigCommand;
import vg.civcraft.mc.civmodcore.utilities.SkinCache;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.GlobalChunkMetaManager;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat.LoadStatisticManager;
import vg.civcraft.mc.civmodcore.world.locations.global.CMCWorldDAO;
import vg.civcraft.mc.civmodcore.world.locations.global.WorldIDManager;

public class CivModCorePlugin extends ACivMod {

    /**
     * Primary constructor used by the real server
     */
    public CivModCorePlugin() {
        super();
    }

    /**
     * Secondary constructor used for testing
     */
    protected CivModCorePlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    private static CivModCorePlugin instance;

    private CivModCoreConfig config;
    private GlobalChunkMetaManager chunkMetaManager;
    private ManagedDatasource database;
    private WorldIDManager worldIdManager;
    private CommandManager commands;
    private SkinCache skinCache;

    @Override
    public void onEnable() {
        instance = this;
        registerConfigClass(DatabaseCredentials.class);
        // Save default resources
        saveDefaultResource("enchants.yml");
        super.onEnable();
        // Load Config
        this.config = new CivModCoreConfig(this);
        this.config.parse();
        // Load Database
        try {
            this.database = ManagedDatasource.construct(this, this.config.getDatabaseCredentials());
            if (this.database != null) {
                final var dao = new CMCWorldDAO(this.database, this);
                if (dao.updateDatabase()) {
                    this.worldIdManager = new WorldIDManager(dao);
                    this.chunkMetaManager = new GlobalChunkMetaManager(dao, this.worldIdManager, this.config.getChunkLoadingThreads());
                    info("Setup database successfully");
                } else {
                    warning("Could not setup database");
                }
            }
        } catch (final Throwable error) {
            warning("Cannot get database from config.", error);
            this.database = null;
        }
        ScoreBoardAPI.setDefaultHeader(this.config.getScoreboardHeader());
        // Register listeners
        registerListener(new ClickableInventoryListener());
        registerListener(DialogManager.INSTANCE);
        registerListener(new ScoreBoardListener());
        registerListener(new PlayerNames());
        // Register commands
        this.commands = new CommandManager(this);
        this.commands.init();
        this.commands.registerCommand(new ConfigCommand());
        this.commands.registerCommand(new StatCommand());
        this.commands.registerCommand(new ChunkMetaCommand());
        // Load APIs
        EnchantUtils.loadEnchantAbbreviations();
        SpawnEggUtils.init();
        BottomLineAPI.init();
        this.skinCache = new SkinCache(this, this.config.getSkinCacheThreads());

        if (this.config.getChunkLoadingStatistics())
            LoadStatisticManager.enable();
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(HumanEntity::closeInventory);
        ChunkMetaAPI.saveAll();
        this.chunkMetaManager.disableWorlds();
        this.chunkMetaManager = null;
        // Disconnect database
        if (this.database != null) {
            this.database.close();
            this.database = null;
        }
        DialogManager.resetDialogs();
        PlayerSettingAPI.saveAll();
        ConfigurationSerialization.unregisterClass(DatabaseCredentials.class);
        if (this.commands != null) {
            this.commands.reset();
            this.commands = null;
        }
        if (this.skinCache != null) {
            this.skinCache.shutdown();
            this.skinCache = null;
        }

        LoadStatisticManager.disable();

        if (this.config != null) {
            this.config.reset();
            this.config = null;
        }
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
