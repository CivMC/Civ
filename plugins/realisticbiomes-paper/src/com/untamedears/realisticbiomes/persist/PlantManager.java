package com.untamedears.realisticbiomes.persist;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
	
	// lock to be used to lock use of writeConn over multiple threads
	private ReentrantLock writeLock;
	// database write thread
	ExecutorService writeService;
	
	////================================================================================= ////
	
	public PlantManager(RealisticBiomes plugin, PersistConfig config) {
		this.plugin = plugin;
		this.config = config;
		
		chunks = new HashMap<Coords, PlantChunk>();
		
		// open the database
		String sDriverName = "org.sqlite.JDBC";
		try {
		Class.forName(sDriverName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String sJdbc = "jdbc:sqlite";
		String sDbUrl = sJdbc + ":" + plugin.getDataFolder().getAbsolutePath() + "/" + config.databaseName;
		int iTimeout = 30;
		
		String synchronousPragma = "PRAGMA synchronous=OFF";
		String countChangesPragma = "PRAGMA count_changes=OFF";
		String journalModePragma = "PRAGMA journal_mode=MEMORY";
		String tempStorePragma = "PRAGMA temp_store=MEMORY";
		
		String makeTableChunk = "CREATE TABLE IF NOT EXISTS chunk (id INTEGER PRIMARY KEY AUTOINCREMENT, w INTEGER, x INTEGER, z INTEGER)";
		String makeTablePlant = "CREATE TABLE IF NOT EXISTS plant (chunkid INTEGER, w INTEGER, x INTEGER, y INTEGER, z INTEGER, date INTEGER, growth REAL, FOREIGN KEY(chunkid) REFERENCES chunk(id))";
		String vacuumDatabase = "VACUUM;";
		
		try {
			// connect to the database
			writeConn = DriverManager.getConnection(sDbUrl);
			readConn = DriverManager.getConnection(sDbUrl);
			Statement stmt = readConn.createStatement();
			stmt.setQueryTimeout(iTimeout);
			
			// set various settings for performance. Makes corruption due to bad shutdowns more common
			stmt.executeUpdate(synchronousPragma);
			stmt.executeUpdate(countChangesPragma);
			stmt.executeUpdate(journalModePragma);
			stmt.executeUpdate(tempStorePragma);
			
			// clean up the database
			stmt.executeUpdate(vacuumDatabase);
			
			// make tables if they don't exist
			stmt.executeUpdate(makeTableChunk);
			stmt.executeUpdate(makeTablePlant);
				
			// load all chunks
			ResultSet rs = stmt.executeQuery("SELECT id, w, x, z FROM chunk");
			while (rs.next()) {
				int id = rs.getInt(1);
				int w = rs.getInt(2);
				int x = rs.getInt(3);
				int z = rs.getInt(4);
				
				PlantChunk pChunk = new PlantChunk(plugin, readConn, writeConn, id);
				chunks.put(new Coords(w,x,0,z), pChunk);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		// create unload batch
		chunksToUnload = new ArrayList<Coords>();
		
		//register the batchTask
		unloadBatchTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
		    @Override  
		    public void run() {
				unloadBatch();
		    }
		}, config.unloadBatchPeriod, config.unloadBatchPeriod);
		
		writeLock = new ReentrantLock();
		writeService = Executors.newSingleThreadExecutor();
		
		log = plugin.getLogger();
	}
	
	// ============================================================================================
	private Logger log;
	// commit transaction in the writeConn database connection
	private class commitRunnable implements Runnable {
	    @Override  
	    public void run() {
	    	writeLock.lock();

    		long start = System.nanoTime()/1000000/*ns/ms*/;
			try {
				writeConn.commit();
				writeConn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			long end = System.nanoTime()/1000000/*ns/ms*/;
			
			log.info("Committed data in "+(end-start)+" ms");
			
			writeLock.unlock();
	    }
	};
	
	// --------------------------------------------------------------------------------------------
	private void unloadBatch() {
		int count = chunksToUnload.size();
		long start = System.nanoTime()/1000000/*ns/ms*/;
		
		// prepare a single transaction with all inserts
		writeLock.lock();
			try {
				writeConn.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	
			for (Coords batchCoords:chunksToUnload) {
				unloadChunk(batchCoords);
			}
		writeLock.unlock();
		
		// write the changes to the database concurrently
		writeService.submit(new commitRunnable());
		
		long end = System.nanoTime()/1000000/*ns/ms*/;
		
		if (plugin.persistConfig.logDB)
			plugin.getLogger().info("db save: "+count+" chunks unloaded in "+(end-start)+" ms");
		
		chunksToUnload.clear();		
	}
	
	public void saveAllAndStop() {
		writeLock.lock();
			try {
				writeConn.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			for (Coords coords:chunks.keySet()) {
				PlantChunk pChunk = chunks.get(coords);
				pChunk.unload(coords);
			}
			
			try {
				writeConn.commit();
				writeConn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		writeLock.unlock();
		
		chunksToUnload = null;
		
		unloadBatchTask.cancel();
		writeService.shutdown();
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
		
		pChunk.unload(coords);
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
	private boolean loadChunk(Coords coords) {
		// if the specified chunk does not exist, then don't load anything
		if (!chunks.containsKey(coords))
			return false;
		
		PlantChunk pChunk = chunks.get(coords);
		// if the plant chunk is already loaded, then there is no need to load
		if (pChunk.isLoaded())
			return true;
		
		// this getWorlds().get(index) could break in the future
		// if the minecraft chunk is unloaded again, then don't load the pChunk
		if (!plugin.getServer().getWorld(WorldID.getMCID(coords.w)).isChunkLoaded(coords.x, coords.z))
			return false;
		
		// finally, just load this thing!
		long start = System.nanoTime()/1000000/*ns/ms*/;
		try {
			readConn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		boolean loaded = pChunk.load(coords);
		try {
			readConn.commit();
			readConn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		long end = System.nanoTime()/1000000/*ns/ms*/;
		
		if (plugin.persistConfig.logDB)
			plugin.getLogger().info("db load chunk["+coords.x+","+coords.z+"]: "+pChunk.getPlantCount()+" entries loaded in "+(end-start)+" ms");
		
		return loaded;
	}
	
	////===========================================================================================
	public void add(Coords coords, Plant plant) {
		Coords chunkCoords = new Coords(coords.w, coords.x/16, 0, coords.z/16);
		
		
		PlantChunk pChunk = null;
		if (!chunks.containsKey(chunkCoords)) {
			writeLock.lock();
				pChunk = new PlantChunk(plugin, readConn, writeConn, -1/*dummy index until assigned when added*/);
			writeLock.unlock();
		}
		else
			pChunk = chunks.get(chunkCoords);
		
		// make sure the chunk is loaded
		loadChunk(chunkCoords);
		
		pChunk.add(coords, plant);
		chunks.put(chunkCoords, pChunk);
	}
	
	public Plant get(Coords coords) {
		Coords chunkCoords = new Coords(coords.w, coords.x/16, 0, coords.z/16);
		
		// if the coord's chunk does not have any data attached to it, then simply
		// exit with failure
		if (!chunks.containsKey(chunkCoords))
			return null;
		
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
