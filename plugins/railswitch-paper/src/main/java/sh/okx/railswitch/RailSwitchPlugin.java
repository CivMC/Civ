package sh.okx.railswitch;

import org.bukkit.event.Listener;
import sh.okx.railswitch.commands.DestinationCommand;
import sh.okx.railswitch.config.SwitchPluginConfiguration;
import sh.okx.railswitch.glue.CitadelGlue;
import sh.okx.railswitch.settings.SettingsManager;
import sh.okx.railswitch.storage.RailSwitchStorage;
import sh.okx.railswitch.switches.SwitchConfiguratorListener;
import sh.okx.railswitch.switches.SwitchConfigurationSessionManager;
import sh.okx.railswitch.switches.SwitchDisplayManager;
import sh.okx.railswitch.switches.SwitchListener;
import sh.okx.railswitch.switches.SwitchMaintenanceListener;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

/**
 * The Rail Switch plugin class
 */
public final class RailSwitchPlugin extends ACivMod implements Listener {

    private CommandManager commandManager;
    private RailSwitchStorage railSwitchStorage;
    private SwitchPluginConfiguration switchConfiguration;
    private SwitchDisplayManager switchDisplayManager;
    private SwitchConfigurationSessionManager configurationSessionManager;
    private ManagedDatasource database;
    private CitadelGlue citadelGlue;

    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        switchConfiguration = new SwitchPluginConfiguration(this);
        switchConfiguration.reload();
        if (!initialiseStorage()) {
            disable();
            return;
        }
        SettingsManager.init(this);
        configurationSessionManager = new SwitchConfigurationSessionManager(this);
        citadelGlue = new CitadelGlue(this);
        registerCoreListeners();
        startDisplayManager();
        initialiseCommands();
    }

    @Override
    public void onDisable() {
        shutdownDisplayManager();
        shutdownConfigurationSession();
        resetCommandManager();
        SettingsManager.reset();
        cleanupDatabase();
        switchConfiguration = null;
        citadelGlue = null;
        super.onDisable();
    }

    public RailSwitchStorage getRailSwitchStorage() {
        return railSwitchStorage;
    }

    public SwitchPluginConfiguration getSwitchConfiguration() {
        return switchConfiguration;
    }

    public SwitchConfigurationSessionManager getConfigurationSessionManager() {
        return configurationSessionManager;
    }

    public CitadelGlue getCitadelGlue() {
        return citadelGlue;
    }

    private DatabaseCredentials resolveCredentials() {
        DatabaseCredentials credentials = loadCredentialsFromConfig();
        if (credentials != null) {
            return credentials;
        }
        return loadCredentialsFromEnvironment();
    }

    private boolean initialiseStorage() {
        DatabaseCredentials credentials = resolveCredentials();
        database = ManagedDatasource.construct(this, credentials);
        if (database == null) {
            getLogger().severe("Failed to create rail switch datasource; disabling plugin.");
            return false;
        }
        railSwitchStorage = new RailSwitchStorage(this, database);
        if (!database.updateDatabase()) {
            getLogger().severe("Failed to run rail switch database migrations; disabling plugin.");
            cleanupDatabase();
            return false;
        }
        railSwitchStorage.load();
        return true;
    }

    private void registerCoreListeners() {
        registerListener(citadelGlue);
        registerListener(new SwitchListener(this, citadelGlue));
        registerListener(configurationSessionManager);
        registerListener(new SwitchConfiguratorListener(this, configurationSessionManager));
        registerListener(new SwitchMaintenanceListener(this));
    }

    private void startDisplayManager() {
        switchDisplayManager = new SwitchDisplayManager(this);
        registerListener(switchDisplayManager);
        switchDisplayManager.start();
    }

    private void shutdownDisplayManager() {
        if (switchDisplayManager != null) {
            switchDisplayManager.shutdown();
            switchDisplayManager = null;
        }
    }

    private void initialiseCommands() {
        commandManager = new CommandManager(this);
        commandManager.init();
        commandManager.registerCommand(new DestinationCommand());
    }

    private void resetCommandManager() {
        if (commandManager != null) {
            commandManager.reset();
            commandManager = null;
        }
    }

    private void shutdownConfigurationSession() {
        if (configurationSessionManager != null) {
            configurationSessionManager.shutdown();
            configurationSessionManager = null;
        }
    }

    private void cleanupDatabase() {
        if (database != null) {
            database.close();
            database = null;
        }
        railSwitchStorage = null;
    }

    /**
     * Loads database credentials from the plugin configuration.
     * Applies localhost environment override if applicable.
     *
     * @return Database credentials from config, or null if not configured
     */
    private DatabaseCredentials loadCredentialsFromConfig() {
        DatabaseCredentials credentials = (DatabaseCredentials) getConfig().get("database");
        if (credentials != null) {
            return applyLocalhostOverride(credentials);
        }
        return null;
    }

    /**
     * Loads database credentials from environment variables.
     *
     * @return Database credentials from environment, or null if incomplete
     */
    private DatabaseCredentials loadCredentialsFromEnvironment() {
        String username = System.getenv("CIV_MYSQL_USERNAME");
        String password = System.getenv("CIV_MYSQL_PASSWORD");
        String host = System.getenv("CIV_MYSQL_HOST");
        if (username == null || password == null || host == null) {
            return null;
        }
        int port = parseInteger(System.getenv("CIV_MYSQL_PORT"), 3306);
        String databaseName = System.getenv("CIV_DATABASE_PREFIX");
        if (databaseName == null) {
            databaseName = "";
        }
        databaseName += "railswitch";
        int poolSize = parseInteger(System.getenv("CIV_MYSQL_POOLSIZE"), 5);
        long connectionTimeout = parseLong(System.getenv("CIV_MYSQL_CONNECTION_TIMEOUT"), 10_000L);
        long idleTimeout = parseLong(System.getenv("CIV_MYSQL_IDLE_TIMEOUT"), 600_000L);
        long maxLifetime = parseLong(System.getenv("CIV_MYSQL_MAX_LIFETIME"), 7_200_000L);
        getLogger().info("Loaded rail switch database credentials from environment variables.");
        return new DatabaseCredentials(username, password, host, port, "mysql", databaseName, poolSize,
            connectionTimeout, idleTimeout, maxLifetime);
    }

    /**
     * Applies localhost environment override to credentials if the host is localhost.
     *
     * @param credentials The original credentials
     * @return Modified credentials with override, or original if no override
     */
    private DatabaseCredentials applyLocalhostOverride(DatabaseCredentials credentials) {
        String envHost = System.getenv("CIV_MYSQL_HOST");
        if (envHost != null && isLocalhost(credentials.host())) {
            getLogger().info("Replacing localhost database host with CIV_MYSQL_HOST environment override.");
            int port = parseInteger(System.getenv("CIV_MYSQL_PORT"), credentials.port());
            return new DatabaseCredentials(
                credentials.username(),
                credentials.password(),
                envHost,
                port,
                credentials.driver(),
                credentials.database(),
                credentials.poolSize(),
                credentials.connectionTimeout(),
                credentials.idleTimeout(),
                credentials.maxLifetime());
        }
        return credentials;
    }

    private int parseInteger(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            getLogger().warning("Invalid integer value '" + value + "'; using fallback " + fallback + ".");
            return fallback;
        }
    }

    private long parseLong(String value, long fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            getLogger().warning("Invalid long value '" + value + "'; using fallback " + fallback + ".");
            return fallback;
        }
    }

    private boolean isLocalhost(String host) {
        if (host == null) {
            return false;
        }
        String trimmed = host.trim();
        return trimmed.equalsIgnoreCase("localhost")
            || trimmed.equals("127.0.0.1")
            || trimmed.equals("::1");
    }

}
