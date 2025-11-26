package vg.civcraft.mc.namelayer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.command.CommandHandler;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.AutoAcceptHandler;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.DefaultGroupHandler;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.misc.ClassHandler;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class NameLayerPlugin extends ACivMod {

    private static BlackList blackList;
    private static GroupManagerDao groupManagerDao;
    private static DefaultGroupHandler defaultGroupHandler;
    private static NameLayerPlugin instance;
    private static AutoAcceptHandler autoAcceptHandler;
    private CommandHandler handle;
    private static ManagedDatasource db;
    private static boolean loadGroups = true;
    private static int groupLimit = 10;
    private static boolean createGroupOnFirstJoin;
    private FileConfiguration config;

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
            if (config.getBoolean("groups.interact", true)) {
                groupManagerDao.loadGroupsInvitations();
                defaultGroupHandler = new DefaultGroupHandler();
                autoAcceptHandler = new AutoAcceptHandler(groupManagerDao.loadAllAutoAccept());
                handle = new CommandHandler(this);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
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
            // First "migration" is conversion from old system to new, and lives outside AssociationList and GroupManagerDao.
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
            groupManagerDao = new GroupManagerDao(getLogger(), db);
            groupManagerDao.registerMigrations();
            NameLayerPlugin.log(Level.INFO, "Removing any cycles...");
            groupManagerDao.removeCycles();
        }

        long begin_time = System.currentTimeMillis();

        try {
            getLogger().log(Level.INFO, "Update prepared, starting database update.");
            if (!db.updateDatabase()) {
                getLogger().log(Level.SEVERE, "Update failed, terminating Bukkit.");
                Bukkit.shutdown();
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Update failed, terminating Bukkit. Cause:", e);
            Bukkit.shutdown();
        }

        getLogger()
            .log(Level.INFO, "Database update took {0} seconds", (System.currentTimeMillis() - begin_time) / 1000);

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
    public static GroupManagerDao getGroupManagerDao() {
        return groupManagerDao;
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
