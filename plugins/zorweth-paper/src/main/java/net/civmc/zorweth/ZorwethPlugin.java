package net.civmc.zorweth;

import com.google.common.base.Strings;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import net.civmc.zorweth.database.RocketTransferDao;
import net.civmc.zorweth.database.ZorwethDatabase;
import net.civmc.zorweth.flight.FlightComputerGui;
import net.civmc.zorweth.mechanics.Fuel;
import net.civmc.zorweth.mechanics.OilMechanics;
import net.civmc.zorweth.oxygen.ActivityManager;
import net.civmc.zorweth.oxygen.OxygenCommand;
import net.civmc.zorweth.oxygen.OxygenDisplay;
import net.civmc.zorweth.oxygen.OxygenManager;
import net.civmc.zorweth.research.ResearchManager;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;

public final class ZorwethPlugin extends JavaPlugin {

    private Clipboard rocketClipboard;
    private HikariDataSource dataSource;
    private StasisHandler stasisHandler;
    private RocketTransferDao rocketTransferDao;
    private CrossServerOttManager crossServerOttManager;
    private ResearchManager researchManager;
    private String serverName;
    private String destinationServer;
    private String sourceWorld;
    private String destinationWorld;
    private String transferFailureMessage;
    private long pioneerEndTimestampMillis;
    private int worldRadius;
    private double deltaVMetersPerSecond;
    private boolean researchEnabled;
    private int researchPhaseOneRuns;
    private int researchPhaseTwoRuns;
    private String mechanicsWorld;
    private boolean mechanicsEnabled;

    private OilMechanics mechanics;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfiguration();
        if (!initDatabase()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Fuel.registerCustomItems();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.rocketClipboard = loadRocketClipboard();
        this.stasisHandler = new StasisHandler();
        this.crossServerOttManager = new CrossServerOttManager(this);
        this.researchManager = new ResearchManager(this, this.researchEnabled, this.researchPhaseOneRuns,
            this.researchPhaseTwoRuns);
        getServer().getPluginManager().registerEvents(this.stasisHandler, this);
        getServer().getPluginManager().registerEvents(new FlightComputerGui(this), this);
        getServer().getPluginManager().registerEvents(new DestinationTransferListener(this), this);
        getServer().getPluginManager().registerEvents(new CrossServerOttArrivalListener(this, this.crossServerOttManager), this);
        Objects.requireNonNull(getCommand("pioneer")).setExecutor(new PioneerCommand(this));

        if (mechanicsEnabled) {
            this.mechanics = new OilMechanics(this, mechanicsWorld);
        }

        loadOxygen();

        double gravity = getConfig().getDouble("gravity");
        if (gravity != 0) {
            getServer().getPluginManager().registerEvents(new GravityListener(gravity), this);
        }
    }

    private void loadOxygen() {
        ConfigurationSection oxygenSection = getConfig().getConfigurationSection("oxygen");
        if (oxygenSection == null || !oxygenSection.getBoolean("enabled")) {
            return;
        }

        ActivityManager activityManager = new ActivityManager();
        getServer().getPluginManager().registerEvents(activityManager, this);

        OxygenManager oxygenManager = OxygenManager.deserialize(this, activityManager, oxygenSection);
        getServer().getPluginManager().registerEvents(oxygenManager, this);
        Objects.requireNonNull(getCommand("oxygen")).setExecutor(new OxygenCommand(oxygenManager));

        getServer().getPluginManager().registerEvents(new OxygenDisplay(this, oxygenManager), this);
    }

    public int recordOilExtraction(final Location location) {
        if (mechanics == null) {
            return 0;
        }
        return mechanics.recordOilExtraction(location);
    }

    @Override
    public void onDisable() {
        if (this.dataSource != null) {
            this.dataSource.close();
            this.dataSource = null;
        }
    }

    public Clipboard getRocketClipboard() {
        return this.rocketClipboard;
    }

    public RocketTransferDao getRocketTransferDao() {
        return this.rocketTransferDao;
    }

