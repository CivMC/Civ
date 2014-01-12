package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.EnderPearlManager;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class EnderPearlListener implements Listener {
	int taskId;
	Server server;
	BastionBlockManager manager;
	EnderPearlManager pearlMang;

	public EnderPearlListener(){
		server=Bastion.getPlugin().getServer();
		manager=Bastion.getBastionManager();

		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		taskId=scheduler.scheduleSyncRepeatingTask(Bastion.getPlugin(),
				new BukkitRunnable(){
			public void run(){
				pearlMang.tick();
			}
		},
		1,1);
		pearlMang=new EnderPearlManager();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleEnderPearlLanded(PlayerTeleportEvent event){
		if(event.isCancelled())
			return;
		manager.handleEnderPearlLanded(event);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProjectileThrownEvent(ProjectileLaunchEvent event) {
		if(event.isCancelled())
			return;
		if(event.getEntity() instanceof EnderPearl) {
			EnderPearl pearl=(EnderPearl) event.getEntity();
			pearlMang.handlePearlLaunched(pearl);
		}
	}
}
