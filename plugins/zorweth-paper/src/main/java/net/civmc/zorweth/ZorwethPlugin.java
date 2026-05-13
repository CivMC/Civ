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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import javax.sql.DataSource;
import net.civmc.zorweth.database.RocketTransferDao;
import net.civmc.zorweth.database.ZorwethDatabase;
import net.civmc.zorweth.flight.FlightComputer;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;

public final class ZorwethPlugin extends JavaPlugin {

    private static ZorwethPlugin instance;

    private Clipboard rocketClipboard;
    private HikariDataSource dataSource;
    private StasisHandler invincibilityHandler;
    private RocketTransferDao rocketTransferDao;
    private String serverName;
    private String destinationServer;
    private String destinationWorld;
    private String transferFailureMessage;

    public static ZorwethPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadConfiguration();
        if (!initDatabase()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.rocketClipboard = loadRocketClipboard();
        this.invincibilityHandler = new StasisHandler();
        getServer().getPluginManager().registerEvents(this.invincibilityHandler, this);
        getServer().getPluginManager().registerEvents(new FlightComputer(this), this);
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

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public RocketTransferDao getRocketTransferDao() {
        return this.rocketTransferDao;
    }

    public StasisHandler getInvincibilityHandler() {
        return this.invincibilityHandler;
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

    private void loadConfiguration() {
        this.serverName = getConfig().getString("server-name", "zorweth");
        this.destinationServer = getConfig().getString("destination-server", this.serverName);
        this.destinationWorld = getConfig().getString("destination-world", "world");
        this.transferFailureMessage = getConfig().getString("transfer-failure-message",
            "Unable to complete rocket transfer. Please reconnect and try again.");
    }

    private boolean initDatabase() {
        final DatabaseCredentials credentials = (DatabaseCredentials) getConfig().get("database");
        if (credentials == null) {
            getLogger().severe("Database credentials are missing from config.yml");
            return false;
        }

        this.dataSource = createDataSource(credentials);
        try (Connection connection = this.dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1");
        } catch (final SQLException exception) {
            getLogger().log(Level.SEVERE, "Unable to connect to the Zorweth database", exception);
            return false;
        }

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
}
