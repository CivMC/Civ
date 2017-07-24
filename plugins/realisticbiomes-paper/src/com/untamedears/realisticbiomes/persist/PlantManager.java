package com.untamedears.realisticbiomes.persist;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.untamedears.realisticbiomes.DropGrouper;
import com.untamedears.realisticbiomes.PersistConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.utils.MaterialAliases;

public class PlantManager {
	private final RealisticBiomes plugin;
	private final PersistConfig config;
	
	// database connection
	private Database db;
	
	public class PlantChunks {
		private final HashMap<ChunkCoords, PlantChunk> map;
		
		public PlantChunks() {
			map = new HashMap<ChunkCoords, PlantChunk>();
		}
		
		PlantChunk get(ChunkCoords coords) {
			return map.get(coords);
		}
		
		void addChunk(PlantChunk chunk) {
			map.put(chunk.getChunkCoord(), chunk);
		}
		
		Set<ChunkCoords> getCoordsSet() {
			return map.keySet();
		}
	}
	
	
	// map of chunk coordinates to plant chunk data
	// an entry of null denotes a chunk that is in the database but is unloaded
	private PlantChunks chunks;
	
	// plants chunks to be unloaded in batches
	private LinkedBlockingQueue<ChunkCoords> chunksToUnload;
	
	// task that periodically unloads chunks in batches
	private BukkitTask unloadBatchTask;
	
	// database write thread
	ExecutorService writeService;

	
	// prepared statements
	private Logger log;
	
	////================================================================================= ////
	
