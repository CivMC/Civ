package isaac.bastion.listeners;


import isaac.bastion.Bastion;
import isaac.bastion.commands.PlayersStates;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ConfigManager;

import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.StructureGrowEvent;


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
		bastionManager.handleBlockPlace(event);
	}

	@EventHandler
	public void waterflowed(BlockFromToEvent  event){
		if(event.isCancelled()){
			return;
		}
		bastionManager.handleFlowingWater(event);
	}

	@EventHandler
	public void treeGrew(StructureGrowEvent event){
		if(event.isCancelled()){
			return;
		}
		bastionManager.handleTreeGrowth(event);
	}

	@EventHandler
	public void pistionPushed(BlockPistonExtendEvent  event){
		if(event.isCancelled()){
			return;
		}
		bastionManager.handlePistonPush(event);
	}
	@EventHandler
	public void bucketPlaced(PlayerBucketEmptyEvent  event){
		if(event.isCancelled()){
			return;
		}
		bastionManager.handleBucketPlace(event);

	}
	@EventHandler
	public void dispensed(BlockDispenseEvent  event){
		if(event.isCancelled()){
			return;
		}

		bastionManager.handleDispensed(event);
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

		if (event.getBlock().getType() == config.getBastionBlockMaterial() && 
				!PlayersStates.playerInMode(event.getPlayer(), Mode.OFF) && event.getReinforcement() instanceof PlayerReinforcement) {
			PlayersStates.touchPlayer(event.getPlayer());
			bastionManager.addBastion(event.getBlock().getLocation(),(PlayerReinforcement) event.getReinforcement());
			event.getPlayer().sendMessage(ChatColor.GREEN+"Bastion block created");
		}
	}
	public BastionBlockManager getBastionManager(){
		return bastionManager;

	}
}