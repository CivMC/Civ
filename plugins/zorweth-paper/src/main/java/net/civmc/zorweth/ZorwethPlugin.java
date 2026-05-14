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
import java.util.logging.Level;
import net.civmc.zorweth.database.RocketTransferDao;
import net.civmc.zorweth.database.ZorwethDatabase;
import net.civmc.zorweth.flight.FlightComputerGui;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;

public final class ZorwethPlugin extends JavaPlugin {

    private Clipboard rocketClipboard;
    private HikariDataSource dataSource;
    private StasisHandler stasisHandler;
    private RocketTransferDao rocketTransferDao;
    private String serverName;
    private String destinationServer;
    private String destinationWorld;
    private String transferFailureMessage;
    private int worldRadius;
    private double deltaVMetersPerSecond;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfiguration();
        if (!initDatabase()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.rocketClipboard = loadRocketClipboard();
        this.stasisHandler = new StasisHandler();
        getServer().getPluginManager().registerEvents(this.stasisHandler, this);
        getServer().getPluginManager().registerEvents(new FlightComputerGui(this), this);
        getServer().getPluginManager().registerEvents(new DestinationTransferListener(this), this);
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

    public String getServerName() {
        return this.serverName;
    }

    public String getDestinationServer() {
        return this.destinationServer;
    }

    public String getDestinationWorld() {
        return this.destinationWorld;
    }

    public String getTransferFailureMessage() {
        return this.transferFailureMessage;
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
        this.destinationWorld = getConfig().getString("destination-world", "world");
        this.transferFailureMessage = getConfig().getString("transfer-failure-message",
            "Unable to complete rocket transfer. Please reconnect and try again.");
        this.worldRadius = getConfig().getInt("world-radius", 0);
        this.deltaVMetersPerSecond = getConfig().getDouble("delta-v-meters-per-second", 10_000.0);
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
}
