package isaac.bastion.manager;

import org.bukkit.configuration.file.FileConfiguration;

import isaac.bastion.Bastion;

public class ConfigManager {
	private FileConfiguration config;
	
	private String host;
	private int port;
	private String database;
	private String prefix;
	
	private String username;
	private String password;
	private int saveTimeInt;
	static String file_name = "config.yml";
	
	public ConfigManager() {
		Bastion.getPlugin().saveDefaultConfig();
		Bastion.getPlugin().reloadConfig();
		config = Bastion.getPlugin().getConfig();
		
		load();
	}
	
	private void load() {
		host = loadString("mysql.host");
		port = loadInt("mysql.port");
		database = loadString("mysql.database");
		prefix = loadString("mysql.prefix");
		
		username = loadString("mysql.username");
		password = loadString("mysql.password");
		int savesPerDay = loadInt("mysql.savesPerDay");
		if (savesPerDay !=0) {
			saveTimeInt = 1728000 / savesPerDay; // ticks * secs * mins * hours
		} else{
			saveTimeInt = 0;
		}
	}
	
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public String getDatabase() {
		return database;
	}
	public String getPrefix() {
		return prefix;
	}

	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public int getTimeBetweenSaves() {
		return saveTimeInt;
	}

	private int loadInt(String field) {
		if (config.isInt(field)) {
			int value = config.getInt(field);
			return value;
		}
		return Integer.MIN_VALUE;
		
	}
	private String loadString(String field) {
		if (config.isString(field)) {
			String value = config.getString(field);
			return value;
		}
		return null;
	}
}
