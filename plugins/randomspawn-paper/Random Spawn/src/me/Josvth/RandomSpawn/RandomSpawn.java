package me.Josvth.RandomSpawn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.Josvth.RandomSpawn.Handlers.CommandHandler;
import me.Josvth.RandomSpawn.Handlers.YamlHandler;
import me.Josvth.RandomSpawn.Listeners.*;

public class RandomSpawn extends JavaPlugin{

	Logger logger;

	public YamlHandler yamlHandler;
	CommandHandler commandHandler;

	RespawnListener respawnListener;
	JoinListener joinListener;
	WorldChangeListener worldChangeListener;
	SignListener signListener;
	DamageListener damageListener;

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
		worldChangeListener = new WorldChangeListener(this);
		signListener = new SignListener(this);
		damageListener = new DamageListener(this);

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

	//	public boolean isFirstJoin(Player player, World world){
	//	
	//		if(!world.getEnvironment().equals(Environment.NORMAL)) return false; 
	//		
	//		File file = new File(world.getWorldFolder() + File.separator + "players" + File.separator + player.getName() + ".dat");
	//
	//		if(file.exists()) return false;
	//		
	//		return true;
	//	}

	// *------------------------------------------------------------------------------------------------------------*
	// | The following chooseSpawn method contains code made by NuclearW                                            |
	// | based on his SpawnArea plugin:                                                                             |
	// | http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/ |
	// *------------------------------------------------------------------------------------------------------------*

	public Location chooseSpawn(World world){

		String worldName = world.getName();

		// I don't like this method
		List<Integer> blacklist = new ArrayList<Integer>();
		List<String> stringblacklist = yamlHandler.worlds.getStringList(worldName + ".spawnblacklist");

		if (stringblacklist == null) stringblacklist = Arrays.asList(new String[]{"8","9","10","11","18","51","81"});

		for(String item : stringblacklist){
			try {
				blacklist.add(Integer.valueOf(item));
			} catch (NumberFormatException e) {}
		} 

		int xmin = yamlHandler.worlds.getInt(worldName +".spawnarea.x-min", -100);
		int xmax = yamlHandler.worlds.getInt(worldName +".spawnarea.x-max", 100);
		int zmin = yamlHandler.worlds.getInt(worldName +".spawnarea.z-min", -100);
		int zmax = yamlHandler.worlds.getInt(worldName +".spawnarea.z-max", 100);
		// Spawn area thickness near border. If 0 spawns whole area
		int thickness = yamlHandler.worlds.getInt(worldName +".spawnarea.thickness", 0);

		String type = yamlHandler.worlds.getString(worldName +".spawnarea.type", "square");

		int xrand = 0;
		int zrand = 0;
		int y = -1;
		
		if(type.equalsIgnoreCase("circle")){

			int xcenter = (xmax - xmin)/2 + xmin;
			int zcenter = (zmax - zmin)/2 + xmin;

			do {

				int r = (int) (Math.random()*( (xmax - thickness) - xcenter + 1) );
				double phi = Math.random() * 2 * Math.PI;

				xrand = (int) (xcenter + Math.cos(phi) * r);
				zrand = (int) (zcenter + Math.sin(phi) * r);

				y = getValidHighestY(world, xrand, zrand, blacklist);

			} while (y == -1);


		}else{

			if(thickness <= 0){

				do {

					xrand = xmin + (int) ( Math.random()*(xmax - xmin + 1) );
					zrand = zmin + (int) ( Math.random()*(zmax - zmin + 1) );

					y = getValidHighestY(world, xrand, zrand, blacklist);

				} while (y == -1);

			}else {

				do {

					int side = (int) (Math.random() * 4d);
					int borderOffset = (int) (Math.random() * (double) thickness);
					if (side == 0) {
						xrand = xmin + borderOffset;
						// Also balancing probability considering thickness
						zrand = zmin + (int) ( Math.random() * (zmax - zmin + 1 - 2*thickness) ) + thickness;
					}
					else if (side == 1) {
						xrand = xmax - borderOffset;
						zrand = zmin + (int) ( Math.random() * (zmax - zmin + 1 - 2*thickness) ) + thickness;
					}
					else if (side == 2) {
						xrand = xmin + (int) ( Math.random() * (xmax - xmin + 1) );
						zrand = zmin + borderOffset;
					}
					else {
						xrand = xmin + (int) ( Math.random() * (xmax - xmin + 1) );
						zrand = zmax - borderOffset;
					}

					y = getValidHighestY(world, xrand, zrand, blacklist);

				} while (y == -1);
				
			}
		}

		return new Location(
				world,
				Double.parseDouble(Integer.toString(xrand) + 0.5),
				Double.parseDouble(Integer.toString(y)),
				Double.parseDouble(Integer.toString(zrand) + 0.5)
				);
	}

	private int getValidHighestY(World world, int x, int z, List<Integer> blacklist) {
		world.getChunkAt(new Location(world, x, 0, z)).load();

		int y = 0;
		int blockid = 0;

		if(world.getEnvironment().equals(Environment.NETHER)){
			int blockYid = world.getBlockTypeIdAt(x, y, z);
			int blockY2id = world.getBlockTypeIdAt(x, y+1, z);			
			while(y < 128 && !(blockYid == 0 && blockY2id == 0)){				
				y++;
				blockYid = blockY2id;
				blockY2id = world.getBlockTypeIdAt(x, y+1, z);
			}
			if(y == 127) return -1;
		}else{
			y = 257;
			while(y >= 0 && blockid == 0){
				y--;
				blockid = world.getBlockTypeIdAt(x, y, z);
			}
			if(y == 0) return -1;
		}

		if (blacklist.contains(blockid)) return -1;
		if (blacklist.contains(81) && world.getBlockTypeIdAt(x, y + 1, z) == 81) return -1; // Check for cacti

		return y;
	}

	// Methods for a save landing :)

	public void sendGround(Player player, Location location){

		location.getChunk().load();

		World world = location.getWorld();

		for(int y = 0 ; y <= location.getBlockY() + 2; y++){
			Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
			player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
		}

	}
}
