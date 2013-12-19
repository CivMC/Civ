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
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
		String playerName=event.getPlayer().getName();
		return handleBlockPlace(location,playerName,true);
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

		boolean shouldCancel = handleBlockPlace(block.getLocation(),foundersName,false);

		event.setCancelled(shouldCancel);
		return shouldCancel;
	}
	private boolean handleBlockPlace(Location loc, String foundersName, boolean shouldHandle) {
		BastionBlock bastion=getBlockingBastion(loc,foundersName);
		if(bastion!=null){
			if(shouldHandle)
				bastion.handlePlaced(loc.getBlock());

			if(bastion.shouldCull())
				bastions.remove(bastion);
			return true;
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

		if(bastion!=null)
			return bastion.infoMessage(dev, player);
		
		bastion=getBlockingBastion(block.getLocation());
		if(bastion!=null){
			if(bastion.blocked(block.getLocation(), playerName)){
				return ChatColor.RED+"A Bastion Block prevents you building";
			} else{
				return ChatColor.GREEN+"A Bastion Block prevents others from building";
			}
		}

		return "";

	}
}