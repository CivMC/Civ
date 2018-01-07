package isaac.bastion.listeners;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.BastionGroupManager;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;

public class CitadelListener implements Listener {
	
	private BastionBlockManager blockManager;
	private BastionGroupManager groupManager;
	
	public CitadelListener() {
		this.blockManager = Bastion.getBastionManager();
		this.groupManager = Bastion.getGroupManager();
	}

	@EventHandler(ignoreCancelled = true)
	public void onReinforcementCreation(ReinforcementCreationEvent event) {
		System.out.println(event.getPlayer().getDisplayName());
		Set<Block> blocks = new CopyOnWriteArraySet<Block>();
		blocks.add(event.getBlock());
		Set<BastionBlock> blocking = blockManager.shouldStopBlockByBlockingBastion(null, blocks, event.getPlayer().getUniqueId());
		Iterator<BastionBlock> iter = blocking.iterator();
		while(iter.hasNext()) {
			BastionType type = iter.next().getType();
			if(!type.isOnlyDirectDestruction() || !type.isBlockReinforcements()) {
				iter.remove();
			}
		}
		if (blocking.size() != 0 && !groupManager.canPlaceBlock(event.getPlayer(), blocking)){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Bastion prevented reinforcement");
			blockManager.erodeFromPlace(event.getPlayer(), blocking);
		}
	}
}
