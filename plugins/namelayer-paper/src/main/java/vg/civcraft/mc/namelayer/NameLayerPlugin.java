package vg.civcraft.mc.namelayer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.cache.NameLayerGroupCache;
import vg.civcraft.mc.namelayer.command.CommandHandler;
import vg.civcraft.mc.namelayer.database.NameLayerReadDao;
import vg.civcraft.mc.namelayer.group.AutoAcceptHandler;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.DefaultGroupHandler;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.misc.ClassHandler;
import vg.civcraft.mc.namelayer.permission.PermissionType;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerInvalidationConsumer;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerRabbitMqConfig;
import vg.civcraft.mc.namelayer.rabbitmq.NameLayerWriteClient;

public class NameLayerPlugin extends ACivMod {

    private static BlackList blackList;
    private static NameLayerReadDao nameLayerReadDao;
    private static DefaultGroupHandler defaultGroupHandler;
    private static NameLayerPlugin instance;
    private static AutoAcceptHandler autoAcceptHandler;
    private static volatile NameLayerGroupCache groupCache;
    private CommandHandler handle;
    private static ManagedDatasource db;
    private static boolean loadGroups = true;
    private static int groupLimit = 10;
    private static boolean createGroupOnFirstJoin;
    private FileConfiguration config;
    private NameLayerInvalidationConsumer invalidationConsumer;
    private NameLayerWriteClient writeClient;
    private BukkitTask freshnessCheckTask;
    private long stateLocalVersion;
    private long staleVersionDetectedAtMillis;
    private final AtomicLong fullResyncCount = new AtomicLong();
    private final AtomicLong targetedReloadCount = new AtomicLong();
    private final AtomicLong targetedReloadFailureCount = new AtomicLong();
    private final AtomicLong rabbitMqReconnectCount = new AtomicLong();

    @Override
    public void onEnable() {
        super.onEnable(); // Need to call this to properly initialize this mod
        saveDefaultConfig();
        reloadConfig();
        config = getConfig();
        loadGroups = config.getBoolean("groups.enable", true);
        groupLimit = config.getInt("groups.grouplimit", 10);
        createGroupOnFirstJoin = config.getBoolean("groups.creationOnFirstJoin", true);
        instance = this;
        loadDatabases();
        ClassHandler.Initialize(Bukkit.getServer());

        NameLayerAPI.init(new GroupManager(), getNameApiDataSource());
        getServer().getPluginManager().registerEvents(new NameLayerAPI(), this);

        registerListener(new PlayerListener());

        if (loadGroups) {
            PermissionType.initialize();
            blackList = new BlackList();
            groupCache = NameLayerGroupCache.loadAll(nameLayerReadDao, getLogger());
            seedInvitationNotificationsFromCache();
            startInvalidationConsumer();
            if (config.getBoolean("groups.interact", true)) {
                defaultGroupHandler = new DefaultGroupHandler();
                autoAcceptHandler = new AutoAcceptHandler(nameLayerReadDao.loadAllAutoAccept());
                handle = new CommandHandler(this);
            }
        }
    }

    private void seedInvitationNotificationsFromCache() {
        final NameLayerGroupCache cache = groupCache;
        if (cache == null) {
            return;
        }
        for (final vg.civcraft.mc.namelayer.group.Group group : cache.snapshotGroups()) {
            for (final java.util.UUID invited : group.getAllInvites()) {
                PlayerListener.addNotification(invited, group);
            }
        }
    }

    @Override
    public void onDisable() {
        if (invalidationConsumer != null) {
            invalidationConsumer.close();
        }
        if (writeClient != null) {
            writeClient.close();
        }
        if (freshnessCheckTask != null) {
            freshnessCheckTask.cancel();
        }
        super.onDisable();
    }

    private void startInvalidationConsumer() {
        final NameLayerRabbitMqConfig rabbitMqConfig;
        try {
            rabbitMqConfig = NameLayerRabbitMqConfig.from(config.getConfigurationSection("rabbitmq"));
        } catch (final IllegalArgumentException exception) {
            getLogger().log(Level.SEVERE, "Invalid NameLayer RabbitMQ configuration", exception);
            return;
        }
        if (!rabbitMqConfig.enabled()) {
            getLogger().log(Level.INFO, "NameLayer RabbitMQ invalidation consumer is disabled");
            return;
        }
        invalidationConsumer = new NameLayerInvalidationConsumer(
            rabbitMqConfig.connectionFactory(),
            rabbitMqConfig.serverId(),
            getLogger(),
            this
        );
        if (!invalidationConsumer.start()) {
            getLogger().log(Level.SEVERE, "NameLayer RabbitMQ invalidation consumer failed to start");
        }
        writeClient = new NameLayerWriteClient(
            rabbitMqConfig.connectionFactory(),
            rabbitMqConfig.serverId(),
            getLogger()
        );
        if (!writeClient.start()) {
            getLogger().log(Level.SEVERE, "NameLayer RabbitMQ write client failed to start");
        }
        startFreshnessCheck(rabbitMqConfig);
    }

