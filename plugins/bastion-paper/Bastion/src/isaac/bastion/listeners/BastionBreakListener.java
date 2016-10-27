package isaac.bastion.listeners;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import isaac.bastion.Bastion;
import isaac.bastion.BastionType;
import isaac.bastion.storage.BastionBlockStorage;
import vg.civcraft.mc.citadel.Utility;

public class BastionBreakListener implements Listener {

	private BastionBlockStorage storage;
	
	public BastionBreakListener(BastionBlockStorage storage) {
		this.storage = storage;
	}
	
	private void dropBastionItem(Location loc) {
		BastionType type = storage.getTypeAtLocation(loc);
		ItemStack item = type.getItemRepresentation();
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getWorld().dropItem(loc.add(0.5, 0.5, 0.5), item).setVelocity(new Vector(0, 0.05, 0));;
			}
		}.runTaskLater(Bastion.getPlugin(), 1);
		storage.deleteDeadBastion(loc);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled()) return;
		Block block = Utility.getRealBlock(event.getBlock());
		if(storage.getTypeAtLocation(block.getLocation()) != null) {
			block.getDrops().clear();
			dropBastionItem(block.getLocation());
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if(event.isCancelled()) return;
		Iterator<Block> iterator = event.blockList().iterator();
		ArrayList<Block> blocks = new ArrayList<Block>();
		while(iterator.hasNext()) {
			Block block = Utility.getRealBlock(iterator.next());
			if(storage.getTypeAtLocation(block.getLocation()) != null) {
				if(blocks.contains(block)) {
					block.getDrops().clear();
					continue;
				}
				blocks.add(block);
				dropBastionItem(block.getLocation());
				block.getDrops().clear();
			}
		}
	}
	
	@EventHandler
	public void onEntityDoorBreak(EntityBreakDoorEvent event) {
		if(event.isCancelled()) return;
		Block block = Utility.getRealBlock(event.getBlock());
		if(storage.getTypeAtLocation(block.getLocation()) != null) {
			block.getDrops().clear();
			dropBastionItem(block.getLocation());
		}
	}
	
	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event) {
		if(event.isCancelled()) return;
		for(Block block : event.getBlocks()) {
			if(storage.getTypeAtLocation(block.getLocation()) != null) {
				if(block.getPistonMoveReaction() == PistonMoveReaction.BREAK) {
					dropBastionItem(block.getLocation());
					block.getDrops().clear();
				} else if(block.getPistonMoveReaction() == PistonMoveReaction.MOVE) {
					Block toBlock = block.getRelative(event.getDirection());
					storage.moveDeadBastion(block.getLocation(), toBlock.getLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent event) {
		if(!event.isSticky() || event.isCancelled()) return;
		for(Block block : event.getBlocks()) {
			if(storage.getTypeAtLocation(block.getLocation()) != null) {
				if(block.getPistonMoveReaction() == PistonMoveReaction.BREAK) {
					dropBastionItem(block.getLocation());
					block.getDrops().clear();
				} else if(block.getPistonMoveReaction() == PistonMoveReaction.MOVE) {
					Block toBlock = block.getRelative(event.getDirection());
					storage.moveDeadBastion(block.getLocation(), toBlock.getLocation());
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		if(event.isCancelled()) return;
		Block block = Utility.getRealBlock(event.getBlock());
		if(storage.getTypeAtLocation(block.getLocation()) != null) {
			block.getDrops().clear();
			dropBastionItem(block.getLocation());
		}
	}
}