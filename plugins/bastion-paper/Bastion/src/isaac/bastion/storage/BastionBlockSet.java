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

public class BastionBlockSet implements Set<BastionBlock>,
Iterable<BastionBlock> {
	private Map<World,SparseQuadTree> blocks;
	private Set<BastionBlock> changed;
	public Map<Long,BastionBlock> blocksById;
	private BastionBlockStorage storage;
	private int task;
	private ConfigManager config;
	public BastionBlockSet() {
		storage=new BastionBlockStorage();
		changed=new TreeSet<BastionBlock>();
		config=Bastion.getConfigManager();

		blocks=new HashMap<World, SparseQuadTree>();
		blocksById=new HashMap<Long,BastionBlock>();
		
		if(Bastion.getPlugin().isEnabled()){
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			task=scheduler.scheduleSyncRepeatingTask(Bastion.getPlugin(),
					new BukkitRunnable(){
				public void run(){
					update();
				}
			},config.getTimeBetweenSaves(),config.getTimeBetweenSaves());
		}
		BastionBlock.set=this;
	}
	public void updated(BastionBlock updated){
		if(!changed.contains(updated)){
			changed.add(updated);
		}
	}
	public void close(){
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.cancelTask(task);
		update();
	}
	public void load(){
		int enderSeachRadius=EnderPearlManager.MAX_TELEPORT+100;
		for(World world : Bukkit.getWorlds()){
			Enumeration<BastionBlock> forWorld=storage.getAllSnitches(world);
			SparseQuadTree bastionsForWorld=new SparseQuadTree(enderSeachRadius);
			while(forWorld.hasMoreElements()){
				BastionBlock toAdd=forWorld.nextElement();
				blocksById.put(toAdd.getId(), toAdd);
				bastionsForWorld.add(toAdd);
			}
			blocks.put(world, bastionsForWorld);
		}
	}
	public Set<QTBox> forLocation(Location loc){
		return blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ());

	}

	public Set<BastionBlock> getPossibleTeleportBlocking(Location loc, double maxDistance){
		Set<QTBox> boxes = blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ(),true);
		
		double maxDistanceSquared = maxDistance * maxDistance;
		
		Set<BastionBlock> result = new TreeSet<BastionBlock>();
		
		for(QTBox box : boxes){
			if(box instanceof BastionBlock){
				BastionBlock bastion = (BastionBlock)box;
				if(bastion.getLocation().distanceSquared(loc) < maxDistanceSquared)
					result.add(bastion);
			}
		}
		return result;
	}
	public BastionBlock getBastionBlock(Location loc) {
		Set<? extends QTBox> possible=forLocation(loc);
		for(QTBox box: possible){
			BastionBlock bastion=(BastionBlock) box;
			if(bastion.getLocation().equals(loc))
				return bastion;
		}
		return null;
	}
	public int update(){
		storage.saveBastionBlocks(changed);
		changed.clear();
		return 0;
	}
	@Override
	public Iterator<BastionBlock> iterator() {
		return blocksById.values().iterator();
	}
	@Override
	public boolean add(BastionBlock toAdd) {
		blocksById.put(toAdd.getId(), toAdd);
		blocks.get(toAdd.getLocation().getWorld()).add(toAdd);
		if(!changed.contains(toAdd))
			changed.add(toAdd);
		else
			return true;
		return false;
	}
	@Override
	public boolean addAll(Collection<? extends BastionBlock> toAdd) {
		for(BastionBlock out: toAdd)
			if(!add(out))
				return false;
		return true;
	}
	@Override
	public void clear() {
		blocks.clear();
		blocksById.clear();
		changed.clear();
	}
	@Override
	public boolean contains(Object in) { //compares by id not by pointer
		if(!(in instanceof BastionBlock))
			throw new IllegalArgumentException("contains only excepts a BastionBlock");
		BastionBlock toTest=(BastionBlock) in;
		return blocksById.values().contains(toTest);
	}
	@Override
	public boolean containsAll(Collection<?> arg0) {
		for(Object in: blocksById.values())
			if(!contains(in))
				return false;
		return true;
	}
	@Override
	public boolean isEmpty() {
		return blocksById.isEmpty();
	}
	@Override
	public boolean remove(Object in) {
		if(in==null){
			return true;
		} else if(in instanceof BastionBlock){
			return remove((BastionBlock) in);
		} else if(in instanceof Location){
			return remove(getBastionBlock((Location) in));
		} else{
			throw new IllegalArgumentException("remove only excepts a BastionBlock");
		}
	}
	private boolean remove(BastionBlock toRemove){
		if(toRemove==null){
			return true;
		}
		if(!changed.contains(toRemove))
			changed.add(toRemove);
		blocksById.remove(toRemove.getId());
		SparseQuadTree forWorld=blocks.get(toRemove.getLocation().getWorld());
		if(forWorld!=null){
			forWorld.remove(toRemove);
		}
		return false;
	}
	@Override
	public boolean removeAll(Collection<?> toRemove) {
		if(toRemove.size()==0)
			return true;
		for(Object block : toRemove){
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
		return blocksById.size();
	}
	@Override
	public Object[] toArray() {
		return blocksById.values().toArray();
	}
	@Override
	public <T> T[] toArray(T[] arg0) {
		return blocksById.values().toArray(arg0);
	}
	public boolean silentRemove(Location loc) {
			return silentRemove(getBastionBlock(loc));
	}
	public boolean silentRemove(BastionBlock toRemove){
		if(toRemove==null){
			return false;
		}
		
		if(!changed.contains(toRemove))
			changed.add(toRemove);
		
		blocksById.remove(toRemove.getId());
		SparseQuadTree forWorld=blocks.get(toRemove.getLocation().getWorld());
		if(forWorld!=null){
			forWorld.remove(toRemove);
		}
		
		return true;
	}
}
