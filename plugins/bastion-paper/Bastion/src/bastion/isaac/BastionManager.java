package bastion.isaac;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bastion.isaac.util.QTBox;
import bastion.isaac.util.SparseQuadTree;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.block.BlockPlaceEvent;

import com.untamedears.citadel.entity.Faction;


public class BastionManager
{
	//private Vector<BastionBlock> bastions;
	private Map<World,SparseQuadTree> bastions;
	private List<World> worlds;
	public BastionManager()
	{
		Bastion.getPlugin();
		worlds=Bukkit.getWorlds();
		bastions=new HashMap<World, SparseQuadTree>();
		for(World world : worlds){
			SparseQuadTree bastionsForWorld=new SparseQuadTree();
			bastions.put(world, bastionsForWorld);
		}
		//bastions = new Vector<BastionBlock>();
	}

	public void addBastion(Location location, int strength, Faction creator) {
		//bastions.add(new BastionBlock(location, strength, creator));
		bastions.get(location.getWorld()).add(new BastionBlock(location, strength, creator));
		Bastion.getPlugin().getLogger().info("bastion added");
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
		        	bastions.remove(bastion);
		        break;
			}
		}
	}
}