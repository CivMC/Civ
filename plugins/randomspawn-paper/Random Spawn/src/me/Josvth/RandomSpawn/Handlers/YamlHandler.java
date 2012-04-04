package me.Josvth.RandomSpawn.Handlers;

import java.io.File;
import java.io.IOException;


import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class YamlHandler{

	RandomSpawn plugin;

	File configFile;
    File worldsFile;
    File spawnLocationsFile;

    public FileConfiguration config;
    public FileConfiguration worlds;
    public FileConfiguration spawnLocations;
	
	public YamlHandler(RandomSpawn instance) {
		plugin = instance;
		setupYamls();
        loadYamls();
	}
	
	public void setupYamls(){
		configFile = new File(this.plugin.getDataFolder(), "config.yml");
        worldsFile = new File(this.plugin.getDataFolder(), "worlds.yml");
        spawnLocationsFile = new File(this.plugin.getDataFolder(), "spawnLocations.yml");
		
        if (!(configFile.exists())){this.plugin.saveResource("config.yml", false);}				// loads default config's on first run
        if (!(worldsFile.exists())){this.plugin.saveResource("worlds.yml", false);}
        if (!(spawnLocationsFile.exists())){this.plugin.saveResource("spawnLocations.yml", false);}
			
	}
	
	public void loadYamls() {
		
		config = new YamlConfiguration();
        worlds = new YamlConfiguration();
        spawnLocations = new YamlConfiguration();
        
		loadConfig();
        loadWorlds();
        loadSpawnLocations();
    }
    
    public void loadConfig(){
        try {
            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }  
    }
    
    public void loadWorlds(){       
        try {
            worlds.load(worldsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadSpawnLocations(){
        try {
            spawnLocations.load(spawnLocationsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveYamls() {
        saveConfig();
        saveWorlds();
        saveSpawnLocations();
    }
    
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveWorlds() {
        try {
            worlds.save(worldsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveSpawnLocations() {
        try {
            spawnLocations.save(spawnLocationsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }	
}
