package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.EnderPearlManager;

import org.bukkit.Server;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EnderPearlListener implements Listener {
	int taskId;
	Server server;
	BastionBlockManager manager;
	EnderPearlManager pearlMang;

	public EnderPearlListener(){
		server=Bastion.getPlugin().getServer();
		manager=Bastion.getBastionManager();
		
		pearlMang=new EnderPearlManager();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void handleEnderPearlLanded(PlayerTeleportEvent event){
		if(event.isCancelled())
			return;
		manager.handleEnderPearlLanded(event);
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onProjectileThrownEvent(ProjectileLaunchEvent event) {
		if(event.isCancelled() || !Bastion.getConfigManager().blockMidAir())
			return;
		if(event.getEntity() instanceof EnderPearl) {
			EnderPearl pearl=(EnderPearl) event.getEntity();
			pearlMang.handlePearlLaunched(pearl);
		}
	}
}
