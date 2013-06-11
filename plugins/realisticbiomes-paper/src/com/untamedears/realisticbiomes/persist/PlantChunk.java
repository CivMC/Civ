package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;

public class PlantChunk {
	RealisticBiomes plugin;
	
	HashMap<Coords,Plant> plants;
	
	// index of this chunk in the database
	long index;
	
	boolean loaded;
	boolean inDatabase;
	
	public PlantChunk(RealisticBiomes plugin, Connection readConn, long index) {
		this.plugin = plugin;
		plants = null;
		this.index = index;
		
		this.loaded = false;
		this.inDatabase = false;
	}
	
	///-------------------------
	
	public synchronized boolean isLoaded() {
		return loaded;
	}
	
	public synchronized int getPlantCount() {
		return plants.keySet().size();
	}
	
	///-------------------------
	
	public synchronized void remove(Coords coords) {
		if (!loaded)
			return;
		
		plants.remove(coords);
	}
	
	public synchronized void add(Coords coords, Plant plant, Connection writeConn) {
		if (!loaded) {
			if (!inDatabase)
				plants = new HashMap<Coords, Plant>();
			else
				load(coords, writeConn);
			
			loaded = true;
		}
		
		plants.put(coords, plant);
	}
	
	public synchronized Plant get(Coords coords) {
		if (!loaded)
			return null;
		
		return plants.get(coords);
	}

	public synchronized boolean load(Coords coords, Connection readConn) {
		// if the data is being loaded, it is known that this chunk is in the database
		inDatabase = true;
		
		if (loaded)
			return true;
		
		World world = plugin.getServer().getWorld(WorldID.getMCID(coords.w));
		
		plants = new HashMap<Coords, Plant>();

		
		try {
			PreparedStatement loadPlantsStmt = readConn.prepareStatement("SELECT w, x, y, z, date, growth FROM plant WHERE chunkid = ?1"); 
			
			loadPlantsStmt.setLong(1, index);
			loadPlantsStmt.execute();
			
			ResultSet rs = loadPlantsStmt.getResultSet();
			while (rs.next()) {
				int w = rs.getInt(1);
				int x = rs.getInt(2);
				int y = rs.getInt(3);
				int z = rs.getInt(4);
				long date = rs.getLong(5);
				float growth = rs.getFloat(6);
				
				// if the plant does not correspond to an actual crop, don't load it
				if (!plugin.getGrowthConfigs().containsKey(world.getBlockAt(x, y, z).getType())) {
					continue;
				}
				
				Plant plant = new Plant(date,growth);
					
				// grow the block
				Block block = world.getBlockAt(x, y, z);
				GrowthConfig growthConfig = plugin.getGrowthConfigs().get(block.getType());
				double growthAmount = growthConfig.getRate(block) * plant.setUpdateTime(System.currentTimeMillis());
				plant.addGrowth((float)growthAmount);
				
				// and update the plant growth
				plugin.getBlockGrower().growBlock(block,coords,plant.getGrowth());
				
				// if the plant isn't finished growing, add it to the 
				// plants
				if (!(plant.getGrowth() >= 1.0)) {
					plants.put(new Coords(w,x,y,z), plant);
				}
			}
			
			loadPlantsStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		loaded = true;
		return true;
	}
	
	public synchronized void unload(Coords chunkCoords, ChunkWriter writeStmts) {
		if (!loaded)
			return;
		
		try {
			// if this chunk was not in the database, then add it to the database
			if (!inDatabase) {
				writeStmts.addChunkStmt.setInt(1, chunkCoords.w);
				writeStmts.addChunkStmt.setInt(2, chunkCoords.x);
				writeStmts.addChunkStmt.setInt(3, chunkCoords.z);
				writeStmts.addChunkStmt.execute();
				writeStmts.getLastChunkIdStmt.execute();
				ResultSet rs = writeStmts.getLastChunkIdStmt.getResultSet();
				index = rs.getLong(1);
				
				inDatabase = true;
			}
			
			// first, delete the old data
			writeStmts.deleteOldDataStmt.setLong(1, index);
			writeStmts.deleteOldDataStmt.execute();
			
			// then replace it with all the recorded plants in this chunk
			if (!plants.isEmpty()) {
				for (Coords coords: plants.keySet()) {
					Plant plant = plants.get(coords);
					
					writeStmts.savePlantsStmt.setLong(1, index);
					writeStmts.savePlantsStmt.setInt(2, coords.w);
					writeStmts.savePlantsStmt.setInt(3, coords.x);
					writeStmts.savePlantsStmt.setInt(4, coords.y);
					writeStmts.savePlantsStmt.setInt(5, coords.z);
					writeStmts.savePlantsStmt.setLong(6, plant.getUpdateTime());
					writeStmts.savePlantsStmt.setFloat(7, plant.getGrowth());
					
					writeStmts.savePlantsStmt.execute();
				}
			}
			else {
				// otherwise just delete the chunk entirely
				writeStmts.deleteChunkStmt.setLong(1, index);
				writeStmts.deleteChunkStmt.execute();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		plants = null;
		loaded = false;
	}
}
