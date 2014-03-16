package isaac.bastion.manager;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.util.QTBox;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.PlayerReinforcement;


public class BastionBlockManager
{
	public BastionBlockSet bastions;
	private ConfigManager config;


	public BastionBlockManager(){
		config=Bastion.getConfigManager();
		bastions=new BastionBlockSet();
		bastions.load();
	}
	public void close(){
		bastions.close();
	}

	public void addBastion(Location location, PlayerReinforcement reinforcement) {
		BastionBlock toAdd=new BastionBlock(location,reinforcement);
		bastions.add(toAdd);
	}

	//called when a player places a block.
	public boolean handleBlockPlace(BlockPlaceEvent event) {
		Location location=event.getBlock().getLocation();
		if(handleBlockPlace(location,event.getPlayer().getName(),true)){
			final BlockState replaced=event.getBlockReplacedState();
			replaced.update(true,false);
			return true;
		}
		return false;
	}
	//called when a Piston extends
	public boolean handlePistonPush(BlockPistonExtendEvent event) {

		BlockFace direction=event.getDirection();
		Block pistionBlock=event.getBlock(); //Get the block representing the piston

		Block pistionArm=pistionBlock.getRelative(direction);

		PlayerReinforcement pistionReinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
				getReinforcement(pistionBlock);

		Faction pistionGroup=null;
		String foundersName=null;


		if(pistionReinforcement instanceof PlayerReinforcement){ //get the owner of the piston's name if we can
			pistionGroup=pistionReinforcement.getOwner();
			foundersName=pistionGroup.getFounder();
		}

		if(blocksAction(pistionArm.getLocation(),pistionBlock.getLocation(),foundersName)){
			event.setCancelled(true);
			return true;
		}

		for(Block block : event.getBlocks()){ //check if any of the b will be inside after being pushed
			Location locationAfter=block.getLocation().add(direction.getModX(),direction.getModY(),direction.getModZ());

			if(blocksAction(locationAfter,pistionBlock.getLocation(),foundersName)){
				event.setCancelled(true);
				return true;
			}
		}
		return false;
	}
	//called when the player uses a bucket to pace liquid
	public boolean handleBucketPlace(PlayerBucketEmptyEvent event) {
		Block clicked=event.getBlockClicked(); //get the block clicked to activate bucket
		Block added=clicked.getRelative(event.getBlockFace()); //get the block where the liquid will be

		Location location=added.getLocation();
		BastionBlock blocking=getBlockingBastion(location,event.getPlayer().getName());
		if(blocking!=null){
			blocking.handlePlaced(added, false);
			event.setCancelled(true);
			return true;
		}
		return false;
	}
	//called when a dispenser fires
	public boolean handleDispensed(BlockDispenseEvent event) {

		PlayerReinforcement pistionReinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
				getReinforcement(event.getBlock()); //get the reinforcement on the dispensor
		Faction pistionGroup=null;
		String foundersName=null;


		if(pistionReinforcement instanceof PlayerReinforcement){ //try to get the owner's name
			pistionGroup=pistionReinforcement.getOwner();
			foundersName=pistionGroup.getFounder();
		}

		Material mat=event.getItem().getType();
		if(mat!=Material.FLINT_AND_STEEL&&mat!=Material.WATER_BUCKET&&mat!=Material.LAVA_BUCKET){
			//if it's not something we're trying to block don't
			return false;
		}

		MaterialData blockData = event.getBlock().getState().getData();
		Dispenser dispenser = (Dispenser) blockData; //get the dispensor object

		BlockFace facing=dispenser.getFacing();
		Block emptiesInto=event.getBlock().getRelative(facing); //find where it empties into 


		if(blocksAction(emptiesInto.getLocation(),event.getBlock().getLocation(),foundersName)){
			event.setCancelled(true);
			return true;
		}

		return false;
	}

	public boolean handleFlowingWater(BlockFromToEvent event) {
		Block start=event.getBlock();
		Block end=event.getToBlock();



		if(blocksAction(end.getLocation(), start.getLocation(), null)){
			event.setCancelled(true);
			return true;
		}

		return false;
	}

	public boolean handleTreeGrowth(StructureGrowEvent event){
		Player player=event.getPlayer();
		String playerName=null;
		
		PlayerReinforcement saplingReinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
				getReinforcement(event.getLocation());
		
		if(saplingReinforcement instanceof PlayerReinforcement)
			playerName=saplingReinforcement.getOwnerName();
		
		if(player!=null&&playerName==null)
			playerName=player.getName();
		

		for(BlockState block : event.getBlocks()){
				if(this.blocksAction(block.getLocation(), event.getLocation(), playerName)){
					event.setCancelled(true);
					return true;
				}
		}
		return false;
	}

