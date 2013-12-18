package isaac.bastion;


import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ConfigManager;

import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;

public final class BastionListener
implements Listener
{
	private BastionBlockManager bastionManager;
	private ConfigManager config;

	public BastionListener()
	{
		bastionManager = Bastion.getBastionManager();
		config=Bastion.getConfigManager();
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(event.isCancelled()){
			return;
		}
		bastionManager.handleBlockPlace(event,true);
	}
	@EventHandler
	public void sandPlaced(EntityBlockFormEvent  event){
		if(event.isCancelled()){
			return;
		}
		bastionManager.handleBlockPlace(event.getBlock().getLocation(),false);
	}
	@EventHandler
	public void pistionPushed(BlockPistonExtendEvent  event){
		if(event.isCancelled()){
			return;
		}
		boolean blocked=false;
		BlockFace direction=event.getDirection();
		
		for(Block block : event.getBlocks()){
			Bastion.getPlugin().getLogger().info("Triggered for "+block.getLocation());
			
			if(bastionManager.handleBlockPlace(block.getLocation().add(direction.getModX(),direction.getModY(),direction.getModZ()),false)){
				blocked=true;
			}
		}
		event.setCancelled(blocked);
	}
	@EventHandler
	public void bucketPlaced(PlayerBucketEmptyEvent  event){
		if(event.isCancelled()){
			return;
		}
		Block placedOn=event.getBlockClicked().getRelative(event.getBlockFace());
		Location placed=placedOn.getLocation();
		boolean shouldCancel = bastionManager.handleBlockPlace(placed,event.getPlayer(),true);
		event.setCancelled(shouldCancel);

	}
	@EventHandler
	public void dispensed(BlockDispenseEvent  event){
		if(event.isCancelled()){
			return;
		}
		Material mat=event.getItem().getType();
		if(mat!=Material.FLINT_AND_STEEL&&mat!=Material.WATER_BUCKET&&mat!=Material.LAVA_BUCKET){
			return;
		}
		
		MaterialData blockData = event.getBlock().getState().getData();
		Dispenser dispenser = (Dispenser) blockData;
		BlockFace facing=dispenser.getFacing();
		Block block=event.getBlock().getRelative(facing);
		
		boolean shouldCancel = bastionManager.handleBlockPlace(block.getLocation(),false);
		Bastion.getPlugin().getLogger().info("shouldCancel="+shouldCancel);
		event.setCancelled(shouldCancel);

	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled()){
			return;
		}
		bastionManager.handleBlockBreakEvent(event);
	}
	@EventHandler
	public void onReinforcement(CreateReinforcementEvent event) {

		if (event.getBlock().getType() == config.getBastionBlockMaterial()) {
			bastionManager.addBastion(event.getBlock().getLocation(),(PlayerReinforcement) event.getReinforcement());
		}
	}
	public BastionBlockManager getBastionManager(){
		return bastionManager;

	}
}