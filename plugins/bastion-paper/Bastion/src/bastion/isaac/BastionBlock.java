package bastion.isaac;

import bastion.isaac.util.QTBox;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;

@SuppressWarnings("rawtypes")
public class BastionBlock implements QTBox, Comparable
{
	private Location location;
	private Faction owner;
	public int strength;

	BastionBlock(Location nLocation, int nStrength, Faction creator)
	{
		owner = creator;
		strength = nStrength;
		location = nLocation;
	}

	Location getLocation(){
		return location;
	}
	boolean blocked(BlockPlaceEvent event)
	{
		String playerName=event.getPlayer().getName();
		if(owner.isMember(playerName)||owner.isFounder(playerName)||owner.isModerator(playerName)){
			//return false;
		}
		if (((event.getBlock().getX() - location.getX()) * (event.getBlock().getX() - location.getX()) + 
				(event.getBlock().getZ() - location.getZ()) * (event.getBlock().getZ() - location.getZ()) > 
		25.0D) || (event.getBlock().getY() <= location.getY())) {
			Bastion.getPlugin().getLogger().info("not blocked");
			return false;
		}
		Bastion.getPlugin().getLogger().info("blocked");
		return true;
	}
	void handlePlaced(Block block) {
		if (strength > 0) {
			block.breakNaturally();
			
			IReinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(location.getBlock());
			strength=reinforcement.getDurability();
			--strength;
			if (reinforcement != null) {
				reinforcement.setDurability(strength);
				Citadel.getReinforcementManager().addReinforcement(reinforcement);
			}

		} else{
			location.getBlock().setType(Material.AIR);
		}

		Bastion.getPlugin().getLogger().info("strength=" + strength);
	}
	boolean shouldCull(){
		
		if(strength > 0){
			return false;
		} else{
			return true;
		}
	}

	@Override
	public int qtXMin() {
		return location.getBlockX()-5;
	}

	@Override
	public int qtXMid() {
		return location.getBlockX();
	}

	@Override
	public int qtXMax() {
		return location.getBlockX()+5;
	}

	@Override
	public int qtZMin() {
		return location.getBlockZ()-5;
	}

	@Override
	public int qtZMid() {
		return location.getBlockZ();
	}

	@Override
	public int qtZMax() {
		return location.getBlockZ()+5;
	}

	@Override
	public int compareTo(Object o) {
		BastionBlock other=(BastionBlock)o;
		int thisX=location.getBlockX();
		int thisY=location.getBlockY();
		int thisZ=location.getBlockZ();
		
		int otherX=other.location.getBlockX();
		int otherY=other.location.getBlockX();
		int otherZ=other.location.getBlockX();
		
		if(thisX<otherX)
			return -1;
		if(thisY<otherY)
			return -1;
		if(thisZ<otherZ)
			return -1;
		
		if(thisX>otherX)
			return -1;
		if(thisY>otherY)
			return -1;
		if(thisZ>otherZ)
			return -1;
		
		return 0;
	}
}