	// TODO: Clean up. Database updates, prepared statement creation, and the like are spread
	//   across three or four classes. Total mess.
	public PlantManager(RealisticBiomes plugin, PersistConfig config) {
		this.plugin = plugin;
		this.config = config;
		
		chunks = new PlantChunks();
		
		db = new Database(config);
		
		try {
			db.available();
		} catch (SQLException se) {
			throw new RuntimeException("Failed to connect to the database! ", se);
		}
		
		// load all chunks
		
		RealisticBiomes.LOG.info("loading PlantChunks");
		long startTime = System.nanoTime()/1000000/*ns/ms*/;

		try (Connection connection = db.getConnection();
				PreparedStatement selectAllFromChunk = connection.prepareStatement(Database.selectAllFromChunk);
				ResultSet rs = selectAllFromChunk.executeQuery();) {
			while (rs.next()) {
				long id = rs.getLong("id");
				int w = rs.getInt("w");
				int x = rs.getInt("x");
				int z = rs.getInt("z");
				
				PlantChunk pChunk = new PlantChunk(plugin, id, new ChunkCoords(w, x, z));
				pChunk.loaded = false;
				pChunk.inDatabase = true;
				RealisticBiomes.doLog(Level.FINER, "\tLoaded plantchunk " + pChunk + " at coords " + new Coords(w,x,0,z));
				chunks.addChunk(pChunk);
			}
			
		} catch (SQLException e) {
			throw new RuntimeException("Failed to load all of the chunks from the database! ", e);
		}
		long endTime = System.nanoTime()/1000000/*ns/ms*/;

		RealisticBiomes.LOG.info("Finished loading all PlantChunks - time taken: " +(endTime-startTime) + "ms");

		// create unload batch
		chunksToUnload = new LinkedBlockingQueue<ChunkCoords>();
		
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
	
	public Database getDb() {
		return this.db;
	}

	/**
	 * call this to load all the plants from all our plant chunks
	 * this should only be called if persistConfig.cacheEntireDatabase is true
	 * 
	 * If we hit a out of memory error here, we shut bukkit down (if we can!)
	 */
	public void cacheAllPlants() {
		
		// If we have set the option to 'cacheEntireDatabase', then after we have loaded all of
		// the plant chunks, then we should go through all the plant chunks and load all
		// of the plants for it.
		// We need to be careful not to run out of memory...
		
		try {
			RealisticBiomes.LOG.info("Attempting to load all of the plants for all the plant chunks!");

			long startTimeOne = System.nanoTime()/1000000/*ns/ms*/;
			
			for (ChunkCoords coords : chunks.getCoordsSet()) {
				
				loadChunk(coords);
			}
			
			long endTimeOne = System.nanoTime()/1000000/*ns/ms*/;
			
			RealisticBiomes.LOG.info("Finished loading all Plants inside the PlantChunks - time taken: " +(endTimeOne-startTimeOne) + "ms");

			
		} catch (OutOfMemoryError oome) {
			
			// try and free up memory so we don't really run out of memory when trying
			// to shut down
			this.chunks = null;
			System.gc();
			
			RealisticBiomes.LOG.severe("OUT OF MEMORY ERROR WHEN LOADING ALL "
					+ "THE PLANTS FOR ALL THE PLANT CHUNKS SHUTTING DOWN BUKKIT! ERROR: " + oome);
			
			// R.I.P in peaces bukkit
			Bukkit.shutdown();
		}
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
				
				// don't do anything if the chunksToUnload is empty
				// (maybe it got emptied after this was submitted to the write service?)
				if (chunksToUnload.isEmpty()) {
					return;
				}
				
				long start = System.nanoTime()/1000000/*ns/ms*/;
				long end;

				int chunksUnloadedCount = chunksToUnload.size();
				
				int plantCounter = 0;
				while (!chunksToUnload.isEmpty()) {
					ChunkCoords batchCoords = chunksToUnload.poll();
					
					if (batchCoords != null) {
						plantCounter += unloadChunk(batchCoords);
					}
				}
				
				end = System.nanoTime()/1000000/*ns/ms*/;
				
				logLoadOrUnloadEvent("Unloaded " + chunksUnloadedCount + " chunks (" + plantCounter + " plants)", 
						config, ChunkDBEvent.UnloadEvent, end-start);
								
			}
		});
	}
	
	/**
	 * enum to define whether a log is for a load or unload event
	 * @author markgrandi
	 *
	 */
	private enum ChunkDBEvent {
		
		LoadEvent, UnloadEvent;
		
	}
	
	/**
	 * helper method to take care of logging chunk load and unload events
	 * This checks the PlantManager's config to see if logDb, logDbProduction and
	 * the load/unload min times
	 * @param prefixString - the message to write
	 * @param whichEvent - the type of event this is for, to check with the config (if productionLogDb)
	 * @param timeTakenInMs - the time taken in milliseconds, used to check against the 
	 * persistConfig's load/unload mintimes
	 */
	private static void logLoadOrUnloadEvent(String message, PersistConfig config, ChunkDBEvent whichEvent, long timeTakenInMs) {
		
		// if productionLogDb is set to true, then we do production logging no matter what 
		// logDb is set to
		if (config.productionLogDb) {
			
			if (whichEvent == ChunkDBEvent.LoadEvent) {
				if (timeTakenInMs >= config.productionLogLoadMintime) {
					RealisticBiomes.LOG.info("[" + whichEvent.toString() + "] "+ message + " - time taken: " +  timeTakenInMs + "ms");
				}
				return;
				
			} else {
				// UnloadEvent
				if (timeTakenInMs >= config.productionLogUnloadMintime) {
					RealisticBiomes.LOG.info("[" + whichEvent.toString() + "] "+ message + " - time taken: " +  timeTakenInMs + "ms");
				}
				return;
			}
		}
		
		// here, productionLogDb is set to false, check to see if logDb is true
		if (config.logDB) {
			RealisticBiomes.LOG.info("[" + whichEvent.toString() + "] "+ message + " - time taken: " +  timeTakenInMs + "ms");
			return;
		}
		
		
	}
	
	public void saveAllAndStop() {
		writeService.submit(new Runnable() {
			public void run() {
				
				try {
					log.info("Starting runnable in saveAllAndStop()");
					
					for (ChunkCoords coords : chunks.getCoordsSet()) {
	
						PlantChunk pChunk = chunks.get(coords);
	
						pChunk.unload();
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
		
		try {
			db.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "error in saveAllAndStop, closing db", e);
		}
		
		log.info("write service finished");
	}
	
	/**
	 * unloads the specified chunk
	 * @param coords - the coordinates (chunk coords) of the chunk to unload
	 * @return an integer representing the number of plants inside the chunk that was unloaded
	 */
	private int unloadChunk(ChunkCoords coords) {
		// if the specified chunk does not exist in the system, or is no longer loaded, nothing needs
		// to be done
		
		// Only query the chunks map once!
		PlantChunk pChunk = chunks.get(coords);
		
		if (pChunk == null || pChunk.isLoaded() == false) {
			return 0;
		}
		
		// if the minecraft chunk is loaded again, then don't unload the pChunk
		if (plugin.getServer().getWorld(WorldID.getMCID(coords.w)).isChunkLoaded(coords.x, coords.z))
			return 0;
		
		// finally, actually unload this thing
		int tmpCount = pChunk.getPlantCount();
		pChunk.unload();
		return tmpCount;
	}
	
	// --------------------------------------------------------------------------------------------
	public void minecraftChunkUnloaded(ChunkCoords coords) {
		// if the pChunk does not exist, there is nothing to unload
		
		// Only query the chunks map once!
		PlantChunk pChunk = chunks.get(coords);
		if (pChunk == null) {
			return;
		}
		
		// if the pChunk is already unloaded then it should stay unloaded -- do nothing
		if (!pChunk.isLoaded()) {
			return;
		}
		
		// Adds the chunk location to a queue to be unloaded asynchronously
		chunksToUnload.add(coords);
	}
	
	////===========================================================================================
	// load the specified chunk, return true if the pChunk is actually loaded
	/**
	 * @brief Loads a PlantChunk instance of the given chunk coordinates
	 * @param coords The chunk coordinates to load
	 * @return The chunk instance that was loaded, otherwise null
	 * 
	 * Returning the actual chunk instance reduces the amount of map queries we need to execute
	 * to get our desired instance
	 */
	public PlantChunk loadChunk(ChunkCoords coords) {
		
		// Limit the query to a 
		PlantChunk pChunk = chunks.get(coords);
		
		if (pChunk == null) {
			//RealisticBiomes.doLog(Level.FINEST, "PlantManager.loadChunk(): returning false as we don't have the plantchunk obj in chunks");
			return null;
		}
		// if the plant chunk is already loaded, then there is no need to load
		if (pChunk.isLoaded()) {
			RealisticBiomes.doLog(Level.FINEST, "PlantManager.loadChunk(): plantChunk already loaded, returning true");
			return pChunk;
		}
		
		// this getWorlds().get(index) could break in the future
		// if the minecraft chunk is unloaded again, then don't load the pChunk
		UUID id = WorldID.getMCID(coords.w);
		World world = plugin.getServer().getWorld(id);
		Chunk c = world.getChunkAt(pChunk.getChunkCoord().x, pChunk.getChunkCoord().z);
		
		if (!world.isChunkLoaded(c)) {
			RealisticBiomes.doLog(Level.FINEST, "PlantManager.loadChunk(): minecraft chunk was unloaded again... returning false");
			return null;
		}
		
		// finally, just load this thing!
		long start = System.nanoTime()/1000000/*ns/ms*/;
		boolean loaded = pChunk.load();
		long end = System.nanoTime()/1000000/*ns/ms*/;
		RealisticBiomes.doLog(Level.FINER, "PlantManager.loadChunk():Had to load chunk, pchunk.load() returned " + loaded);
		
		logLoadOrUnloadEvent("Loaded chunk ["+coords.x+","+coords.z+"]", this.config, ChunkDBEvent.LoadEvent, end-start);
		
		// Return the valid instance if success
		if (loaded == true) {
			return pChunk;
		}
		
		return null;
	}
	
	////===========================================================================================
	/**
	 * Adds a specified plant to the correct PlantChunk
	 * This should only be called after we have verified that the plant does not already exist in the same spot
	 * or else this will override the existing plant!!
	 * @param coords - the coordinates of the plant
	 * @param plant - the plant object itself
	 */
	public void addPlant(Block block, Plant plant) {
		ChunkCoords chunkCoords = new ChunkCoords(block.getChunk());
		Coords blockCoords = new Coords(block);
		
		// TESTING
		RealisticBiomes.doLog(Level.FINER, "PlantManager.add() called at coords " + blockCoords + " and plant " + plant);

		
		// Only query the map a single time, optimize !!!
		PlantChunk pChunk = loadChunk(chunkCoords);
		
		if (pChunk == null) {
			pChunk = new PlantChunk(plugin, -1/*dummy index until assigned when added*/, chunkCoords);
			chunks.addChunk(pChunk); 
			pChunk.loaded = true; // its loaded because its a brand new plant chunk. 
			RealisticBiomes.doLog(Level.FINER, "PlantManager.add() creating new plantchunk: " + pChunk + "at coords " + chunkCoords);
		}
		else {
			RealisticBiomes.doLog(Level.FINER, "PlantManager.add(): loading existing plant chunk");
		}
		
		// add the plant
		pChunk.addPlant(blockCoords, plant);
	}
	
	public Plant getPlantFromBlock(Block block) {
		ChunkCoords chunkCoord = new ChunkCoords(block.getChunk());
		
		// if the coords's chunk does not have any data attached to it, then simply
		// exit with failure		
		PlantChunk pChunk = chunks.get(chunkCoord);
		if (pChunk == null) {
			RealisticBiomes.doLog(Level.FINER, "PlantManager.get() returning null due to not containing the Plantchunk object in 'chunks'");
			return null;
		}
		
		// load the plant data if it is not yet loaded
		if (pChunk.isLoaded() == false) {
			pChunk.load();
		}
		
		return pChunk.get(new Coords(block));
	}
	
	
	/**
	 * @brief Determines if a particular chunk has been loaded
	 * @param chunk The chunk instance to check
	 * @return true if the chunk is already loaded
	 */
	public boolean isChunkLoaded(ChunkCoords coords) {
		PlantChunk pChunk = chunks.get(coords);
		
		if (pChunk != null && pChunk.isLoaded()) {
			return true;
		}
		
		return false;
	}
	
	public void growChunk(Chunk chunk) {
		ChunkCoords coords = new ChunkCoords(chunk);
		
		// Verify the chunk is loaded first and bugger out if there's no data to work with
		PlantChunk pChunk = loadChunk(coords);
		if (pChunk == null) {
			return;
		}

		RealisticBiomes.doLog(Level.FINER, "PlantManager.growChunk() group: " + pChunk.getPlantCoords().size());
		DropGrouper dropGrouper = new DropGrouper(chunk.getWorld());
		
		// We can assume the chunk will be loaded at this point
		
		// Create a deep copy of the plant set to iterate over so we don't run into problems when
		// trying to remove fully-grown crops.
		// growAndPersistBlock() will remove records from the set when the crops are fully grown
		for (Coords position : new HashSet<Coords>(pChunk.getPlantCoords())) {
			Block block = chunk.getBlock(position.x,  position.y,  position.z);
			
			block = MaterialAliases.getOriginBlock(block, block.getType());
			
			if (block != null) {
				plugin.growAndPersistBlock(block, false, null, null, dropGrouper);
			}
		}
		
		dropGrouper.done();
	}
	
	
	/**
	 * @brief Removes a plant record from the PlantChunk storage
	 * @param block The targeted block location
	 */
	public void removePlant(Block block) {
		ChunkCoords chunkCoords = new ChunkCoords(block.getChunk());
		PlantChunk pChunk = chunks.get(chunkCoords);

		if (pChunk == null)
			return;
		
		// make sure the chunk is loaded
		loadChunk(chunkCoords);
		
		RealisticBiomes.doLog(Level.FINER, "PlantManager.removePlant(): removing plant: " + block.getLocation());
		
		pChunk.remove(new Coords(block));		
	}
}
