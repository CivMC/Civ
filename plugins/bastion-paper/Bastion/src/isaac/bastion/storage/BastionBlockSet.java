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
	private Map<Integer,BastionBlock> blocksById;
	private BastionBlockStorage storage;
	private int task;
	private ConfigManager config;
	public BastionBlockSet() {
		BastionBlock.set=this;
		storage=new BastionBlockStorage();
		changed=new TreeSet<BastionBlock>();
		config=Bastion.getConfigManager();

		blocks=new HashMap<World, SparseQuadTree>();
		blocksById=new HashMap<Integer,BastionBlock>();
		load();

		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		task=scheduler.scheduleSyncRepeatingTask(Bastion.getPlugin(),
				new BukkitRunnable(){
			public void run(){
				update();
			}
		},config.getTimeBetweenSaves()/500,config.getTimeBetweenSaves()/500);
		//Bastion.getPlugin().getLogger().info("set up save for every "+config.getTimeBetweenSaves());
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
	private void load(){
		int enderSeachRadius=EnderPearlManager.MAX_TELEPORT+100;
		for(World world : Bukkit.getWorlds()){
			Enumeration<BastionBlock> forWorld=storage.getAllSnitches(world);
			SparseQuadTree bastionsForWorld=new SparseQuadTree(enderSeachRadius);
			while(forWorld.hasMoreElements()){
				BastionBlock toAdd=forWorld.nextElement();
				blocksById.put(toAdd.getID(), toAdd);
				bastionsForWorld.add(toAdd);
			}
			blocks.put(world, bastionsForWorld);
		}

		for(int i=1;i<BastionBlock.getHighestID();++i){
			BastionBlock block=blocksById.get(i);
			if(block!=null)
				if(block.shouldCull())
					remove(block);
		}
	}
	public Set<QTBox> forLocation(Location loc){
		return blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ());

	}
	
	public Set<BastionBlock> getPossibleTeleportBlocking(Location loc,String playerName){
		Set<QTBox> all=blocks.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ(),true);
		Set<BastionBlock> mightBlock=new TreeSet<BastionBlock>();
		
		for(QTBox box : all){
			if(box instanceof BastionBlock){
				BastionBlock block=(BastionBlock) box;
				if(!block.canPlace(playerName)){
					mightBlock.add(block);
				}
			}
		}
		return mightBlock;
	}
	public BastionBlock getBastionBlock(Location loc) {
		Set<? extends QTBox> possible=forLocation(loc);
		for(QTBox box: possible){
			BastionBlock bastion=(BastionBlock) box;
			if(bastion.getLocation().equals(loc))
				return bastion;
		}
		Bastion.getPlugin().getLogger().info("didn't find");
		return null;
	}
	public int update(){
		storage.saveBastionBlocks(changed);
		changed.clear();
		return 0;
	}
	@Override
	public Iterator<BastionBlock> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean add(BastionBlock toAdd) {
		blocksById.put(toAdd.getID(), toAdd);
		blocks.get(toAdd.getLocation().getWorld()).add(toAdd);
		if(!changed.contains(toAdd))
			changed.add(toAdd);
		return false;
	}
	@Override
	public boolean addAll(Collection<? extends BastionBlock> arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void clear() {
		blocks.clear();
		blocksById.clear();
		changed.clear();
	}
	@Override
	public boolean contains(Object in) {
		if(!(in instanceof BastionBlock))
			throw new IllegalArgumentException("contains only excepts a BastionBlock");
		BastionBlock toTest=(BastionBlock) in;
		return blocksById.containsKey(toTest.getID());
	}
	@Override
	public boolean containsAll(Collection<?> arg0) {
		return blocksById.isEmpty();
	}
	@Override
	public boolean isEmpty() {
		return blocksById.isEmpty();
	}
	@Override
	public boolean remove(Object in) {
		if(in==null){
			return true;
		}
		if(in instanceof BastionBlock){
			removeBastionBlock((BastionBlock) in);
		} else if(in instanceof Location){
			Location loc=(Location) in;
			removeBastionBlock(getBastionBlock(loc));
		} else{
			throw new IllegalArgumentException("remove only excepts a BastionBlock");
		}
		return false;
	}
	private void removeBastionBlock(BastionBlock toRemove){
		if(toRemove==null){
			return;
		}
		if(!changed.contains(toRemove))
			changed.add(toRemove);
		blocksById.remove(toRemove.getID());
		SparseQuadTree forWorld=blocks.get(toRemove.getLocation().getWorld());
		if(forWorld!=null){
			forWorld.remove(toRemove);
		}
		toRemove.close();
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> toRemove) {
		if(toRemove.size()==0)
			return true;
		if((toRemove.iterator().next() instanceof BastionBlock)){
			for(BastionBlock block : (Collection<BastionBlock>) toRemove){
				remove(block);
			}
		} else{
			throw new IllegalArgumentException("remove only excepts a BastionBlock");
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
		return null;
	}
	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean silentRemove(BastionBlock toRemove) {
		
		if(toRemove==null){
			
			return false;
		}
		if(!changed.contains(toRemove))
			changed.add(toRemove);
		blocksById.remove(toRemove.getID());
		SparseQuadTree forWorld=blocks.get(toRemove.getLocation().getWorld());
		if(forWorld!=null){
			forWorld.remove(toRemove);
		} else{
			Bastion.getPlugin().getLogger().info("forWorld was null");
		}
		toRemove.silentClose();
		return true;
	}

}
