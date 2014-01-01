package isaac.bastion.manager;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.util.QTBox;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;
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
	}

	public boolean handleBlockPlace(BlockPlaceEvent event) {
		Location location=event.getBlock().getLocation();
		if(handleBlockPlace(location,event.getPlayer(),true)){
			event.setCancelled(true);
			return true;
		}
		return false;
	}
	public boolean handlePistonPush(BlockPistonExtendEvent event) {
		boolean blocked=false;
		BlockFace direction=event.getDirection();
		Block pistionBlock=event.getBlock();

		Block pistionArm=pistionBlock.getRelative(direction);

		PlayerReinforcement pistionReinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(pistionBlock);
		Faction pistionGroup=null;
		String foundersName=null;


		if(pistionReinforcement instanceof PlayerReinforcement){
			pistionGroup=pistionReinforcement.getOwner();
			foundersName=pistionGroup.getFounder();
		}

		if(handleBlockPlace(pistionArm.getLocation(),foundersName,false)){
			blocked=true;
		}

		for(Block block : event.getBlocks()){
			Location locationAfter=block.getLocation().add(direction.getModX(),direction.getModY(),direction.getModZ());

			if(handleBlockPlace(locationAfter,foundersName,false)){
				blocked=true;
				break;
			}
		}
		event.setCancelled(blocked);
		return blocked;
	}
	public boolean handleBucketPlace(PlayerBucketEmptyEvent event) {
		Block clicked=event.getBlockClicked();
		Block added=clicked.getRelative(event.getBlockFace());

		Location location=added.getLocation();
		Set<? extends QTBox> possible=bastions.forLocation(location);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom){
			if (bastion.blocked(location,event.getPlayer().getName())){
				event.setCancelled(true);
				if(bastion.shouldCull())
					bastions.remove(bastion);
				return true;
			}
		}
		return false;
	}
	public boolean handleDispensed(BlockDispenseEvent event) {

		PlayerReinforcement pistionReinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().getReinforcement(event.getBlock());
		Faction pistionGroup=null;
		String foundersName=null;


		if(pistionReinforcement instanceof PlayerReinforcement){
			pistionGroup=pistionReinforcement.getOwner();
			foundersName=pistionGroup.getFounder();
		}

		Material mat=event.getItem().getType();
		if(mat!=Material.FLINT_AND_STEEL&&mat!=Material.WATER_BUCKET&&mat!=Material.LAVA_BUCKET){
			return false;
		}

		MaterialData blockData = event.getBlock().getState().getData();
		Dispenser dispenser = (Dispenser) blockData;

		BlockFace facing=dispenser.getFacing();
		Block block=event.getBlock().getRelative(facing);

		BastionBlock wouldStop = getBlockingBastion(block.getLocation(),foundersName);
		if(wouldStop!=null)
			if(wouldStop.blocked(block.getLocation())&&!wouldStop.blocked(event.getBlock().getLocation())){
				event.setCancelled(true);
				return true;
			}

		return false;
	}

	public boolean handleFlowingWater(BlockFromToEvent event) {
		Block start=event.getBlock();
		Block end=event.getToBlock();

		BastionBlock blocking=getBlockingBastion(end.getLocation());
		if(blocking!=null){
			if(blocking.blocked(end.getLocation())&&(!blocking.blocked(start.getLocation()))){
				event.setCancelled(true);
				return true;
			}
		}

		event.setCancelled(false);
		return false;
	}

	public boolean handleTreeGrowth(StructureGrowEvent event){
		Player player=event.getPlayer();
		String playerName=null;
		if(player!=null)
			playerName=player.getName();
		boolean shouldCancel=false;

		for(BlockState block : event.getBlocks()){
			BastionBlock blocking=getBlockingBastion(block.getLocation());
			if(blocking!=null){
				if(blocking.blocked(block.getLocation(),playerName)&&!blocking.blocked(event.getLocation())){
					shouldCancel=true;
					break;
				}
			}
		}

		event.setCancelled(shouldCancel);
		return shouldCancel;
	}

	private boolean handleBlockPlace(Location loc, Player player, boolean shouldHandle) {
		BastionBlock bastion=getBlockingBastion(loc,player.getName());
		if(bastion!=null){
			if(shouldHandle){
				bastion.handlePlaced(loc.getBlock());
				player.getInventory().remove(new ItemStack(loc.getBlock().getType()));
			}
			if(bastion.shouldCull())
				bastions.remove(bastion);
			return true;
		}
		return false;
	}

	private boolean handleBlockPlace(Location loc, String player, boolean shouldHandle) {
		BastionBlock bastion=getBlockingBastion(loc,player);
		if(bastion!=null){
			if(shouldHandle)
				bastion.handlePlaced(loc.getBlock());

			if(bastion.shouldCull())
				bastions.remove(bastion);
			return true;
		}
		return false;
	}

	public boolean handleEnderPearlThrown(EnderPearl pearl){
		LivingEntity thrower=pearl.getShooter();
		String playerName=null;
		if(thrower instanceof Player){
			playerName=((Player) thrower).getName();
		}
		BastionBlock blocking=getBlockingBastion(pearl.getLocation(),playerName);
		if(blocking!=null){
			if(blocking.enderPearlBlocked(pearl.getLocation(), playerName)){
				pearl.remove();
				if(thrower instanceof Player){
					blocking.handleTeleport(pearl.getLocation(), (Player) thrower);
				}
				return true;
			}
		}
		if(thrower instanceof Player){
			Player player=(Player) thrower;
			Location playerLocation=player.getLocation();
			blocking=getBlockingBastion(playerLocation,playerName);
			if(blocking!=null){
				pearl.remove();
				blocking.handleTeleport(playerLocation, (Player) thrower);
			}

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
			if (bastion.blocked(loc)){
				return bastion;
			}
		}
		return null;
	}

	public void handleBlockBreakEvent(BlockBreakEvent event){
		if (event.getBlock().getType() == config.getBastionBlockMaterial()) {
			bastions.remove(event.getBlock().getLocation());
		}
	}

	public String infoMessage(boolean dev,PlayerInteractEvent event){
		Block clicked=event.getClickedBlock();
		BlockFace clickedFace=event.getBlockFace();
		Block block=clicked.getRelative(clickedFace);

		Player player=event.getPlayer();
		String playerName=player.getName();

		BastionBlock bastion=bastions.getBastionBlock(clicked.getLocation());

		if(bastion!=null){
			event.setCancelled(true);
			return bastion.infoMessage(dev, player);
		}

		bastion=getBlockingBastion(block.getLocation(),playerName);
		if(bastion==null){
			bastion=getBlockingBastion(block.getLocation());
			if(bastion==null)
				return ChatColor.GREEN+"A Bastion Block prevents others from building";
		} else{
			return ChatColor.RED+"A Bastion Block prevents you building";
		}
		return null;

	}
}