	private boolean handleBlockPlace(Location loc, String player, boolean shouldHandle) {
		BastionBlock bastion=getBlockingBastion(loc,player);
		if(bastion!=null){
			if(shouldHandle){
				bastion.handlePlaced(loc.getBlock(),true);
			}
			if(bastion.shouldCull())
				bastions.remove(bastion);
			return true;
		}
		return false;
	}

	public boolean handleEnderPearlLanded(PlayerTeleportEvent event){
		if(event.getCause()!=TeleportCause.ENDER_PEARL)
			return false;

		Player player=event.getPlayer();
		Location landing=event.getTo();
		landing.add(0,1,0);
		Location from=event.getFrom();
		from.add(0,1,0);
		BastionBlock blockingTo=getBlockingBastion(landing,player.getName());
		BastionBlock blockingFrom=getBlockingBastion(from,player.getName());
		if(blockingTo!=null){
			if(blockingTo.enderPearlBlocked(landing, player.getName())){
				blockingTo.handleTeleport(landing, player);
				event.setCancelled(true);
				return true;
			}
		}

		if(blockingFrom!=null){
			if(blockingFrom.enderPearlBlocked(from, player.getName())){
				blockingFrom.handleTeleport(from, player);
				event.setCancelled(true);
				return true;
			}
		}
		return false;
	}

	private BastionBlock getBlockingBastion(Location loc, String foundersName){
		Set<? extends QTBox> possible=bastions.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom){
			if (bastion.blocked(loc,foundersName)){
				return bastion;
			}
		}
		return null;
	}

	private BastionBlock getBlockingBastion(Location loc){
		Set<? extends QTBox> possible=bastions.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom){
			if (bastion.inField(loc)){
				return bastion;
			}
		}
		return null;
	}
	private Set<BastionBlock> getBlockingBastions(Location loc){
		Set<? extends QTBox> possible=bastions.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		Set<BastionBlock> result=new TreeSet<BastionBlock>();
		for (BastionBlock bastion : possibleRandom){
			if (bastion.inField(loc)){
				result.add(bastion);
			}
		}
		return result;
	}

	private BastionBlock independantlyBlockedBy(Location a,Location b){
		Set<BastionBlock> bastions=getBlockingBastions(a);
		BastionBlock blocking=null;
		if(bastions.size()==0){
			blocking=getBlockingBastion(b);
			return blocking;
		}

		for(BastionBlock bastion:bastions){
			blocking=getBlockingBastion(b,bastion.getReinforcement().getOwnerName());
			if(blocking==null)
				return null;
		}
		return blocking;
	}

	private boolean blocksAction(Location result,Location creater,String player){
		BastionBlock blocking=independantlyBlockedBy(creater,result);
		if(player==null||player==""){
			blocking=independantlyBlockedBy(creater,result);
		} else{
			blocking=getBlockingBastion(result,player);
			if(blocking!=null){
				blocking=independantlyBlockedBy(creater,result);
			}
		}
		if(blocking!=null)
			return !blocking.inField(creater);

		return false;
	}

	public void handleBlockBreakEvent(BlockBreakEvent event){
		if (event.getBlock().getType() == config.getBastionBlockMaterial()) {
			bastions.remove(event.getBlock().getLocation());
		}
	}

	public String infoMessage(boolean dev,PlayerInteractEvent event){
		Block clicked=event.getClickedBlock();
		BlockFace clickedFace=event.getBlockFace();
		Block block=clicked.getRelative(clickedFace); //get the block above the clicked block. Kind of like you clicked air

		Player player=event.getPlayer();
		String playerName=player.getName();

		BastionBlock bastion=bastions.getBastionBlock(clicked.getLocation()); //Get the bastion at the location clicked.

		if(bastion!=null){ //See if anything was found
			return bastion.infoMessage(dev, player); //If there is actually something there tell the player about it.
		}

		bastion=getBlockingBastion(block.getLocation(),playerName);
		if(bastion==null){
			bastion=getBlockingBastion(block.getLocation());
			if(bastion!=null){
				return ChatColor.GREEN+"A Bastion Block prevents others from building";
			}
		} else{
			return ChatColor.RED+"A Bastion Block prevents you building";
		}
		return null;

	}
}