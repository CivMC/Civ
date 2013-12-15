package com.untamedears.realisticbiomes.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.block.Block;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;
import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;

public class PlantChunk {
	RealisticBiomes plugin;

	HashMap<Coords, Plant> plants;
	
	// a list of what plants need to be 'updated' in the database
	ArrayList<Coords> updatedPlants;

	// index of this chunk in the database
	long index;

	boolean loaded;
	boolean inDatabase;

	public PlantChunk(RealisticBiomes plugin, Connection readConn, long index) {
		this.plugin = plugin;
		plants = new HashMap<Coords, Plant>();
		this.index = index;

		this.loaded = false;
		this.inDatabase = false; // if we are already in the database, PlantManager sets this to true

		this.updatedPlants = new ArrayList<Coords>();
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

		plants.remove(coords);
	}

	public synchronized void add(Coords coords, Plant plant,
			Connection writeConn) {

		RealisticBiomes.doLog(Level.FINER,"plantchunk.add(): called with coords: "
				+ coords + " and plant " + plant);
		RealisticBiomes.doLog(Level.FINER, "plantchunk.add(): is loaded? " + loaded);
		if (!loaded) {

			load(coords, writeConn);

			loaded = true;
		}

		this.updatedPlants.add(coords);
		plants.put(coords, plant);
	}

	public synchronized Plant get(Coords coords) {
		if (!loaded) {
			RealisticBiomes.doLog(Level.FINER,"Plantchunk.get(): returning null cause not loaded");
			return null;
		}
		return plants.get(coords);
	}

	/**
	 * Loads the plants from the database into this PlantChunk object.
	 * 
	 * @param coords
	 * @param readConn
	 * @return
	 */
	public synchronized boolean load(Coords coords, Connection readConn) {
		// if the data is being loaded, it is known that this chunk is in the
		// database

		// TODO: plant chunk objects need to know their own coordinates, we
		// should
		// not be passing them in to load / unload!

		RealisticBiomes.doLog(Level.FINER, 
				"Plantchunk.load() called with coords: " + coords);

		if (loaded) {
			RealisticBiomes.doLog(Level.FINER, "Plantchunk.load(): plant chunk is already loaded, returning true");
			return true;
		}

		World world = plugin.getServer().getWorld(WorldID.getMCID(coords.w));


		// execute the load plant statement
		try {

			ChunkWriter.loadPlantsStmt.setLong(1, index);
			RealisticBiomes.doLog(Level.FINER,
					"PlantChunk.load() executing sql query: "
							+ ChunkWriter.loadPlantsStmt.toString());
			ChunkWriter.loadPlantsStmt.execute();

			ResultSet rs = ChunkWriter.loadPlantsStmt.getResultSet();
			while (rs.next()) {
				int w = rs.getInt("w");
				int x = rs.getInt("x");
				int y = rs.getInt("y");
				int z = rs.getInt("z");
				long date = rs.getLong(5);
				float growth = rs.getFloat(6);

				RealisticBiomes.doLog(Level.FINEST, String
								.format("PlantChunk.load(): got result: w:%s x:%s y:%s z:%s date:%s growth:%s",
										w, x, y, z, date, growth));

				// if the plant does not correspond to an actual crop, don't
				// load it
				if (!plugin.getGrowthConfigs().containsKey(
						world.getBlockAt(x, y, z).getType())) {
					RealisticBiomes.doLog(Level.FINER, "Plantchunk.load(): plant we got from db doesn't correspond to an actual crop, not loading");
					continue;
				}

				Plant plant = new Plant(date, growth);

				// TODO MARK: this code seems very similar to
				// RealisticBiomes.growAndPersistBlock()
				// grow the block
				Block block = world.getBlockAt(x, y, z);
				GrowthConfig growthConfig = plugin.getGrowthConfigs().get(
						block.getType());
				double growthAmount = growthConfig.getRate(block)
						* plant.setUpdateTime(System.currentTimeMillis() / 1000L);
				plant.addGrowth((float) growthAmount);

				// and update the plant growth
				plugin.getBlockGrower().growBlock(block, coords,
						plant.getGrowth());

				// if the plant isn't finished growing, add it to the
				// plants
				// TODO MARK: should we mark this crop for removal if it is fully grown / equal to 1.0?
				if (!(plant.getGrowth() >= 1.0)) {
					plants.put(new Coords(w, x, y, z), plant);
					RealisticBiomes.doLog(Level.FINER, "PlantChunk.load(): plant not finished growing, adding to plants list");
				}

				// END MARK TODO
			}
		} catch (SQLException e) {
			throw new DataSourceException(
					String.format(
							"Failed to execute/load the data from the plants table (In PlantChunk.load()) with chunkId %s, coords %s",
							index, coords), e);
		}

		// TODO: this always returns true...refactor that!
		loaded = true;
		return true;
	}

