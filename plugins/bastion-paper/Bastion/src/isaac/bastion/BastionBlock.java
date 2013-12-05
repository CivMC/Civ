package isaac.bastion;

import java.util.Set;
import java.util.TreeSet;

import isaac.bastion.util.QTBox;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;

@SuppressWarnings("rawtypes")
public class BastionBlock implements QTBox, Comparable
{
	private Location location;
	private int id;
	public int strength;
	public boolean loaded=true;
	public boolean ghost=false;
	private static int highestID=-1;

	public BastionBlock(Location nlocation, PlayerReinforcement reinforcement){
		location = nlocation;

		strength=reinforcement.getDurability();
		loaded=false;
		if(id>highestID){
			highestID=id;
		}
	}
	public BastionBlock(Location nLocation,int nID)
	{
		id=nID;
		Bastion.getPlugin().getLogger().info("created Bastion block with id "+id);
		location = nLocation;

		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location.getBlock());

		strength=reinforcement.getDurability();

		if(id>highestID){
			highestID=id;
		}
	}
	public void close(){
		loaded=false;
		ghost=true;
		location.getBlock().setType(Material.AIR);
	}
	public void free_id(){
		if(id==highestID){
			--highestID;
		}
	}
	static public int getHighestID(){
		return highestID;
	}
	public Location getLocation(){
		return location;
	}
	public boolean ghost(){
		return ghost;
	}
	public boolean loaded(){
		return loaded;
	}
	public boolean blocked(BlockPlaceEvent event)
	{
		String playerName=event.getPlayer().getName();
		PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(location.getBlock());
		if(reinforcement instanceof PlayerReinforcement){
			Faction owner = reinforcement.getOwner();
			if(owner.isMember(playerName)||owner.isFounder(playerName)||owner.isModerator(playerName)){
				//return false;
			}

			if (((event.getBlock().getX() - location.getX()) * (event.getBlock().getX() - location.getX()) + 
					(event.getBlock().getZ() - location.getZ()) * (event.getBlock().getZ() - location.getZ()) > 25.0D)
					|| (event.getBlock().getY() <= location.getY())) {

				Bastion.getPlugin().getLogger().info("not blocked");
				return false;
			}

			Bastion.getPlugin().getLogger().info("blocked");
		}
		return true;
	}
	public void handlePlaced(Block block) {
		block.breakNaturally();

		IReinforcement reinforcement = Citadel.getReinforcementManager().getReinforcement(location.getBlock());
		if (reinforcement != null) {
			strength=reinforcement.getDurability();
			--strength;
			reinforcement.setDurability(strength);
			Citadel.getReinforcementManager().addReinforcement(reinforcement);
		}

		Bastion.getPlugin().getLogger().info("strength=" + strength);
	}
	public boolean shouldCull(){

		if(strength > 0||ghost){
			return false;
		} else{
			return true;
		}
	}
	public int getID(){
		return id;
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