    public ResearchManager getResearchManager() {
        return this.researchManager;
    }

    public String getServerName() {
        return this.serverName;
    }

    public String getDestinationServer() {
        return this.destinationServer;
    }

    public String getSourceWorld() {
        return this.sourceWorld;
    }

    public String getDestinationWorld() {
        return this.destinationWorld;
    }

    public String getTransferFailureMessage() {
        return this.transferFailureMessage;
    }

    public long getPioneerEndTimestampMillis() {
        return this.pioneerEndTimestampMillis;
    }

    public int getWorldRadius() {
        return this.worldRadius;
    }

    public double getDeltaVMetersPerSecond() {
        return this.deltaVMetersPerSecond;
    }

    private void loadConfiguration() {
        this.serverName = getConfig().getString("server-name", "zorweth");
        this.destinationServer = getConfig().getString("destination-server", this.serverName);
        this.sourceWorld = getConfig().getString("source-world", "world");
        this.destinationWorld = getConfig().getString("destination-world", "world");
        this.transferFailureMessage = getConfig().getString("transfer-failure-message",
            "Unable to complete rocket transfer. Please reconnect and try again.");
        this.pioneerEndTimestampMillis = getConfig().getLong("pioneer-end-timestamp", 0L);
        this.worldRadius = getConfig().getInt("world-radius", 0);
        this.deltaVMetersPerSecond = getConfig().getDouble("delta-v-meters-per-second", 10_000.0);
        this.researchEnabled = getConfig().getBoolean("research.enabled", true);
        this.researchPhaseOneRuns = loadPositiveInt("research.phase-one-runs", 1);
        this.researchPhaseTwoRuns = loadPositiveInt("research.phase-two-runs", 1);
        this.mechanicsEnabled = getConfig().getBoolean("mechanics.enabled", false);
        this.mechanicsWorld = getConfig().getString("mechanics.world");
    }

    private int loadPositiveInt(final String path, final int defaultValue) {
        final int value = getConfig().getInt(path, defaultValue);
        if (value < 1) {
            getLogger().warning(path + " must be at least 1, using " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    private boolean initDatabase() {
        final DatabaseCredentials credentials = (DatabaseCredentials) getConfig().get("database");
        if (credentials == null) {
            getLogger().severe("Database credentials are missing from config.yml");
            return false;
        }

        this.dataSource = createDataSource(credentials);

        try {
            ZorwethDatabase.migrate(this.dataSource);
            this.rocketTransferDao = new RocketTransferDao(this.dataSource);
            return true;
        } catch (final SQLException exception) {
            getLogger().log(Level.SEVERE, "Unable to migrate the Zorweth database", exception);
            return false;
        }
    }

    private HikariDataSource createDataSource(final DatabaseCredentials credentials) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:" + credentials.driver() + "://" + credentials.host() + ":" +
            credentials.port() + "/" + credentials.database());
        config.setConnectionTimeout(credentials.connectionTimeout());
        config.setIdleTimeout(credentials.idleTimeout());
        config.setMaxLifetime(credentials.maxLifetime());
        config.setMaximumPoolSize(credentials.poolSize());
        config.setUsername(credentials.username());
        if (!Strings.isNullOrEmpty(credentials.password())) {
            config.setPassword(credentials.password());
        }
        return new HikariDataSource(config);
    }

    private Clipboard loadRocketClipboard() {
        final File file = new File(getDataFolder(), "rocket.schem");
        final ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            throw new IllegalStateException("Could not find clipboard format for " + file.getPath());
        }

        try (FileInputStream stream = new FileInputStream(file);
             ClipboardReader reader = format.getReader(stream)) {
            return reader.read();
        } catch (final IOException exception) {
            throw new IllegalStateException("Failed to load " + file.getPath(), exception);
        }
    }

    public StasisHandler getStasisHandler() {
        return stasisHandler;
    }

    public CrossServerOttManager getCrossServerOttManager() {
        return this.crossServerOttManager;
    }
}
