package me.josvth.randomspawn;

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

import me.josvth.randomspawn.handlers.CommandHandler;
import me.josvth.randomspawn.handlers.YamlHandler;
import me.josvth.randomspawn.listeners.*;

public class RandomSpawn extends JavaPlugin{

	public YamlHandler yamlHandler;
	CommandHandler commandHandler;

	RespawnListener respawnListener;
	JoinListener joinListener;
	WorldChangeListener worldChangeListener;
	SignListener signListener;
	DamageListener damageListener;

	@Override
	public void onEnable() {

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
		getLogger().info(message);
	}

	public void logDebug(String message){
		if (yamlHandler.config.getBoolean("debug",false)) { getLogger().info("(DEBUG) " + message); }
	}

	public void logWarning(String message){
		getLogger().warning(message);
	}

	public void playerInfo(Player player, String message){
		player.sendMessage(ChatColor.AQUA + "[RandomSpawn] " + ChatColor.RESET + message);
	}

	// *------------------------------------------------------------------------------------------------------------*
	// | The following chooseSpawn method contains code made by NuclearW                                            |
	// | based on his SpawnArea plugin:                                                                             |
	// | http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/ |
	// *------------------------------------------------------------------------------------------------------------*

	public Location chooseSpawn(World world){

		String worldName = world.getName();

		// I don't like this method
		List<Integer> blacklist = new ArrayList<Integer>();
		
		if( yamlHandler.worlds.contains( worldName + ".spawnblacklist") )
			blacklist = yamlHandler.worlds.getIntegerList(worldName + ".spawnblacklist");
		else
			blacklist = Arrays.asList(new Integer[]{8,9,10,11,18,51,81});			
	
		double xmin = yamlHandler.worlds.getDouble(worldName +".spawnarea.x-min", -100);
		double xmax = yamlHandler.worlds.getDouble(worldName +".spawnarea.x-max", 100);
		double zmin = yamlHandler.worlds.getDouble(worldName +".spawnarea.z-min", -100);
		double zmax = yamlHandler.worlds.getDouble(worldName +".spawnarea.z-max", 100);
				
		// Spawn area thickness near border. If 0 spawns whole area
		int thickness = yamlHandler.worlds.getInt(worldName +".spawnarea.thickness", 0);

		String type = yamlHandler.worlds.getString(worldName +".spawnarea.type", "square");
				
		double xrand = 0;
		double zrand = 0;
		double y = -1;
		
		if(type.equalsIgnoreCase("circle")){

			double xcenter = xmin + (xmax - xmin)/2;
			double zcenter = zmin + (zmax - zmin)/2;
			
			do {

				double r = Math.random() * (xmax - xcenter);
				double phi = Math.random() * 2 * Math.PI;

				xrand = xcenter + Math.cos(phi) * r;
				zrand = zcenter + Math.sin(phi) * r;

				y = getValidHighestY(world, xrand, zrand, blacklist);
								
			} while (y == -1);


		} else {

			if(thickness <= 0){

				do {
					
					xrand = xmin + Math.random()*(xmax - xmin + 1);
					zrand = zmin + Math.random()*(zmax - zmin + 1);

					y = getValidHighestY(world, xrand, zrand, blacklist);

				} while (y == -1);

			}else {

				do {
					
					int side = (int) (Math.random() * 4d);
					double borderOffset = Math.random() * (double) thickness;
					if (side == 0) {
						xrand = xmin + borderOffset;
						// Also balancing probability considering thickness
						zrand = zmin + Math.random() * (zmax - zmin + 1 - 2*thickness) + thickness;
					}
					else if (side == 1) {
						xrand = xmax - borderOffset;
						zrand = zmin + Math.random() * (zmax - zmin + 1 - 2*thickness) + thickness;
					}
					else if (side == 2) {
						xrand = xmin + Math.random() * (xmax - xmin + 1);
						zrand = zmin + borderOffset;
					}
					else {
						xrand = xmin + Math.random() * (xmax - xmin + 1);
						zrand = zmax - borderOffset;
					}

					y = getValidHighestY(world, xrand, zrand, blacklist);

				} while (y == -1);
				
			}
		}
	
		Location location = new Location(world, xrand, y, zrand);
				
		return location;
	}

	private double getValidHighestY(World world, double x, double z, List<Integer> blacklist) {
		
		world.getChunkAt(new Location(world, x, 0, z)).load();

		double y = 0;
		int blockid = 0;

		if(world.getEnvironment().equals(Environment.NETHER)){
			int blockYid = world.getBlockTypeIdAt((int) x, (int) y, (int) z);
			int blockY2id = world.getBlockTypeIdAt((int) x, (int) (y+1), (int) z);
			while(y < 128 && !(blockYid == 0 && blockY2id == 0)){				
				y++;
				blockYid = blockY2id;
				blockY2id = world.getBlockTypeIdAt((int) x, (int) (y+1), (int) z);
			}
			if(y == 127) return -1;
		}else{
			y = 257;
			while(y >= 0 && blockid == 0){
				y--;
				blockid = world.getBlockTypeIdAt((int) x, (int) y, (int) z);
			}
			if(y == 0) return -1;
		}

		if (blacklist.contains(blockid)) return -1;
		if (blacklist.contains(81) && world.getBlockTypeIdAt((int) x, (int) (y+1), (int) z) == 81) return -1; // Check for cacti

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
