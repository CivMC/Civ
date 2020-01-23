package sh.okx.railswitch;

import com.google.common.base.CharMatcher;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.railswitch.database.ConnectionPool;
import sh.okx.railswitch.database.MySQLConnectionPool;
import sh.okx.railswitch.database.RailSwitchDatabase;
import sh.okx.railswitch.database.SQLiteConnectionPool;
import sh.okx.railswitch.listener.DetectorRailActivateListener;

public class RailSwitch extends JavaPlugin {
    private boolean timings;
    private RailSwitchDatabase database;
    
    @Override
    public void onEnable() {
        super.onEnable();
        saveDefaultConfig();
        loadDatabase();
        
        if (getConfig().getBoolean("timings")) {
            timings = true;
        }
        
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvents(new DetectorRailActivateListener(this), this);
        getCommand("setdestination").setExecutor(new SetDestinationCommand(this));
    }
    
    public RailSwitchDatabase getDatabase() {
        return database;
    }
    
    private void loadDatabase() {
        ConfigurationSection config = getConfig();
        
        ConnectionPool pool;
        String type = config.getString("database-type");
        if (type.equalsIgnoreCase("mysql")) {
            String username = config.getString("mysql.username");
            String host = config.getString("mysql.host");
            String password = config.getString("mysql.password");
            String database = config.getString("mysql.database");
            int port = config.getInt("mysql.port");
            
            pool = new MySQLConnectionPool(host, port, database, username, password);
        } else if (type.equalsIgnoreCase("sqlite")) {
            pool = new SQLiteConnectionPool(getDataFolder(), config.getString("sqlite.file-name"));
        } else {
            throw new RuntimeException("Invalid database-type in config.yml. Disabling plugin.");
        }
        
        this.database = new RailSwitchDatabase(pool, getLogger());
    }
    
    /**
     * make sure the message doesn't have any weirdness
     */
    public boolean isValidDestination(String message) {
        // Each destination must be fewer than 40 characters.
        for (String dest : message.split(" ")) {
            if (dest.length() > 40) {
                return false;
            }
        }
        
        return CharMatcher.inRange('0', '9')
                .or(CharMatcher.inRange('a', 'z'))
                .or(CharMatcher.inRange('A', 'Z'))
                .or(CharMatcher.anyOf("!\"#$%&'()*+,-./;:<=>?@[]\\^_`{|}~ ")).matchesAllOf(message);
    }
    
    public boolean isTimings() {
        return timings;
    }
}
