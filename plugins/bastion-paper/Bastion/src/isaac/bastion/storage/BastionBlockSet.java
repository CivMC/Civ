package isaac.bastion.storage;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.ConfigManager;
import isaac.bastion.manager.EnderPearlManager;
import isaac.bastion.util.QTBox;
import isaac.bastion.util.SparseQuadTree;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class BastionBlockSet implements Set<BastionBlock>, Iterable<BastionBlock> {
	private Map<World,SparseQuadTree> blocks;
	private Set<BastionBlock> changed;
	public Set<BastionBlock> bastionBlocks;
	private BastionBlockStorage storage;
	private int task;
	private ConfigManager config;
	
	
	public BastionBlockSet() {
		storage = new BastionBlockStorage();
		changed = new TreeSet<BastionBlock>();
		config = Bastion.getConfigManager();

		blocks = new HashMap<World, SparseQuadTree>();
		bastionBlocks = new TreeSet<BastionBlock>();
		
		if (Bastion.getPlugin().isEnabled()) {
			task = new BukkitRunnable(){
				public void run(){
					update();
				}
			}.runTaskTimer(Bastion.getPlugin(),config.getTimeBetweenSaves(),config.getTimeBetweenSaves()).getTaskId();
		}
		BastionBlock.set=this;
	}
	
	//note only for BastionBlocks already in db
	public void updated(BastionBlock updated) {
		if (!changed.contains(updated)) {
			changed.add(updated);
		}
	}
	
	public void close() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.cancelTask(task);
		update();
	}
	
	public void load() {
		int enderSeachRadius = EnderPearlManager.MAX_TELEPORT + 100;
		for (World world : Bukkit.getWorlds()) {
			Enumeration<BastionBlock> forWorld = storage.getAllBastions(world);
			SparseQuadTree bastionsForWorld = new SparseQuadTree(enderSeachRadius);
			while (forWorld.hasMoreElements()) {
				BastionBlock toAdd = forWorld.nextElement();
				bastionBlocks.add(toAdd);
				bastionsForWorld.add(toAdd);
			}
			blocks.put(world, bastionsForWorld);
		}
	}
	
	public Set<QTBox> forLocation(Location loc) {
		return blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ());

	}

	public Set<BastionBlock> getPossibleTeleportBlocking(Location loc, double maxDistance) {
		Set<QTBox> boxes = blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ(), true);
		
		double maxDistanceSquared = maxDistance * maxDistance;
		double maxBoxDistanceSquared = maxDistanceSquared * 2.0;
		
		Set<BastionBlock> result = new TreeSet<BastionBlock>();
		
		for (QTBox box : boxes) {
			if (box instanceof BastionBlock) {
				BastionBlock bastion = (BastionBlock)box;
				// Fixed for square field nearness, using diagonal distance as max -- (radius * sqrt(2)) ^ 2
				if (((bastion.getType().isSquare() && bastion.getLocation().distanceSquared(loc) <= maxBoxDistanceSquared) ||   
							(!bastion.getType().isSquare() && bastion.getLocation().distanceSquared(loc) <= maxDistanceSquared)) &&
							(!bastion.getType().isRequireMaturity() || bastion.isMature())) {
					result.add(bastion);
				}
			}
		}
		return result;
	}
	
	public Set<BastionBlock> getPossibleFlightBlocking(double maxDistance, Location...locs) {
		Set<QTBox> boxes = null;
		Set<BastionBlock> result = new TreeSet<BastionBlock>();		
		double maxDistanceSquared = maxDistance * maxDistance;
		double maxBoxDistanceSquared = maxDistanceSquared * 2.0;
		
		for (Location loc: locs) {
			boxes = blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ(), true);
			
			for (QTBox box : boxes) {
				if (box instanceof BastionBlock) {
					BastionBlock bastion = (BastionBlock)box;
					// Fixed for square field nearness, using diagonal distance as max -- (radius * sqrt(2)) ^ 2
					if (((bastion.getType().isSquare() && bastion.getLocation().distanceSquared(loc) <= maxBoxDistanceSquared) ||   
								(!bastion.getType().isSquare() && bastion.getLocation().distanceSquared(loc) <= maxDistanceSquared)) &&
								(!bastion.getType().isElytraRequireMature() || bastion.isMature())) {
						result.add(bastion);
					}
				}
			}
		}
		return result;
	}
	
	public BastionBlock getBastionBlock(Location loc) {
		Set<? extends QTBox> possible = forLocation(loc);
		for (QTBox box: possible) {
			BastionBlock bastion = (BastionBlock) box;
			if (bastion.getLocation().equals(loc)) {
				return bastion;
			}
		}
		return null;
	}
	
	public int update() {
		int count = changed.size();
		for (BastionBlock toUpdate: changed) {
			toUpdate.update(BastionBlockStorage.db);
		}
		changed.clear();
		Bastion.getPlugin().getLogger().info("updated " + count + " blocks");
		return count;
	}
	
	@Override
	public boolean add(BastionBlock toAdd) {
		toAdd.save(BastionBlockStorage.db); //maybe should cache and run in different thread
		
		bastionBlocks.add(toAdd);
		blocks.get(toAdd.getLocation().getWorld()).add(toAdd);
		return false;
	}
	
	public boolean remove(BastionBlock toRemove) {
		boolean in_set = false;
		if (toRemove == null) {
			return false;
		}
		toRemove.delete(BastionBlockStorage.db); //maybe should cache and run in different thread
		
		in_set = bastionBlocks.remove(toRemove);
		changed.remove(toRemove);
		
		SparseQuadTree forWorld = blocks.get(toRemove.getLocation().getWorld());
		if (forWorld != null) {
			forWorld.remove(toRemove);
		}
		return in_set;
	}
	
	@Override
	public Iterator<BastionBlock> iterator() {
		return bastionBlocks.iterator();
	}
	
	@Override
	public boolean addAll(Collection<? extends BastionBlock> toAdd) {
		for (BastionBlock out: toAdd) {
			if (!add(out)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void clear() {
		blocks.clear();
		bastionBlocks.clear();
		changed.clear();
	}
	
	@Override
	public boolean contains(Object in) { //compares by id not by pointer
		if (!(in instanceof BastionBlock)) {
			throw new IllegalArgumentException("Contains only accepts a BastionBlock");
		}
		BastionBlock toTest = (BastionBlock) in;
		return bastionBlocks.contains(toTest);
	}
	
	@Override
	public boolean containsAll(Collection<?> arg0) {
		for (Object in: bastionBlocks) {
			if (!contains(in)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		return bastionBlocks.isEmpty();
	}
	
	@Override
	public boolean remove(Object in) {
		if (in == null) {
			return true;
		} else if (in instanceof BastionBlock) {
			return remove((BastionBlock) in);
		} else if (in instanceof Location) {
			return remove(getBastionBlock((Location) in));
		} else {
			throw new IllegalArgumentException("you didn't provide a BastionBlock or Location");
		}
	}

	@Override
	public boolean removeAll(Collection<?> toRemove) {
		if (toRemove.size() == 0) {
			return true;
		}
		for(Object block : toRemove) {
			remove(block);
		}
		return false;
	}
	
	@Override
	public boolean retainAll(Collection<?> arg0) {
		return false;
	}
	
	@Override
	public int size() {
		return bastionBlocks.size();
	}
	
	@Override
	public Object[] toArray() {
		return bastionBlocks.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] arg0) {
		return bastionBlocks.toArray(arg0);
	}
}
