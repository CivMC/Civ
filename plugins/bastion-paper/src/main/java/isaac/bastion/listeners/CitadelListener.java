package isaac.bastion.listeners;

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
		Set<Block> blocks = new CopyOnWriteArraySet<Block>();
		blocks.add(event.getBlock());
		Set<BastionBlock> preblocking = blockManager.shouldStopBlockByBlockingBastion(null, blocks, event.getPlayer().getUniqueId());
		Set<BastionBlock> blocking = new CopyOnWriteArraySet<BastionBlock>();
		for(BastionBlock bastion : preblocking) {
			BastionType type = bastion.getType();
			if(type.isBlockReinforcements()) {
				blocking.add(bastion);
			}
		}
		if (blocking.size() != 0 && !groupManager.canPlaceBlock(event.getPlayer(), blocking)){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Bastion prevented reinforcement");
			blockManager.erodeFromPlace(event.getPlayer(), blocking);
		}
	}
}
