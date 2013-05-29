package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;
import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;

public class PlantChunk {
	RealisticBiomes plugin;
	
	HashMap<Coords,Plant> plants;
	
	// index of this chunk in the database
	long index;
	
	boolean loaded;
	boolean inDatabase;
	
	private static PreparedStatement deleteOldDataStmt = null;
	private static PreparedStatement loadPlantsStmt = null;
	private static PreparedStatement deleteChunkStmt = null;
	private static PreparedStatement addChunkStmt = null;
	private static PreparedStatement savePlantsStmt = null;
	private static PreparedStatement getLastChunkIdStmt = null;
	
	public PlantChunk(RealisticBiomes plugin, Connection readConn, Connection writeConn, long index) {
		this.plugin = plugin;
		plants = null;
		this.index = index;
		
		this.loaded = false;
		this.inDatabase = false;

		if (deleteOldDataStmt == null) {
			try {
			deleteOldDataStmt = writeConn.prepareStatement("DELETE FROM plant WHERE chunkid = ?1");
			
			loadPlantsStmt = readConn.prepareStatement("SELECT w, x, y, z, date, growth FROM plant WHERE chunkid = ?1");
			
			addChunkStmt = writeConn.prepareStatement("INSERT INTO chunk (w, x, z) VALUES (?, ?, ?)");
			getLastChunkIdStmt = writeConn.prepareStatement("SELECT last_insert_rowid()");	
			
			savePlantsStmt = writeConn.prepareStatement("INSERT INTO plant (chunkid, w, x, y, z, date, growth) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)");
			
			deleteChunkStmt = writeConn.prepareStatement("DELETE FROM chunk WHERE id = ?1");
			} catch (SQLException e) {
				throw new DataSourceException("Failed to create the prepared statements in PlantChunk", e);
			}
		}
	}
	
	///-------------------------
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public int getPlantCount() {
		return plants.keySet().size();
	}
	
	///-------------------------
	
	public void remove(Coords coords) {
		if (!loaded)
			return;
		
		plants.remove(coords);
	}
	
	public void add(Coords coords, Plant plant) {
		if (!loaded) {
			plants = new HashMap<Coords, Plant>();
			loaded = true;
		}
		
		plants.put(coords, plant);
	}
	
	public Plant get(Coords coords) {
		if (!loaded)
			return null;
		
		return plants.get(coords);
	}

	public boolean load(Coords coords) {
		// if the data is being loaded, it is known that this chunk is in the database
		inDatabase = true;
		
		if (loaded)
			return true;
		
		World world = plugin.getServer().getWorld(WorldID.getMCID(coords.w));
		
		plants = new HashMap<Coords, Plant>();
		
		try {
			loadPlantsStmt.setLong(1, index);
			loadPlantsStmt.execute();
			ResultSet rs = loadPlantsStmt.getResultSet();
			while (rs.next()) {
				int w = rs.getInt("w");
				int x = rs.getInt("x");
				int y = rs.getInt("y");
				int z = rs.getInt("z");
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
		}
		catch (SQLException e) {
			throw new DataSourceException(String.format("Failed to execute/load the data from the plants table (In PlantChunk) with chunkId %s, coords %s", index, coords), e); 
		}
		
		loaded = true;
		return true;
	}
	
	public void unload(Coords chunkCoords) {
		if (!loaded)
			return;
		
		try {
			// if this chunk was not in the database, then add it to the database
			if (!inDatabase) {
				addChunkStmt.setInt(1, chunkCoords.w);
				addChunkStmt.setInt(2, chunkCoords.x);
				addChunkStmt.setInt(3, chunkCoords.z);
				addChunkStmt.execute();
				getLastChunkIdStmt.execute();
				ResultSet rs = getLastChunkIdStmt.getResultSet();
				index = rs.getLong(1);
				
				inDatabase = true;
			}
			
			// first, delete the old data
			deleteOldDataStmt.setLong(1, index);
			deleteOldDataStmt.execute();
			
			// then replace it with all the recorded plants in this chunk
			if (!plants.isEmpty()) {
				for (Coords coords: plants.keySet()) {
					Plant plant = plants.get(coords);
					float growth = plant.getGrowth();
					
					if (growth > 0.0) {
						savePlantsStmt.setLong(1, index);
						savePlantsStmt.setInt(2, coords.w);
						savePlantsStmt.setInt(3, coords.x);
						savePlantsStmt.setInt(4, coords.y);
						savePlantsStmt.setInt(5, coords.z);
						savePlantsStmt.setLong(6, plant.getUpdateTime());
						savePlantsStmt.setFloat(7, plant.getGrowth());
						
						savePlantsStmt.execute();
					}
				}
			}
			else {
				// otherwise just delete the chunk entirely
				deleteChunkStmt.setLong(1, index);
				deleteChunkStmt.execute();
			}
		}
		catch (SQLException e) {
			throw new DataSourceException(String.format("Failed to unload the chunk (In PlantChunk), index %s, coords %s",  index, chunkCoords), e);
		}
		
		plants = null;
		loaded = false;
	}
}
