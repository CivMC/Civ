package com.untamedears.realisticbiomes.persist;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;
import com.untamedears.realisticbiomes.PersistConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;

public class PlantManager {
	private RealisticBiomes plugin;
	private PersistConfig config;
	
	// database connection
	private Connection writeConn;
	private Connection readConn;
	
	// map of chunk coordinates to plant chunk data
	// an entry of null denotes a chunk that is in the database but is unloaded
	private HashMap<Coords, PlantChunk> chunks;
	
	// plants chunks to be unloaded in batches
	private ArrayList<Coords> chunksToUnload;
	
	// task that periodically unloads chunks in batches
	private BukkitTask unloadBatchTask;
	
	// database write thread
	ExecutorService writeService;
	private ChunkWriter chunkWriter;
	
	
	// prepared statements
	PreparedStatement makeTableChunk;
	PreparedStatement makeTablePlant;
	PreparedStatement selectAllFromChunk;
	
	private Logger log;
	
	////================================================================================= ////
	
	public PlantManager(RealisticBiomes plugin, PersistConfig config) {
		this.plugin = plugin;
		this.config = config;
		
		chunks = new HashMap<Coords, PlantChunk>();
		
		// open the database
		String sDriverName = "com.mysql.jdbc.Driver";
		try {
			Class.forName(sDriverName);
		} catch (ClassNotFoundException e) {
			throw new DataSourceException("Failed to initalize the " + sDriverName + " driver class!", e);
		}
		
		// TODO: make failures here fail more gracefully....
		String jdbcUrl = "jdbc:mysql://" + config.host + ":" + config.port + "/" + config.databaseName + "?user=" + config.user + "&password=" + config.password;
		int iTimeout = 30;
		
		// Try and connect to the database
		try {
			writeConn = DriverManager.getConnection(jdbcUrl);
			readConn = DriverManager.getConnection(jdbcUrl);
			Statement stmt = readConn.createStatement();
			stmt.setQueryTimeout(iTimeout);
			
		} catch (SQLException e) {
			throw new DataSourceException("Failed to connect to the database with the jdbcUrl: " + jdbcUrl, e);
		}
		
		// Create the prepared statements
		
		try {
			// we need InnoDB storage engine or else we can't do foreign keys!
			this.makeTableChunk = writeConn.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS %s_chunk " +
							"(id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
							"w INTEGER, x INTEGER, z INTEGER," +
							"INDEX chunk_coords_idx (w, x, z)) " +
							"ENGINE INNODB", config.prefix));
			