	/**
	 * unloads the plant chunk, and saves it to the database.
	 * 
	 * Note that this is called by PlantManager.saveAllAndStop(), so that method
	 * takes care of setting autocommit to false/true and actually committing to
	 * the database
	 * 
	 * @param chunkCoords - the coordinates that this plant chunk belongs to
	 * @param writeStmts
	 */
	public synchronized void unload(Coords chunkCoords) {

		boolean isNewChunk = false;
		RealisticBiomes.doLog(Level.FINEST,"PlantChunk.unload(): called with coords "
				+ chunkCoords + "plantchunk object: " + this);
		// TODO: plant chunk objects need to know their own coordinates, we
		// should
		// not be passing them in to load / unload!
		if (!loaded) {
			RealisticBiomes.doLog(Level.FINER, "Plantchunk.unload(): not loaded so returning");
			return;
		}

		try {
			// if this chunk was not in the database, then add it to the
			// database
			RealisticBiomes.doLog(Level.FINER,"PlantChunk.unload(): is inDatabase?: "
					+ inDatabase);
			if (!inDatabase) {
				
				isNewChunk = true;

				RealisticBiomes.doLog(Level.FINER, "not in database, adding new chunk");
				ChunkWriter.addChunkStmt.setInt(1, chunkCoords.w);
				ChunkWriter.addChunkStmt.setInt(2, chunkCoords.x);
				ChunkWriter.addChunkStmt.setInt(3, chunkCoords.z);
				ChunkWriter.addChunkStmt.execute();
				ChunkWriter.getLastChunkIdStmt.execute();
				ResultSet rs = ChunkWriter.getLastChunkIdStmt.getResultSet();

				// need to call rs.next() to get the first result, and make sure
				// we get the index, and throw an exception
				// if we don't
				if (rs.next()) {
					index = rs.getLong(1);
					RealisticBiomes.doLog(Level.FINER, "plantchunk.unload(): got new autoincrement index, it is now "
									+ index);
				} else {
					throw new DataSourceException(
							"Trying to add the chunk to the database, but was unable to get "
									+ "the last inserted statement to get the index");
				}

				// make sure to commit this newly added chunk, or else when we
				// add plants to the
				// database later in this method we dont get a Constraint
				// Failure exception

				RealisticBiomes.doLog(Level.FINER, "plantchunk.unload(): committing new plantchunk with index "
								+ this.index);
				ChunkWriter.writeConnection.commit();
				inDatabase = true;
			}

		} catch (SQLException e) {

			throw new DataSourceException(
					String.format(
							"Failed to unload the chunk (In PlantChunk, adding chunk to db if needed), index %s, coords %s, PlantChunk obj: %s",
							index, chunkCoords, this), e);
		}

		try {
			// put all the plants into the database
			// if we are already unloaded then don't do anything
			if (loaded) {
				
				if(isNewChunk) {
					unloadAsNewChunk();
					
				} else {
					
					unloadAsExistingChunk();
				}
				
			}
		} catch (SQLException e) {
			throw new DataSourceException(
					String.format(
							"Failed to unload the chunk (In PlantChunk, "
									+ "replacing with new data/deleting), index %s, coords %s, PlantChunk obj: %s",
							index, chunkCoords, this), e);
		}

		plants = new HashMap<Coords, Plant>();
		loaded = false;
	}
	
