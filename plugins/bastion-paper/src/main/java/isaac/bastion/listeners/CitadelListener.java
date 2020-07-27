package isaac.bastion.listeners;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import isaac.bastion.utils.BastionSettingManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.Permissions;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.BastionGroupManager;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class CitadelListener implements Listener {
	
	private BastionBlockManager blockManager;
	private BastionGroupManager groupManager;
	
	public CitadelListener() {
		this.blockManager = Bastion.getBastionManager();
		this.groupManager = Bastion.getGroupManager();
	}

	@EventHandler(ignoreCancelled = true)
	public void onReinforcementCreation(ReinforcementCreationEvent event) {
		Set<BastionBlock> preblocking = blockManager.getBlockingBastions(event.getReinforcement().getLocation(), event.getPlayer(), PermissionType.getPermission(Permissions.BASTION_PLACE));
		Set<BastionBlock> blocking = new CopyOnWriteArraySet<>();
		for(BastionBlock bastion : preblocking) {
			BastionType type = bastion.getType();
			if(type.isBlockReinforcements()) {
				blocking.add(bastion);
			}
		}
		if ((!blocking.isEmpty()) && !groupManager.canPlaceBlock(event.getPlayer(), blocking)){
			event.setCancelled(true);

			BastionSettingManager settings = Bastion.getSettingManager();
			if (!settings.getIgnorePlacementMessages(event.getPlayer().getUniqueId())) {
				event.getPlayer().sendMessage(ChatColor.RED + "Bastion prevented reinforcement");
			}
			blockManager.erodeFromPlace(event.getPlayer(), blocking);
		}
	}
}