    private void startFreshnessCheck(final NameLayerRabbitMqConfig rabbitMqConfig) {
        if (!rabbitMqConfig.freshnessCheckEnabled()) {
            return;
        }
        final long intervalTicks = Math.max(20L, rabbitMqConfig.freshnessCheckIntervalSeconds() * 20L);
        final long jitterTicks = Math.max(0L, rabbitMqConfig.freshnessCheckJitterSeconds() * 20L);
        final long staleGraceMillis = Math.max(0L, rabbitMqConfig.freshnessCheckStaleGraceSeconds() * 1000L);
        final long initialDelay = intervalTicks + (jitterTicks == 0L ? 0L : ThreadLocalRandom.current().nextLong(jitterTicks + 1L));
        freshnessCheckTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            final NameLayerGroupCache activeCache = groupCache;
            if (activeCache == null) {
                return;
            }
            final long localVersion = activeCache.getAppliedVersion();
            final long databaseVersion = nameLayerReadDao.loadCacheVersion();
            if (databaseVersion <= localVersion) {
                if (localVersion > databaseVersion) {
                    getLogger().log(Level.WARNING, "Applied version is in the future? Constraint localVersion <= databaseVersion violated");
                }
                stateLocalVersion = 0L;
                staleVersionDetectedAtMillis = 0L;
                return;
            }
            if (stateLocalVersion != localVersion || staleVersionDetectedAtMillis == 0L) {
                stateLocalVersion = localVersion;
                staleVersionDetectedAtMillis = System.currentTimeMillis();
                return;
            }
            final long staleMillis = System.currentTimeMillis() - staleVersionDetectedAtMillis;
            if (staleMillis < staleGraceMillis) {
                return;
            }
            getLogger().log(Level.WARNING, "NameLayer cache version stayed stale for " + staleMillis + "ms; full-resyncing");
            fullResyncGroupCache();
            stateLocalVersion = 0L;
            staleVersionDetectedAtMillis = 0L;
        }, initialDelay, intervalTicks);
    }

    public static NameLayerPlugin getInstance() {
        return instance;
    }

    private void loadDatabases() {
        String host = config.getString("sql.hostname", "localhost");
        int port = config.getInt("sql.port", 3306);
        String dbname = config.getString("sql.dbname", "namelayer");
        String username = config.getString("sql.username");
        String password = config.getString("sql.password");
        int poolsize = config.getInt("sql.poolsize", 10);
        long connectionTimeout = config.getLong("sql.connection_timeout", 10000l);
        long idleTimeout = config.getLong("sql.idle_timeout", 600000l);
        long maxLifetime = config.getLong("sql.max_lifetime", 7200000l);
        try {
            db = ManagedDatasource.construct(this,
                new DatabaseCredentials(username, password, host, port, "mysql", dbname, poolsize,
                    connectionTimeout, idleTimeout, maxLifetime));
            db.getConnection().close();
        } catch (Exception se) {
            NameLayerPlugin.log(Level.WARNING, "Could not connect to DataBase, shutting down!");
            Bukkit.shutdown();
            return;
        }

        if (!db.isManaged()) {
            // First "migration" is conversion from old system to new, and lives outside AssociationList.
            boolean isNew = true;
            try (Connection connection = db.getConnection();
                 PreparedStatement checkNewInstall = connection.prepareStatement("SELECT * FROM db_version LIMIT 1;");
                 // See if this was a new install. If it was, db_version statement will fail. If it isn't, it'll succeed.
                 //   If the version statement fails, return true; this is new install, carryon.
                 ResultSet rs = checkNewInstall.executeQuery();) {
                isNew = !rs.next();
            } catch (SQLException se) {
                NameLayerPlugin.log(Level.INFO, "New installation: Welcome to Namelayer!");
            }

            if (!isNew) {
                try (Connection connection = db.getConnection();
                     PreparedStatement migrateInstall = connection.prepareStatement(
                         "INSERT INTO managed_plugin_data (plugin_name, current_migration_number, last_migration)"
                             +
                             " SELECT plugin_name, max(db_version), `timestamp` FROM db_version WHERE plugin_name = '"
                             + this.getName() + "' LIMIT 1;");) {
                    int rows = migrateInstall.executeUpdate();
                    if (rows == 1) {
                        NameLayerPlugin.log(Level.INFO, "Migration successful!");
                    } else {
                        Bukkit.shutdown();
                        NameLayerPlugin.log(Level.SEVERE,
                            "Migration failed; db_version exists but uncaptured. Could be version problem.");
                        return;
                    }
                } catch (SQLException se) {
                    Bukkit.shutdown();
                    // Migration failed...
                    NameLayerPlugin.log(Level.SEVERE, "Migration failure!");
                    return;
                }
            }
        } else {
            NameLayerPlugin.log(Level.INFO, "Welcome back, oldtimer.");
        }


        if (loadGroups) {
            nameLayerReadDao = new NameLayerReadDao(getLogger(), db);
        }
    }

    private DataSource getNameApiDataSource() {
        ConfigurationSection section = config.getConfigurationSection("nameapi.database");

        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl("jdbc:" + section.getString("driver", "mariadb") + "://" + section.getString("host", "localhost") + ":" +
            section.getInt("port", 3306) + "/" + section.getString("database", "minecraft"));
        dbConfig.setConnectionTimeout(section.getInt("connection_timeout", 10_000));
        dbConfig.setIdleTimeout(section.getInt("idle_timeout", 600_000));
        dbConfig.setMaxLifetime(section.getInt("max_lifetime", 7_200_000));
        dbConfig.setMaximumPoolSize(section.getInt("poolsize", 1));
        dbConfig.setUsername(section.getString("user", "root"));
        String password = section.getString("password");
        if (password != null && !password.isBlank()) {
            dbConfig.setPassword(password);
        }
        return new HikariDataSource(dbConfig);
    }

    /**
     * @return Returns the GroupManagerDatabase.
     */
    public static NameLayerReadDao getNameLayerReadDao() {
        return nameLayerReadDao;
    }

    public static NameLayerGroupCache getGroupCache() {
        return groupCache;
    }

    public static NameLayerWriteClient getWriteClient() {
        return instance == null ? null : instance.writeClient;
    }

    public static void fullResyncGroupCache() {
        final long startedAtMillis = System.currentTimeMillis();
        groupCache = NameLayerGroupCache.loadAll(nameLayerReadDao, getInstance().getLogger());
        if (defaultGroupHandler != null) {
            defaultGroupHandler.reloadAll();
        }
        if (autoAcceptHandler != null) {
            autoAcceptHandler.reloadAll(nameLayerReadDao.loadAllAutoAccept());
        }
        final long count = instance.fullResyncCount.incrementAndGet();
        getInstance().getLogger().log(
            Level.INFO,
            "NameLayer full resync completed in " + (System.currentTimeMillis() - startedAtMillis) + "ms; count=" + count
        );
    }

    public static void recordTargetedReload(final int groupCount, final long elapsedMillis) {
        final long count = instance.targetedReloadCount.incrementAndGet();
        getInstance().getLogger().log(
            Level.INFO,
            "NameLayer targeted reload completed for " + groupCount + " groups in " + elapsedMillis + "ms; count=" + count
        );
    }

    public static void recordTargetedReloadFailure(final int groupCount) {
        final long count = instance.targetedReloadFailureCount.incrementAndGet();
        getInstance().getLogger().log(
            Level.WARNING,
            "NameLayer targeted reload failed for " + groupCount + " groups; failures=" + count
        );
    }

    public static void recordRabbitMqReconnect() {
        final long count = instance.rabbitMqReconnectCount.incrementAndGet();
        getInstance().getLogger().log(Level.WARNING, "NameLayer RabbitMQ reconnect scheduled; count=" + count);
    }

    public static void log(Level level, String message) {
        if (level == Level.INFO) {
            NameLayerPlugin.getInstance().getSLF4JLogger().info(message);
        } else if (level == Level.WARNING) {
            NameLayerPlugin.getInstance().getSLF4JLogger().warn(message);
        } else if (level == Level.SEVERE) {
            NameLayerPlugin.getInstance().getSLF4JLogger().error(message);
        }
    }

    public static String getSpecialAdminGroup() {
        return "Name_Layer_Special";
    }

    public static boolean createGroupOnFirstJoin() {
        return createGroupOnFirstJoin;
    }

    public int getGroupLimit() {
        return groupLimit;
    }

    public static BlackList getBlackList() {
        return blackList;
    }

    public static AutoAcceptHandler getAutoAcceptHandler() {
        return autoAcceptHandler;
    }

    public static DefaultGroupHandler getDefaultGroupHandler() {
        return defaultGroupHandler;
    }

}
