package me.Josvth.RandomSpawn;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.Josvth.RandomSpawn.Listeners.*;

public class RandomSpawn extends JavaPlugin{

	protected RandomSpawnLogger logger;
	RandomSpawnYamlHandler yamlHandler;
	RandomSpawnCommandHandler commandHandler;
	
	RandomSpawnRespawnListener respawnListener;
	RandomSpawnJoinListener joinListener;
	RandomSpawnDeathListener deathListener;
	RandomSpawnSignListener signListener;
	
	public HashMap<Player,RandomSpawnSpawner> tasks = new HashMap<Player,RandomSpawnSpawner>();
	
	
	@Override
	public void onDisable() {
		for(RandomSpawnSpawner spawner : tasks.values()){
			spawner.forcestop(true);
		}
	}

	@Override
	public void onEnable() {
		
		//setup handlers
		this.logger = new RandomSpawnLogger(this);
		
		this.yamlHandler = new RandomSpawnYamlHandler(this);
		logDebug("Yamls loaded!");
		
		this.commandHandler = new RandomSpawnCommandHandler(this);
		logDebug("Commands registered!");
		
		//setup listeners
		this.respawnListener = new RandomSpawnRespawnListener(this);
		this.joinListener = new RandomSpawnJoinListener(this);
		this.deathListener = new RandomSpawnDeathListener(this);
		this.signListener = new RandomSpawnSignListener(this);
	}
	
    public void playerInfo(Player player, String message){
    	this.logger.playerInfo(player, message);
    }
    
	public void logInfo(String message){
        this.logger.info(message);
    }
    
    public void logDebug(String message){
       if (yamlHandler.config.getBoolean("debug",false)) {
            this.logger.debug(message);
      }
    }
    
    public void logWarning(String message){
        this.logger.warn(message);
    }
        
    public RandomSpawnYamlHandler getYamlHandler(){
		return yamlHandler;   	
    }

	public void randomSpawnPlayer(Player player, World spawnWorld) {		
		RandomSpawnSpawner spawner = new RandomSpawnSpawner(this, player, spawnWorld);		
		spawner.setTaskId(this.getServer().getScheduler().scheduleSyncRepeatingTask(this, spawner, 0, this.yamlHandler.config.getInt("generator.interval",4)));
		player.setMaximumNoDamageTicks(12000);
		player.setNoDamageTicks(12000);
		player.sendMessage(yamlHandler.config.getString("messages.pleasewait", "Please wait while your spawn is being loaded."));
	}
}
