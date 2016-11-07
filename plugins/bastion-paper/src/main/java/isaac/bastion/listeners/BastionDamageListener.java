package isaac.bastion.listeners;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.EnderPearlManager;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class BastionDamageListener implements Listener {
	private BastionBlockManager manager;
	private EnderPearlManager pearlMan;

	public BastionDamageListener() {
		manager = Bastion.getBastionManager();
		pearlMan = new EnderPearlManager();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event) {
		Set<Block> blocks = new CopyOnWriteArraySet<Block>();
		blocks.add(event.getBlock());
		Set<BastionBlock> preblocking = manager.shouldStopBlock(null, blocks,event.getPlayer().getUniqueId());
		
		if (preblocking.size() != 0){
			Set<BastionBlock> blocking = clearNonBlocking(preblocking);

			if (blocking.size() == 0) {
				return;
			}
			
			manager.erodeFromPlace(event.getPlayer(),blocking);
			
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Bastion removed block");
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void onWaterFlow(BlockFromToEvent  event){
		Set<Block> blocks = new CopyOnWriteArraySet<Block>();
		blocks.add(event.getToBlock());
		Set<BastionBlock> blocking = manager.shouldStopBlock(event.getBlock(),blocks, null);
		
		if(blocking.size() != 0){
			event.setCancelled(true);
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void onTreeGrow(StructureGrowEvent event){
		HashSet<Block> blocks = new HashSet<Block>();
		for(BlockState state: event.getBlocks()) {
			blocks.add(state.getBlock());
		}
		
		Player player = event.getPlayer();
		UUID playerName = null;
		if (player != null) {
			playerName = player.getUniqueId();
		}
		
		Set<BastionBlock> blocking = clearNonBlocking(manager.shouldStopBlock(event.getLocation().getBlock(), blocks, playerName));
		
		if (blocking.size() != 0) {
			event.setCancelled(true);
		}
	}

	@EventHandler (ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent  event){
		Block pistion = event.getBlock();
		Set<Block> involved = new HashSet<Block>(event.getBlocks());
		involved.add(pistion.getRelative(event.getDirection()));
		
		Set<BastionBlock> blocking = clearNonBlocking(manager.shouldStopBlock(pistion, involved, null));
		
		if (blocking.size() != 0) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent  event){
		Set<Block> blocks = new HashSet<Block>();
		blocks.add(event.getBlockClicked().getRelative(event.getBlockFace()));
		
		Set<BastionBlock> blocking = manager.shouldStopBlock(null, blocks, event.getPlayer().getUniqueId());
		
		if (blocking.size() != 0) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onDispense(BlockDispenseEvent  event){
		if (!(event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.FLINT_AND_STEEL)) return;
		
		Set<Block> blocks = new HashSet<Block>();
		blocks.add(event.getBlock().getRelative( ((Dispenser) event.getBlock().getState().getData()).getFacing()));
		
		Set<BastionBlock> blocking = clearNonBlocking(manager.shouldStopBlock(event.getBlock(),blocks, null));
		
		if(blocking.size() != 0) {
			event.setCancelled(true);
		}
	}
	
	/*@EventHandler (ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		BastionBlock bastion = Bastion.getBastionStorage().getBastionBlock(event.getBlock().getLocation());
		if (bastion != null) {
			bastion.destroy();
		}
	}*/
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
	public void handleEnderPearlLanded(PlayerTeleportEvent event){
		if (event.getPlayer().hasPermission("Bastion.bypass")) return; //I'm not totally sure about the implications of this combined with humbug. It might cause some exceptions. Bukkit will catch.
		if (event.getCause() != TeleportCause.ENDER_PEARL) return; // Only handle enderpearl cases
		
		Set<BastionBlock> blocking = manager.getBlockingBastions(event.getTo(), event.getPlayer(), PermissionType.getPermission("BASTION_PEARL"));
		
		Iterator<BastionBlock> i = blocking.iterator();
		
		while(i.hasNext()) {
			BastionBlock bastion = i.next();
			if(!bastion.getType().isBlockPearls() || (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}
		
		if(blocking.size() > 0) {
			manager.erodeFromTeleport(event.getPlayer(), blocking);
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
		
		blocking = manager.getBlockingBastions(event.getFrom(), event.getPlayer(), PermissionType.getPermission("BASTION_PEARL"));
		
		i = blocking.iterator();
		
		while(i.hasNext()) {
			BastionBlock bastion = i.next();
			if(bastion.getType().isOnlyDirectDestruction() || !bastion.getType().isBlockPearls() || (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}
		
		if(blocking.size() > 0) {
			manager.erodeFromTeleport(event.getPlayer(), blocking);
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
			pearlMan.handlePearlLaunched(pearl);
		}
	}
	
	private Set<BastionBlock> clearNonBlocking(Set<BastionBlock> preblocking) {
		Set<BastionBlock> blocking = new HashSet<BastionBlock>();
		for (BastionBlock bastion : preblocking) {
			if (!bastion.getType().isOnlyDirectDestruction()) {
				blocking.add(bastion);
			}
		}
		return blocking;
	}
}