	/**
	 * Helper method that will unload this Plantchunk as if it was a new chunk in the database
	 * and therefore all plants that are in this Plantchunk need to be inserted into the database. 
	 * 
	 * @throws SQLException
	 */
	private void unloadAsNewChunk() throws SQLException {
		
		if (!plants.isEmpty()) {

			// delete plants in the database for this chunk and re-add them
			// this is OK because rb_plant does not have a autoincrement index
			// so it won't explode. However, does this have a negative performance impact?
			// TODO: add listener for block break event, and if its a plant, we remove it
			// from the correct plantchunk? Right now if a plant gets destroyed before
			// it is fully grown then it won't get remove from the database
			//ChunkWriter.deleteOldPlantsStmt.setLong(1, index);
			//ChunkWriter.deleteOldPlantsStmt.execute();

			int coordCounter = 0;
			boolean needToExec = false;
			
			RealisticBiomes.doLog(Level.FINER, "PlantChunk.unloadAsNewChunk(): Unloading plantchunk with index: " + this.index);
			
			// go through all the plants that this plantchunk contains
			for (Coords coords : plants.keySet()) {
				if (!needToExec) {
					needToExec = true;
				}
				
				Plant plant = plants.get(coords);

				ChunkWriter.addPlantStmt.clearParameters();
				ChunkWriter.addPlantStmt.setLong(1, index);
				ChunkWriter.addPlantStmt.setInt(2, coords.w);
				ChunkWriter.addPlantStmt.setInt(3, coords.x);
				ChunkWriter.addPlantStmt.setInt(4, coords.y);
				ChunkWriter.addPlantStmt.setInt(5, coords.z);
				ChunkWriter.addPlantStmt.setLong(6,
						plant.getUpdateTime());
				ChunkWriter.addPlantStmt.setFloat(7,
						plant.getGrowth());
				
				ChunkWriter.addPlantStmt.addBatch();
				
				
				// execute the statement if we hit 1000 batches
				if ((coordCounter + 1) % 1000 == 0) {
					
					ChunkWriter.addPlantStmt.executeBatch();
					coordCounter = 0;
					needToExec = false;
				}
				
			} // end for
			
			// if we have left over statements afterwards, execute them
			if (needToExec) {
				ChunkWriter.addPlantStmt.executeBatch();
			}
			
		} 
	
	} // end unloadAsNewChunk()
	
	
	/**
	 * Helper method that will unload this Plantchunk as if it was an existing chunk in the database
	 * and only 'updated' plants need to be updated in the database
	 * 
	 * @throws SQLException
	 */
	private void unloadAsExistingChunk () throws SQLException {
		
		int coordCounter = 0;
		boolean needToExec = false;
		
		RealisticBiomes.doLog(Level.FINER, "PlantChunk.unloadAsExistingChunk(): Unloading plantchunk with index: " + this.index);
		
		// go through all the plants that this plantchunk contains
		for (Coords coords : updatedPlants) {
			if (!needToExec) {
				needToExec = true;
			}
			
			Plant plant = plants.get(coords);

			RealisticBiomes.doLog(Level.FINER, String.format("PlantChunk.unloadAsExistingChunk(): \n\tunloading plant in updatedPlants: %s", plants));
		
			// set the parameters for the updatePlantStatement
			
			// the two things we are updating
			ChunkWriter.updatePlantStmt.setLong(1, plant.getUpdateTime());
			ChunkWriter.updatePlantStmt.setFloat(2, plant.getGrowth());
			
			// these are just to get the correct row
			ChunkWriter.updatePlantStmt.setInt(3, coords.w);
			ChunkWriter.updatePlantStmt.setInt(4, coords.x);
			ChunkWriter.updatePlantStmt.setInt(5, coords.y);
			ChunkWriter.updatePlantStmt.setInt(6, coords.z);

			// execute the statement if we hit 1000 batches
			if ((coordCounter + 1) % 1000 == 0) {
				
				ChunkWriter.updatePlantStmt.executeBatch();
				coordCounter = 0;
				needToExec = false;
			}
			
		} // end for
		
		// if we have left over statements afterwards, execute them
		if (needToExec) {
			ChunkWriter.updatePlantStmt.executeBatch();
		}
	}
	

}
