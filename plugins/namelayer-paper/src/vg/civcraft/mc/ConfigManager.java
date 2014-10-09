package vg.civcraft.mc;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

	private FileConfiguration config;
	public void setConfigOptions(FileConfiguration config){
		this.config = config;
	}
}
