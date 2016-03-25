package vg.civcraft.mc.citadel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import vg.civcraft.mc.citadel.database.CitadelReinforcementData;
import vg.civcraft.mc.citadel.misc.CitadelStatics;
import vg.civcraft.mc.citadel.misc.LoadingCacheNullException;
import vg.civcraft.mc.citadel.reinforcement.NullReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class ReinforcementManager {

	private CitadelReinforcementData db;
	private long dayMultiplier;

	// This shit is cool
	private RemovalListener<Location, Reinforcement> removalListener = new RemovalListener<Location, Reinforcement>() {
		public void onRemoval(RemovalNotification<Location, Reinforcement> removal) {
			Reinforcement rein = removal.getValue();
			if (rein == null || rein instanceof NullReinforcement){
				return;
			}
			if (rein.isDirty()) {
				saveReinforcement(rein);
			}
		}
	};
	
	private LoadingCache<Location, Reinforcement> reinforcements = CacheBuilder
			.newBuilder().maximumSize(CitadelConfigManager.getMaxCacheSize())
			.expireAfterAccess(CitadelConfigManager.getMaxCacheMinutes(), TimeUnit.MINUTES)
			.removalListener(removalListener)
			.build(new CacheLoader<Location, Reinforcement>() {
				public Reinforcement load(Location loc) throws Exception {
					if (loc == null) {
						Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager cache load called with null");
					}

					Reinforcement rein = db.getReinforcement(loc);
					if (rein == null) {
						return new NullReinforcement(loc);
					}
					CitadelStatics.updateHitStat(CitadelStatics.LOAD);
					// decrement cache because it gets increased from getReinforcement()
					CitadelStatics.decrementHitStat(CitadelStatics.CACHE);
					return rein;
				}
			});
	
	public ReinforcementManager(CitadelReinforcementData db) {
		this.db = db;
		this.dayMultiplier = (long)CitadelConfigManager.getDayMultiplier();
		
		scheduleSave();
	}
	
	public long getDayMultiplier(){
		return dayMultiplier;
	}

	/**
	 * Saves the reinforcement to the database. If the reinforcement durability
	 * is less than or equal to zero it will delete it from the database.
	 * @param The Reinforcement to save
	 */
	public void saveReinforcement(Reinforcement rein) {
		if (rein == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager saveReinforcement called with null");
			return;
		}
		if (rein.getDurability() <= 0) {
			deleteReinforcement(rein);
		}
		CitadelStatics.updateHitStat(CitadelStatics.UPDATE);
		db.saveReinforcement(rein);
		rein.setDirty(false);
	}

	/**
	 * Saves the initial reinforcement into the database.
	 * @param The Reinforcement to save
	 */
	public void saveInitialReinforcement(Reinforcement rein) {
		// Do a check first, there might be an edge case for example half slabs where there is a reinforcement
		// but it got this far.  Lets just keep the one already there and ignore this new one.
		// If this is some other case then the code already in place should have deleted the reinforcement EX: Air.
		if (rein == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager saveInitialReinforcement called with null");
			return;
		}
		if (getReinforcement(rein.getLocation()) == null) {
			reinforcements.put(rein.getLocation(), rein);
			CitadelStatics.updateHitStat(CitadelStatics.INSERT);
			db.insertReinforcement(rein);
		}
	}

	/**
	 * Returns the Reinforcement from the specified location.
	 * 
	 * @param loc
	 * @return Reinforcement
	 */
	public Reinforcement getReinforcement(Location loc) {
		if (loc == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager getReinforcement called with null");
			return null;
		}

		try {
			Reinforcement rein = reinforcements.get(loc);
			if (rein instanceof NullReinforcement) {
				return null;
			}
			CitadelStatics.updateHitStat(CitadelStatics.CACHE);
			return rein;
		} catch (Exception e) {
			// So we ignore cache errors?
			if (!(e.getCause() instanceof LoadingCacheNullException)) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Returns the Reinforcement from the specified block.
	 * 
	 * @param block
	 * @return Reinforcement
	 */
	public Reinforcement getReinforcement(Block block) {
		if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager getReinforcement block called with null");
			return null;
		}
		return getReinforcement(block.getLocation());
	}

	/**
	 * Deletes the reinforcement. Should get called from the saveReinforcement
	 * method if the durability of the reinforcement is less than or equal to 0.
	 * 
	 * @param rein
	 */
	public void deleteReinforcement(Reinforcement rein) {
		if (rein == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager deleteReinforcement called with null");
			return;
		}
		reinforcements.invalidate(rein.getLocation());
		CitadelStatics.updateHitStat(CitadelStatics.DELETE);
		db.deleteReinforcement(rein);
	}

	/**
	 * Used to flush all the reinforcements to the db on shutdown. Can be called
	 * else where if too a manual flush is wanted.
	 */
	public void invalidateAllReinforcements() {
		reinforcements.invalidateAll();
	}

	/**
	 * Returns if the location is reinforced or not.
	 * 
	 * @param loc
	 *            - The location of the potential reinforcement.
	 * @return Returns true if one was found.
	 */
	public boolean isReinforced(Location loc) {
		if (loc == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager isReinforced called with null");
			return false;
		}

		return getReinforcement(loc) != null;
	}

	/**
	 * Returns if the block is reinforced or not.
	 * 
	 * @param block
	 *            - The block of the potential reinforcement.
	 * @return Returns true if one was found.
	 */
	public boolean isReinforced(Block block) {
		if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager isReinforced block called with null");
			return false;
		}

		return isReinforced(block.getLocation());
	}
	
	// Saves periodicly all the reinforcements.
	private void scheduleSave(){
		Bukkit.getScheduler().runTaskTimerAsynchronously(Citadel.getInstance(), new Runnable(){

			@Override
			public void run() {
				try {
					long cached = 0l;
					long dirty = 0l;
					long s = 0l;
					if (CitadelConfigManager.shouldLogInternal()) {
						Citadel.Log("Running Scheduled Save");
						s = System.currentTimeMillis();
					}
					List<Reinforcement> reins = new ArrayList<Reinforcement>();
					synchronized(reinforcements){
						for (Reinforcement r: reinforcements.asMap().values()) {
							if (r.isDirty()) {
								reins.add(r);
								dirty++;
							}
							cached++;
						}
					}
					for (Reinforcement r: reins) {
						saveReinforcement(r);
					}
					if (CitadelConfigManager.shouldLogInternal()) {
						s = System.currentTimeMillis() - s;
						Citadel.Log("Scheduled Save complete in " + s + " ms. Cache holds " +
							cached + " entries, " + dirty + " entries saved to DB.");
					}
				} catch (Exception e) {
					Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager scheduled save encountered a problem", e);
				}
			}
			
		}, 100, CitadelConfigManager.getTickRepeatingSave());
	}
	
	/**
	 * This gets all reinforcements in a chunk.  This should not be called regularly synchronously as this will call the database first.
	 * After it grabs the reinforcements it checks if they are already in the cache and if they are it skips it and if not it puts it in there.
	 * Then returns the list of reinforcements in the Chunk.
	 */
	public List<Reinforcement> getReinforcementsByChunk(Chunk chunk){
		if (chunk == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager getReinforcementsByChunk called with null");
			return new ArrayList<Reinforcement>();
		}

		List<Reinforcement> reins = db.getReinforcements(chunk);
		List<Reinforcement> reins_new = new ArrayList<Reinforcement>();
		for (Reinforcement rein: reins){
			if (reinforcements.getIfPresent(rein.getLocation()) == null){
				reinforcements.put(rein.getLocation(), rein);
				reins_new.add(rein);
			}
			else {
				Reinforcement r = null;
				try {
					r = reinforcements.get(rein.getLocation());
				} catch (ExecutionException e) {
					Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager getReinforcementsByChunk called with null", e);
				}
				reins_new.add(r);
			}
		}
		return reins_new;
	}
	
	public void loadReinforcementChunk(Chunk chunk) {
		if (chunk == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementManager loadReinforcementChunk called with null");
			return;
		}
		List<Reinforcement> reins = db.getReinforcements(chunk);
		for (Reinforcement rein: reins){
			Reinforcement r = reinforcements.getIfPresent(rein.getLocation());
			if (r == null || r instanceof NullReinforcement) {
				reinforcements.put(rein.getLocation(), rein);
			}
		}
	}
}
