package me.Josvth.RandomSpawn;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.Josvth.RandomSpawn.Handlers.CommandHandler;
import me.Josvth.RandomSpawn.Handlers.YamlHandler;
import me.Josvth.RandomSpawn.Listeners.*;

public class RandomSpawn extends JavaPlugin{

	Logger logger;

	public YamlHandler yamlHandler;
	CommandHandler commandHandler;

	RespawnListener respawnListener;
	JoinListener joinListener;
	SignListener signListener;
	
	@Override
	public void onEnable() {

		logger = Logger.getLogger("Minecraft");

		//setup handlers
		yamlHandler = new YamlHandler(this);
		logDebug("Yamls loaded!");

		commandHandler = new CommandHandler(this);
		logDebug("Commands registered!");

		//setup listeners
		respawnListener = new RespawnListener(this);
		joinListener = new JoinListener(this);
		signListener = new SignListener(this);
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

	// Getting and setting of saved spawn
	
	public Location getPlayerSpawn(Player player, World world){

		String[] coordinates = player.getMetadata(world.getName() + ".spawn").get(0).asString().split("[,]");

		Vector vector = new Vector(
				Double.parseDouble(coordinates[0]), 
				Double.parseDouble(coordinates[1]), 
				Double.parseDouble(coordinates[2])
				);

		return vector.toLocation(world);

	}

	public void setPlayerSpawn(Player player, Location location){
		player.setMetadata(location.getWorld().getName()  + ".spawn", new FixedMetadataValue(this, location.toVector().toString()));
	}
	
	// *------------------------------------------------------------------------------------------------------------*
	// | The random location methods contain code made by NuclearW                                                  |
	// | based on his SpawnArea plugin:                                                                             |
	// | http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/ |
	// *------------------------------------------------------------------------------------------------------------*
	
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
				Double.parseDouble(Integer.toString(xrand)) + 0.5, 
				Double.parseDouble(Integer.toString(y)),
				Double.parseDouble(Integer.toString(zrand)) + 0.5
				);
	}

	private int getValidHighestBlock(World world, int x, int z) {
		world.getChunkAt(new Location(world, x, 0, z)).load();

		int y = world.getHighestBlockYAt(x, z);
		int blockid = world.getBlockTypeIdAt(x, y - 1, z);
		
		logDebug("Type = " + Material.getMaterial(blockid));
		
		if (blockid == 8) return -1;
		if (blockid == 9) return -1;
		if (blockid == 10) return -1;
		if (blockid == 11) return -1;
		if (blockid == 51) return -1;
		if (blockid == 18) return -1;
		
		blockid = world.getBlockTypeIdAt(x, y + 1, z);

		if (blockid == 81) return -1;

		return y;
	}
	
	public void sendGround(Player player, Location location){		
		
		Location groundLocation = location.subtract(0, 1, 0);
		
		groundLocation.getChunk().load();
		
		if(canCauseBlockUpdate(groundLocation.getBlock())){
			player.sendBlockChange(groundLocation, Material.DIRT, (byte) 0);
		}else{
			player.sendBlockChange(groundLocation, groundLocation.getBlock().getType(), groundLocation.getBlock().getData());
		}
		
//		for(int xx = location.getBlockX() - 1; xx <= location.getBlockX() + 1; xx++){
//			for(int zz = location.getBlockZ() -1; zz <= location.getBlockZ() + 1; zz++){
//				
//				location.getWorld().getChunkAt(new Location(location.getWorld(), xx, 0, zz)).load();
//				
//				int y = location.getWorld().getHighestBlockYAt(xx, zz);
//				
//				Location groundLocation = new Location(location.getWorld(), xx, y - 1, zz);
//				
//				Block groundBlock = groundLocation.getBlock();
//				
//				if(canCauseBlockUpdate(groundBlock)){
//					player.sendBlockChange(groundLocation, Material.DIRT, (byte) 0);
//					logDebug("MADE DIRT!");
//				}else{
//					player.sendBlockChange(groundLocation, groundBlock.getType(), groundBlock.getData());
//					logDebug("Typeground = " + groundBlock.getType());
//				}
//			}
//		}	
	}
	
	private boolean canCauseBlockUpdate(Block block){
		
		block.getChunk().load();
		
		int blockid = block.getTypeId();
		
		if (blockid == 8) return true;
		if (blockid == 9) return true;
		if (blockid == 10) return true;
		if (blockid == 11) return true;
		if (blockid == 12) return true;
		if (blockid == 13) return true;
		
		return false;
	}	
}