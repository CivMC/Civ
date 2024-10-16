package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.storage.BastionBlockStorage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BastionBreakListener implements Listener {

	private BastionBlockStorage storage;
	private BastionBlockManager manager;
	
	public BastionBreakListener(BastionBlockStorage storage, BastionBlockManager manager) {
		this.storage = storage;
		this.manager = manager;
	}
	
	private void dropBastionItem(Location loc, BastionType type, Player player, TextComponent chatMessage) {
		ItemStack item = type.getItemRepresentation();
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getWorld().dropItem(loc.add(0.5, 0.5, 0.5), item).setVelocity(new Vector(0, 0.05, 0));;

				if(player != null && chatMessage != null) {
					player.spigot().sendMessage(chatMessage);
				}
			}
		}.runTaskLater(Bastion.getPlugin(), 1);
		storage.deleteDeadBastion(loc); // just in case.
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		// we will only reach this is allowed by Citadel -- e.g. is not cancelled.
		Block block = event.getBlock();
		BastionType type = storage.getTypeAtLocation(block.getLocation());
		if (type == null) {
			type = storage.getAndRemovePendingBastion(block.getLocation());
		}
		if(type != null) {
			Bastion.getPlugin().getLogger().log(Level.INFO, "BastionType broken {0}", type.toString());

			TextComponent chatMessage = null;
			BastionBlock bastion = storage.getBastionBlock(block.getLocation());
			if (bastion != null) {
				if(this.manager.canListBastionsForGroup(event.getPlayer(), bastion.getListGroupId())) {
					chatMessage = this.manager.bastionDeletedMessageComponent(bastion);
				}

				bastion.destroy();
			}
			event.setCancelled(true);
			block.setType(Material.AIR);
			dropBastionItem(block.getLocation(), type, event.getPlayer(), chatMessage);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		Iterator<Block> iterator = event.blockList().iterator();
		HashSet<Block> blocks = new HashSet<>();
		while(iterator.hasNext()) {
			Block block = iterator.next();
			BastionType type = storage.getTypeAtLocation(block.getLocation());
			if (type == null) {
				type = storage.getAndRemovePendingBastion(block.getLocation());
			}
			if( type != null) {
				if(blocks.contains(block)) {
					continue;
				}
				blocks.add(block);
				block.setType(Material.AIR);
				dropBastionItem(block.getLocation(), type, null, null);
				iterator.remove(); // don't explode it, we've got it covered now.
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		Iterator<Block> iterator = event.blockList().iterator();
		HashSet<Block> blocks = new HashSet<>();
		while(iterator.hasNext()) {
			Block block = iterator.next();
			BastionType type = storage.getTypeAtLocation(block.getLocation());
			if (type == null) {
				type = storage.getAndRemovePendingBastion(block.getLocation());
			}
			if( type != null) {
				if(blocks.contains(block)) {
					continue;
				}
				blocks.add(block);
				block.setType(Material.AIR);
				dropBastionItem(block.getLocation(), type, null, null);
				iterator.remove(); // don't explode it, we've got it covered now.
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		// we will only reach this if citadel allows
		for(Block block : event.getBlocks()) {
			BastionType type = storage.getTypeAtLocation(block.getLocation());
			if (type == null) {
				type = storage.getAndRemovePendingBastion(block.getLocation());
			}
			if(type != null) {
				if(block.getPistonMoveReaction() == PistonMoveReaction.BREAK) {
					dropBastionItem(block.getLocation(), type, null, null);
					block.setType(Material.AIR);
				} else if(block.getPistonMoveReaction() == PistonMoveReaction.MOVE) {
					Block toBlock = block.getRelative(event.getDirection());
					storage.moveDeadBastion(block.getLocation(), toBlock.getLocation());
					// TODO might need special handling if was pending previously
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		if(!event.isSticky()) return;
		for(Block block : event.getBlocks()) {
			BastionType type = storage.getTypeAtLocation(block.getLocation());
			if (type == null) {
				type = storage.getAndRemovePendingBastion(block.getLocation());
			}
			if(type != null) {
				if(block.getPistonMoveReaction() == PistonMoveReaction.BREAK) {
					dropBastionItem(block.getLocation(), type, null, null);
					block.setType(Material.AIR);
				} else if(block.getPistonMoveReaction() == PistonMoveReaction.MOVE) {
					Block toBlock = block.getRelative(event.getDirection());
					storage.moveDeadBastion(block.getLocation(), toBlock.getLocation());
					// TODO might need special handling if pending previously
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		Block block = event.getBlock();
		BastionType type = storage.getTypeAtLocation(block.getLocation());
		if (type == null) {
			type = storage.getAndRemovePendingBastion(block.getLocation());
		}
		if(type != null) {
			block.setType(Material.AIR);
			dropBastionItem(block.getLocation(), type, null, null);
			event.setCancelled(true);
		}
	}
}
