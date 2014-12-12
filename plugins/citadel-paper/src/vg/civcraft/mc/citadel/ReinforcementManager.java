package vg.civcraft.mc.citadel;

import java.util.concurrent.TimeUnit;

import net.minecraft.util.com.google.common.cache.CacheBuilder;
import net.minecraft.util.com.google.common.cache.CacheLoader;
import net.minecraft.util.com.google.common.cache.LoadingCache;
import net.minecraft.util.com.google.common.cache.RemovalListener;
import net.minecraft.util.com.google.common.cache.RemovalNotification;

import org.bukkit.Location;
import org.bukkit.block.Block;

import vg.civcraft.mc.citadel.database.CitadelReinforcementData;
import vg.civcraft.mc.citadel.misc.LoadingCacheNullException;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class ReinforcementManager {

	private CitadelReinforcementData db;
	
	// This shit is cool
	private RemovalListener<Location, Reinforcement> removalListener = 
			new RemovalListener<Location, Reinforcement>(){
		public void onRemoval(RemovalNotification<Location, Reinforcement> removal){
			Reinforcement rein = removal.getValue();
			saveReinforcement(rein);
		}
	};
	private LoadingCache<Location, Reinforcement> reinforcements = 
			CacheBuilder.newBuilder()
			.maximumSize(CitadelConfigManager.getMaxCacheSize())
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.removalListener(removalListener)
			.build(
					new CacheLoader<Location, Reinforcement>(){
						public Reinforcement load(Location loc) throws Exception{
							Reinforcement rein = db.getReinforcement(loc);
							if (rein == null){
								throw new LoadingCacheNullException();
							}
							return rein;
						}
					});
	
	public ReinforcementManager(CitadelReinforcementData db){
		this.db = db;
	}
	/**
	 * Saves the reinforcement to the database.
	 * If the reinforcement durability is less than or equal to zero
	 * it will delete it from the database.
	 * @param The Reinforcement to save
	 */
	public void saveReinforcement(Reinforcement rein){
		if (rein.getDurability() <= 0)
			deleteReinforcement(rein);
		db.saveReinforcement(rein);
	}
	/**
	 * Saves the initial reinforcement into the database.
	 * @param The Reinforcement to save
	 */
	public void saveInitialReinforcement(Reinforcement rein){
		reinforcements.put(rein.getLocation(), rein);
		db.insertReinforcement(rein);
	}
	/**
	 * Returns the Reinforcement from the specified location.
	 * @param loc
	 * @return Reinforcement
	 */
	public Reinforcement getReinforcement(Location loc){
		try{
			return reinforcements.get(loc);
		} catch(Exception e){
			if (!(e instanceof LoadingCacheNullException)); // i dont get why this doesnt work
					//e.printStackTrace();
		}
		return null;
	}
	/**
	 * Returns the Reinforcement from the specified block.
	 * @param block
	 * @return Reinforcement
	 */
	public Reinforcement getReinforcement(Block block){
		return getReinforcement(block.getLocation());
	}
	/**
	 * Deletes the reinforcement. Should get called from the saveReinforcement
	 * method if the durability of the reinforcement is less than or equal to 0.
	 * @param rein
	 */
	public void deleteReinforcement(Reinforcement rein){
		reinforcements.invalidate(rein.getLocation());
		db.deleteReinforcement(rein);
	}
	/**
	 * Used to flush all the reinforcements to the db on shutdown.
	 * Can be called else where if too a manual flush is wanted.
	 */
	public void invalidateAllReinforcements(){
		reinforcements.invalidateAll();
	}
	/**
	 * @return Returns the next reinforcement Id for reinforcements.
	 */
	public int getNextReinforcementID(){
		return db.getLastReinId();
	}
}
