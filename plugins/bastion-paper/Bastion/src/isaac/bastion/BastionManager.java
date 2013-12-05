package isaac.bastion;

import isaac.bastion.storage.BastionBlockStorage;
import isaac.bastion.util.QTBox;
import isaac.bastion.util.SparseQuadTree;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.block.BlockPlaceEvent;

import com.untamedears.citadel.entity.PlayerReinforcement;


public class BastionManager
{
	private Map<World,SparseQuadTree> bastions;
	private Map<Integer,BastionBlock> bastionsByID;
	private BastionBlockStorage storage;
	public BastionManager()
	{
		Bastion.getPlugin();
		
		storage=new BastionBlockStorage();
		
		bastions=new HashMap<World, SparseQuadTree>();
		bastionsByID=new HashMap<Integer,BastionBlock>();
		
		load();
	}
	
	public boolean load() {
		for(World world : Bukkit.getWorlds()){
			Enumeration<BastionBlock> forWorld=storage.getAllSnitches(world);
			SparseQuadTree bastionsForWorld=new SparseQuadTree();
			while(forWorld.hasMoreElements()){
				BastionBlock toAdd=forWorld.nextElement();
				bastionsByID.put(toAdd.getID(), toAdd);
				bastionsForWorld.add(toAdd);
				Bastion.getPlugin().getLogger().info("Loaded Bastion");
			}
			bastions.put(world, bastionsForWorld);
		}
		return false;
	}
	
	public boolean save() {
		storage.saveBastionBlocks(bastionsByID);
		
		return false;
	}
	

	public void addBastion(Location location, PlayerReinforcement reinforcement) {
		BastionBlock toAdd=new BastionBlock(location,reinforcement);
		bastions.get(location.getWorld()).add(toAdd);
		bastionsByID.put(toAdd.getID(),toAdd);
		Bastion.getPlugin().getLogger().info("bastion added");
	}
	public BastionBlock getBastionBlock(Location loc) {
		Set<? extends QTBox> possible=bastions.get(loc.getWorld()).find(loc.getBlockX(), loc.getBlockZ());
		for(QTBox box: possible){
			BastionBlock bastion=(BastionBlock) box;
			Bastion.getPlugin().getLogger().info("found possible");
			if(bastion.getLocation().equals(loc))
				return bastion;
		}
		Bastion.getPlugin().getLogger().info("didn't find");
		return null;
	}
	public void removeBastion(Location location) {
		BastionBlock toRemove=getBastionBlock(location);
		Bastion.getPlugin().getLogger().info("removeBastion calle toRemove==null"+(toRemove==null));
		if(toRemove!=null){
			toRemove.close();
		
			bastions.get(location.getWorld()).remove(toRemove);
		}
	}
	public void removeBastion(BastionBlock toRemove) {
		Bastion.getPlugin().getLogger().info("removeBastion calle toRemove==null"+(toRemove==null));
		if(toRemove!=null){
			toRemove.close();
			bastions.get(toRemove.getLocation().getWorld()).remove(toRemove);
		}
	}
	
	public void handleBlockPlace(BlockPlaceEvent event) {
		Location location=event.getBlock().getLocation();
		Set<? extends QTBox> possible=bastions.get(event.getBlock().getLocation().getWorld()).find(location.getBlockX(), location.getBlockZ());
		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);
		for (BastionBlock bastion : possibleRandom){
			if (bastion.blocked(event)){
				bastion.handlePlaced(event.getBlock());
		        if(bastion.shouldCull())
		        	removeBastion(bastion);
		        break;
			}
		}
	}
}