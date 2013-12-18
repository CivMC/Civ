package isaac.bastion.manager;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.util.QTBox;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.untamedears.citadel.entity.PlayerReinforcement;


public class BastionBlockManager
{
	public BastionBlockSet bastions;
	private ConfigManager config;
	public BastionBlockManager()
	{
		config=Bastion.getConfigManager();
		bastions=new BastionBlockSet();
	}
	public void close(){
		bastions.close();
	}

	public void addBastion(Location location, PlayerReinforcement reinforcement) {
		BastionBlock toAdd=new BastionBlock(location,reinforcement);
		bastions.add(toAdd);
		Bastion.getPlugin().getLogger().info("bastion added");
	}

	public boolean handleBlockPlace(BlockPlaceEvent event, boolean shouldHandle) {
		Location location=event.getBlock().getLocation();
		Set<? extends QTBox> possible=bastions.forLocation(location);
		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Bastion.getPlugin().getLogger().info("There are "+possibleRandom.size()+" possiblities.");
		Collections.shuffle(possibleRandom);
		for (BastionBlock bastion : possibleRandom){
			if (bastion.blocked(event)){
				if(shouldHandle)
					bastion.handlePlaced(event.getBlock());
				event.setCancelled(true);
				if(bastion.shouldCull())
					bastions.remove(bastion);
				return true;
			}
		}
		return false;
	}
	public boolean handleBlockPlace(Location loc, Player placed, boolean shouldHandle) {
		Set<? extends QTBox> possible=bastions.forLocation(loc);
		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Bastion.getPlugin().getLogger().info("There are "+possibleRandom.size()+" possiblities.");
		Collections.shuffle(possibleRandom);
		for (BastionBlock bastion : possibleRandom){
			if (bastion.blocked(loc,placed)){
				if(shouldHandle)
					bastion.handlePlaced(loc.getBlock());
				if(bastion.shouldCull())
					bastions.remove(bastion);
				return true;
			}
		}
		return false;
	}
	public boolean handleBlockPlace(Location loc,boolean shouldHandle){
		Set<? extends QTBox> possible=bastions.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Bastion.getPlugin().getLogger().info("There are "+possibleRandom.size()+" possiblities.");
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom){
			if (bastion.blocked(loc)){
				if(shouldHandle)
					bastion.handlePlaced(loc.getBlock());
				if(bastion.shouldCull())
					bastions.remove(bastion);
				return true;
			}
		}
		return false;
	}

	public void handleBlockBreakEvent(BlockBreakEvent event){
		if (event.getBlock().getType() == config.getBastionBlockMaterial()) {
			Bastion.getPlugin().getLogger().info("Block break "+event.getBlock().toString());
			bastions.remove(event.getBlock().getLocation());
		}
	}
}