package isaac.bastion.listeners;


import isaac.bastion.Bastion;
import isaac.bastion.commands.PlayersStates;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.StructureGrowEvent;

import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.namelayer.group.groups.PublicGroup;


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
	@EventHandler(priority = EventPriority.LOW)
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
	public void onReinforcement(ReinforcementCreationEvent event) {

		if (event.getBlock().getType() == config.getBastionBlockMaterial() && 
				!PlayersStates.playerInMode(event.getPlayer(), Mode.OFF) && event.getReinforcement() instanceof PlayerReinforcement) {
			PlayersStates.touchPlayer(event.getPlayer());
			PlayerReinforcement rein = (PlayerReinforcement) event.getReinforcement();
			if (rein.getGroup() instanceof PublicGroup){
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.GREEN + "Bastion's cannot be reinforced under a PublicGroup.");
			}
			else{
				bastionManager.addBastion(event.getBlock().getLocation(), rein);
				event.getPlayer().sendMessage(ChatColor.GREEN+"Bastion block created");
			}
		}
	}
	public BastionBlockManager getBastionManager(){
		return bastionManager;

	}
}
