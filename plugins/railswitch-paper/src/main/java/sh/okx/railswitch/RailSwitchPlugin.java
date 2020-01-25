package sh.okx.railswitch;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.railswitch.database.ConnectionPool;
import sh.okx.railswitch.database.MySQLConnectionPool;
import sh.okx.railswitch.database.RailSwitchDatabase;
import sh.okx.railswitch.database.SQLiteConnectionPool;

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
    
    public RailSwitchDatabase getDatabase() {
        return database;
    }
    
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
    
    public boolean isDebug() {
        return this.debug;
    }
    
}