			// we need InnoDB storage engine or else we can't do foreign keys!
			this.makeTablePlant = writeConn.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS %s_plant" +
							"(chunkId INTEGER, w INTEGER, x INTEGER, y INTEGER, z INTEGER, date INTEGER UNSIGNED, growth REAL, " +
							"INDEX plant_coords_idx (w, x, y, z), INDEX plant_chunk_idx (chunkId), " +
							"CONSTRAINT chunkIdConstraint FOREIGN KEY (chunkId) REFERENCES %s_chunk (id))" +
							"ENGINE INNODB", config.prefix, config.prefix));
			
			
			this.selectAllFromChunk = readConn.prepareStatement(String.format("SELECT id, w, x, z FROM %s_chunk", config.prefix));
			
			
			// create chunk writer
			chunkWriter = new ChunkWriter(writeConn, config);
			
		} catch (SQLException e) {
			throw new DataSourceException("Failed to create the prepared statements! (for table creation)", e);
			
		}
		
		// run the prepared statements that create the tables if they do not exist in the database
		try {
			
			RealisticBiomes.LOG.fine("creating chunk table (if necessary) with prepared statement:" + this.makeTableChunk.toString());
			plugin.getLogger().fine("creating plant table (if necessary) with prepared statement:" + this.makeTablePlant.toString());

			this.makeTableChunk.execute();
			this.makeTablePlant.execute();
						
		} catch (SQLException e) {
			
			throw new DataSourceException("Caught exception when trying to run the " +
					"'create xx_chunk and xx_plant' tables if they don't exist!", e);
		}

		// load all chunks
		
		try {
			ResultSet rs = this.selectAllFromChunk.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				int w = rs.getInt("w");
				int x = rs.getInt("x");
				int z = rs.getInt("z");
				
				PlantChunk pChunk = new PlantChunk(plugin, readConn, id);
				chunks.put(new Coords(w,x,0,z), pChunk);
			}
			
		} catch (SQLException e) {
			throw new DataSourceException("Failed to load all of the chunks from the database! ", e);
		}

			

		
		// create unload batch
		chunksToUnload = new ArrayList<Coords>();
		
		//register the batchTask
		unloadBatchTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
		    @Override  
		    public void run() {
				unloadBatch();
		    }
		}, config.unloadBatchPeriod, config.unloadBatchMaxTime);
		
		writeService = Executors.newSingleThreadExecutor();
		
		log = plugin.getLogger();
	}
	
	// ============================================================================================	
	// a method that is run through a timer, that unloads any chunks that are inside the 'chunksToUnload' list
	// --------------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------------
	private void unloadBatch() {
		// no need to do anything if the queue is empty
		if (chunksToUnload.isEmpty())
			return;
		
		// prepare a single transaction with all inserts
		writeService.submit(new Runnable() {
			public void run() {
				
				long start = System.nanoTime()/1000000/*ns/ms*/;
				long end;
				
				int chunksUnloadedCount = chunksToUnload.size();
				
				try {
					writeConn.setAutoCommit(false);
				} catch (SQLException e) {
					throw new DataSourceException("unable to set autocommit to false in unloadBatch", e);

				}
				
				while (!chunksToUnload.isEmpty()) {
					Coords batchCoords = chunksToUnload.remove(chunksToUnload.size()-1);
					unloadChunk(batchCoords);
				}
				
				// write the changes to the database
				try {
					writeConn.commit();
					writeConn.setAutoCommit(true);
				} catch (SQLException e) {
					throw new DataSourceException("unable to set autocommit to true in unloadBatch", e);
				}
				
				end = System.nanoTime()/1000000/*ns/ms*/;
				
				if (plugin.persistConfig.logDB)
					log.info("Committed data: Unloaded and saved  "+chunksUnloadedCount+" chunks in "+(end-start)+" ms");					
			}
		});
	}
	
	public void saveAllAndStop() {
		writeService.submit(new Runnable() {
			public void run() {
				
				try {
				log.info("Starting runnable in saveAllAndStop()");
				try {
					writeConn.setAutoCommit(false);
				} catch (SQLException e) {
					log.info("Exception in saveAllAndStop runnable!" + e);
					throw new DataSourceException("unable to set autocommit to false in saveAllAndStop", e);
				}				
				
				for (Coords coords:chunks.keySet()) {

					PlantChunk pChunk = chunks.get(coords);

					pChunk.unload(coords, chunkWriter);
				}
				
				try {
					writeConn.commit();
					writeConn.setAutoCommit(true);
				} catch (SQLException e) {
					log.info("Exception in saveAllAndStop runnable!" + e);

					throw new DataSourceException("unable to set autocommit to true in saveAllAndStop", e);
				}	
				
				log.info("finished runnable in saveAllAndStop()");
				} catch (Exception e) {
					
					log.log(Level.SEVERE, "error in run() when shutting down!", e);
				}
			} // end run()
		});
		
		chunksToUnload = null;
		
		unloadBatchTask.cancel();
		writeService.shutdown();
		log.info("seeing if writeservice is finished...");
		while (!writeService.isTerminated()) {
			try {
				log.info("not finished, waiting for 5 sec");
				writeService.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// Keep trying to shut down
			}
		}
		log.info("write service finished");
	}
	
	private void unloadChunk(Coords coords) {
		// if the specified chunk does not exist in the system, or is no longer loaded, nothing needs
		// to be done
		if (!chunks.containsKey(coords) || !chunks.get(coords).isLoaded())
			return;
		
		// if the minecraft chunk is loaded again, then don't unload the pChunk
		if (plugin.getServer().getWorld(WorldID.getMCID(coords.w)).isChunkLoaded(coords.x, coords.z))
			return;
		
		// finally, actually unload this thing
		PlantChunk pChunk = chunks.get(coords);
		
		pChunk.unload(coords, chunkWriter);
	}
	
	// --------------------------------------------------------------------------------------------
	public void minecraftChunkUnloaded(Coords coords) {
		// if the pChunk does not exist, there is nothing to unload
		if (!chunks.containsKey(coords))
			return;
		
		PlantChunk pChunk = chunks.get(coords);
		// if the pChunk is already unloaded then it should stay unloaded -- do nothing
		if (!pChunk.isLoaded())
			return;
		
		chunksToUnload.add(coords);		
	}
	
	////===========================================================================================
	// load the specified chunk, return true if the pChunk is actually loaded
	public boolean loadChunk(Coords coords) {
		// if the specified chunk does not exist, then don't load anything
		if (!chunks.containsKey(coords)) {
			RealisticBiomes.LOG.finer("PlantManager.loadChunk(): returning false as we don't have the plantchunk obj in chunks");
			return false;
		}
		
		PlantChunk pChunk = chunks.get(coords);
		// if the plant chunk is already loaded, then there is no need to load
		if (pChunk.isLoaded()) {
			RealisticBiomes.LOG.finer("PlantManager.loadChunk(): plantChunk already loaded, returning true");
			return true;
		}
		
		// this getWorlds().get(index) could break in the future
		// if the minecraft chunk is unloaded again, then don't load the pChunk
		if (!plugin.getServer().getWorld(WorldID.getMCID(coords.w)).isChunkLoaded(coords.x, coords.z)) {
			RealisticBiomes.LOG.finer("PlantManager.loadChunk(): minecraft chunk was unloaded again... returning false");
			return false;
		}
		
		// finally, just load this thing!
		long start = System.nanoTime()/1000000/*ns/ms*/;
		try {
			readConn.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DataSourceException("unable to set autocommit to false in loadchunk", e);
		}
		boolean loaded = pChunk.load(coords, readConn);
		RealisticBiomes.LOG.finer("PlantManager.loadChunk(): pchunk.load() returned " + loaded);
		try {
			readConn.setAutoCommit(true);
		} catch (SQLException e) {
			throw new DataSourceException("unable to set autocommit to true in loadchunk", e);
		}
		long end = System.nanoTime()/1000000/*ns/ms*/;
		
		if (plugin.persistConfig.logDB) {
			plugin.getLogger().finer("db load chunk["+coords.x+","+coords.z+"]: "+pChunk.getPlantCount()+" entries loaded in "+(end-start)+" ms");
		}
		
		return loaded;
	}
	
	////===========================================================================================
	/**
	 * Adds a specified plant to the correct PlantChunk
	 * This should only be called after we have verified that the plant does not already exist in the same spot
	 * or else this will override the existing plant!!
	 * @param coords - the coordinates of the plant
	 * @param plant - the plant object itself
	 */
	public void add(Coords coords, Plant plant) {
		Coords chunkCoords = new Coords(coords.w, coords.x/16, 0, coords.z/16);
		
		// TESTING
		this.log.finer("PlantManager.add() called at coords " + coords + " and plant " + plant);
		
		PlantChunk pChunk = null;
		if (!chunks.containsKey(chunkCoords)) {
			pChunk = new PlantChunk(plugin, readConn, -1/*dummy index until assigned when added*/);
			chunks.put(chunkCoords, pChunk); 
			this.log.finer("creating new plantchunk");
		}
		else {
			pChunk = chunks.get(chunkCoords);
			this.log.finer("PlantManager.add(): loading existing plant chunk");
		}
		
		// make sure the chunk is loaded
		loadChunk(chunkCoords);
		
		// add the plant
		pChunk.add(coords, plant, readConn);
		
		// since the chunk was loaded before the new plant was added, the state of the block
		// may not match a previously destroyed crop. Force the block to growth state 0
		Block block = plugin.getServer().getWorld(WorldID.getMCID(coords.w)).getBlockAt(coords.x, coords.y, coords.z);
		plugin.getBlockGrower().growBlock(block, coords, 0.0f);
	}
	
	public Plant get(Coords coords) {
		Coords chunkCoords = new Coords(coords.w, coords.x/16, 0, coords.z/16);
		
		// if the coord's chunk does not have any data attached to it, then simply
		// exit with failure
		if (!chunks.containsKey(chunkCoords)){
			plugin.getLogger().finer("PlantManager.get() returning null due to not containing the Plantchunk object in 'chunks'");

			return null;
		}
		
		PlantChunk pChunk = chunks.get(chunkCoords);
		
		// load the plant data if it is not yet loaded
		loadChunk(chunkCoords);
		
		return pChunk.get(coords);
	}
	
	// determine if the chunk corresponding to the given block coords is loaded
	public boolean chunkLoaded(Coords coords) {
		Coords chunkCoords = new Coords(coords.w, coords.x/16, 0, coords.z/16);
		return (chunks.containsKey(chunkCoords) && chunks.get(chunkCoords).isLoaded());
	}
	
	public void remove(Coords coords) {
		Coords chunkCoords = new Coords(coords.w, coords.x/16, 0, coords.z/16);
		PlantChunk pChunk = chunks.get(chunkCoords);

		if (pChunk == null)
			return;
		
		// make sure the chunk is loaded
		loadChunk(chunkCoords);
		
		pChunk.remove(coords);		
	}
}
