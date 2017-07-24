package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.DropGrouper;
import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.utils.MaterialAliases;

public class PlantChunk {
	private final RealisticBiomes plugin;
	private final ChunkCoords coords;
	// index of this chunk in the database
	private long index;
	
	private HashMap<Coords, Plant> plants;

	boolean loaded;
	boolean inDatabase;

	public PlantChunk(RealisticBiomes plugin, long index, ChunkCoords coords) {
		this.plugin = plugin;
		plants = new HashMap<Coords, Plant>();
		this.index = index;
		this.coords = coords;

		this.loaded = false;
		this.inDatabase = false;
		RealisticBiomes.doLog(Level.FINER,"PlantChunk() called with coords: " + coords);
	}

	/**
	 * tostring override
	 */
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("<PlantChunk: index: " + index);
		sb.append(" loaded: " + loaded);
		sb.append(" inDatabase: " + inDatabase);
		sb.append(" plants: \n");

		if (this.plants != null) {
			for (Coords iterCoords : plants.keySet()) {

				sb.append("\tPlantHashmapEntry[coords: " + iterCoords);
				sb.append(" = plant: " + plants.get(iterCoords));
				sb.append(" ] \n");

			}
		}

		sb.append(" > ");
		return sb.toString();
	}
	
	
	public ChunkCoords getChunkCoord() {
		return coords;
	}

	// /-------------------------

	public synchronized boolean isLoaded() {
		return loaded;
	}

	public synchronized int getPlantCount() {

		if (plants == null) {
			RealisticBiomes.LOG
					.severe("PLANTS HASHMAP IS NULL, THIS SHOULD NOT HAPPEN");
			return 0;
		}
		return plants.keySet().size();
	}

	// /-------------------------

	public synchronized void remove(Coords coords) {
		if (!loaded)
			return;

		RealisticBiomes.doLog(Level.FINER,"plantchunk.remove(): called with coords: " + coords);
		
		plants.remove(coords);
	}

	public synchronized void addPlant(Coords coords, Plant plant) {

		RealisticBiomes.doLog(Level.FINER,"plantchunk.add(): called with coords: "
				+ coords + " and plant " + plant);
		RealisticBiomes.doLog(Level.FINER, "plantchunk.add(): is loaded? " + loaded);
		if (!loaded) {

			load();

			loaded = true;
		}

		plants.put(coords, plant);
	}

	public synchronized Plant get(Coords coords) {
		if (!loaded) {
			RealisticBiomes.doLog(Level.FINER,"Plantchunk.get(): returning null cause not loaded");
			return null;
		}
		return plants.get(coords);
	}
	
	public synchronized Set<Coords> getPlantCoords() {
		return plants.keySet();
	}
	
	public synchronized boolean load() {
		// wrapper.
		try { 
			return innerLoad();
		} catch (RuntimeException dse) {
			// assume DB has gone away, reconnect and try one more time.
			RealisticBiomes.LOG.log(Level.WARNING, "Looks like DB has gone away: ", dse);			
		}
		
		// if we are here, had failure -- one retry, then bail
		try {
			return innerLoad();
		} catch(RuntimeException dse) {
			RealisticBiomes.LOG.log(Level.WARNING, "DB really has gone away: ", dse);
			throw dse;
		}
	}

	/**
	 * Loads the plants from the database into this PlantChunk object.
	 * 
	 * @param coords
	 * @return
	 */
	private boolean innerLoad() {
		// if the data is being loaded, it is known that this chunk is in the
		// database

		RealisticBiomes.doLog(Level.FINER, 
				"Plantchunk.load() called with coords: " + coords);

		if (loaded) {
			return true;
		}

		World world = plugin.getServer().getWorld(WorldID.getMCID(coords.w));

		DropGrouper dropGrouper = new DropGrouper(world);

		// execute the load plant statement
		try (Connection connection = plugin.getPlantManager().getDb().getConnection();
				PreparedStatement loadPlantsStmt = connection.prepareStatement(Database.loadPlantsStmt);){

			loadPlantsStmt.setLong(1, index);
			RealisticBiomes.doLog(Level.FINER, "PlantChunk.load() executing sql query: " + Database.loadPlantsStmt);
			
			try (ResultSet rs = loadPlantsStmt.executeQuery()) {
				while (rs.next()) {
					int w = rs.getInt("w");
					int x = rs.getInt("x");
					int y = rs.getInt("y");
					int z = rs.getInt("z");
					long date = rs.getLong(5);
					float growth = rs.getFloat(6);
					float fruitGrowth = rs.getFloat(7);
	
					RealisticBiomes.doLog(Level.FINEST, String
									.format("PlantChunk.load(): got result: w:%s x:%s y:%s z:%s date:%s growth:%s",
											w, x, y, z, date, growth));
	
					// if the plant does not correspond to an actual crop, don't load it
					if (MaterialAliases.getConfig(plugin.materialGrowth, world.getBlockAt(x, y, z)) == null) {
						RealisticBiomes.doLog(Level.FINER, "Plantchunk.load(): plant we got from db doesn't correspond to an actual crop, not loading");
						continue;
					}
	
					Plant plant = new Plant(date, growth, fruitGrowth);
	
					Block block = world.getBlockAt(x, y, z);
					GrowthConfig growthConfig = MaterialAliases.getConfig(plugin.materialGrowth, block);
					if (growthConfig.isPersistent()) {
						plugin.growPlant(plant, block, growthConfig, null, dropGrouper);
					}
	
					// if the plant isn't finished growing, add it to the plants
					if (!plant.isFullyGrown()) {
						plants.put(new Coords(w, x, y, z), plant);
						RealisticBiomes.doLog(Level.FINER, "PlantChunk.load(): plant not finished growing, adding to plants list");
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(
					String.format(
							"Failed to execute/load the data from the plants table (In PlantChunk.load()) with chunkId %s, coords %s",
							index, coords), e);
		}

		dropGrouper.done();
		
		// TODO: this always returns true...refactor that!
		loaded = true;
		return true;
	}

	public synchronized void unload() {
		// wrapper.
		try { 
			innerUnload();
			return;
		} catch (RuntimeException dse) {
			// assume DB has gone away, reconnect and try one more time.
			RealisticBiomes.LOG.log(Level.WARNING, "Looks like DB has gone away: ", dse);			
		}
		
		// if we are here, had failure -- do one retry
		try {
			innerUnload();
		} catch(RuntimeException dse) {
			RealisticBiomes.LOG.log(Level.WARNING, "DB really has gone away: ", dse);
			throw dse;
		}
	}

	
	/**
	 * unloads the plant chunk, and saves it to the database.
	 */
	private void innerUnload() {

		RealisticBiomes.doLog(Level.FINEST,"PlantChunk.innerUnload(): called with coords "
				+ coords + "plantchunk object: " + this);
		
		if (!loaded) {
			RealisticBiomes.doLog(Level.FINEST, "Plantchunk.innerUnload(): not loaded so returning");
			return;
		}

		try {
			// if this chunk was not in the database, then add it to the
			// database
			RealisticBiomes.doLog(Level.FINEST,"PlantChunk.innerUnload(): is inDatabase?: " + inDatabase);
			if (!inDatabase) {
				RealisticBiomes.doLog(Level.FINEST, "  not in database, adding new chunk");
				try (Connection connection = plugin.getPlantManager().getDb().getConnection();
						PreparedStatement addChunkStmt = connection.prepareStatement(Database.addChunkStmt, Statement.RETURN_GENERATED_KEYS);) {
					addChunkStmt.setInt(1, coords.w);
					addChunkStmt.setInt(2, coords.x);
					addChunkStmt.setInt(3, coords.z);
					addChunkStmt.execute();
					try (ResultSet rs = addChunkStmt.getGeneratedKeys()) {
						// need to call rs.next() to get the first result, and make sure
						// we get the index, and throw an exceptionif we don't
						if (rs.next()) {
							index = rs.getLong(1);
							RealisticBiomes.doLog(Level.FINEST, "plantchunk.innerUnload(): got new autoincrement index, it is now "
											+ index);
						} else {
							throw new RuntimeException(
									"Trying to add the chunk to the database, but was unable to get "
											+ "the last inserted statement to get the index");
						}
					}
				}
				inDatabase = true;
			}
		} catch (SQLException e) {
			throw new RuntimeException(
					String.format(
							"Failed to unload the chunk (In PlantChunk, adding chunk to db if needed), index %s, coords %s, PlantChunk obj: %s",
							index, coords, this), e);
		}

		try {
			// put all the plants into the database
			// if we are already unloaded then don't do anything
			if (loaded) {
				if (!plants.isEmpty()) {
					try (Connection connection = plugin.getPlantManager().getDb().getConnection();){
						connection.setAutoCommit(false);

						// delete plants in the database for this chunk and re-add them
						// this is OK because rb_plant does not have a autoincrement index
						// so it won't explode. However, does this have a negative performance impact?
						// TODO: add listener for block break event, and if its a plant, we remove it
						// from the correct plantchunk? Right now if a plant gets destroyed before
						// it is fully grown then it won't get remove from the database
						try (PreparedStatement deleteOldPlantsStmt = connection.prepareStatement(Database.deleteOldPlantsStmt);) {
							deleteOldPlantsStmt.setLong(1, index);
							deleteOldPlantsStmt.execute();
						}

						int coordCounter = 0;
						boolean needToExec = false;
						
						try (PreparedStatement addPlantStmt = connection.prepareStatement(Database.addPlantStmt);) {
							RealisticBiomes.doLog(Level.FINEST, "PlantChunk.unload(): Unloading plantchunk with index: " + this.index);
							for (Coords coords : plants.keySet()) {
								if (!needToExec) {
									needToExec = true;
								}
							
								Plant plant = plants.get(coords);
		
								addPlantStmt.clearParameters();
								addPlantStmt.setLong(1, index);
								addPlantStmt.setInt(2, coords.w);
								addPlantStmt.setInt(3, coords.x);
								addPlantStmt.setInt(4, coords.y);
								addPlantStmt.setInt(5, coords.z);
								addPlantStmt.setLong(6, plant.getUpdateTime());
								addPlantStmt.setFloat(7, plant.getGrowth());
								addPlantStmt.setFloat(8, plant.getFruitGrowth());
								
								addPlantStmt.addBatch();
								
								// execute the statement if we hit 100 batches -- most connections limit to 100 in a batch
								if ((++coordCounter) % 100 == 0) {
									
									addPlantStmt.executeBatch();
									coordCounter = 0;
									needToExec = false;
								}
								
							} // end for
							
							// if we have left over statements afterwards, execute them
							if (needToExec) {
								addPlantStmt.executeBatch();
							}
						}

						connection.commit();
						connection.setAutoCommit(true);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(
					String.format("Failed to unload the chunk (In PlantChunk, "
							+ "replacing with new data/deleting), index %s, coords %s, PlantChunk obj: %s",
							index, coords, this), e);
		}

		// only set loaded to false and reset the plants HashMap
		// only if we are not caching the entire database
		if (!this.plugin.persistConfig.cacheEntireDatabase) {
			RealisticBiomes.doLog(Level.FINER, String.format("PlantChunk.unload(): clearing hashmap for chunk at %s", coords));
			plants = new HashMap<Coords, Plant>();
			loaded = false;
		} 
	}

}
