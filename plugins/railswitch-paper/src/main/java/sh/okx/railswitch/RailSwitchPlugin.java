package sh.okx.railswitch;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.railswitch.database.ConnectionPool;
import sh.okx.railswitch.database.MySQLConnectionPool;
import sh.okx.railswitch.database.RailSwitchDatabase;
import sh.okx.railswitch.database.SQLiteConnectionPool;

/**
 * The Rail Switch plugin class
 */
public class RailSwitchPlugin extends JavaPlugin {
    
    private boolean debug;
    
    private RailSwitchDatabase database;
    
    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        loadDatabase();
        this.debug = getConfig().getBoolean("debug", getConfig().getBoolean("timings", false));
        getServer().getPluginManager().registerEvents(new DetectorRailActivateListener(this), this);
        getCommand("setdestination").setExecutor(new SetDestinationCommand(this));
    }
    
    @Override
    public void onDisable() {
        super.onDisable();
        HandlerList.unregisterAll(this);
    }
    
    /**
     * Retrieves the database connection setup that was created by {@link RailSwitchPlugin#loadDatabase()}.
     *
     * @return Returns the database connection setup.
     */
    public RailSwitchDatabase getDatabase() {
        return database;
    }
    
    /**
     * Create a database connection setup by the configuration.
     */
    private void loadDatabase() {
        ConfigurationSection config = getConfig();
        ConnectionPool pool;
        String type = config.getString("database-type");
        if (type.equalsIgnoreCase("mysql")) {
            pool = new MySQLConnectionPool(
                    config.getString("mysql.host"),
                    config.getInt("mysql.port"),
                    config.getString("mysql.database"),
                    config.getString("mysql.username"),
                    config.getString("mysql.password"));
        }
        else if (type.equalsIgnoreCase("sqlite")) {
            pool = new SQLiteConnectionPool(
                    getDataFolder(),
                    config.getString("sqlite.file-name"));
        }
        else {
            throw new RuntimeException("Invalid database-type in config.yml. Disabling plugin.");
        }
        this.database = new RailSwitchDatabase(pool, getLogger());
    }
    
    /**
     * Determines whether the plugin is in debug mode.
     *
     * @return Returns whether the plugin is in debug mode.
     */
    public boolean isDebug() {
        return this.debug;
    }
    
}
