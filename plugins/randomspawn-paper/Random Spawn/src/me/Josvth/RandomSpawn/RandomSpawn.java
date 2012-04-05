package me.Josvth.RandomSpawn;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.Josvth.RandomSpawn.Handlers.CommandHandler;
import me.Josvth.RandomSpawn.Handlers.YamlHandler;
import me.Josvth.RandomSpawn.Listeners.*;

public class RandomSpawn extends JavaPlugin{

	Logger logger;

	public YamlHandler yamlHandler;
	CommandHandler commandHandler;

	RandomSpawnRespawnListener respawnListener;
	RandomSpawnJoinListener joinListener;
	RandomSpawnSignListener signListener;
	DebugListener debugListener;

	public HashMap<Player,RandomSpawnSpawner> tasks = new HashMap<Player,RandomSpawnSpawner>();

	@Override
	public void onDisable() {
		for(RandomSpawnSpawner spawner : tasks.values()){
			spawner.forcestop(true);
		}
	}

	@Override
	public void onEnable() {

		logger = Logger.getLogger("Minecraft");

		//setup handlers
		yamlHandler = new YamlHandler(this);
		logDebug("Yamls loaded!");

		commandHandler = new CommandHandler(this);
		logDebug("Commands registered!");

		//setup listeners
		respawnListener = new RandomSpawnRespawnListener(this);
		joinListener = new RandomSpawnJoinListener(this);
		signListener = new RandomSpawnSignListener(this);

		if(yamlHandler.config.getBoolean("debug",false)){
			debugListener = new DebugListener(this);
		}
	}

	public void logInfo(String message){
		logger.info("[Random Spawn] " + message);
	}

	public void logDebug(String message){
		if (yamlHandler.config.getBoolean("debug",false)) { logger.info("[Random Spawn] (DEBUG) " + message); }
	}

	public void logWarning(String message){
		logger.warning("[Random Spawn] " + message);
	}

	public void playerInfo(Player player, String message){
		player.sendMessage(ChatColor.AQUA + "[RandomSpawn] " + ChatColor.RESET + message);
	}

	//    public boolean hasValidEnviroment(World world){
	//    	return world.getEnvironment() != Environment.NETHER;
	//    }

	//	public void randomSpawnPlayer(Player player, World spawnWorld) {		
	//		RandomSpawnSpawner spawner = new RandomSpawnSpawner(this, player, spawnWorld);		
	//		
	//		spawner.setTaskId(getServer().getScheduler().scheduleSyncRepeatingTask(this, spawner, 0, yamlHandler.config.getInt("generator.interval",4)));
	//		player.setMaximumNoDamageTicks(12000);
	//		player.setNoDamageTicks(12000);
	//		
	//		if(yamlHandler.config.contains("messages.pleasewait")){
	//			player.sendMessage(yamlHandler.config.getString("messages.pleasewait"));
	//		}
	//	}

	public Location chooseSpawn(World world){

		String worldName = world.getName();

		int xmin = yamlHandler.worlds.getInt(worldName +".spawnarea.x-min", -100);
		int xmax = yamlHandler.worlds.getInt(worldName +".spawnarea.x-max", 100);
		int zmin = yamlHandler.worlds.getInt(worldName +".spawnarea.z-min", -100);
		int zmax = yamlHandler.worlds.getInt(worldName +".spawnarea.z-max", 100);

		int xrand = 0;
		int zrand = 0;
		int y = -1;

		do {
			xrand = xmin + (int) ( Math.random()*(xmax - xmin) + 0.5 );
			zrand = zmin + (int) ( Math.random()*(zmax - zmin) + 0.5 );
			y = getValidHighestBlock(world, xrand,zrand);
		}while (y == -1);

		return new Location(
				world, 
				Double.parseDouble(Integer.toString(xrand))+0.5, 
				Double.parseDouble(Integer.toString(y)),
				Double.parseDouble(Integer.toString(zrand))+0.5
				);

	}

	private int getValidHighestBlock(World world, int x, int z) {
		world.getChunkAt(new Location(world, x, 0, z)).load();

		int y = world.getHighestBlockYAt(x, z);
		int blockid = world.getBlockTypeIdAt(x, y - 1, z);

		if (blockid == 8) return -1;
		if (blockid == 9) return -1;
		if (blockid == 10) return -1;
		if (blockid == 11) return -1;
		if (blockid == 51) return -1;

		blockid = world.getBlockTypeIdAt(x, y + 1, z);

		if (blockid == 81) return -1;

		return y;
	}
}
