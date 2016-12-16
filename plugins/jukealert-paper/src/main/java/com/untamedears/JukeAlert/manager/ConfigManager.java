package com.untamedears.JukeAlert.manager;

import static com.untamedears.JukeAlert.util.Utility.setDebugging;

import org.bukkit.configuration.file.FileConfiguration;

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
	private int daysFromLastAdminVisitForLoggedSnitchCulling;
	private int daysFromLastAdminVisitForNonLoggedSnitchCulling;
	private boolean snitchEntryCullingEnabled;
	private boolean allowTriggeringLevers;
	private int maxEntryCount;
	private int minEntryLifetimeDays;
	private int maxEntryLifetimeDays;
	private boolean snitchCullingEnabled;
	private int maxSnitchLifetimeDays;
    private Double maxAlertDistanceAll = null;
    private Double maxAlertDistanceNs = null;
    private int maxPlayerAlertCount;
	private boolean taxReinforcementPerAlert;
    private int alertRateLimit;
    private boolean enableInvisibility;
    private boolean toggleRestartCheckGroup;
    private boolean displayOwnerOnBreak;
    private boolean softDelete;
    private boolean multipleWorldSupport = false;

    private boolean broadcastAllServers;

	private FileConfiguration config;

	public ConfigManager()
	{
		this.plugin = JukeAlert.getInstance();
		this.config = plugin.getConfig();
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		this.config = plugin.getConfig();
		this.load();
	}

	/**
	 * Load configuration
	 */
	private void load()
	{
        username = config.getString("mysql.username");
        host     = config.getString("mysql.host");
        password = config.getString("mysql.password");
        database = config.getString("mysql.database");
        prefix   = config.getString("mysql.prefix");
        port     = config.getInt("mysql.port");

        setDefaultCuboidSize(config.getInt("settings.defaultCuboidSize"));
        logsPerPage = config.getInt("settings.logsPerPage");
		daysFromLastAdminVisitForLoggedSnitchCulling = config.getInt("settings.daysFromLastAdminVisitForLoggedSnitchCulling");
		daysFromLastAdminVisitForNonLoggedSnitchCulling = config.getInt("settings.daysFromLastAdminVisitForNonLoggedSnitchCulling");
        allowTriggeringLevers = config.getBoolean("settings.allowTriggeringLevers",false);
        setDebugging(config.getBoolean("settings.debugging"));
        if (config.isDouble("settings.max_alert_distance")) {
            maxAlertDistanceAll = config.getDouble("settings.max_alert_distance");
        }
        if (config.isDouble("settings.max_alert_distance_ns")) {
            maxAlertDistanceNs = config.getDouble("settings.max_alert_distance_ns");
        }
        maxPlayerAlertCount = config.getInt("settings.max_player_alert_count", Integer.MAX_VALUE);
        taxReinforcementPerAlert = config.getBoolean("settings.tax_reinforcement_per_alert");

        snitchEntryCullingEnabled = config.getBoolean("entryculling.enabled", true);
        maxEntryCount = config.getInt("entryculling.maxcount", 200);
        minEntryLifetimeDays = config.getInt("entryculling.minlifetime", 1);
        maxEntryLifetimeDays = config.getInt("entryculling.maxlifetime", 8);
        snitchCullingEnabled = config.getBoolean("snitchculling.enabled", false);
        maxSnitchLifetimeDays = config.getInt("snitchculling.maxlifetime", 21);
        alertRateLimit = config.getInt("settings.alertratelimit", 70);
        enableInvisibility = config.getBoolean("settings.enableinvisiblity", false);
        toggleRestartCheckGroup = config.getBoolean("settings.togglerestartgroupcheck", false);
        displayOwnerOnBreak = config.getBoolean("settings.displayOwnerOnSnitchBreak", true);
        softDelete = config.getBoolean("settings.softDelete", true);
        multipleWorldSupport = config.getBoolean("settings.multipleWorldSupport", false);

        broadcastAllServers = config.getBoolean("mercury.broadcastallservers", false);
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

	public int getDaysFromLastAdminVisitForNonLoggedSnitchCulling() {
		return daysFromLastAdminVisitForNonLoggedSnitchCulling;
	}

	public int getDaysFromLastAdminVisitForLoggedSnitchCulling() {
		return daysFromLastAdminVisitForLoggedSnitchCulling;
	}

	public Boolean getAllowTriggeringLevers() {
		return allowTriggeringLevers;
	}

	public void setLogsPerPage(int logsPerPage) {
		this.logsPerPage = logsPerPage;
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

    public Double getMaxAlertDistanceAll() {
        return maxAlertDistanceAll;
    }

    public Double getMaxAlertDistanceNs() {
        return maxAlertDistanceNs;
    }

    public int getMaxPlayerAlertCount() {
        return maxPlayerAlertCount;
    }

    public boolean getTaxReinforcementPerAlert() {
        return taxReinforcementPerAlert;
    }

    public int getAlertRateLimit() {
        return alertRateLimit;
    }

    public boolean getInvisibilityEnabled(){
    	return enableInvisibility;
    }

    public boolean isDisplayOwnerOnBreak() {
        return displayOwnerOnBreak;
    }

    public boolean isSoftDelete() {
    	return softDelete;
    }

    public boolean getMultipleWorldSupport() {
        return multipleWorldSupport;
    }

    public boolean getToggleRestartCheckGroup(){
    	return toggleRestartCheckGroup;
    }

	public boolean getBroadcastAllServers() {
		return broadcastAllServers;
	}

}
