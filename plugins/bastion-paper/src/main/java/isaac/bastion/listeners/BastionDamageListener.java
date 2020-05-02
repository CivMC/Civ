package isaac.bastion.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.Permissions;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.BastionGroupManager;
import isaac.bastion.manager.EnderPearlManager;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class BastionDamageListener implements Listener {
	private BastionBlockManager blockManager;
	private BastionGroupManager groupManager;
	private EnderPearlManager pearlManager;

	public BastionDamageListener() {
		blockManager = Bastion.getBastionManager();
		groupManager = Bastion.getGroupManager();
		pearlManager = new EnderPearlManager();
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Set<Block> blocks = new CopyOnWriteArraySet<>();
		blocks.add(event.getBlock());
		Set<BastionBlock> blocking = blockManager.shouldStopBlockByBlockingBastion(null, blocks,event.getPlayer().getUniqueId());
		
		if (!blocking.isEmpty()){
			//TODO here used to be a linked group check which was removed because it was broken
			blockManager.erodeFromPlace(event.getPlayer(), blocking);
			
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Bastion removed block");
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void onWaterFlow(BlockFromToEvent  event){
		Set<Block> blocks = new HashSet<>();
		blocks.add(event.getToBlock());		
		if(stopBlockEvent(null, event.getBlock(), blocks)){
			event.setCancelled(true);
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void onTreeGrow(StructureGrowEvent event){
		HashSet<Block> blocks = new HashSet<>();
		for(BlockState state: event.getBlocks()) {
			blocks.add(state.getBlock());
		}
		if (stopBlockEvent(event.getPlayer(), event.getLocation().getBlock(), blocks)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent  event){
		Set<Block> involved = new HashSet<>();
		event.getBlocks().forEach( b -> {involved.add(b); involved.add(b.getRelative(event.getDirection())); } );
		involved.add(event.getBlock().getRelative(event.getDirection()));
		if (stopBlockEvent(null, event.getBlock(), involved)) {
			event.setCancelled(true);
		}
	}

	/* Symmetry */
	@EventHandler (ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent  event){
		Set<Block> involved = new HashSet<>();
		event.getBlocks().forEach( b -> {involved.add(b); involved.add(b.getRelative(event.getDirection())); } );
		involved.add(event.getBlock().getRelative(event.getDirection()));
		if (stopBlockEvent(null, event.getBlock(), involved)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent  event){
		if (stopBlockEvent(event.getPlayer(), event.getBlockClicked().getRelative(event.getBlockFace()))) {
			event.setCancelled(true);
		}
	}
	
	private boolean stopBlockEvent(Player player, Block block) {
		Set<Block> blocks = new HashSet<>();
		blocks.add(block);
		return stopBlockEvent(player, block, blocks);
	}
	
	private boolean stopBlockEvent(Player player, Block source, Collection<Block> block) {
		Set<Block> blocks = new HashSet<>();
		blocks.addAll(block);
		UUID uuid = player != null ? player.getUniqueId() : null;
		Set<BastionBlock> blocking = blockManager.shouldStopBlockByBlockingBastion(source, blocks, uuid);
		return !blocking.isEmpty();
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onDispense(BlockDispenseEvent  event){
		if (!(event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.FLINT_AND_STEEL)) return;
		
		Set<Block> blocks = new HashSet<>();
		blocks.add(event.getBlock().getRelative( ((Dispenser) event.getBlock().getBlockData()).getFacing()));
		if(stopBlockEvent(null, event.getBlock(), blocks)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void handleEnderPearlLanded(PlayerTeleportEvent event){
		if (event.getPlayer().hasPermission("Bastion.bypass")) return; //I'm not totally sure about the implications of this combined with humbug. It might cause some exceptions. Bukkit will catch.
		if (event.getCause() != TeleportCause.ENDER_PEARL) return; // Only handle enderpearl cases
		
		Set<BastionBlock> blocking = blockManager.getBlockingBastions(event.getTo(), event.getPlayer(), PermissionType.getPermission(Permissions.BASTION_PEARL));
		
		Iterator<BastionBlock> i = blocking.iterator();
		
		while(i.hasNext()) {
			BastionBlock bastion = i.next();
			if(bastion.getType().isOnlyDirectDestruction() || !bastion.getType().isBlockPearls() || (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}
		
		if(blocking.size() > 0) {
			blockManager.erodeFromTeleport(event.getPlayer(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED + "Ender pearl blocked by Bastion Block");
			boolean consume = false;
			for(BastionBlock block : blocking) {
				if(block.getType().isConsumeOnBlock()) {
					consume = true;
				}
			}
			if(!consume) {
				event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			}
			event.setCancelled(true);
			return;
		}
		
		
		blocking = blockManager.getBlockingBastions(event.getFrom(), event.getPlayer(), PermissionType.getPermission(Permissions.BASTION_PEARL));
		
		i = blocking.iterator();
		
		while(i.hasNext()) {
			BastionBlock bastion = i.next();
			if(bastion.getType().canPearlOut() || bastion.getType().isOnlyDirectDestruction() || !bastion.getType().isBlockPearls() || (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}
		
		if(blocking.size() > 0) {
			blockManager.erodeFromTeleport(event.getPlayer(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED + "Ender pearl blocked by Bastion Block");
			boolean consume = false;
			for(BastionBlock block : blocking) {
				if(block.getType().isConsumeOnBlock()) {
					consume = true;
				}
			}
			if(!consume) {
				event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			}
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void onProjectileThrownEvent(ProjectileLaunchEvent event) {
		if (event.getEntity() instanceof EnderPearl) {
			EnderPearl pearl = (EnderPearl) event.getEntity();
			pearlManager.handlePearlLaunched(pearl);
		}
	}
}

