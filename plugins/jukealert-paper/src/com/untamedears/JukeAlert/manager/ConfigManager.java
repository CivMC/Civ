package com.untamedears.JukeAlert.manager;

import static com.untamedears.JukeAlert.util.Utility.setDebugging;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.untamedears.JukeAlert.JukeAlert;

public class ConfigManager 
{
	private JukeAlert plugin;
	
	private String username;
	private String host;
	private String password;
	private String database;
	private String prefix;
	private int port;
	
	private int defaultCuboidSize;
	
	private int logsPerPage;
	private boolean snitchEntryCullingEnabled;
	private int maxEntryCount;
	private int minEntryLifetimeDays;
	private int maxEntryLifetimeDays;
	private boolean snitchCullingEnabled;
	private int maxSnitchLifetimeDays;

	private File main;
	private FileConfiguration config;
	private FileConfiguration cleanConfig;
	
	public ConfigManager()
	{
		this.plugin      = JukeAlert.getInstance();
		this.config      = plugin.getConfig();
		this.cleanConfig = new YamlConfiguration();
		this.main        = new File(plugin.getDataFolder() + File.separator + "config.yml");
		this.load();
	}
	
	/**
	 * Load configuration
	 */
	private void load()
	{
		boolean exists = main.exists();

        if (exists)
        {
            try
            {
                config.options().copyDefaults(true);
                config.load(main);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            config.options().copyDefaults(true);
        }
        
        username = loadString("mysql.username");
        host     = loadString("mysql.host");
        password = loadString("mysql.password");
        database = loadString("mysql.database");
        prefix   = loadString("mysql.prefix");
        port     = loadInt("mysql.port");
        
        setDefaultCuboidSize(loadInt("settings.defaultCuboidSize"));
        logsPerPage = loadInt("settings.logsPerPage");
        setDebugging(loadBoolean("settings.debugging"));
        
        snitchEntryCullingEnabled = loadBoolean("entryculling.enabled", true);
        maxEntryCount = loadInt("entryculling.maxcount", 200);
        minEntryLifetimeDays = loadInt("entryculling.minlifetime", 1);
        maxEntryLifetimeDays = loadInt("entryculling.maxlifetime", 8);
        snitchCullingEnabled = loadBoolean("snitchculling.enabled", false);
        maxSnitchLifetimeDays = loadInt("snitchculling.maxlifetime", 21);

        save();
    }

    private Boolean loadBoolean(String path)
    {
        if (config.isBoolean(path))
        {
            boolean value = config.getBoolean(path);
            cleanConfig.set(path, value);
            return value;
        }
        return false;
    }

    private Boolean loadBoolean(String path, boolean defaultValue)
    {
        if (config.isBoolean(path))
        {
            boolean value = config.getBoolean(path);
            cleanConfig.set(path, value);
            return value;
        }
        return defaultValue;
    }

    private String loadString(String path)
    {
        if (config.isString(path))
        {
            String value = config.getString(path);
            cleanConfig.set(path, value);
            return value;
        }

        return "";
    }

    private int loadInt(String path)
    {
        if (config.isInt(path))
        {
            int value = config.getInt(path);
            cleanConfig.set(path, value);
            return value;
        }

        return 0;
    }

    private int loadInt(String path, int defaultValue) {
        if (config.isInt(path)) {
            int value = config.getInt(path);
            cleanConfig.set(path, value);
            return value;
        }
        return defaultValue;
    }

    private double loadDouble(String path)
    {
        if (config.isDouble(path))
        {
            double value = config.getDouble(path);
            cleanConfig.set(path, value);
            return value;
        }

        return 0;
    }
    
    private List<String> loadStringList(String path)
    {
    	if(config.isList(path))
    	{
    		List<String> value = config.getStringList(path);
    		cleanConfig.set(path, value);
    		return value;
    	}
    	
    	return null;
    }
    
    public void save()
    {
        try
        {
            cleanConfig.save(main);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getDefaultCuboidSize() {
		return defaultCuboidSize;
	}

	public void setDefaultCuboidSize(int defaultCuboidSize) {
		this.defaultCuboidSize = defaultCuboidSize;
	}
	
	public int getLogsPerPage() {
		return logsPerPage;
	}

	public void setLogsPerPage(int logsPerPage) {
		this.logsPerPage = logsPerPage;
	}

	public File getMain() {
		return main;
	}

	public void setMain(File main) {
		this.main = main;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public void setConfig(FileConfiguration config) {
		this.config = config;
	}

	public FileConfiguration getCleanConfig() {
		return cleanConfig;
	}

	public void setCleanConfig(FileConfiguration cleanConfig) {
		this.cleanConfig = cleanConfig;
	}

	public boolean getSnitchEntryCullingEnabled() {
		return snitchEntryCullingEnabled;
	}

	public int getMaxSnitchEntryCount() {
		return maxEntryCount;
	}

	public int getMinSnitchEntryLifetime() {
		return minEntryLifetimeDays;
	}

	public int getMaxSnitchEntryLifetime() {
		return maxEntryLifetimeDays;
	}

	public boolean getSnitchCullingEnabled() {
		return snitchCullingEnabled;
	}

	public int getMaxSnitchLifetime() {
		return maxSnitchLifetimeDays;
	}

}
