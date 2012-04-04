
// *------------------------------------------------------------------------------------------------------------*
// | The random location methods contain code made by NuclearW                                                  |
// | based on his SpawnArea plugin:                                                                             |
// | http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/ |
// *------------------------------------------------------------------------------------------------------------*

// *------------------------------------------------------------------------------------------------------*
// | The area generation is based on Brettflan's fill tool in WorldBorder                                 |
// | http://dev.bukkit.org/server-mods/worldborder/                                                       |
// *------------------------------------------------------------------------------------------------------*

package me.Josvth.RandomSpawn;

import java.io.File;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class RandomSpawnSpawner implements Runnable{
	
	World world;
	String worldName;
	
	Player player;
	String playerName;
	
	Location spawnLocation = null;
	
	RandomSpawn plugin;
	int taskID = 0;
	
	int viewDistance;
	int chunksPerInterval;
		
	//Random Location fields
	int xmin; 
	int xmax;
	int zmin;
	int zmax;
	
	int xrand;
	int zrand;
	int y;
	
	int tries = 0;
	boolean readyfornexttry = true;
	
	//Spiral Generation fields
	int processedChunks;
	int skippedChunks;
	long chunkaverage = 0;
	
	int refchunkx;
	int refchunkz;
	
	int chunkx;
	int chunkz;
	
	int slength = 0;
	int current = 1;

	int negpos = 1;
	boolean xside = true;
	
	boolean readyfornextrun = true;
	
	//Status fields
	long startTime;
	String status = "Initialising";
	boolean initialised = false;
	boolean finished = false;
	boolean forcestopped = false;
		
	public RandomSpawnSpawner(RandomSpawn instance, Player player, World world){
		
		startTime = System.currentTimeMillis();
		
		plugin = instance;
				
		this.player = player;
		this.world = world;
		
		logStatus("New task created!");
	}

	public void setTaskId(int id) {
		taskID = id;
		plugin.logDebug("Has gotten an id!");
	}
	
	private void initialise() {
		
		plugin.tasks.put(player,this);
		logStatus(player.getName() + " is added to the deadlist.");
		
		viewDistance = plugin.getServer().getViewDistance();
		chunksPerInterval = plugin.yamlHandler.config.getInt("generator.chunksperrun",32);
		
		logStatus("Viewdistance: " + viewDistance);
		logStatus("Chunks per interval: " + chunksPerInterval);
		
		worldName = world.getName();
		playerName = player.getName();
		
		xmin = plugin.yamlHandler.worlds.getInt(worldName +".spawnarea.x-min", -100);
		xmax = plugin.yamlHandler.worlds.getInt(worldName +".spawnarea.x-max", 100);
		zmin = plugin.yamlHandler.worlds.getInt(worldName +".spawnarea.z-min", -100);
		zmax = plugin.yamlHandler.worlds.getInt(worldName +".spawnarea.z-max", 100);
				
		initialised = true;
				
		logStatus("Initialisation done! (" + (System.currentTimeMillis() - startTime) + "ms)"); 
		
	}
	
	@Override
	public void run() {
		
		if (!initialised){
			initialise();
		}
		
		if(!player.isOnline()){
			logStatus("Player " + playerName + " is not online anymore. Force stopping task!");
			forcestop(true);
			return;
		}
						
		if (spawnLocation == null){
			changeStatus("Chosing spawn.");
			chooseRandomLocation();
			return;
		}else{
			changeStatus("Generating spawn.");
			generateArea();
		}
	}		
	
	private void chooseRandomLocation(){
		if(!readyfornexttry) return;
		readyfornexttry = false;
		
		tries++;
		generateRandomXZ();
		
		int y = checklocation();
		
		if(y != -1){
			spawnLocation = new Location(world, Double.parseDouble(Integer.toString(xrand))+0.5, Double.parseDouble(Integer.toString(y)) ,Double.parseDouble(Integer.toString(zrand))+0.5);
			logStatus("Random Location found: " + spawnLocation);
		}
		
		readyfornexttry = true;
	}
	
	private void generateRandomXZ(){
		xrand = xmin + (int) ( Math.random()*(xmax - xmin) + 0.5 );
		zrand = zmin + (int) ( Math.random()*(zmax - zmin) + 0.5 );
	}
	
	private int checklocation(){
		Chunk chunk = world.getChunkAt(new Location(world, xrand, 0, zrand));
		
		chunkx = chunk.getX();
		chunkz = chunk.getZ();
		refchunkx = chunkx;
		refchunkz = chunkz;	
		
		chunk.load();
				
		int y = world.getHighestBlockYAt(xrand, zrand);
		int blockid = world.getBlockTypeIdAt(xrand, y - 1, zrand);
		
		if (blockid == 8) {return -1;}
		if (blockid == 9) {return -1;}
		if (blockid == 10) {return -1;}
		if (blockid == 11) {return -1;}
		if (blockid == 51) {return -1;}

		blockid = world.getBlockTypeIdAt(xrand, y + 1, zrand);
		if (blockid == 81) {return -1;}
		
		return y;	
	}
	
	private void generateArea(){
		
		if(!readyfornextrun){
			logStatus("Not ready for next generation run!");
			return;
		}
						
		readyfornextrun = false;
		
		for (int loop = 0; loop < chunksPerInterval ; loop ++){			
		
			if(finished || forcestopped) return;
			
			if (world.isChunkLoaded(chunkx, chunkz)){
				skippedChunks++;
			}else{
				world.loadChunk(chunkx, chunkz, true);
				world.refreshChunk(chunkx, chunkz);
			}
						
			processedChunks++;
			
			nextChunk();
		}

		readyfornextrun = true;
		
	}
	
	private void nextChunk(){
		if (chunkx == refchunkx + viewDistance && chunkz == refchunkz - viewDistance){
			teleportPlayer();
		}

		if (current < slength){
			current++;
		}else{
			current = 1;
			xside ^= true;
			if (!xside){
				slength++;
				negpos = negpos * -1;
			}
		}

		if (xside){
			chunkx = chunkx + negpos;
		}else{
			chunkz = chunkz + negpos;
		}
	}
	
	private void finish() {
		finished = true;		
				
		logStatus("Finished! (" + (System.currentTimeMillis() - startTime) + "ms)");
		logStatus(processedChunks + " chunks processed.");
		logStatus(skippedChunks + " chunks skipped.");
		logStatus(tries + " random location tries.");
		
		plugin.tasks.remove(player);
		logStatus("involving " + playerName + " is removed from the tasklist.");
		plugin.getServer().getScheduler().cancelTask(taskID);
	}
	
	public void forcestop(Boolean delete) {
		forcestopped = true;
				
		logStatus("Forcestopped! (" + (System.currentTimeMillis() - startTime) + "ms)");
		logStatus(processedChunks + " chunks processed.");
		logStatus(skippedChunks + " chunks skipped.");
		logStatus(tries + " random location tries.");
		
		if(delete){
			if (player.isOnline()){
				logStatus("Failed to delete " + player.getName() + " from world folder. Player is still online.");
			}else{
				logStatus("Player is not online! Removing from world folder! Causing first join!");
				File file = new File(player.getWorld().getWorldFolder() + "/players/" + player.getName() + ".dat");
				Boolean deleted = file.delete();
				
				if (deleted){
					logStatus(player.getName() + " is succesfully deleted from "+ worldName + " players folder.");
				}else{
					logStatus("Failed to delete " + player.getName() + " from "+ worldName + " players folder.");
				}
			}
		}
		
		plugin.tasks.remove(player);
		logStatus("involving " + playerName + " is removed from the tasklist.");
		plugin.getServer().getScheduler().cancelTask(taskID);
	}
		
	private void changeStatus(String newstatus){
		if (status != newstatus){
			status = newstatus;
			logStatus(status);
		}
	}
	
	private void logStatus(String message){
		if (taskID == 0){
			plugin.logDebug("Task NOID: " + message);
		}else{
			plugin.logDebug("Task "+ taskID + ": " + message);
		}
	}
	
	private void teleportPlayer() {
	
		player.setMaximumNoDamageTicks(400);
		player.setNoDamageTicks(400);
		player.setHealth(player.getMaxHealth());
		player.setFallDistance(-100);
		player.setVelocity(new Vector(0,0,0));
		
		if (this.plugin.yamlHandler.config.getBoolean("fallfromsky", false)){
			spawnLocation.setY(300);
			logStatus(playerName + " is dropped at: " + spawnLocation.getX() + "," + spawnLocation.getZ());			
		}else{
			logStatus(playerName + " is spawned at: " + spawnLocation.getX() + "," + spawnLocation.getY() + "," + spawnLocation.getZ());		
			
			if(plugin.yamlHandler.config.contains("messages.randomspawned")){
				player.sendMessage(plugin.yamlHandler.config.getString("messages.randomspawned"));
			}
		}
		
		if (this.plugin.yamlHandler.worlds.getBoolean(worldName + ".keeprandomspawns",false)){					//checks if spawn should be saved
			
			this.plugin.yamlHandler.spawnLocations.set(worldName + "." + playerName + ".x", spawnLocation.getX());
			this.plugin.yamlHandler.spawnLocations.set(worldName + "." + playerName + ".y", spawnLocation.getY());
			this.plugin.yamlHandler.spawnLocations.set(worldName + "." + playerName + ".z", spawnLocation.getZ());
			this.plugin.yamlHandler.saveSpawnLocations();
			
			logStatus(playerName + "'s Random Spawn location is saved!");
		}
		
		logStatus(playerName + " Falldistance: " + player.getFallDistance() + " Velocity: " + player.getVelocity() + " No damage tickes: " + player.getNoDamageTicks());
		
		// Meta data for debug listener
		if(plugin.yamlHandler.config.getBoolean("debug", false)){
			player.removeMetadata("lasttimerandomspawned", plugin);
			player.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
		}
		
		if (player.isOnline()){
			player.teleport(spawnLocation);
			finish();
		}else{
			forcestop(true);
		}
	}	
}
