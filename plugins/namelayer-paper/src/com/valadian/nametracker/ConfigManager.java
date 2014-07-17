package com.valadian.nametracker;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

	public void setConfigOptions(FileConfiguration config){
		if (!config.contains("sql.hostname")) config.set("sql.hostname", "localhost");

		if (!config.contains("sql.port")) config.set("sql.port", 3306);

		if (!config.contains("sql.dbname")) config.set("sql.dbname", "nametracker");

		if (!config.contains("sql.username")) config.set("sql.username", "");

		if (!config.contains("sql.password")) config.set("sql.password", "");
	}
}
