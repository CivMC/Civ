package isaac.bastion.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import org.bukkit.material.MaterialData;

import isaac.bastion.Bastion;
import isaac.bastion.BastionType;
import isaac.bastion.commands.PlayersStates;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.manager.BastionBlockManager;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

public final class BastionListener implements Listener {
	private BastionBlockManager bastionManager;
	public static HashMap<Location, BastionType> pendingBastions = new HashMap<Location, BastionType>();

	public BastionListener() {
		bastionManager = Bastion.getBastionManager();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		bastionManager.handleBlockPlace(event);
		if(!event.isCancelled()) {
			MaterialData mat = new MaterialData(event.getBlock().getType(), event.getBlock().getData());
			String lore = "";
			if(event.getItemInHand() != null && event.getItemInHand().hasItemMeta() && event.getItemInHand().getItemMeta().hasLore()) {
				lore = event.getItemInHand().getItemMeta().getLore().get(0);
			}
			BastionType type = BastionType.getBastionType(mat, lore);
			if(type != null) pendingBastions.put(event.getBlock().getLocation(), type);
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void waterflowed(BlockFromToEvent  event){
		bastionManager.handleFlowingWater(event);
	}

	@EventHandler (ignoreCancelled = true)
	public void treeGrew(StructureGrowEvent event){
		bastionManager.handleTreeGrowth(event);
	}

	@EventHandler (ignoreCancelled = true)
	public void pistionPushed(BlockPistonExtendEvent  event){
		bastionManager.handlePistonPush(event);
	}
	
	@EventHandler (ignoreCancelled = true)
	public void bucketPlaced(PlayerBucketEmptyEvent  event){
		bastionManager.handleBucketPlace(event);
	}
	
	@EventHandler (ignoreCancelled = true)
	public void dispensed(BlockDispenseEvent  event){
		bastionManager.handleDispensed(event);
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		bastionManager.handleBlockBreakEvent(event);
	}
	
	@EventHandler
	public void onReinforcement(ReinforcementCreationEvent event) {
		BastionType type = pendingBastions.get(event.getBlock().getLocation());
		if(type != null && 
				!PlayersStates.playerInMode(event.getPlayer(), Mode.OFF) && event.getReinforcement() instanceof PlayerReinforcement) {
			PlayersStates.touchPlayer(event.getPlayer());
			PlayerReinforcement rein = (PlayerReinforcement) event.getReinforcement();
			bastionManager.addBastion(event.getBlock().getLocation(), rein, type);
			event.getPlayer().sendMessage(ChatColor.GREEN + "Bastion block created");
		}
	}
	
	public BastionBlockManager getBastionManager(){
		return bastionManager;
	}
}
